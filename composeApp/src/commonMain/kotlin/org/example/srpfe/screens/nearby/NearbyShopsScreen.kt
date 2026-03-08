package org.example.srpfe.screens.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import compose.icons.FeatherIcons
import compose.icons.feathericons.Camera
import compose.icons.feathericons.Navigation
import org.example.ApiRepository
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private data class NearbyShop(
    val name: String,
    val openingTime: String,
    val photos: List<String>,
    val coordinates: GeoPoint,
)

@Composable
fun NearbyShopsScreen(
    apiRepository: ApiRepository,
    navController: NavHostController,
) {
    val uriHandler = LocalUriHandler.current
    val gpsLocation = rememberPlatformLocation()
    val fallbackLocation = GeoPoint(latitude = 47.4979, longitude = 19.0402)
    val userLocation = gpsLocation ?: fallbackLocation
    var radiusKm by remember { mutableFloatStateOf(2.5f) }

    val shops =
        remember {
            // todo dummy data
            listOf(
                NearbyShop(
                    name = "Spar Oktogon",
                    openingTime = "06:30 - 22:00",
                    photos = listOf("Streetfront", "Produce aisle"),
                    coordinates = GeoPoint(47.5050, 19.0638),
                ),
                NearbyShop(
                    name = "Lidl Rákóczi út",
                    openingTime = "07:00 - 21:00",
                    photos = listOf("Entrance", "Weekly deals"),
                    coordinates = GeoPoint(47.4963, 19.0704),
                ),
                NearbyShop(
                    name = "Aldi Corvin",
                    openingTime = "07:00 - 21:00",
                    photos = listOf("Storefront", "Checkout area"),
                    coordinates = GeoPoint(47.4857, 19.0716),
                ),
                NearbyShop(
                    name = "Tesco Express Deák",
                    openingTime = "06:00 - 23:00",
                    photos = listOf("Facade", "Fresh bakery"),
                    coordinates = GeoPoint(47.4973, 19.0541),
                ),
            )
        }

    val visibleShops =
        shops
            .map { shop -> shop to distanceKm(userLocation, shop.coordinates) }
            .filter { (_, distance) -> distance <= radiusKm }
            .sortedBy { (_, distance) -> distance }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Nearby Shops",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text =
                if (gpsLocation != null) {
                    "Using current location"
                } else {
                    "GPS unavailable, using city-center fallback"
                },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Radius: ${radiusKm.formatDecimals(1)} km",
            style = MaterialTheme.typography.titleMedium,
        )
        Slider(
            value = radiusKm,
            onValueChange = { radiusKm = it },
            valueRange = 0.5f..10f,
        )

        if (visibleShops.isEmpty()) {
            Text(
                text = "No shops found in this radius.",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                visibleShops.forEach { (shop, distance) ->
                    NearbyShopCard(
                        shop = shop,
                        distanceKm = distance,
                        onNavigate = {
                            uriHandler.openUri(
                                "https://www.google.com/maps/dir/?api=1&destination=${shop.coordinates.latitude},${shop.coordinates.longitude}",
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NearbyShopCard(
    shop: NearbyShop,
    distanceKm: Double,
    onNavigate: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = shop.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Opening hours: ${shop.openingTime}",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Distance: ${distanceKm.formatDecimals(2)} km",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier =
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                shop.photos.forEach { label ->
                    PhotoPlaceholder(label = label)
                }
            }

            Button(onClick = onNavigate) {
                Icon(FeatherIcons.Navigation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open in navigation app")
            }
        }
    }
}

@Composable
private fun PhotoPlaceholder(label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier =
                Modifier
                    .size(width = 148.dp, height = 96.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = FeatherIcons.Camera,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun distanceKm(
    from: GeoPoint,
    to: GeoPoint,
): Double {
    val earthRadiusKm = 6371.0
    val latDistance = (to.latitude - from.latitude).toRadians()
    val lonDistance = (to.longitude - from.longitude).toRadians()
    val a =
        sin(latDistance / 2).pow(2) +
            cos(from.latitude.toRadians()) *
            cos(to.latitude.toRadians()) *
            sin(lonDistance / 2).pow(2)
    return 2 * earthRadiusKm * asin(sqrt(a))
}

private fun Double.toRadians(): Double = this * PI / 180.0

private fun Number.formatDecimals(decimals: Int): String {
    val factor = 10.0.pow(decimals)
    val rounded = kotlin.math.round(this.toDouble() * factor) / factor
    val text = rounded.toString()
    val dotIndex = text.indexOf('.')
    if (decimals == 0) {
        return text.substringBefore('.')
    }
    if (dotIndex == -1) {
        return text + "." + "0".repeat(decimals)
    }
    val currentDecimals = text.length - dotIndex - 1
    return if (currentDecimals >= decimals) {
        text
    } else {
        text + "0".repeat(decimals - currentDecimals)
    }
}
