package org.example.srpfe.screens.sales

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.SalesResponse

private val supportedSalesChains = listOf("SPAR", "ALDI", "PENNY", "LIDL", "TESCO")

data class SalesUiState(
    val salesChains: List<String> = supportedSalesChains,
    val selectedStoreName: String? = null,
    val sales: SalesResponse? = null,
    val isLoadingSales: Boolean = false,
    val errorMessage: String? = null,
)

class SalesViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    suspend fun loadSalesForDefaultChain() {
        val selectedStoreName = _uiState.value.selectedStoreName ?: supportedSalesChains.first()
        _uiState.value = _uiState.value.copy(selectedStoreName = selectedStoreName)
        loadSales(storeName = selectedStoreName, preserveCurrentSales = false)
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
