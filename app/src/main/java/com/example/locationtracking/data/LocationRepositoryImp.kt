package com.example.locationtracking.data

import com.example.locationtracking.domain.LocationRepository
import android.Manifest
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val fused: FusedLocationProviderClient,
    private val fileDataSource: LocalFileDataSource
) : LocationRepository {

    private val _lastLocation = MutableStateFlow<Location?>(null)
    override fun observeLastLocation(): Flow<Location?> = _lastLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    var isUpdating = false

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override suspend fun startLocationUpdates(intervalMs: Long) {
        if (isUpdating) {
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateDistanceMeters(0f)
            .build()

        locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(result: LocationResult) {
                if (!isUpdating) {
                    Log.d("LocationRepo", "Received location but not tracking, ignoring")
                    return
                }

                val loc: Location = result.lastLocation ?: return
                Log.d("LocationRepo", "New location: ${loc.latitude}, ${loc.longitude}")

                _lastLocation.value = loc
                val ts = Instant.ofEpochMilli(loc.time).toString()
                fileDataSource.appendLocationLine(ts, loc.latitude, loc.longitude)
            }
        }

        try {
            fused.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            isUpdating = true
        } catch (e: Exception) {
            locationCallback = null
            throw e
        }
    }

    override suspend fun stopLocationUpdates() {
        isUpdating = false

        val callback = locationCallback
        if (callback == null) {
            Log.d("LocationRepo", "locationCallback is null, nothing to stop")
            return
        }

        try {
            Log.d("LocationRepo", "Removing location updates with callback: $callback")
            fused.removeLocationUpdates(callback)
            Log.d("LocationRepo", "Location updates removed successfully")
        } catch (e: Exception) {
            Log.e("LocationRepo", "Error removing location updates", e)
        } finally {
            locationCallback = null
            Log.d("LocationRepo", "locationCallback set to null")
        }
    }

    override fun getLoggedLines(): List<String> = fileDataSource.readAllLines()

    override fun clearLogFile() {
        fileDataSource.clearAllLines()
    }




}

object TrackingState {
    val isTracking = MutableStateFlow(false)
}