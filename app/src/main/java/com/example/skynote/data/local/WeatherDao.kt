package com.example.skynote.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface WeatherDao
{
    @Query("SELECT * FROM weather_data ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestWeather(): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather_data")
    suspend fun clearWeather()
}