package org.example.srpfe.auth

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

internal object DesktopFirebaseAuth {
    private val delegate = DesktopFirebaseAuthClient(
        signInUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=${AuthConfig.FIREBASE_API_KEY}",
        refreshUrl = "https://securetoken.googleapis.com/v1/token?key=${AuthConfig.FIREBASE_API_KEY}",
        requestUri = AuthConfig.GOOGLE_DESKTOP_REDIRECT_URI,
    )

    fun idTokenChanges(): Flow<AuthenticatedUser?> = delegate.idTokenChanges()

    fun currentUser(): AuthenticatedUser? = delegate.currentUser()

    suspend fun signInWithGoogle(googleIdToken: String): AuthenticatedUser = delegate.signInWithGoogle(googleIdToken)

    suspend fun refreshCurrentUser(): AuthenticatedUser? = delegate.refreshCurrentUser()

    fun signOut() = delegate.signOut()
}

internal class DesktopFirebaseAuthClient(
    private val signInUrl: String,
    private val refreshUrl: String,
    private val requestUri: String,
    private val client: HttpClient = HttpClient.newHttpClient(),
    private val nowMillis: () -> Long = System::currentTimeMillis,
) {
    private val session = MutableStateFlow<DesktopSession?>(null)
    private val refreshMutex = Mutex()

    fun idTokenChanges(): Flow<AuthenticatedUser?> = session.map { it?.user }

    fun currentUser(): AuthenticatedUser? = session.value?.user

    suspend fun signInWithGoogle(googleIdToken: String): AuthenticatedUser {
        val postBody = "id_token=${encode(googleIdToken)}&providerId=google.com"
        val request = buildJsonObject {
            put("postBody", postBody)
            put("requestUri", requestUri)
            put("returnSecureToken", true)
        }
        val response = post(
            signInUrl,
            "application/json",
            request.toString(),
        )
        val user = AuthenticatedUser(
            authSource = AuthSource.FIREBASE,
            uid = response.requiredString("localId"),
            idToken = response.requiredString("idToken"),
            displayName = response.optionalString("displayName"),
            email = response.optionalString("email"),
            photoUrl = response.optionalString("photoUrl"),
        )
        session.value = DesktopSession(
            user = user,
            refreshToken = response.requiredString("refreshToken"),
            expiresAtMillis = expiresAt(response.requiredString("expiresIn")),
        )
        return user
    }

    suspend fun refreshCurrentUser(): AuthenticatedUser? = refreshMutex.withLock {
        val current = session.value ?: return@withLock null
        if (nowMillis() < current.expiresAtMillis - 60_000) {
            return@withLock current.user
        }
        val body = "grant_type=refresh_token&refresh_token=${encode(current.refreshToken)}"
        val response = post(
            refreshUrl,
            "application/x-www-form-urlencoded",
            body,
        )
        val updated = current.copy(
            user = current.user.copy(idToken = response.requiredString("id_token")),
            refreshToken = response.requiredString("refresh_token"),
            expiresAtMillis = expiresAt(response.requiredString("expires_in")),
        )
        if (session.value === current) {
            session.value = updated
            updated.user
        } else {
            session.value?.user
        }
    }

    fun signOut() {
        session.value = null
    }

    private suspend fun post(url: String, contentType: String, body: String): JsonObject = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder(URI(url))
            .header("Content-Type", contentType)
            .timeout(Duration.ofSeconds(20))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val data = Json.parseToJsonElement(response.body()).jsonObject
        if (response.statusCode() !in 200..299) {
            val message = data["error"]?.jsonObject?.get("message")?.jsonPrimitive?.content
                ?: "HTTP ${response.statusCode()}"
            error("Firebase sign-in failed: $message")
        }
        data
    }

    private fun JsonObject.requiredString(name: String): String =
        optionalString(name)?.takeIf { it.isNotBlank() }
            ?: error("Firebase response is missing $name.")

    private fun JsonObject.optionalString(name: String): String? =
        get(name)?.jsonPrimitive?.contentOrNull

    private fun expiresAt(seconds: String): Long =
        nowMillis() + seconds.toLong() * 1_000

    private fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private data class DesktopSession(
        val user: AuthenticatedUser,
        val refreshToken: String,
        val expiresAtMillis: Long,
    )
}
