package org.example.srp_fe.screens.physicallist
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import org.example.ApiRepository

@Composable
actual fun PlatformPhysicalListScreen(
    apiRepository: ApiRepository,
    navController: NavHostController
) {
    val viewModel by remember { mutableStateOf(PhysicalListViewModel(apiRepository)) }
    val context = LocalContext.current
    val photoUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
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
        apiRepository = apiRepository,
        navController = navController
    )
}
