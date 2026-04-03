package com.simats.nutritrace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class MarkAsReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notifId = intent.getStringExtra("NOTIFICATION_ID")
        if (notifId != null) {
            NotificationRepository.removeNotification(context, notifId)
            NotificationManagerCompat.from(context).cancel(notifId.hashCode())
            
            // Broadcast an event so NotificationsActivity can update its UI if it's currently open
            val updateIntent = Intent("com.simats.nutritrace.NOTIFICATIONS_UPDATED")
            context.sendBroadcast(updateIntent)
        }
    }
}
