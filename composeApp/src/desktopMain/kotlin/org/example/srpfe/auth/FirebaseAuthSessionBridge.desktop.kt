package org.example.srpfe.auth

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

actual object FirebaseAuthSessionBridge {
    actual suspend fun currentUser(): FirebaseUser? = null

    actual fun idTokenChanges(): Flow<FirebaseUser?> = emptyFlow()

    actual suspend fun signOut() = Unit
}
