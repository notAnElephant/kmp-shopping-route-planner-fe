package org.example.srp_fe.screens.physicallist

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.StateFlow
import org.example.ApiRepository
import org.openapitools.client.models.ShopList

data class PhysicalListUiState(
    val imageBitmap: ImageBitmap? = null,
    val ocrResult: ShopList? = null,
)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhysicalListViewModel(
    apiRepository: ApiRepository,
) {
    val uiState: StateFlow<PhysicalListUiState>
}
