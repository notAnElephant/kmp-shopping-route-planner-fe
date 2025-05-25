package org.example.srp_fe.screens.physicallist

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.kashif.cameraK.ui.CameraPreview
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import kotlinx.coroutines.launch


@Composable
fun PhysicalListCameraScreen(
    onImageCaptured: (ImageBitmap) -> Unit
) {
    val cameraController = remember { mutableStateOf<CameraController?>(null) }
    val errorState = remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraConfiguration = {
                setCameraLens(CameraLens.BACK)
                setFlashMode(FlashMode.OFF)
                setImageFormat(ImageFormat.JPEG)
                setDirectory(Directory.PICTURES)
            },
            onCameraControllerReady = {
                cameraController.value = it
                // Additional setup if required
            }
        )


        Spacer(modifier = Modifier.height(16.dp))

        val coroutineScope = rememberCoroutineScope()

        Button(
            onClick = {
                coroutineScope.launch {
                    cameraController.value?.takePicture(
//                        onImageCaptured = asd
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Capture")
        }


        errorState.value?.let { error ->
            Text("Error: $error", modifier = Modifier.padding(16.dp))
        }
    }
}
