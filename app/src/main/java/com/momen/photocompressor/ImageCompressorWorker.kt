package com.momen.photocompressor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class ImageCompressorWorker(
    private val appContext: Context, private val params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            val stringUri = params.inputData.getString(KEY_CONTENT_URI)
            Log.e("ImageProcessing", "Received URI: $stringUri")

            if (stringUri.isNullOrEmpty()) {
                return@withContext Result.failure()
            }

            val compressionThresholdInBytes =
                params.inputData.getLong(KEY_COMPRESSION_THRESHOLD, 0L)

            val uri = Uri.parse(stringUri)
            val bytes = if (uri.scheme == "http" || uri.scheme == "https") {
                // Download the image from the URL
                downloadImage(uri.toString()) ?: return@withContext Result.failure()
            } else {
                // Read from local storage
                appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: return@withContext Result.failure()

            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            var outputBytes: ByteArray
            var quality = 100
            do {
                val outputStream = ByteArrayOutputStream()
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
                    outputBytes = it.toByteArray()
                    quality -= (quality * 0.1).roundToInt()
                }
            } while (outputBytes.size > compressionThresholdInBytes && quality > 5)

            val file = File(appContext.cacheDir, "${params.id}.jpg")
            file.writeBytes(outputBytes)
            Result.success(workDataOf(KEY_RESULT_PATH to file.absolutePath))
        }
    }

    private fun downloadImage(urlString: String): ByteArray? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.inputStream.use { it.readBytes() }
        } catch (e: Exception) {
            Log.e("ImageCompressorWorker", "Error downloading image: ${e.message}")
            null
        }
    }

    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_PATH = "KEY_RESULT_PATH"
    }
}
