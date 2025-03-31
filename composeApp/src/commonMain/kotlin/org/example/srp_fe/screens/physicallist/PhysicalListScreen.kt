package org.example.srp_fe.screens.physicallist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import org.example.ApiRepository

@Composable
fun PhysicalListScreen(apiRepository: ApiRepository, navController: NavHostController) {
    val viewModel by remember { mutableStateOf(PhysicalListViewModel(apiRepository)) }
    val uiState by viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if (uiState.imageBitmap == null) {
            Text("No image captured yet")
        } else {
            Image(bitmap = uiState.imageBitmap ?: return@Column, contentDescription = null)
        }

        Button(onClick = { viewModel.openCamera() }) {
            Text("Take Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            coroutineScope.launch { viewModel.sendImageForOCR() }
        }) {
            Text("Extract Text")
        }
    }
}
