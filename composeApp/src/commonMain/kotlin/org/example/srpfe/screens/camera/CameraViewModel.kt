package org.example.srpfe.screens.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.example.ApiRepository
import org.example.srpfe.screens.shopmapdrawer.ShopMapDrawerViewModel.Companion.log
import org.lighthousegames.logging.logging
import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.CreateShoppingListItemRequest

class CameraViewModel(
    private val apiRepository: ApiRepository,
) : ViewModel() {
    private val _ocrResult = MutableStateFlow<List<CreateShoppingListItemRequest>>(emptyList())
    val ocrResult: StateFlow<List<CreateShoppingListItemRequest>> = _ocrResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    suspend fun processImageWithOcr(byteArray: ByteArray) {
        try {
            _isLoading.value = true
            _error.value = null

            // Convert ByteArray to Base64ByteArray
            val base64Image = Base64ByteArray(byteArray)

            // Call the API
            val result = apiRepository.googleOcr(listOf(base64Image))

            // Update the result
            _ocrResult.value = result
            log.d { result }
        } catch (e: Exception) {
            _error.value = e.message ?: "Unknown error occurred"
        } finally {
            _isLoading.value = false
        }
    }

    // This method is used when an image is uploaded from the device
    // It reuses the same logic as processImageWithOcr
    suspend fun processUploadedImageWithOcr(byteArray: ByteArray) {
        processImageWithOcr(byteArray)
    }

    companion object {
        val log = logging()
    }
}
