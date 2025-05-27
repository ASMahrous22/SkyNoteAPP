package com.example.skynote.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context)
{
    private val prefs: SharedPreferences = context.getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)

    // Temperature Unit
    fun setTempUnit(unit: String)
    {
        prefs.edit().putString("unit", unit).apply()
    }

    fun getTempUnit(): String
    {
        return prefs.getString("unit", "metric") ?: "metric"
    }

    // Wind Speed Unit
    fun setWindSpeedUnit(unit: String)
    {
        prefs.edit().putString("wind_unit", unit).apply()
    }

    fun getWindSpeedUnit(): String
    {
        return prefs.getString("wind_unit", "m/s") ?: "m/s"
    }

    // Language
    fun setLanguage(lang: String)
    {
        prefs.edit().putString("language", lang).apply()
    }

    fun getLanguage(): String
    {
        return prefs.getString("language", "en") ?: "en"
    }

    // Location Source
    fun setLocationSource(source: String)
    {
        prefs.edit().putString("location_source", source).apply()
    }

    fun getLocationSource(): String
    {
        return prefs.getString("location_source", "gps") ?: "gps"
    }

    fun setLastLatitude(lat: Double)
    {
        prefs.edit().putFloat("last_latitude", lat.toFloat()).apply()
    }

    fun getLastLatitude(): Double
    {
        return prefs.getFloat("last_latitude", 0.0f).toDouble()
    }

    fun setLastLongitude(lon: Double)
    {
        prefs.edit().putFloat("last_longitude", lon.toFloat()).apply()
    }

    fun getLastLongitude(): Double
    {
        return prefs.getFloat("last_longitude", 0.0f).toDouble()
    }
}
