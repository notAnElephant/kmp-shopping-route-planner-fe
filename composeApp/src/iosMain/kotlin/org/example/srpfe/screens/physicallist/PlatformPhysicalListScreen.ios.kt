package org.example.srpfe.screens.physicallist

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

@Composable
actual fun PlatformPhysicalListScreen() {
    val viewModel = koinInject<PhysicalListViewModel>()

    PhysicalListScreen(
        viewModel = viewModel,
        onLaunchCamera = { /* not yet implemented on iOS */ },
    )
}
