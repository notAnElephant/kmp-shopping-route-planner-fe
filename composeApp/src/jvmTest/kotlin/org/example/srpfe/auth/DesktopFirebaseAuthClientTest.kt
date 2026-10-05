package org.example.srpfe.auth

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DesktopFirebaseAuthClientTest {
    @Test
    fun signsInRefreshesAndSignsOut() = runBlocking {
        var signInBody = ""
        var refreshBody = ""
        var nowMillis = 0L
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/signin") { exchange ->
                signInBody = exchange.requestBody.use { it.readAllBytes().toString(StandardCharsets.UTF_8) }
                exchange.respond(200, """{"localId":"firebase-uid","idToken":"firebase-token-1","refreshToken":"refresh-1","expiresIn":"120","displayName":"Test User","email":"test@example.com"}""")
            }
            createContext("/refresh") { exchange ->
                refreshBody = exchange.requestBody.use { it.readAllBytes().toString(StandardCharsets.UTF_8) }
                exchange.respond(200, """{"user_id":"firebase-uid","id_token":"firebase-token-2","refresh_token":"refresh-2","expires_in":"3600"}""")
            }
            start()
        }
        try {
            val auth = DesktopFirebaseAuthClient(
                signInUrl = "http://localhost:${server.address.port}/signin",
                refreshUrl = "http://localhost:${server.address.port}/refresh",
                requestUri = "http://localhost:8083/callback",
                nowMillis = { nowMillis },
            )
            val user = auth.signInWithGoogle("google-token")
            assertEquals("firebase-uid", user.uid)
            assertEquals("firebase-token-1", user.idToken)
            assertEquals("Test User", user.displayName)
            assertEquals("id_token=google-token&providerId=google.com", Json.parseToJsonElement(signInBody).jsonObject.getValue("postBody").jsonPrimitive.content)
            assertEquals("http://localhost:8083/callback", Json.parseToJsonElement(signInBody).jsonObject.getValue("requestUri").jsonPrimitive.content)

            assertEquals("firebase-token-1", auth.refreshCurrentUser()?.idToken)
            assertTrue(refreshBody.isEmpty())
            nowMillis = 61_000L
            assertEquals("firebase-token-2", auth.refreshCurrentUser()?.idToken)
            assertEquals("grant_type=refresh_token&refresh_token=refresh-1", refreshBody)
            assertEquals("firebase-token-2", auth.currentUser()?.idToken)

            auth.signOut()
            assertNull(auth.currentUser())
            assertNull(auth.refreshCurrentUser())
        } finally {
            server.stop(0)
        }
    }

    @Test
    fun firebaseErrorsReachTheSignInCaller() = runBlocking {
        val server = HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext("/signin") { it.respond(400, """{"error":{"message":"INVALID_IDP_RESPONSE"}}""") }
            start()
        }
        try {
            val auth = DesktopFirebaseAuthClient(
                signInUrl = "http://localhost:${server.address.port}/signin",
                refreshUrl = "http://localhost:${server.address.port}/refresh",
                requestUri = "http://localhost:8083/callback",
            )
            val error = runCatching { auth.signInWithGoogle("invalid-token") }.exceptionOrNull()
            assertEquals("Firebase sign-in failed: INVALID_IDP_RESPONSE", error?.message)
            assertNull(auth.currentUser())
        } finally {
            server.stop(0)
        }
    }

    private fun HttpExchange.respond(status: Int, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        responseHeaders.add("Content-Type", "application/json")
        sendResponseHeaders(status, bytes.size.toLong())
        responseBody.use { it.write(bytes) }
    }
}
