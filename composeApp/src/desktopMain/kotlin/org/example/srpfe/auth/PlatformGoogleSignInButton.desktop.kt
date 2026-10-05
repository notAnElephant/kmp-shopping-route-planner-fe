package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import java.awt.Desktop
import java.net.BindException
import java.net.InetSocketAddress
import java.net.URI
import java.net.URLEncoder
import java.net.URLDecoder
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
    var signInInProgress by remember { mutableStateOf(false) }

    DesktopUiContainer(
        modifier = modifier,
        onClick = {
            if (signInInProgress) return@DesktopUiContainer
            signInInProgress = true
            coroutineScope.launch {
                try {
                    onResult(runCatching { DesktopGoogleSignIn.signIn() })
                } finally {
                    signInInProgress = false
                }
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

internal object DesktopGoogleSignIn {
    suspend fun signIn(): AuthenticatedUser? {
        val state = generateRandomString()
        val nonce = generateRandomString()
        val callbackPort = AuthConfig.GOOGLE_DESKTOP_CALLBACK_PORT
        val redirectUri = AuthConfig.GOOGLE_DESKTOP_REDIRECT_URI
        val callback = CompletableDeferred<TokenPayload>()
        val server = startCallbackServer(state = state, callback = callback, port = callbackPort)

        try {
            openBrowser(buildAuthorizationUrl(state = state, nonce = nonce, redirectUri = redirectUri))
            val tokens =
                withTimeoutOrNull(300_000L) {
                    callback.await()
                } ?: error("Google sign-in timed out before the browser callback arrived.")
            return DesktopFirebaseAuth.signInWithGoogle(tokens.idToken)
        } finally {
            server.stop(0)
        }
    }

    internal fun startCallbackServer(
        state: String,
        callback: CompletableDeferred<TokenPayload>,
        port: Int,
    ): HttpServer =
        try {
            HttpServer.create(InetSocketAddress("localhost", port), 0).apply {
                createContext("/callback") { exchange ->
                    when {
                        exchange.requestURI.path == "/callback/token" && exchange.requestMethod == "POST" -> {
                            handleTokenCallback(exchange = exchange, expectedState = state, callback = callback)
                        }

                        exchange.requestURI.path == "/callback" && exchange.requestMethod == "GET" -> {
                            exchange.respondHtml(callbackPageHtml())
                        }

                        else -> exchange.respondText("Not found.", 404)
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
        val body = exchange.requestBody.use { it.readNBytes(16_385) }
        if (body.size > 16_384) {
            exchange.respondText("Authorization response is too large.", 413)
            return
        }
        val params = body.toString(StandardCharsets.UTF_8).split('&').filter { it.contains('=') }.associate {
            val (key, value) = it.split('=', limit = 2)
            URLDecoder.decode(key, StandardCharsets.UTF_8) to URLDecoder.decode(value, StandardCharsets.UTF_8)
        }
        val receivedState = params["state"]
        val idToken = params["id_token"]
        val oauthError = params["error"]

        when {
            receivedState != expectedState -> {
                exchange.respondText("Authorization failed: OAuth state mismatch.", 400)
            }

            !oauthError.isNullOrBlank() -> {
                val description = params["error_description"]?.take(300)
                exchange.respondText("Authorization failed. Return to the app for details.", 400)
                callback.completeExceptionally(IllegalStateException("Google sign-in failed: $oauthError${description?.let { ": $it" }.orEmpty()}"))
            }

            idToken.isNullOrBlank() -> {
                exchange.respondText("Authorization failed. Return to the app for details.", 400)
                callback.completeExceptionally(IllegalStateException("Missing Google ID token."))
            }

            else -> {
                exchange.respondText("Authorization is complete. You can close this window and return to the app.")
                callback.complete(TokenPayload(idToken = idToken))
            }
        }
    }

    private fun callbackPageHtml(): String =
        """
        <!doctype html>
        <html>
          <head><meta name="referrer" content="no-referrer"></head>
          <body>
            <script>
              const hash = window.location.hash.startsWith('#') ? window.location.hash.slice(1) : '';
              const params = new URLSearchParams(hash);
              history.replaceState(null, '', '/callback');
              fetch('/callback/token', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString(),
                cache: 'no-store'
              }).then(async response => {
                document.body.textContent = await response.text();
              }).catch(() => {
                document.body.textContent = 'Could not contact the desktop app. Return to the app and try again.';
              });
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
        val encodedResponseType = URLEncoder.encode("id_token", StandardCharsets.UTF_8.toString())

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

internal data class TokenPayload(
    val idToken: String,
)

private fun HttpExchange.respondHtml(body: String) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    responseHeaders.add("Content-Type", "text/html; charset=utf-8")
    responseHeaders.add("Cache-Control", "no-store")
    responseHeaders.add("Referrer-Policy", "no-referrer")
    sendResponseHeaders(200, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}

private fun HttpExchange.respondText(body: String, status: Int = 200) {
    val bytes = body.toByteArray(StandardCharsets.UTF_8)
    responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
    responseHeaders.add("Cache-Control", "no-store")
    sendResponseHeaders(status, bytes.size.toLong())
    responseBody.use { it.write(bytes) }
}
