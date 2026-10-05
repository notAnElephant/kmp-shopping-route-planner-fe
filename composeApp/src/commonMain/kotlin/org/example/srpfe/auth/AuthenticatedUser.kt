package org.example.srpfe.auth

enum class AuthSource {
    FIREBASE,
    GOOGLE,
}

data class AuthenticatedUser(
    val authSource: AuthSource,
    val uid: String? = null,
    val idToken: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
)
