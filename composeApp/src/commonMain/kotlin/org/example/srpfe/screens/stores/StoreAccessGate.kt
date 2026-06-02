package org.example.srpfe.screens.stores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.auth.AuthSource
import org.koin.compose.koinInject

@Composable
internal fun StoreAccessGate(
    content: @Composable () -> Unit,
) {
    val authSession = koinInject<AuthSession>()
    val authenticatedUser by authSession.currentUser.collectAsState()

    when {
        authenticatedUser == null -> {
            StoreAccessMessage("Sign in with Google to manage stores.")
        }

        authenticatedUser?.authSource != AuthSource.FIREBASE -> {
            StoreAccessMessage("Store management is only available on the Firebase-backed mobile sign-in flow.")
        }

        else -> content()
    }
}

@Composable
private fun StoreAccessMessage(message: String) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
