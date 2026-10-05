package org.example.srpfe.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual object FirebaseAuthSessionBridge {
    actual suspend fun currentUser(): AuthenticatedUser? = Firebase.auth.currentUser?.toAuthenticatedUser()

    actual suspend fun refreshCurrentUser(): AuthenticatedUser? =
        Firebase.auth.currentUser?.toAuthenticatedUser()

    actual fun idTokenChanges(): Flow<AuthenticatedUser?> =
        Firebase.auth.idTokenChanged.map { it?.toAuthenticatedUser() }

    actual suspend fun signOut() {
        Firebase.auth.signOut()
    }
}
