package org.example.srpfe.screens.stores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.openapitools.client.models.Store

@Composable
fun StoresScreen(
    onCreateStore: () -> Unit,
    onOpenStore: (Int) -> Unit,
) {
    StoreAccessGate {
        val viewModel = koinViewModel<StoresViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val lifecycleOwner = LocalLifecycleOwner.current
        var storePendingDelete by remember { mutableStateOf<Store?>(null) }

        LaunchedEffect(Unit) {
            viewModel.loadStores()
        }

        androidx.compose.runtime.DisposableEffect(lifecycleOwner, viewModel) {
            val observer =
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        coroutineScope.launch {
                            viewModel.loadStores()
                        }
                    }
                }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Stores",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Button(
                onClick = onCreateStore,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add store")
            }

            if (uiState.errorMessage != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                        )
                        OutlinedButton(onClick = viewModel::dismissError) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            when {
                uiState.isLoading && uiState.stores.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.stores.isEmpty() -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("No stores are available.")
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.loadStores()
                                    }
                                },
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uiState.stores,
                            key = { it.id ?: it.name },
                        ) { store ->
                            StoreRow(
                                store = store,
                                isDeleting = uiState.deletingStoreId == store.id,
                                onOpen = { store.id?.let(onOpenStore) },
                                onDelete = { storePendingDelete = store },
                            )
                        }
                    }
                }
            }
        }

        if (storePendingDelete != null) {
            AlertDialog(
                onDismissRequest = { storePendingDelete = null },
                title = { Text("Delete store") },
                text = {
                    Text("Delete ${storePendingDelete?.name}? This cannot be undone from the app.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val storeId = storePendingDelete?.id ?: return@Button
                            coroutineScope.launch {
                                viewModel.deleteStore(storeId)
                                storePendingDelete = null
                            }
                        },
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { storePendingDelete = null }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
private fun StoreRow(
    store: Store,
    isDeleting: Boolean,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (!store.location.isNullOrBlank()) {
                Text(store.location)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpen,
                    enabled = store.id != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open details")
                }
                OutlinedButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (isDeleting) "Deleting..." else "Delete store")
                }
            }
        }
    }
}
