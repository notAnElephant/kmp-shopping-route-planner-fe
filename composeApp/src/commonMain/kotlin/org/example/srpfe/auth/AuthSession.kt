package org.example.srpfe.auth

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSession {
    private val _currentUser = MutableStateFlow<AuthenticatedUser?>(null)
    val currentUser: StateFlow<AuthenticatedUser?> = _currentUser.asStateFlow()

    fun setCurrentUser(user: AuthenticatedUser?) {
        _currentUser.value = user
    }

    suspend fun syncFromFirebaseUser(firebaseUser: FirebaseUser?) {
        _currentUser.value =
            firebaseUser?.let { user ->
                AuthenticatedUser(
                    authSource = AuthSource.FIREBASE,
                    uid = user.uid,
                    idToken = user.getIdToken(forceRefresh = false),
                    displayName = user.displayName,
                    email = user.email,
                    photoUrl = user.photoURL,
                )
            }
    }

    suspend fun syncFromPlatformAuth() {
        syncFromFirebaseUser(FirebaseAuthSessionBridge.currentUser())
    }

    suspend fun requireBackendIdToken(): String {
        val refreshedUser =
            when (_currentUser.value?.authSource) {
                AuthSource.FIREBASE -> {
                    syncFromFirebaseUser(FirebaseAuthSessionBridge.currentUser())
                    _currentUser.value
                }
                else -> _currentUser.value
            }

        val user = refreshedUser ?: error("You need to sign in before using this feature.")
        check(user.authSource == AuthSource.FIREBASE) {
            "Backend-authenticated features are only available for Firebase-backed sign-ins."
        }

        return user.idToken ?: error("Missing Firebase ID token.")
    }

    suspend fun signOut() {
        FirebaseAuthSessionBridge.signOut()
        _currentUser.value = null
    }
}
