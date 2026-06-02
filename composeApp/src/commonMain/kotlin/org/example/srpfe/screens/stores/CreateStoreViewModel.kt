package org.example.srpfe.screens.stores

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.Store

data class CreateStoreUiState(
    val draftName: String = "",
    val draftLocation: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class CreateStoreViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateStoreUiState())
    val uiState: StateFlow<CreateStoreUiState> = _uiState.asStateFlow()

    fun updateDraftName(value: String) {
        _uiState.value = _uiState.value.copy(draftName = value)
    }

    fun updateDraftLocation(value: String) {
        _uiState.value = _uiState.value.copy(draftLocation = value)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    suspend fun createStore(): Store? {
        val currentState = _uiState.value
        val name = currentState.draftName.trim()
        val location = currentState.draftLocation.trim().ifBlank { null }
        if (name.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Store name is required.")
            return null
        }

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        return runCatching {
            apiRepository.createStore(
                Store(
                    name = name,
                    location = location,
                ),
            )
        }.fold(
            onSuccess = { store ->
                _uiState.value = CreateStoreUiState()
                store
            },
            onFailure = { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Could not create store.",
                    )
                null
            },
        )
    }
}
