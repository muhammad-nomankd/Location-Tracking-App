package com.example.locationtracking.util

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.locationtracking.MainActivity
import com.example.locationtracking.service.ForegroundLocationService
import javax.inject.Inject

class NotificationHelper @Inject constructor() {

    private val CHANNEL_ID = "location_tracking_channel"
    private val CHANNEL_NAME = "Location Tracking"
    private val CHANNEL_DESCRIPTION = "Notifications for location tracking status"
    private val NOTIFICATION_ID = 12345

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun hideTrackingNotification(context: Context) {
        Log.d("NotificationHelper", "Hiding tracking notification")
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ID)
            Log.d("NotificationHelper", "Notification cancelled successfully")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error cancelling notification", e)
        }
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun checkNotificationPermission(context: Context) {
        val enabled = areNotificationsEnabled(context)
        Log.d("NotificationHelper", "Notifications enabled: $enabled")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            Log.d("NotificationHelper", "Channel importance: ${channel?.importance}")
        }
    }

    fun buildTrackingNotification(context: Context): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val stopTrackingIntent = Intent(context, ForegroundLocationService::class.java).apply {
            action = ForegroundLocationService.ACTION_STOP
        }

        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, pendingIntentFlags
        )

        val stopTrackingPendingIntent = PendingIntent.getService(
            context, 1, stopTrackingIntent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Tap to view current location")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppPendingIntent).setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Location tracking is currently active. Your GPS coordinates are being recorded every 5 seconds. Tap to view current location."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setAutoCancel(false)
            .addAction(
                R.drawable.ic_media_pause, "Stop Tracking", stopTrackingPendingIntent
            ).build()

        Log.d("NotificationHelper", "Notification built")
        return notification
    }

}