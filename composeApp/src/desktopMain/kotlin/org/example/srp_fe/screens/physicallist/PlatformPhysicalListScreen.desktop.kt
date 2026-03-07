package org.example.srpfe.screens.physicallist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.example.ApiRepository
import org.example.srpfe.screens.camera.CameraViewModel

@Composable
actual fun PlatformPhysicalListScreen(
    apiRepository: ApiRepository,
    navController: NavHostController
) {
    val viewModel = remember { CameraViewModel(apiRepository) }
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val ocrResult by viewModel.ocrResult.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        ocrResult?.let {
            Text("OCR Result: $it")
            Spacer(modifier = Modifier.height(16.dp))
        }

        error?.let {
            Text("Error: $it")
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    val imageFile = FileKit.openFilePicker(type = FileKitType.Image)
                    val bytes =
                        imageFile?.let {
                            FileKit.compressImage(
                                bytes = it.readBytes(),
                                quality = 80,
                                maxWidth = 1024,
                                maxHeight = 1024,
                            )
                        }

                    if (bytes != null) {
                        viewModel.processUploadedImageWithOcr(bytes)
                    }
                }
            },
        ) {
            Text("Upload Image")
        }
    }
}
