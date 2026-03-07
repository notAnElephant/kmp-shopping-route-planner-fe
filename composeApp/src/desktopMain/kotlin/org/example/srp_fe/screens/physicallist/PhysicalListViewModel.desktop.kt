package org.example.srpfe.screens.physicallist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.example.ApiRepository

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PhysicalListViewModel actual constructor(
    apiRepository: ApiRepository,
) {
    private val _uiState = MutableStateFlow(PhysicalListUiState())
    actual val uiState: StateFlow<PhysicalListUiState> = _uiState
}
