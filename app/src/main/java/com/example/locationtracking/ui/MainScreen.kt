package com.example.locationtracking.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.locationtracking.vm.TrackingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@androidx.annotation.RequiresPermission(
    android.Manifest.permission.POST_NOTIFICATIONS
)
@Composable
fun MainScreen(
    viewModel: TrackingViewModel, onStartTrackingClick: () -> Unit, onStopTrackingClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lines by viewModel.lines.collectAsState()


    LaunchedEffect(uiState) {
        viewModel.loadLines()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.isTracking) "TRACKING" else "STOPPED",
            color = if (uiState.isTracking) MaterialTheme.colorScheme.onBackground else Color.Gray,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Current Location: ${uiState.lastLatitude}, ${uiState.lastLongitude}",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Last Updated: ${uiState.lastTimestamp ?: "--"}",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onStartTrackingClick, enabled = !uiState.isTracking
        ) {
            Text("Start Tracking")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onStopTrackingClick,
            enabled = uiState.isTracking,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Stop Tracking")
        }
        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Saved Locations", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Clear All",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Red,
                    modifier = Modifier.clickable { viewModel.clearLogFile() }
                       )
            }

        }
        Spacer(Modifier.height(8.dp))
        LazyColumn {
            items(lines) { line ->
                Text(text = line, style = MaterialTheme.typography.bodyMedium)
            }
        }

    }
}