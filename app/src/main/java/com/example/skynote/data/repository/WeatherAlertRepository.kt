package com.example.skynote.data.repository

import android.content.Context
import com.example.skynote.data.local.AppDatabase
import com.example.skynote.data.local.WeatherAlert

class WeatherAlertRepository(context: Context)
{
    private val weatherAlertDao = AppDatabase.getDatabase(context).weatherAlertDao()

   suspend fun getAllAlerts() = weatherAlertDao.getAllAlerts()

    suspend fun insertAlert(alert: WeatherAlert) {
        weatherAlertDao.insertAlert(alert)
    }

    suspend fun updateAlert(alert: WeatherAlert) {
        weatherAlertDao.updateAlert(alert)
    }

    suspend fun deleteAlert(alert: WeatherAlert) {
        weatherAlertDao.deleteAlert(alert)
    }
}