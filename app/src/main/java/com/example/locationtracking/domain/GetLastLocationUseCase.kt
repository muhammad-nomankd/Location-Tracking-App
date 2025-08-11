package com.example.locationtracking.domain


import android.location.Location
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetLastLocationUseCase @Inject constructor(private val repo: LocationRepository) {
    operator fun invoke(): Flow<Location?> = repo.observeLastLocation()
}