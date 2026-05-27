package org.example.srpfe.screens.physicallist

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.StateFlow
import org.example.ApiRepository
import org.openapitools.client.models.CreateShoppingListItemRequest

data class PhysicalListUiState(
    val imageBitmap: ImageBitmap? = null,
    val ocrResult: List<CreateShoppingListItemRequest> = emptyList(),
)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhysicalListViewModel(
    apiRepository: ApiRepository,
) {
    val uiState: StateFlow<PhysicalListUiState>
}
