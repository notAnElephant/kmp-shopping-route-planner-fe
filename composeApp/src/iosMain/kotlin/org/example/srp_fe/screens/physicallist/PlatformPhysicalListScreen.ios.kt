package org.example.srp_fe.screens.physicallist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.example.ApiRepository

@Composable
actual fun PlatformPhysicalListScreen(
    apiRepository: ApiRepository,
    navController: NavHostController
) {
    // TODO: Implement iOS-specific camera logic
    // For now, just pass a no-op camera callback
    val viewModel = PhysicalListViewModel(apiRepository)

    PhysicalListScreen(
        viewModel = viewModel,
        onLaunchCamera = { /* not yet implemented on iOS */ },
        apiRepository = apiRepository,
        navController = navController
    )
}
