package com.example.locationtracking.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
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

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val NOTIF_ID = 1
    private var tracking = false
    private val updateIntervalMs = 5000L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("ForegroundService", "Service created")
        notificationHelper.createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ForegroundService", "onStartCommand called with action: ${intent?.action}")

        when (intent?.action) {
            ACTION_START -> startForegroundServiceWork()
            ACTION_STOP -> {
                Log.d("ForegroundService", "Received STOP action")
                stopForegroundServiceWork()
            }
            else -> {
                Log.d("ForegroundService", "Unknown action or null intent")
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundServiceWork() {
        if (tracking) {
            Log.d("ForegroundService", "Already tracking, ignoring start request")
            return
        }

        Log.d("ForegroundService", "Starting foreground service work")
        tracking = true

        try {
            val notification = notificationHelper.buildTrackingNotification(this)
            startForeground(NOTIF_ID, notification)
            Log.d("ForegroundService", "Foreground service started with notification")

            serviceScope.launch {
                try {
                    Log.d("ForegroundService", "Starting location updates")
                    repository.startLocationUpdates(updateIntervalMs)
                } catch (e: SecurityException) {
                    Log.e("ForegroundService", "Security exception in location updates", e)
                    stopSelf()
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Exception in location updates", e)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            Log.e("ForegroundService", "Exception starting foreground service", e)
            tracking = false
            stopSelf()
        }
    }

    private fun stopForegroundServiceWork() {
        Log.d("ForegroundService", "Stopping foreground service work, tracking: $tracking")

        if (!tracking) {
            Log.d("ForegroundService", "Not tracking, but still cleaning up")
        }

        tracking = false

        serviceScope.launch {
            try {
                Log.d("ForegroundService", "Stopping location updates in repository")
                repository.stopLocationUpdates()
                Log.d("ForegroundService", "Location updates stopped successfully")
            } catch (e: Exception) {
                Log.e("ForegroundService", "Exception stopping location updates", e)
            } finally {
                Log.d("ForegroundService", "Stopping foreground and self")
                try {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Error stopping foreground", e)
                }
                stopSelf()
                Log.d("ForegroundService", "Service cleanup completed")
            }
        }
    }

    override fun onDestroy() {
        Log.d("ForegroundService", "Service being destroyed")
        tracking = false

        // Ensure location updates are stopped even during destruction
        serviceScope.launch {
            try {
                repository.stopLocationUpdates()
            } catch (e: Exception) {
                Log.e("ForegroundService", "Error stopping updates during destroy", e)
            }
        }

        serviceScope.cancel()
        super.onDestroy()
        Log.d("ForegroundService", "Service destroyed")
    }

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
                // Use startService with STOP action to ensure onStartCommand is called
                context.startService(intent)
                Log.d("ForegroundService", "Service stop intent sent")
            } catch (e: Exception) {
                Log.e("ForegroundService", "Exception stopping service", e)
                // Fallback to stopService
                try {
                    context.stopService(intent)
                } catch (fallbackException: Exception) {
                    Log.e("ForegroundService", "Fallback stop also failed", fallbackException)
                }
            }
        }
    }
}