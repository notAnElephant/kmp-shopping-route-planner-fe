package org.example.srpfe.screens.stores

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.openapitools.client.models.AccessibilityOptions
import org.openapitools.client.models.OpeningHours
import org.openapitools.client.models.ParkingOptions

@Composable
fun StoreDetailsScreen(
    storeId: Int,
    onBack: () -> Unit,
    onOpenMapEditor: () -> Unit,
    onStoreDeleted: () -> Unit,
) {
    StoreAccessGate {
        val viewModel =
            koinViewModel<StoreDetailsViewModel>(
                parameters = { parametersOf(storeId) },
            )
        val uiState by viewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val uriHandler = LocalUriHandler.current

        LaunchedEffect(storeId) {
            viewModel.loadStore()
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Store Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            if (uiState.isLoading && uiState.store == null) {
                CircularProgressIndicator()
            } else if (uiState.store == null && uiState.hasAttemptedInitialLoad) {
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.loadStore()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Retry")
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Back")
                }
            } else {
                OutlinedTextField(
                    value = uiState.draftName,
                    onValueChange = viewModel::updateDraftName,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Store name") },
                    singleLine = true,
                )

                OutlinedTextField(
                    value = uiState.draftLocation,
                    onValueChange = viewModel::updateDraftLocation,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Location") },
                    singleLine = true,
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.saveStore()
                        }
                    },
                    enabled = !uiState.isSaving && !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save changes")
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            viewModel.loadPlaceDetails()
                        }
                    },
                    enabled = !uiState.isLoadingPlaceDetails && !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.isLoadingPlaceDetails) "Loading place details..." else "Load place details")
                }

                Button(
                    onClick = onOpenMapEditor,
                    enabled = uiState.store != null && !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Open map editor")
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            if (viewModel.deleteStore()) {
                                onStoreDeleted()
                            }
                        }
                    },
                    enabled = !uiState.isDeleting,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (uiState.isDeleting) "Deleting..." else "Delete store")
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Back")
                }

                uiState.placeDetails?.let { placeDetails ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Place details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            PlaceDetailLine("Google place id", placeDetails.id)
                            PlaceDetailLine("Phone", placeDetails.internationalPhoneNumber)
                            PlaceDetailLine("Website", placeDetails.websiteUri)
                            PlaceDetailLine("Maps", placeDetails.googleMapsUri)
                            PlaceDetailLine("Rating", placeDetails.rating?.toString())
                            PlaceDetailLine("Ratings", placeDetails.userRatingCount?.toString())
                            PlaceDetailLine("Price level", placeDetails.priceLevel?.toString())
                            PlaceDetailLine("Photos", placeDetails.photos?.joinToString { it.name })
                            PlaceDetailLine("Parking", formatParking(placeDetails.parkingOptions))
                            PlaceDetailLine("Accessibility", formatAccessibility(placeDetails.accessibilityOptions))
                            PlaceDetailLine("Opening hours", formatOpeningHours(placeDetails.regularOpeningHours))

                            placeDetails.websiteUri?.let { websiteUri ->
                                Button(
                                    onClick = { uriHandler.openUri(websiteUri) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Open website")
                                }
                            }

                            placeDetails.googleMapsUri?.let { mapsUri ->
                                OutlinedButton(
                                    onClick = { uriHandler.openUri(mapsUri) },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Open in Google Maps")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceDetailLine(
    label: String,
    value: String?,
) {
    if (value.isNullOrBlank()) {
        return
    }
    Text("$label: $value")
}

private fun formatParking(parkingOptions: ParkingOptions?): String? {
    if (parkingOptions == null) {
        return null
    }
    val values =
        buildList {
            if (parkingOptions.freeParkingLot == true) add("Free parking lot")
            if (parkingOptions.freeStreetParking == true) add("Free street parking")
            if (parkingOptions.freeGarageParking == true) add("Free garage parking")
        }
    return values.takeIf { it.isNotEmpty() }?.joinToString()
}

private fun formatAccessibility(accessibilityOptions: AccessibilityOptions?): String? =
    if (accessibilityOptions?.wheelchairAccessibleEntrance == true) {
        "Wheelchair accessible entrance"
    } else {
        null
    }

private fun formatOpeningHours(openingHours: OpeningHours?): String? =
    openingHours?.periods
        ?.takeIf { it.isNotEmpty() }
        ?.joinToString(separator = "; ") { period ->
            "${dayName(period.open.day)} ${formatTime(period.open.hour, period.open.minute)} - " +
                "${dayName(period.close.day)} ${formatTime(period.close.hour, period.close.minute)}"
        }

private fun formatTime(
    hour: Int,
    minute: Int,
): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private fun dayName(day: Int): String =
    when (day) {
        0 -> "Sun"
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        else -> "Day $day"
    }
