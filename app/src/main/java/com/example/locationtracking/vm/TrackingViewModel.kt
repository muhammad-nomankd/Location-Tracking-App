package com.example.locationtracking.vm

import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locationtracking.data.LocationRepositoryImpl
import com.example.locationtracking.data.TrackingState
import com.example.locationtracking.domain.GetLastLocationUseCase
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
    private val repo: LocationRepositoryImpl
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()
    val isTracking = repo.isUpdating

    val isTrackingService = TrackingState.isTracking.asStateFlow()

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
        try {
            _uiState.value = _uiState.value.copy(isTracking = true)
            TrackingState.isTracking.value = true
            notificationHelper.checkNotificationPermission(context)
            ForegroundLocationService.startService(context)
        } catch (e: Exception) {
            Log.e("TrackingViewModel", "Exception starting service", e)
            _uiState.value = _uiState.value.copy(isTracking = false)
            TrackingState.isTracking.value = false
        }

        viewModelScope.launch {
            try {
                startTrackingUseCase()
            } catch (e: Exception) {
                Log.e("TrackingViewModel", "Error starting tracking", e)
                _uiState.value = _uiState.value.copy(isTracking = false)
                TrackingState.isTracking.value = false
                notificationHelper.hideTrackingNotification(context)
            }
        }
    }

    fun onStopTracking(context: Context) {
        Log.d("TrackingViewModel", "onStopTracking called")

        _uiState.value = _uiState.value.copy(isTracking = false)
        TrackingState.isTracking.value = false
        viewModelScope.launch {
            try {

                stopTrackingUseCase()

                ForegroundLocationService.stopService(context)

                notificationHelper.hideTrackingNotification(context)

                loadLines()

                Log.d("TrackingViewModel", "Stop tracking completed successfully")
            } catch (e: Exception) {
                Log.e("TrackingViewModel", "Error stopping tracking", e)

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateLocationInState(location: Location) {

            val timestamp = Instant.ofEpochMilli(location.time)
                .atZone(java.time.ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            _uiState.value = _uiState.value.copy(
                lastLatitude = location.latitude,
                lastLongitude = location.longitude,
                lastTimestamp = timestamp
            )

    }

    fun clearLogFile(){
        repo.clearLogFile()
        loadLines()
    }
}