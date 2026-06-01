package org.example.srpfe.screens.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SalesScreen() {
    val viewModel = koinViewModel<SalesViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadStores()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Store sales",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Select a store to load the current backend offers.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        when {
            uiState.isLoadingStores && uiState.stores.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.stores.isEmpty() -> {
                EmptySalesState(
                    message = uiState.errorMessage ?: "No stores are available.",
                    onRetry = {
                        coroutineScope.launch {
                            viewModel.loadStores()
                        }
                    },
                )
            }

            else -> {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        items = uiState.stores,
                        key = { it.id ?: it.name },
                    ) { store ->
                        FilterChip(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.selectStore(store.name)
                                }
                            },
                            selected = uiState.selectedStoreName == store.name,
                            label = { Text(store.name) },
                        )
                    }
                }

                uiState.sales?.let { sales ->
                    SalesSummaryCard(
                        store = sales.store,
                        validFrom = sales.validFrom,
                        validTo = sales.validTo,
                        onRefresh = {
                            coroutineScope.launch {
                                viewModel.refreshSelectedStore()
                            }
                        },
                    )
                }

                if (uiState.errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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

                if (uiState.isLoadingSales && uiState.sales == null) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (uiState.isLoadingSales && uiState.sales != null) {
                            item {
                                Text(
                                    text = "Refreshing offers...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        val offers = uiState.sales?.offers.orEmpty()
                        if (offers.isEmpty()) {
                            item {
                                Text("No offers are available for this store.")
                            }
                        } else {
                            items(
                                items = offers,
                                key = { offer -> offer },
                            ) { offer ->
                                OfferCard(offer = offer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SalesSummaryCard(
    store: String,
    validFrom: String,
    validTo: String,
    onRefresh: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = store,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text("Valid from: $validFrom")
            Text("Valid to: $validTo")
            Button(onClick = onRefresh) {
                Text("Refresh offers")
            }
        }
    }
}

@Composable
private fun OfferCard(offer: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = offer,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun EmptySalesState(
    message: String,
    onRetry: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
