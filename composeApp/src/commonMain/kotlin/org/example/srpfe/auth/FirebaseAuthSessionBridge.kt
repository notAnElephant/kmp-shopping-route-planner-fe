package org.example.srpfe.auth

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

expect object FirebaseAuthSessionBridge {
    suspend fun currentUser(): FirebaseUser?

    fun idTokenChanges(): Flow<FirebaseUser?>

    suspend fun signOut()
}
