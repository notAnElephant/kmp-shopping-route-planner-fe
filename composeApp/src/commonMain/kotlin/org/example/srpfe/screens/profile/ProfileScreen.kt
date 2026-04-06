package org.example.srpfe.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import org.example.ApiRepository
import org.example.srpfe.auth.AuthConfig
import org.example.srpfe.auth.AuthenticatedUser
import org.example.srpfe.auth.PlatformGoogleSignInButton
import org.example.srpfe.utils.isMobile

@Composable
fun ProfileScreen(
    apiRepository: ApiRepository,
    navController: NavHostController,
) {
    var statusMessage by remember { mutableStateOf("Not signed in") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        if (AuthConfig.GOOGLE_SERVER_CLIENT_ID.isBlank()) {
            Text(
                text = "Set AuthConfig.googleServerClientId before using Google Sign-In.",
                color = MaterialTheme.colorScheme.error,
            )
            return@Column
        }

        val onSignInResult = { result: Result<AuthenticatedUser?> ->
            statusMessage =
                result.fold(
                    onSuccess = { user ->
                        "Signed in as ${user?.displayName ?: user?.email ?: "unknown user"}"
                    },
                    onFailure = { error ->
                        "Sign-in failed: ${error.message ?: "Unknown error"}"
                    },
                )
        }

        PlatformGoogleSignInButton(
            onResult = onSignInResult,
        ) {
            GoogleSignInButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                fontSize = 19.sp,
            ) {
                it()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(statusMessage)

        if (!isMobile()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Desktop OAuth redirect URI: ${AuthConfig.GOOGLE_DESKTOP_REDIRECT_URI}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
