package com.momen.photocompressor

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil3.compose.AsyncImage
import com.momen.photocompressor.ui.theme.PhotoCompressorTheme
import java.io.File

class ImageSharingActivity : ComponentActivity() {
    private lateinit var workManager: WorkManager
    private val viewModel by viewModels<ImageProcessingViewModel>()
    private var inputUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workManager = WorkManager.getInstance(applicationContext)
        enableEdgeToEdge()
        setContent {
            PhotoCompressorTheme {
                val workerResult = viewModel.workId?.let { id ->
                    workManager.getWorkInfoByIdLiveData(id).observeAsState().value
                }
                LaunchedEffect(key1 = workerResult?.outputData) {
                    Log.e("ImageProcessing", "Worker result: ${workerResult?.outputData}")
                    if (workerResult?.outputData != null) {
                        val filePath =
                            workerResult.outputData.getString(ImageCompressorWorker.KEY_RESULT_PATH)
                        filePath?.let {
                            val bitmap = BitmapFactory.decodeFile(it)
                            viewModel.updateCompressedBitmap(bitmap)
                            viewModel.updateCompressedFilePath(it)
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize().padding(top = 50.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    TextField(
                        value = viewModel.inputUrl,
                        onValueChange = { viewModel.updateInputUrl(it) },
                        label = { Text("Enter Image URL or Share an Image") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (viewModel.inputUrl.isNotEmpty()) {
                                inputUri = Uri.parse(viewModel.inputUrl)
                                processImage(inputUri)
                            }
                        }
                    ) {
                        Text("Compress Image from URL or Shared Content")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    viewModel.uncompressedImageUri?.let {
                        Text(text = "Uncompressed image")
                        AsyncImage(
                            model = it,
                            contentDescription = null,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    viewModel.compressedBitmap?.let {
                        Text(text = "Compressed image")
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { downloadCompressedImage() }) {
                            Text("Download Compressed Image")
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } ?: return

        inputUri = uri
        processImage(uri)
    }

    private fun processImage(uri: Uri?) {
        uri?.let {
            viewModel.updateUncompressedImageUri(it)
            val request = OneTimeWorkRequestBuilder<ImageCompressorWorker>().setInputData(
                workDataOf(
                    ImageCompressorWorker.KEY_CONTENT_URI to it.toString(),
                    ImageCompressorWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 20L
                )
            ).setConstraints(
                Constraints(requiresStorageNotLow = true)
            ).build()

            viewModel.updateWorkId(request.id)
            workManager.enqueue(request)
        }
    }

    private fun downloadCompressedImage() {
        val filePath = viewModel.compressedFilePath ?: return
        val file = File(filePath)
        val contentUri = FileProvider.getUriForFile(
            this,
            "com.momen.photocompressor.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Save or Share Image"))
    }
}
