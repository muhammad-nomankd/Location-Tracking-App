package com.example.locationtracking.domain

import javax.inject.Inject // <-- Add this import

class StartTrackingUseCase @Inject constructor(private val repo: LocationRepository) {
    suspend operator fun invoke(intervalMs: Long = 5000L) {
        repo.startLocationUpdates(intervalMs)
    }
}