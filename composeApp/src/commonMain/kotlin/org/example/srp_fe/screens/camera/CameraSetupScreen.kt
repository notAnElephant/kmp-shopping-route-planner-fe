package org.example.srp_fe.screens.camera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import kotlinx.coroutines.launch

@Composable
fun CameraSetupScreen(){
	// Initialize and check permissions
	val permissions = providePermissions()
	val cameraPermissionState = remember { mutableStateOf(permissions.hasCameraPermission()) }

// Request permissions if needed
	if (!cameraPermissionState.value) {
		permissions.RequestCameraPermission(
			onGranted = { cameraPermissionState.value = true },
			onDenied = { println("Camera Permission Denied") }
		)
	}

	val cameraController = remember { mutableStateOf<CameraController?>(null) }

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
		}
	)

// Display your custom camera UI once controller is ready
	cameraController.value?.let { controller ->
		CameraScreen(cameraController = controller)
	}

}

@Composable
fun CameraScreen(cameraController: CameraController) {
	val scope = rememberCoroutineScope()

	Button(
		onClick = {
			scope.launch {
				when (val result = cameraController.takePicture()) {
					is ImageCaptureResult.Success -> {
//						use result.byteArray
						// TODO Handle the captured image - send it to backend later on
					}
					is ImageCaptureResult.Error -> {
						println("Image Capture Error: ${result.exception.message}")
					}
				}
			}
		}
	) {
		Text("Capture")
	}
}
