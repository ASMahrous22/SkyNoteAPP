package com.example.skynote.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WeatherAlertDao {
    @Query("SELECT * FROM weather_alerts")
    suspend fun getAllAlerts(): List<WeatherAlert>

    @Insert
    suspend fun insertAlert(alert: WeatherAlert)

    @Update
    suspend fun updateAlert(alert: WeatherAlert)

    @Delete
    suspend fun deleteAlert(alert: WeatherAlert)
}