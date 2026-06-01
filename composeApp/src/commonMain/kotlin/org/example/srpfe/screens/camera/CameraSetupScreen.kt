package org.example.srpfe.screens.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kashif.cameraK.compose.CameraKScreen
import com.kashif.cameraK.compose.rememberCameraKState
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.state.CameraConfiguration
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CameraSetupScreen() {
    // Initialize and check permissions
    val permissions = providePermissions()
    val cameraPermissionState = remember { mutableStateOf(permissions.hasCameraPermission()) }

    val viewModel = koinViewModel<CameraViewModel>()

    // Request permissions if needed
    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState.value = true },
            onDenied = { println("Camera Permission Denied") },
        )
    }

    val cameraState by rememberCameraKState(
        config =
            CameraConfiguration(
                cameraLens = CameraLens.BACK,
                flashMode = FlashMode.OFF,
                imageFormat = ImageFormat.JPEG,
                directory = Directory.PICTURES,
            ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CameraKScreen(
            modifier = Modifier.fillMaxSize(),
            cameraState = cameraState,
        ) { state ->
            CameraScreen(cameraController = state.controller, viewModel = viewModel)
        }
    }
}

@Composable
fun CameraScreen(
    cameraController: CameraController,
    viewModel: CameraViewModel,
) {
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()
    val ocrResult by viewModel.ocrResult.collectAsState()
    val error by viewModel.error.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Show loading indicator if processing
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Show result or error if available
        if (ocrResult.isNotEmpty()) {
            Text(
                text = "OCR Result: ${ocrResult.joinToString { "${it.shoppingItemName} (${it.attributes})" }}",
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
            )
        }

        error?.let { errorMsg ->
            Text(
                text = "Error: $errorMsg",
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
            )
        }

        // Buttons at the bottom
        Row(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
        ) {
            // Capture button
            Button(
                onClick = {
                    scope.launch {
                        when (val result = cameraController.takePictureToFile()) {
                            is ImageCaptureResult.Success -> {
                                println("Captured Image Byte Array: ${result.byteArray.size} bytes")

                                // Send the captured image to backend using the ViewModel
                                viewModel.processImageWithOcr(result.byteArray)
                            }

                            is ImageCaptureResult.SuccessWithFile -> {
                                println("Captured Image File: ${result.filePath}")
                            }

                            is ImageCaptureResult.Error -> {
                                println("Image Capture Error: ${result.exception.message}")
                            }
                        }
                    }
                },
            ) {
                Text("Capture")
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Upload button
            Button(
                onClick = {
                    scope.launch {
                        println("Upload button clicked - implement with FileKit")

                        try {
                            // Pick only images
                            val imageFile = FileKit.openFilePicker(type = FileKitType.Image)

                            val byteArray =
                                imageFile?.let {
                                    FileKit.compressImage(
                                        bytes = it.readBytes(),
                                        quality = 80,
                                        maxWidth = 1024,
                                        maxHeight = 1024,
                                    )
                                }
                            if (byteArray != null) {
                                viewModel.processUploadedImageWithOcr(byteArray)
                            }
                        } catch (e: Exception) {
                            println("Error in file picking: ${e.message}")
                        }
                    }
                },
            ) {
                Text("Upload")
            }
        }
    }
}
