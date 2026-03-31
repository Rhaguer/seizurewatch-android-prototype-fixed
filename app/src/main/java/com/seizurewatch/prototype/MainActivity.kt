package com.seizurewatch.prototype

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.seizurewatch.prototype.ui.CameraHeartRateScreen

class MainActivity : ComponentActivity() {

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)

        setContent {
            MaterialTheme {
                Surface {
                    CameraHeartRateScreen()
                }
            }
        }
    }
}
