package com.example.locationtracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.locationtracking.data.LocationRepositoryImpl
import com.example.locationtracking.data.TrackingState
import com.example.locationtracking.domain.LocationRepository
import com.example.locationtracking.util.NotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundLocationService : Service() {

    @Inject
    lateinit var repository: LocationRepository
    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var LocationRepoImp: LocationRepositoryImpl

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val NOTIF_ID = 1
    private var tracking = false
    private val updateIntervalMs = 500L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            ACTION_START -> startForegroundServiceWork()
            ACTION_STOP -> {
                stopForegroundServiceWork()
                TrackingState.isTracking.value = false
            }
            else -> {
                Log.d("ForegroundService", "Unknown action or null intent")
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWork() {
        if (tracking) {
            return
        }

        tracking = true

        try {
            val notification = notificationHelper.buildTrackingNotification(this)
            startForeground(NOTIF_ID, notification)

            serviceScope.launch {
                try {
                    repository.startLocationUpdates(updateIntervalMs)
                } catch (e: SecurityException) {
                    stopSelf()
                } catch (e: Exception) {
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            tracking = false
            stopSelf()
        }
    }

    private fun stopForegroundServiceWork() {

        if (!tracking) {
            return
        }

        tracking = false

        serviceScope.launch {
            try {
                repository.stopLocationUpdates()
            } catch (e: Exception) {
                Log.e("ForegroundService", "Exception stopping location updates", e)
            } finally {
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Error stopping foreground", e)
                }
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        tracking = false

        serviceScope.launch {
            try {
                repository.stopLocationUpdates()
            } catch (e: Exception) {
                Log.e("ForegroundService", "Error stopping updates during destroy", e)
            }
        }

        serviceScope.cancel()
        super.onDestroy() }

    companion object {
        const val ACTION_START = "com.example.locationtracker.action.START"
        const val ACTION_STOP = "com.example.locationtracker.action.STOP"

        fun startService(context: Context) {
            Log.d("ForegroundService", "Starting service via companion")
            val intent = Intent(context, ForegroundLocationService::class.java).apply {
                action = ACTION_START
            }
            try {
                ContextCompat.startForegroundService(context, intent)
                Log.d("ForegroundService", "Service start intent sent")
            } catch (e: Exception) {
                Log.e("ForegroundService", "Exception starting service", e)
            }
        }

        fun stopService(context: Context) {
            Log.d("ForegroundService", "Stopping service via companion")
            val intent = Intent(context, ForegroundLocationService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e("ForegroundService", "Exception stopping service", e)
                try {
                    context.stopService(intent)
                } catch (fallbackException: Exception) {
                    Log.e("ForegroundService", "Fallback stop also failed", fallbackException)
                }
            }
        }
    }
}