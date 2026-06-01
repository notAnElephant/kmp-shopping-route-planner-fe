package org.example.srpfe.screens.physicallist
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import org.koin.compose.koinInject

@Composable
actual fun PlatformPhysicalListScreen() {
    val viewModel = koinInject<PhysicalListViewModel>()
    val context = LocalContext.current
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoUri.value?.let { uri ->
                    viewModel.onPhotoCaptured(uri, context)
                }
            }
        }

    PhysicalListScreen(
        viewModel = viewModel,
        onLaunchCamera = {
            val uri = viewModel.createImageUri(context)
            photoUri.value = uri
            launcher.launch(uri)
        },
    )
}
