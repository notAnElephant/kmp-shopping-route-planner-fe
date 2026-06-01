package org.example.srpfe.screens.sales

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.SalesResponse
import org.openapitools.client.models.Store

data class SalesUiState(
    val stores: List<Store> = emptyList(),
    val selectedStoreName: String? = null,
    val sales: SalesResponse? = null,
    val isLoadingStores: Boolean = false,
    val isLoadingSales: Boolean = false,
    val errorMessage: String? = null,
)

class SalesViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    suspend fun loadStores() {
        _uiState.value = _uiState.value.copy(isLoadingStores = true, errorMessage = null)
        runCatching {
            apiRepository.getStores().sortedBy(Store::name)
        }.onSuccess { stores ->
            val selectedStoreName = _uiState.value.selectedStoreName ?: stores.firstOrNull()?.name
            _uiState.value =
                _uiState.value.copy(
                    stores = stores,
                    selectedStoreName = selectedStoreName,
                    isLoadingStores = false,
                )
            if (selectedStoreName != null) {
                loadSales(storeName = selectedStoreName, preserveCurrentSales = false)
            }
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoadingStores = false,
                    errorMessage = error.message ?: "Could not load stores.",
                )
        }
    }

    suspend fun selectStore(storeName: String) {
        _uiState.value = _uiState.value.copy(selectedStoreName = storeName, errorMessage = null)
        loadSales(storeName = storeName, preserveCurrentSales = false)
    }

    suspend fun refreshSelectedStore() {
        _uiState.value.selectedStoreName?.let {
            loadSales(storeName = it, preserveCurrentSales = true)
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private suspend fun loadSales(
        storeName: String,
        preserveCurrentSales: Boolean,
    ) {
        _uiState.value =
            _uiState.value.copy(
                isLoadingSales = true,
                errorMessage = null,
                sales = if (preserveCurrentSales) _uiState.value.sales else null,
            )
        runCatching {
            apiRepository.getSales(storeName)
        }.onSuccess { sales ->
            _uiState.value =
                _uiState.value.copy(
                    sales = sales,
                    isLoadingSales = false,
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    sales = null,
                    isLoadingSales = false,
                    errorMessage = error.message ?: "Could not load sales.",
                )
        }
    }
}
