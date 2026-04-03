package com.simats.nutritrace

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class NotificationsActivity : AppCompatActivity() {

    private lateinit var llContainer: LinearLayout
    private lateinit var svNotifications: android.widget.ScrollView
    private lateinit var llEmptyState: LinearLayout
    private lateinit var tvMarkAllRead: TextView

    private val notificationUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        // Request POST_NOTIFICATIONS permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        val toolbar = findViewById<ConstraintLayout>(R.id.clToolbar)
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            val density = view.resources.displayMetrics.density
            val horizontalPadding = (16 * density).toInt()
            val verticalPadding = (16 * density).toInt()
            view.setPadding(horizontalPadding, insets.top + verticalPadding, horizontalPadding, verticalPadding)
            windowInsets
        }

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        tvMarkAllRead = findViewById(R.id.tvMarkAllRead)
        llContainer = findViewById(R.id.llNotificationContainer)
        svNotifications = findViewById(R.id.svNotifications)
        llEmptyState = findViewById(R.id.llEmptyState)

        ivBack.setOnClickListener { finish() }

        tvMarkAllRead.setOnClickListener {
            NotificationRepository.clearAll(this)
            
            // Also cancel from Android system tray
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.cancelAll()
            
            loadNotifications()
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "NUTRITRACE_ALERTS",
                "Nutritrace Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
        
        // Register receiver for when Mark As Read is clicked on push notification
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationUpdateReceiver, IntentFilter("com.simats.nutritrace.NOTIFICATIONS_UPDATED"), Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(notificationUpdateReceiver, IntentFilter("com.simats.nutritrace.NOTIFICATIONS_UPDATED"))
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotifications()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationUpdateReceiver)
    }

    private fun loadNotifications() {
        llContainer.removeAllViews()
        val notifications = NotificationRepository.getNotifications(this)

        if (notifications.isEmpty()) {
            svNotifications.visibility = android.view.View.GONE
            tvMarkAllRead.visibility = android.view.View.GONE
            llEmptyState.visibility = android.view.View.VISIBLE
            return
        }

        svNotifications.visibility = android.view.View.VISIBLE
        tvMarkAllRead.visibility = android.view.View.VISIBLE
        llEmptyState.visibility = android.view.View.GONE

        for (notif in notifications) {
            val itemView = layoutInflater.inflate(R.layout.item_notification, llContainer, false)
            val root = itemView.findViewById<ConstraintLayout>(R.id.clNotificationRoot)
            val ivIcon = itemView.findViewById<ImageView>(R.id.ivNotificationIcon)
            val vIconBg = itemView.findViewById<android.view.View>(R.id.vIconBg)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvNotificationTitle)
            val tvMessage = itemView.findViewById<TextView>(R.id.tvNotificationMessage)
            val tvTime = itemView.findViewById<TextView>(R.id.tvNotificationTime)

            tvTitle.text = notif.title
            tvMessage.text = notif.message
            
            // Format time difference loosely (e.g. JUST NOW if under 1 minute)
            val diffMs = System.currentTimeMillis() - notif.timestamp
            val mins = diffMs / 60000
            if (mins < 1) {
                tvTime.text = notif.time
            } else if (mins < 60) {
                tvTime.text = "$mins MINS AGO"
            } else {
                val hours = mins / 60
                tvTime.text = "$hours HOURS AGO"
            }

            val bgShape = android.graphics.drawable.GradientDrawable()
            bgShape.shape = android.graphics.drawable.GradientDrawable.OVAL

            when (notif.type) {
                0 -> { // Scan Reminder
                    root.setBackgroundResource(R.drawable.bg_notification_green)
                    ivIcon.setImageResource(R.drawable.ic_expand_scan)
                    ivIcon.setColorFilter(android.graphics.Color.parseColor("#16B88A"))
                    bgShape.setColor(android.graphics.Color.parseColor("#1A16B88A"))
                }
                1 -> { // Health Alert
                    root.setBackgroundResource(R.drawable.bg_notification_red)
                    ivIcon.setImageResource(R.drawable.ic_alert_custom)
                    ivIcon.setColorFilter(android.graphics.Color.parseColor("#FF4B55"))
                    bgShape.setColor(android.graphics.Color.parseColor("#1AFF4B55"))
                }
                2 -> { // Smart Insight
                    root.setBackgroundResource(R.drawable.bg_notification_cyan)
                    ivIcon.setImageResource(R.drawable.ic_chart_arrow)
                    ivIcon.setColorFilter(android.graphics.Color.parseColor("#14B8A6"))
                    bgShape.setColor(android.graphics.Color.parseColor("#1A14B8A6"))
                }
            }
            vIconBg.background = bgShape
            
            // Allow individual dismissal
            root.setOnClickListener {
                NotificationRepository.removeNotification(this, notif.id)
                
                // Also cancel from Android system tray
                val manager = getSystemService(android.app.NotificationManager::class.java)
                manager?.cancel(notif.id.hashCode())
                
                loadNotifications()
            }
            
            llContainer.addView(itemView)
        }
    }
}
