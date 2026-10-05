package org.example.srpfe.auth

import kotlinx.coroutines.flow.Flow

actual object FirebaseAuthSessionBridge {
    actual suspend fun currentUser(): AuthenticatedUser? = DesktopFirebaseAuth.currentUser()

    actual suspend fun refreshCurrentUser(): AuthenticatedUser? = DesktopFirebaseAuth.refreshCurrentUser()

    actual fun idTokenChanges(): Flow<AuthenticatedUser?> = DesktopFirebaseAuth.idTokenChanges()

    actual suspend fun signOut() {
        DesktopFirebaseAuth.signOut()
    }
}
