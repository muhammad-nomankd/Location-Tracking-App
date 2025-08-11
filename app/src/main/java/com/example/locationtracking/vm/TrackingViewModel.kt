package com.example.locationtracking.vm

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracking.domain.GetLastLocationUseCase
import com.example.locationtracking.domain.LocationRepository
import com.example.locationtracking.domain.StartTrackingUseCase
import com.example.locationtracking.domain.StopTrackingUseCase
import com.example.locationtracking.service.ForegroundLocationService
import com.example.locationtracking.ui.TrackingUiState
import com.example.locationtracking.util.NotificationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val startTrackingUseCase: StartTrackingUseCase,
    private val stopTrackingUseCase: StopTrackingUseCase,
    private val getLastLocationUseCase: GetLastLocationUseCase,
    private val notificationHelper: NotificationHelper,
    private val repo: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getLastLocationUseCase().collect { location ->
                location?.let {
                    updateLocationInState(it)
                }
            }
        }
    }

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines

    fun loadLines() {
        val data = repo.getLoggedLines()
        _lines.value = data
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onStartTracking(context: Context) {
        _uiState.value = _uiState.value.copy(isTracking = true)
        notificationHelper.checkNotificationPermission(context)

        try {
            ForegroundLocationService.startService(context)
        } catch (e: Exception) {
            Log.e("TrackingViewModel", "Exception starting service", e)
            _uiState.value = _uiState.value.copy(isTracking = false)
        }

        viewModelScope.launch {
            try {
                startTrackingUseCase()
            } catch (e: Exception) {
                Log.e("TrackingViewModel", "Error starting tracking", e)
                _uiState.value = _uiState.value.copy(isTracking = false)
                notificationHelper.hideTrackingNotification(context)
            }
        }
    }

    fun onStopTracking(context: Context) {
        Log.d("TrackingViewModel", "onStopTracking called")

        // Update UI state immediately
        _uiState.value = _uiState.value.copy(isTracking = false)

        viewModelScope.launch {
            try {
                // First stop location updates through repository
                Log.d("TrackingViewModel", "Stopping location updates via repository")
                stopTrackingUseCase()

                // Then stop the service
                Log.d("TrackingViewModel", "Stopping foreground service")
                ForegroundLocationService.stopService(context)

                // Finally hide notification
                notificationHelper.hideTrackingNotification(context)

                Log.d("TrackingViewModel", "Stop tracking completed successfully")
            } catch (e: Exception) {
                Log.e("TrackingViewModel", "Error stopping tracking", e)
                // Still try to clean up even if there was an error
                try {
                    ForegroundLocationService.stopService(context)
                    notificationHelper.hideTrackingNotification(context)
                } catch (cleanupException: Exception) {
                    Log.e("TrackingViewModel", "Error during cleanup", cleanupException)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocationInState(location: Location) {
        // Only update if we're still tracking
        if (_uiState.value.isTracking) {
            val timestamp = Instant.ofEpochMilli(location.time)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            _uiState.value = _uiState.value.copy(
                lastLatitude = location.latitude,
                lastLongitude = location.longitude,
                lastTimestamp = timestamp
            )

            Log.d("TrackingViewModel", "Location updated: ${location.latitude}, ${location.longitude}")
        }
    }

    fun clearLogFile(){
        repo.clearLogFile()
        loadLines()
    }
}