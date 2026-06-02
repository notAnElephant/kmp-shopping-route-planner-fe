package org.example.srpfe.screens.stores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateStoreScreen(
    onBack: () -> Unit,
    onStoreCreated: (Int) -> Unit,
) {
    StoreAccessGate {
        val viewModel = koinViewModel<CreateStoreViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Add Store",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            OutlinedTextField(
                value = uiState.draftName,
                onValueChange = viewModel::updateDraftName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Store name") },
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.draftLocation,
                onValueChange = viewModel::updateDraftLocation,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
                singleLine = true,
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val store = viewModel.createStore()
                        val storeId = store?.id ?: return@launch
                        onStoreCreated(storeId)
                    }
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (uiState.isSaving) "Creating..." else "Create store")
            }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Back")
            }
        }
    }
}
