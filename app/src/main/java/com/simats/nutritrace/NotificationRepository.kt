package com.simats.nutritrace

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationRepository {
    private const val PREFS_NAME = "nutritrace_notifications"
    private const val KEY_NOTIFICATIONS = "notifications_list"

    fun addNotification(context: Context, notification: NotificationData) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = getNotifications(context).toMutableList()
        notifications.add(0, notification) // Add to top
        val json = Gson().toJson(notifications)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun getNotifications(context: Context): List<NotificationData> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_NOTIFICATIONS, null)
        if (json.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<NotificationData>>() {}.type
        return try {
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun removeNotification(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifications = getNotifications(context).toMutableList()
        val iterator = notifications.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().id == id) {
                iterator.remove()
            }
        }
        val json = Gson().toJson(notifications)
        prefs.edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun clearAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_NOTIFICATIONS).apply()
    }
}
