package org.example.srpfe.screens.nearby

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformLocation(): GeoPoint? {
    val context = LocalContext.current

    return remember(context) {
        val fineGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == 0
        val coarseGranted = context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == 0
        if (!fineGranted && !coarseGranted) {
            null
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                .asSequence()
                .mapNotNull { provider ->
                    runCatching { locationManager?.getLastKnownLocation(provider) }.getOrNull()
                }.firstOrNull()
                ?.let { GeoPoint(it.latitude, it.longitude) }
        }
    }
}
