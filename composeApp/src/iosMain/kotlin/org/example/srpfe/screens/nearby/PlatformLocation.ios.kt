package org.example.srpfe.screens.nearby

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager

@Composable
@OptIn(ExperimentalForeignApi::class)
actual fun rememberPlatformLocation(): GeoPoint? =
    remember {
        val manager = CLLocationManager()
        manager.requestWhenInUseAuthorization()
        manager.location?.coordinate?.useContents {
            GeoPoint(latitude = latitude, longitude = longitude)
        }
    }
