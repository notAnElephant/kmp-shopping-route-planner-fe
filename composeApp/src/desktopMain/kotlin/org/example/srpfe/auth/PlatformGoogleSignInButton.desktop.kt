package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase

@Composable
actual fun PlatformGoogleSignInButton(
    modifier: Modifier,
    onResult: (Result<AuthenticatedUser?>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
) {
    GoogleButtonUiContainerFirebase(
        modifier = modifier,
        onResult = { result ->
            onResult(
                result.map { user ->
                    user?.let {
                        AuthenticatedUser(
                            displayName = it.displayName,
                            email = it.email,
                        )
                    }
                },
            )
        },
    ) {
        content(this::onClick)
    }
}
