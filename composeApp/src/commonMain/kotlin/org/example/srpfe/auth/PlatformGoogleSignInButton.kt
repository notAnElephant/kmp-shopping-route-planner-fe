package org.example.srpfe.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformGoogleSignInButton(
    modifier: Modifier = Modifier,
    onResult: (Result<AuthenticatedUser?>) -> Unit,
    content: @Composable (onClick: () -> Unit) -> Unit,
)
