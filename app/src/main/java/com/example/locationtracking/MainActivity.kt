package com.example.locationtracking

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.core.content.ContextCompat
import com.example.locationtracking.ui.MainScreen
import com.example.locationtracking.vm.TrackingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: TrackingViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestForegroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkAndRequestBackgroundLocation()
            } else {
                Toast.makeText(this, "Foreground location denied. Cannot track location.", Toast.LENGTH_LONG).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestBackgroundLocationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                checkAndRequestNotificationPermission()
            } else {
                Toast.makeText(this, "Background location denied. Tracking will stop when app is closed.", Toast.LENGTH_LONG).show()
                checkAndRequestNotificationPermission()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "Notification permission denied. You won't see tracking notifications.", Toast.LENGTH_LONG).show()
            }
            viewModel.onStartTracking(this@MainActivity)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndRequestForegroundLocation() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkAndRequestBackgroundLocation()
            }

            else -> {
                requestForegroundLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndRequestBackgroundLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                    checkAndRequestNotificationPermission()
                }
                else -> {
                    try {
                        requestBackgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    } catch (e: Exception) {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                        Toast.makeText(this, "Enable 'Allow all the time' in Location settings.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            checkAndRequestNotificationPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                    viewModel.onStartTracking(this@MainActivity)
                }
                else -> {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            viewModel.onStartTracking(this@MainActivity)
        }
    }
    @androidx.annotation.RequiresPermission(
        android.Manifest.permission.POST_NOTIFICATIONS
    )
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                MainScreen(
                    viewModel = viewModel,
                    onStartTrackingClick = { checkAndRequestForegroundLocation() },
                    onStopTrackingClick = { viewModel.onStopTracking(this) }
                )
            }
        }
    }
}
