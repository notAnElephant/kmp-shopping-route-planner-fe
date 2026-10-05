package org.example.srpfe.auth

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopGoogleSignInTest {
    private val client = HttpClient.newHttpClient()

    @Test
    fun callbackPostsTokensWithoutPuttingThemInTheUrl() = runBlocking {
        val callback = CompletableDeferred<TokenPayload>()
        val server = DesktopGoogleSignIn.startCallbackServer("expected-state", callback, 0)
        try {
            val baseUrl = "http://localhost:${server.address.port}"
            val page = client.send(
                HttpRequest.newBuilder(URI("$baseUrl/callback")).GET().build(),
                HttpResponse.BodyHandlers.ofString(),
            )
            assertEquals(200, page.statusCode())
            assertTrue(page.body().contains("method: 'POST'"))
            assertTrue(page.body().contains("history.replaceState"))

            val wrongState = post("$baseUrl/callback/token", "state=wrong-state&id_token=token")
            assertEquals(400, wrongState.statusCode())
            assertFalse(callback.isCompleted)

            val result = post("$baseUrl/callback/token", "state=expected-state&id_token=id-token")
            assertEquals(200, result.statusCode())
            assertEquals(TokenPayload("id-token"), callback.await())
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun googleErrorIsReportedToTheApp() = runBlocking {
        val callback = CompletableDeferred<TokenPayload>()
        val server = DesktopGoogleSignIn.startCallbackServer("expected-state", callback, 0)
        try {
            val result = post(
                "http://localhost:${server.address.port}/callback/token",
                "state=expected-state&error=access_denied&error_description=User+cancelled",
            )
            assertEquals(400, result.statusCode())
            val error = withTimeout(1_000) { runCatching { callback.await() }.exceptionOrNull() }
            assertEquals("Google sign-in failed: access_denied: User cancelled", error?.message)
        } finally {
            server.stop(0)
        }
    }

    private fun post(url: String, body: String): HttpResponse<String> = client.send(
        HttpRequest.newBuilder(URI(url))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build(),
        HttpResponse.BodyHandlers.ofString(),
    )
}
