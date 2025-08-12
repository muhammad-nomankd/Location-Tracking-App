package com.example.locationtracking.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject // <-- Add this import

class StartTrackingUseCase @Inject constructor(private val repo: LocationRepository, @ApplicationContext private val context: Context) {
    suspend operator fun invoke(intervalMs: Long = 5000L) {
        repo.startLocationUpdates(intervalMs,context)
    }
}