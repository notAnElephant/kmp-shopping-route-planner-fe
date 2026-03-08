package org.example.srpfe.screens.nearby

import androidx.compose.runtime.Composable

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

@Composable
expect fun rememberPlatformLocation(): GeoPoint?
