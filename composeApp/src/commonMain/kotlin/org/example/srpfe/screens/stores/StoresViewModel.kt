package org.example.srpfe.screens.stores

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.Store

data class StoresUiState(
    val stores: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val deletingStoreId: Int? = null,
    val errorMessage: String? = null,
)

class StoresViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoresUiState())
    val uiState: StateFlow<StoresUiState> = _uiState.asStateFlow()

    suspend fun loadStores() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            apiRepository.getStores().sortedBy(Store::name)
        }.onSuccess { stores ->
            _uiState.value =
                _uiState.value.copy(
                    stores = stores,
                    isLoading = false,
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load stores.",
                )
        }
    }

    suspend fun deleteStore(id: Int) {
        _uiState.value = _uiState.value.copy(deletingStoreId = id, errorMessage = null)
        runCatching {
            apiRepository.deleteStore(id)
        }.onSuccess {
            _uiState.value =
                _uiState.value.copy(
                    deletingStoreId = null,
                    stores = _uiState.value.stores.filterNot { it.id == id },
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    deletingStoreId = null,
                    errorMessage = error.message ?: "Could not delete store.",
                )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun resetForSignedOutUser() {
        _uiState.value = StoresUiState()
    }
}
