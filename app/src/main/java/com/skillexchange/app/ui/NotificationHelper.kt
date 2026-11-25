package com.skillexchange.app.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "swappr_exchange_status_channel"

    fun notifyStatusChange(context: Context, requestTitle: String, newStatus: String, providerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Swappr Exchange Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications when your skill exchange status updates"
            notificationManager.createNotificationChannel(channel)
        }

        val message = "Your request for \"$requestTitle\" with $providerName is now $newStatus!"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Exchange Status Updated 🔄")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: Exception) {}
    }

    fun notifyNewRequest(context: Context, requestTitle: String, providerName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Swappr Exchange Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val message = "Your exchange request for \"$requestTitle\" was sent to $providerName!"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Exchange Request Sent 🚀")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (_: Exception) {}
    }
}
