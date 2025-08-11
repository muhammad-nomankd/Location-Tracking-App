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

    /* fun showTrackingNotification(context: Context) {
         createNotificationChannel(context)

         // Create intent to open app when notification is tapped
         val intent = Intent(context, MainActivity::class.java).apply {
             flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
         }

         val pendingIntentFlags =
             PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

         val pendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

         // Build the notification
         val notification = NotificationCompat.Builder(context, CHANNEL_ID)
             .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Use system location icon
             .setContentTitle("Location Tracking Active")
             .setContentText("Your location is being tracked and saved")
             .setStyle(NotificationCompat.BigTextStyle()
                 .bigText("Location tracking is currently active. Your GPS coordinates are being recorded every 5 seconds. Tap to view current location."))
             .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Important for visibility
             .setContentIntent(pendingIntent)
             .setAutoCancel(false) // Don't dismiss when tapped
             .setOngoing(true) // Makes it persistent
             .setShowWhen(true)
             .setWhen(System.currentTimeMillis())
             .setColor(0xFF0000FF.toInt()) // Blue color
             .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
             .build()

         try {
             val notificationManager = NotificationManagerCompat.from(context)

             if (!notificationManager.areNotificationsEnabled()) {
                 Log.e("NotificationHelper", "Notifications are disabled for this app!")
                 return
             }

             notificationManager.notify(NOTIFICATION_ID, notification)
             Log.d("NotificationHelper", "Notification posted with ID: $NOTIFICATION_ID")

         } catch (e: SecurityException) {
             Log.e("NotificationHelper", "SecurityException: Missing notification permission", e)
         } catch (e: Exception) {
             Log.e("NotificationHelper", "Exception showing notification", e)
         }
     }*/

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

        val stopTrackingIntent = Intent(context, MainActivity::class.java).apply {
            action = "STOP_TRACKING"
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, pendingIntentFlags
        )

        val stopTrackingPendingIntent = PendingIntent.getActivity(
            context, 1, stopTrackingIntent, pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Tap to view current location")
            .setSmallIcon(R.drawable.ic_menu_mylocation)
            .setContentIntent(openAppPendingIntent).setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setAutoCancel(false)
            // Add stop button to notification
            .addAction(
                R.drawable.ic_media_pause, "Stop Tracking", stopTrackingPendingIntent
            ).build()

        Log.d("NotificationHelper", "Notification built")
        return notification
    }

}