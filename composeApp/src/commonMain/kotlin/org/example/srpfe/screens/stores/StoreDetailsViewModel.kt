package org.example.srpfe.screens.stores

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.openapitools.client.models.PlaceDetailsResponse
import org.openapitools.client.models.Store

data class StoreDetailsUiState(
    val store: Store? = null,
    val draftName: String = "",
    val draftLocation: String = "",
    val placeDetails: PlaceDetailsResponse? = null,
    val hasAttemptedInitialLoad: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isLoadingPlaceDetails: Boolean = false,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
)

class StoreDetailsViewModel(
    private val apiRepository: ApiRepository,
    private val storeId: Int,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StoreDetailsUiState())
    val uiState: StateFlow<StoreDetailsUiState> = _uiState.asStateFlow()

    suspend fun loadStore() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            apiRepository.getStore(storeId)
        }.onSuccess { store ->
            _uiState.value =
                _uiState.value.copy(
                    store = store,
                    draftName = store.name,
                    draftLocation = store.location.orEmpty(),
                    hasAttemptedInitialLoad = true,
                    isLoading = false,
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    store = null,
                    hasAttemptedInitialLoad = true,
                    isLoading = false,
                    errorMessage = error.message ?: "Could not load store.",
                )
        }
    }

    fun updateDraftName(value: String) {
        _uiState.value = _uiState.value.copy(draftName = value)
    }

    fun updateDraftLocation(value: String) {
        _uiState.value = _uiState.value.copy(draftLocation = value)
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    suspend fun saveStore(): Boolean {
        val currentState = _uiState.value
        val name = currentState.draftName.trim()
        val store = currentState.store ?: return false
        if (name.isBlank()) {
            _uiState.value = currentState.copy(errorMessage = "Store name is required.")
            return false
        }

        _uiState.value = currentState.copy(isSaving = true, errorMessage = null)
        return runCatching {
            apiRepository.updateStore(
                id = storeId,
                store =
                    store.copy(
                        name = name,
                        location = currentState.draftLocation.trim().ifBlank { null },
                    ),
            )
        }.fold(
            onSuccess = { updatedStore ->
                _uiState.value =
                    _uiState.value.copy(
                        store = updatedStore,
                        draftName = updatedStore.name,
                        draftLocation = updatedStore.location.orEmpty(),
                        isSaving = false,
                    )
                true
            },
            onFailure = { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Could not save store.",
                    )
                false
            },
        )
    }

    suspend fun loadPlaceDetails() {
        if (_uiState.value.store == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Load the store before requesting place details.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoadingPlaceDetails = true, errorMessage = null)
        runCatching {
            apiRepository.getStorePlaceDetails(storeId)
        }.onSuccess { placeDetails ->
            _uiState.value =
                _uiState.value.copy(
                    placeDetails = placeDetails,
                    isLoadingPlaceDetails = false,
                )
        }.onFailure { error ->
            _uiState.value =
                _uiState.value.copy(
                    isLoadingPlaceDetails = false,
                    errorMessage = error.message ?: "Could not load place details.",
                )
        }
    }

    suspend fun deleteStore(): Boolean {
        if (_uiState.value.store == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Load the store before deleting it.")
            return false
        }
        _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
        return runCatching {
            apiRepository.deleteStore(storeId)
        }.fold(
            onSuccess = {
                _uiState.value = _uiState.value.copy(isDeleting = false)
                true
            },
            onFailure = { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isDeleting = false,
                        errorMessage = error.message ?: "Could not delete store.",
                    )
                false
            },
        )
    }
}
