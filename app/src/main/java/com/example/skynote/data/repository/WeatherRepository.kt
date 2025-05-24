package com.example.skynote.data.repository

import android.content.Context
import com.example.skynote.data.model.WeatherResponse
import com.example.skynote.data.remote.RetrofitClient
import com.example.skynote.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(context: Context)
{
    private val prefs = PreferenceManager(context)

    suspend fun getWeatherForecast(
        lat: Double,
        lon: Double,
        units: String,
        lang: String,
        apiKey: String ): WeatherResponse?
    {
        val unit = prefs.getTempUnit()
        val lang = prefs.getLanguage()

        return withContext(Dispatchers.IO)
        {
            try
            {
                RetrofitClient.weatherService.getWeatherForecast(lat, lon, unit, lang, apiKey)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                null
            }
        }
    }
}
