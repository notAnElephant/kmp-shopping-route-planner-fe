package org.example.srpfe.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow

actual object FirebaseAuthSessionBridge {
    actual suspend fun currentUser(): FirebaseUser? = Firebase.auth.currentUser

    actual fun idTokenChanges(): Flow<FirebaseUser?> = Firebase.auth.idTokenChanged

    actual suspend fun signOut() {
        Firebase.auth.signOut()
    }
}
