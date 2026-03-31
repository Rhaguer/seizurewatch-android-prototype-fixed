package com.seizurewatch.prototype.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.seizurewatch.prototype.camera.HeartRateAnalyzer

@Composable
fun CameraHeartRateScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var bpm by remember { mutableIntStateOf(0) }
    val previewView = remember { PreviewView(context) }

    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Medición con cámara",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = if (bpm > 0) "BPM: $bpm" else "Coloca el dedo sobre la cámara trasera y el flash",
            style = MaterialTheme.typography.bodyLarge
        )

        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f, fill = false)
        )

        if (!hasCameraPermission) {
            Text("Falta permiso de cámara")
        }

        Button(onClick = { }) {
            Text("Midiendo...")
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(
            ContextCompat.getMainExecutor(context),
            HeartRateAnalyzer { detected ->
                bpm = detected
            }
        )

        cameraProvider.unbindAll()
        val camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )

        camera.cameraControl.enableTorch(true)
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                val provider = ProcessCameraProvider.getInstance(context).get()
                provider.unbindAll()
            } catch (_: Exception) {
            }
        }
    }
}
