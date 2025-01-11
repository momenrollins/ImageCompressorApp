package com.momen.photocompressor

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class ImageProcessingViewModel : ViewModel() {
    var uncompressedImageUri: Uri? by mutableStateOf(null)
        private set
    var compressedBitmap: Bitmap? by mutableStateOf(null)
        private set

    var workId: UUID? by mutableStateOf(null)
        private set

    var inputUrl: String by mutableStateOf("")
        private set

    fun updateUncompressedImageUri(uri: Uri) {
        uncompressedImageUri = uri
    }

    fun updateCompressedBitmap(bitmap: Bitmap) {
        compressedBitmap = bitmap
    }

    fun updateWorkId(id: UUID) {
        workId = id
    }

    fun updateInputUrl(url: String) {
        inputUrl = url
    }

}