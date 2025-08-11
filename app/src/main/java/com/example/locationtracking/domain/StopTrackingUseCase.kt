package com.example.locationtracking.domain

import jakarta.inject.Inject

class StopTrackingUseCase @Inject constructor(private val repo: LocationRepository) {
    suspend operator fun invoke() {
        repo.stopLocationUpdates()
    }
}