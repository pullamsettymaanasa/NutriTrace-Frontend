package com.simats.nutritrace

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.UUID

class NotificationReceiver : BroadcastReceiver() {

    private val dynamicTips = listOf(
        NotificationData("", 1, "Health Alert", "Thyroid Alert: Avoid highly processed foods.", "JUST NOW"),
        NotificationData("", 0, "Scan Reminder", "Scan your food before eating.", "JUST NOW"),
        NotificationData("", 2, "Smart Insight", "Fewer ingredients usually mean safer food.", "JUST NOW"),
        NotificationData("", 0, "Hydration Reminder", "Don't forget to drink water today!", "JUST NOW"),
        NotificationData("", 2, "Smart Insight", "Eating organic fruits can reduce pesticide intake.", "JUST NOW")
    )

    override fun onReceive(context: Context, intent: Intent) {
        val randomTip = dynamicTips.random().copy(id = UUID.randomUUID().toString())
        NotificationRepository.addNotification(context, randomTip)

        val icon = when (randomTip.type) {
            0 -> R.drawable.ic_expand_scan
            1 -> R.drawable.ic_alert_custom
            else -> R.drawable.ic_chart_arrow
        }

        // Intent when the notification itself is tapped (open app)
        val contentIntent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            randomTip.id.hashCode(),
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent for "Mark as Read" action
        val markReadIntent = Intent(context, MarkAsReadReceiver::class.java).apply {
            putExtra("NOTIFICATION_ID", randomTip.id)
        }
        val pendingMarkReadIntent = PendingIntent.getBroadcast(
            context,
            randomTip.id.hashCode(),
            markReadIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, "NUTRITRACE_ALERTS")
            .setSmallIcon(icon)
            .setContentTitle(randomTip.title)
            .setContentText(randomTip.message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingContentIntent)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_success_check, "Mark as Read", pendingMarkReadIntent)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(context).notify(randomTip.id.hashCode(), builder.build())
    }
}
