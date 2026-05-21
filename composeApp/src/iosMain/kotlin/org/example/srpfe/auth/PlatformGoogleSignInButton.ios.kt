package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import kotlinx.coroutines.launch

@Composable
actual fun PlatformGoogleSignInButton(
    modifier: Modifier,
    onResult: (Result<AuthenticatedUser?>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    GoogleButtonUiContainerFirebase(
        modifier = modifier,
        onResult = { result ->
            coroutineScope.launch {
                onResult(
                    result.map { user ->
                        user?.let {
                            AuthenticatedUser(
                                authSource = AuthSource.FIREBASE,
                                uid = it.uid,
                                idToken = it.getIdToken(forceRefresh = false),
                                displayName = it.displayName,
                                email = it.email,
                                photoUrl = it.photoURL,
                            )
                        }
                    },
                )
            }
        },
    ) {
        content(this::onClick)
    }
}
