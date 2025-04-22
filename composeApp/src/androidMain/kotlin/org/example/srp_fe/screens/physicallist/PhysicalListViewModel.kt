package org.example.srp_fe.screens.physicallist

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.ApiRepository
import org.openapitools.client.infrastructure.Base64ByteArray
import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class PhysicalListViewModel actual constructor(
    private val apiRepository: ApiRepository
) {
    private val _uiState = MutableStateFlow(PhysicalListUiState())
    val uiState: StateFlow<PhysicalListUiState> = _uiState

    private var currentPhotoPath: String? = null

    fun createImageUri(context: Context): Uri {
        val imageFile = File.createTempFile("photo_", ".jpg", context.cacheDir).apply {
            currentPhotoPath = absolutePath
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }

    fun onPhotoCaptured(uri: Uri, context: Context) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val imageBitmap = bitmap?.asImageBitmap()

        if (bitmap != null && imageBitmap != null) {
            _uiState.update { it.copy(imageBitmap = imageBitmap) }

            CoroutineScope(Dispatchers.IO).launch {
                val byteArrayOutputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                val imageBytes = byteArrayOutputStream.toByteArray()

                val base64Wrapped = listOf(Base64ByteArray(imageBytes))
                val result = apiRepository.googleOcr(base64Wrapped)

                _uiState.update { it.copy(ocrResult = result) }
            }
        }
    }
}
