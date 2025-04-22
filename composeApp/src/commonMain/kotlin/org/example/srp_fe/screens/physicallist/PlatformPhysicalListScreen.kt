package org.example.srp_fe.screens.physicallist

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import org.example.ApiRepository

@Composable
expect fun PlatformPhysicalListScreen(
    apiRepository: ApiRepository,
    navController: NavHostController
)
