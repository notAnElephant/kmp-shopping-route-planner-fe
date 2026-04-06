package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mmk.kmpauth.google.GoogleButtonUiContainer

@Composable
actual fun PlatformGoogleSignInButton(
    modifier: Modifier,
    onResult: (Result<AuthenticatedUser?>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
) {
    GoogleButtonUiContainer(
        modifier = modifier,
        onGoogleSignInResult = { user ->
            onResult(
                Result.success(
                    user?.let {
                        AuthenticatedUser(
                            displayName = it.displayName,
                            email = it.email,
                        )
                    },
                ),
            )
        },
    ) {
        content(this::onClick)
    }
}
