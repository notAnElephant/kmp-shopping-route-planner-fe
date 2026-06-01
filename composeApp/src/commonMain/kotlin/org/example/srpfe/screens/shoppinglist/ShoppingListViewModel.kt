package org.example.srpfe.screens.shoppinglist

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.ShoppingList

data class ShoppingListUiState(
    val shoppingLists: List<ShoppingList> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val draftName: String = "",
    val draftItemName: String = "",
    val draftItemQuantity: String = "",
    val draftItems: List<CreateShoppingListItemRequest> = emptyList(),
    val editingListId: Int? = null,
)

class ShoppingListViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    suspend fun loadShoppingLists() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            apiRepository.getShoppingLists()
        }.onSuccess { shoppingLists ->
            _uiState.value =
                _uiState.value.copy(
                    shoppingLists = shoppingLists,
                    isLoading = false,
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load shopping lists.",
                )
        }
    }

    fun resetForSignedOutUser() {
        _uiState.value = ShoppingListUiState()
    }

    fun updateDraftName(value: String) {
        _uiState.value = _uiState.value.copy(draftName = value)
    }

    fun updateDraftItemName(value: String) {
        _uiState.value = _uiState.value.copy(draftItemName = value)
    }

    fun updateDraftItemQuantity(value: String) {
        _uiState.value = _uiState.value.copy(draftItemQuantity = value)
    }

    fun addDraftItem() {
        val currentState = _uiState.value
        val itemName = currentState.draftItemName.trim()
        val itemQuantity = currentState.draftItemQuantity.trim()
        if (itemName.isBlank() || itemQuantity.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Item name and quantity are required.")
            return
        }

        _uiState.value =
            currentState.copy(
                draftItems =
                    currentState.draftItems +
                        CreateShoppingListItemRequest(
                            shoppingItemName = itemName,
                            attributes = itemQuantity,
                        ),
                draftItemName = "",
                draftItemQuantity = "",
                errorMessage = null,
            )
    }

    fun removeDraftItem(index: Int) {
        val currentItems = _uiState.value.draftItems.toMutableList()
        if (index !in currentItems.indices) {
            return
        }

        currentItems.removeAt(index)
        _uiState.value = _uiState.value.copy(draftItems = currentItems)
    }

    fun startEditing(shoppingList: ShoppingList) {
        _uiState.value =
            _uiState.value.copy(
                editingListId = shoppingList.id,
                draftName = shoppingList.name,
                draftItems = emptyList(),
                draftItemName = "",
                draftItemQuantity = "",
                errorMessage = null,
            )
    }

    fun cancelEditing() {
        _uiState.value =
            _uiState.value.copy(
                editingListId = null,
                draftName = "",
                draftItems = emptyList(),
                draftItemName = "",
                draftItemQuantity = "",
            )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    suspend fun saveShoppingList() {
        val currentState = _uiState.value
        val name = currentState.draftName.trim()
        if (name.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "List name is required.")
            return
        }
        if (currentState.draftItems.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Add at least one shopping list item.")
            return
        }

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        val editingListId = currentState.editingListId
        val requestItems = currentState.draftItems

        val saveResult =
            runCatching {
                if (editingListId == null) {
                    apiRepository.createShoppingList(
                        CreateShoppingListRequest(
                            name = name,
                            items = requestItems,
                        ),
                    )
                } else {
                    apiRepository.updateShoppingList(
                        id = editingListId,
                        request = CreateShoppingListRequest(name = name, items = requestItems),
                    )
                }
            }

        saveResult
            .onSuccess {
                cancelEditing()
                _uiState.value = _uiState.value.copy(isSaving = false)
                loadShoppingLists()
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Could not save shopping list.",
                    )
            }
    }

    suspend fun deleteShoppingList(id: Int) {
        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
        runCatching {
            apiRepository.deleteShoppingList(id)
        }.onSuccess {
            if (_uiState.value.editingListId == id) {
                cancelEditing()
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            loadShoppingLists()
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Could not delete shopping list.",
                )
        }
    }
}
