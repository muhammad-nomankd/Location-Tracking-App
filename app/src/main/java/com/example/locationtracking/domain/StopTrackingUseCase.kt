package com.example.locationtracking.domain

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class StopTrackingUseCase @Inject constructor(private val repo: LocationRepository,@ApplicationContext private val context: Context) {
    suspend operator fun invoke() {
        repo.stopLocationUpdates(context)
    }
}