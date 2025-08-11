package com.example.locationtracking.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.locationtracking.vm.TrackingViewModel

// TrackingScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TrackingScreen(viewModel: TrackingViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val ctx = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Location Tracker") }) }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tracking: ${uiState.isTracking}")
            Text("Latitude: ${uiState.lastLatitude ?: "n/a"}")
            Text("Longitude: ${uiState.lastLongitude ?: "n/a"}")
            Text("Last updated: ${uiState.lastTimestamp ?: "n/a"}")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.onStartTracking(ctx) }) {
                    Text("Start")
                }
                Button(onClick = { viewModel.onStopTracking(ctx) }) {
                    Text("Stop")
                }
            }
        }
    }
}
