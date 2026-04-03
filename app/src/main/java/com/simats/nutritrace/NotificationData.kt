package com.simats.nutritrace

data class NotificationData(
    val id: String,
    val type: Int,
    val title: String,
    val message: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)
