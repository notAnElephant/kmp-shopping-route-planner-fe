package org.example.srpfe.screens.physicallist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PhysicalListScreen(
    viewModel: PhysicalListViewModel,
    onLaunchCamera: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (uiState.imageBitmap == null) {
            Text("No image captured yet")
        } else {
            Image(bitmap = uiState.imageBitmap!!, contentDescription = null)
        }

        Button(onClick = onLaunchCamera) {
            Text("Take Photo")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
//            coroutineScope.launch { viewModel.sendImageForOCR() }
            // TODO send image for ocr?
        }) {
            Text("Extract Text")
        }
    }
}
