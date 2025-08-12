package com.example.locationtracking.domain // Added package declaration

import android.content.Context
import android.location.Location // Import android.location.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    suspend fun startLocationUpdates(intervalMs: Long = 1000L,context: Context)
    suspend fun stopLocationUpdates(context: Context)
    fun observeLastLocation(): Flow<Location?>
    fun getLoggedLines(): List<String>
    fun clearLogFile()
}
