package com.example.locationtracking.domain // Added package declaration

import android.location.Location // Import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun startLocationUpdates(intervalMs: Long = 5000L)
    suspend fun stopLocationUpdates()
    fun observeLastLocation(): Flow<Location?> // Changed to Flow<Location?> and using android.location.Location
    fun getLoggedLines(): List<String>

    fun clearLogFile()
}
