package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import java.awt.Desktop
import java.net.BindException
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpExchange

@Composable
actual fun PlatformGoogleSignInButton(
    modifier: Modifier,
    onResult: (Result<AuthenticatedUser?>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    DesktopUiContainer(
        modifier = modifier,
        onClick = {
            coroutineScope.launch {
                onResult(runCatching { DesktopGoogleSignIn.signIn() })
            }
        },
        content = content,
    )
}

@Composable
private fun DesktopUiContainer(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
) {
    content(onClick)
}

private object DesktopGoogleSignIn {
    suspend fun signIn(): AuthenticatedUser? {
        val state = generateRandomString()
        val nonce = generateRandomString()
        val callbackPort = AuthConfig.GOOGLE_DESKTOP_CALLBACK_PORT
        val redirectUri = "http://localhost:$callbackPort/callback"
        val callback = CompletableDeferred<TokenPayload>()
        val server = startCallbackServer(state = state, callback = callback, port = callbackPort)

        try {
            openBrowser(buildAuthorizationUrl(state = state, nonce = nonce, redirectUri = redirectUri))
            val tokens =
                withTimeoutOrNull(120_000L) {
                    callback.await()
                } ?: error("Google sign-in timed out before the browser callback arrived.")
            val credential = GoogleAuthProvider.credential(tokens.idToken, tokens.accessToken)
            val user = Firebase.auth.signInWithCredential(credential).user
            return user?.let {
                AuthenticatedUser(
                    authSource = AuthSource.FIREBASE,
                    uid = it.uid,
                    idToken = it.getIdToken(forceRefresh = false),
                    displayName = it.displayName,
                    email = it.email,
                    photoUrl = it.photoURL,
                )
            }
        } finally {
            server.stop(0)
        }
    }

    private fun startCallbackServer(
        state: String,
        callback: CompletableDeferred<TokenPayload>,
        port: Int,
    ): HttpServer =
        try {
            HttpServer.create(InetSocketAddress("localhost", port), 0).apply {
                createContext("/callback") { exchange ->
                    when {
                        exchange.requestURI.path == "/callback/token" -> {
                            handleTokenCallback(exchange = exchange, expectedState = state, callback = callback)
                        }

                        else -> {
                            exchange.respondHtml(callbackPageHtml(state))
                        }
                    }
                }
                start()
            }
        } catch (error: BindException) {
            throw IllegalStateException(
                "Desktop sign-in could not start its callback server on port $port. " +
                    "Choose a free port with SHOPMAP_OAUTH_CALLBACK_PORT and allow " +
                    "http://localhost:$port/callback in the Google OAuth client.",
                error,
            )
        }

    private fun handleTokenCallback(
        exchange: HttpExchange,
        expectedState: String,
        callback: CompletableDeferred<TokenPayload>,
    ) {
        val params = exchange.requestURI.rawQuery.orEmpty().split('&').filter { it.contains('=') }.associate {
            val (key, value) = it.split('=', limit = 2)
            key to java.net.URLDecoder.decode(value, StandardCharsets.UTF_8)
        }
        val receivedState = params["state"]
        val idToken = params["id_token"]
        val accessToken = params["access_token"]

        when {
            receivedState != expectedState -> {
                callback.completeExceptionally(IllegalStateException("OAuth state mismatch."))
                exchange.respondText("Authorization failed. You can close this window.")
            }

            idToken.isNullOrBlank() -> {
                callback.completeExceptionally(IllegalStateException("Missing Google ID token."))
                exchange.respondText("Authorization failed. You can close this window.")
            }

            else -> {
                callback.complete(TokenPayload(idToken = idToken, accessToken = accessToken))
                exchange.respondText("Authorization is complete. You can close this window and return to the app.")
            }
        }
    }

    private fun callbackPageHtml(state: String): String =
        """
        <!doctype html>
        <html>
          <body>
            <script>
              const hash = window.location.hash.startsWith('#') ? window.location.hash.slice(1) : '';
              const params = new URLSearchParams(hash);
              const idToken = params.get('id_token');
              const accessToken = params.get('access_token');
              const receivedState = params.get('state');
              const query = new URLSearchParams();
              if (idToken) query.set('id_token', idToken);
              if (accessToken) query.set('access_token', accessToken);
              if (receivedState) query.set('state', receivedState);
              window.location.replace('/callback/token?' + query.toString());
            </script>
            Completing sign-in...
          </body>
        </html>
        """.trimIndent()

    private fun buildAuthorizationUrl(
        state: String,
        nonce: String,
        redirectUri: String,
    ): String {
        val encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8.toString())
        val encodedScopes = URLEncoder.encode("email profile openid", StandardCharsets.UTF_8.toString())
        val encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8.toString())
        val encodedNonce = URLEncoder.encode(nonce, StandardCharsets.UTF_8.toString())
        val encodedResponseType = URLEncoder.encode("id_token token", StandardCharsets.UTF_8.toString())

        return buildString {
            append("https://accounts.google.com/o/oauth2/v2/auth")
            append("?client_id=${AuthConfig.GOOGLE_SERVER_CLIENT_ID}")
            append("&redirect_uri=$encodedRedirect")
            append("&response_type=$encodedResponseType")
            append("&scope=$encodedScopes")
            append("&nonce=$encodedNonce")
            append("&state=$encodedState")
        }
    }

    private fun openBrowser(url: String) {
        check(Desktop.isDesktopSupported()) {
            "Desktop browser integration is not supported on this machine."
        }
        Desktop.getDesktop().browse(URI(url))
    }

    private fun generateRandomString(length: Int = 32): String {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}

private data class TokenPayload(
    val idToken: String,
    val accessToken: String?,
)

private fun HttpExchange.respondHtml(body: String) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    responseHeaders.add("Content-Type", "text/html; charset=utf-8")
    sendResponseHeaders(200, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}

private fun HttpExchange.respondText(body: String) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
    sendResponseHeaders(200, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}
