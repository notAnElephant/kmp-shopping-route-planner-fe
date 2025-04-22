package org.example.srp_fe.screens.physicallist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.example.ApiRepository

@Composable
actual fun PlatformPhysicalListScreen(
    apiRepository: ApiRepository,
    navController: NavHostController
) {
    androidx.compose.material.Text("This screen is not available on desktop.")
}
