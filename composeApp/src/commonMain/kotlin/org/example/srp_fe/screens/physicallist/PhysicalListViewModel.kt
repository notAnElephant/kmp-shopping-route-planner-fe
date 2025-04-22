package org.example.srp_fe.screens.physicallist

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.example.ApiRepository
import org.lighthousegames.logging.logging

data class UiState(
    val imageBitmap: ImageBitmap? = null,
    val ocrText: String = ""
)

class PhysicalListViewModel(apiRepository: ApiRepository) : ViewModel() {
    companion object {
        val log = logging()
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState


    fun openCamera() {
        log.d { "openCamera" }
        TODO("Not yet implemented")
    }

    fun sendImageForOCR() {
        log.d { "sendImageForOCR" }
        TODO("Not yet implemented")
    }

}
