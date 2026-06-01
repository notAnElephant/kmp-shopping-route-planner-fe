package org.example.srpfe.screens.shoppinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.auth.AuthSource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.ShoppingList

@Composable
fun ShoppingListScreen() {
    val viewModel = koinViewModel<ShoppingListViewModel>()
    val authSession = koinInject<AuthSession>()
    val uiState by viewModel.uiState.collectAsState()
    val authenticatedUser by authSession.currentUser.collectAsState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(authenticatedUser?.uid, authenticatedUser?.authSource) {
        if (authenticatedUser?.authSource == AuthSource.FIREBASE) {
            viewModel.loadShoppingLists()
        } else {
            viewModel.resetForSignedOutUser()
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when {
            authenticatedUser == null -> {
                Text("Sign in with Google to manage shopping lists.")
            }

            authenticatedUser?.authSource != AuthSource.FIREBASE -> {
                Text("Shopping list sync is only available on the Firebase-backed mobile sign-in flow.")
            }

            else -> {
                Text(
                    text = "Your shopping lists",
                    style = MaterialTheme.typography.headlineSmall,
                )

                ShoppingListEditor(
                    uiState = uiState,
                    onNameChange = viewModel::updateDraftName,
                    onItemNameChange = viewModel::updateDraftItemName,
                    onItemQuantityChange = viewModel::updateDraftItemQuantity,
                    onAddItem = viewModel::addDraftItem,
                    onRemoveItem = viewModel::removeDraftItem,
                    onCancelEditing = viewModel::cancelEditing,
                    onSave = {
                        coroutineScope.launch {
                            viewModel.saveShoppingList()
                        }
                    },
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        itemsIndexed(
                            items = uiState.shoppingLists,
                            key = { _, shoppingList -> shoppingList.id ?: shoppingList.name },
                        ) { _, shoppingList ->
                            ShoppingListCard(
                                shoppingList = shoppingList,
                                onEdit = { viewModel.startEditing(shoppingList) },
                                onDelete = {
                                    coroutineScope.launch {
                                        shoppingList.id?.let { id ->
                                            viewModel.deleteShoppingList(id)
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingListEditor(
    uiState: ShoppingListUiState,
    onNameChange: (String) -> Unit,
    onItemNameChange: (String) -> Unit,
    onItemQuantityChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCancelEditing: () -> Unit,
    onSave: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                if (uiState.editingListId == null) "Create shopping list" else "Edit shopping list",
                style = MaterialTheme.typography.titleMedium,
            )

            OutlinedTextField(
                value = uiState.draftName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("List name") },
                singleLine = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = uiState.draftItemName,
                    onValueChange = onItemNameChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Item") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = uiState.draftItemQuantity,
                    onValueChange = onItemQuantityChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Quantity") },
                    singleLine = true,
                )
            }

            Button(
                onClick = onAddItem,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Add item")
            }

            if (uiState.draftItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.draftItems.forEachIndexed { index, item ->
                        DraftItemRow(
                            item = item,
                            onRemove = { onRemoveItem(index) },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onSave,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (uiState.editingListId == null) "Create list" else "Save changes")
                }
                if (uiState.editingListId != null) {
                    OutlinedButton(
                        onClick = onCancelEditing,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun DraftItemRow(
    item: CreateShoppingListItemRequest,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("${item.shoppingItemName} (${item.attributes})")
        OutlinedButton(onClick = onRemove) {
            Text("Remove")
        }
    }
}

@Composable
private fun ShoppingListCard(
    shoppingList: ShoppingList,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = shoppingList.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text("ID: ${shoppingList.id ?: "pending"}")
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEdit) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onDelete) {
                    Text("Delete")
                }
            }
        }
    }
}
