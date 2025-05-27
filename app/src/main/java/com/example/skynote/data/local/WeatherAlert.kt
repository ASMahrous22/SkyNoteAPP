package com.example.skynote.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_alerts")
data class WeatherAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateTime: Long, // Timestamp for the alert (in milliseconds)
    val durationMinutes: Int, // Duration the alert is active
    val alarmType: String, // "notification" or "alarm_sound"
    val isActive: Boolean = true // Whether the alert is active
)