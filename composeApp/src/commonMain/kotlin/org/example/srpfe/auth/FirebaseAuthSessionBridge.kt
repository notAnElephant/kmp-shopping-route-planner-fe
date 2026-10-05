package org.example.srpfe.auth

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

expect object FirebaseAuthSessionBridge {
    suspend fun currentUser(): AuthenticatedUser?

    suspend fun refreshCurrentUser(): AuthenticatedUser?

    fun idTokenChanges(): Flow<AuthenticatedUser?>

    suspend fun signOut()
}

suspend fun FirebaseUser.toAuthenticatedUser(): AuthenticatedUser =
    AuthenticatedUser(
        authSource = AuthSource.FIREBASE,
        uid = uid,
        idToken = getIdToken(forceRefresh = false),
        displayName = displayName,
        email = email,
        photoUrl = photoURL,
    )
