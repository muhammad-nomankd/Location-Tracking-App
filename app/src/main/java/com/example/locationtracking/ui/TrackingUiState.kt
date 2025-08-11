package com.example.locationtracking.ui

// UiState.kt
data class TrackingUiState(
    val isTracking: Boolean = false,
    val lastLatitude: Double? = null,
    val lastLongitude: Double? = null,
    val lastTimestamp: String? = null
)
