package org.example.srp_fe.screens.physicallist

import androidx.compose.ui.graphics.ImageBitmap
import org.example.ApiRepository

data class PhysicalListUiState(
    val imageBitmap: ImageBitmap? = null,
    val ocrResult: String? = null
)


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhysicalListViewModel(apiRepository: ApiRepository)