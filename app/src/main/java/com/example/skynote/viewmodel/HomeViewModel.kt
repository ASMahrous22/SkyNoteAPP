package com.example.skynote.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.skynote.data.local.AppDatabase
import com.example.skynote.data.local.WeatherEntity
import com.example.skynote.data.model.*
import com.example.skynote.data.remote.RetrofitClient
import com.example.skynote.utils.NetworkHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppDatabase.getDatabase(application).weatherDao()
    private val networkHelper = NetworkHelper(application)
    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> get() = _weatherData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun fetchWeather(lat: Double, lon: Double, units: String, lang: String, apiKey: String) {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                if (networkHelper.isNetworkAvailable()) {
                    val weatherResponse = fetchWeatherFromApi(lat, lon, units, lang, apiKey)
                    if (weatherResponse != null) {
                        _weatherData.postValue(weatherResponse)
                        saveWeatherToRoom(weatherResponse)
                        Log.d("HomeViewModel", "Weather fetched: ${weatherResponse.city.name}")
                    } else {
                        Log.w("HomeViewModel", "Weather API returned null")
                    }
                } else {
                    val lastWeather = repository.getLatestWeather()
                    if (lastWeather != null) {
                        _weatherData.postValue(convertToWeatherResponse(lastWeather))
                        Log.d("HomeViewModel", "Using cached weather for ${lastWeather.cityName}")
                    } else {
                        _weatherData.postValue(null)
                        Log.w("HomeViewModel", "No cached weather available")
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching weather: ${e.message}")
                _weatherData.postValue(null)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun fetchWeatherFromApi(lat: Double, lon: Double, units: String, lang: String, apiKey: String): WeatherResponse? {
        return try {
            RetrofitClient.weatherService.getWeatherForecast(lat, lon, units, lang, apiKey)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "API call failed: ${e.message}")
            null
        }
    }

    private suspend fun saveWeatherToRoom(weather: WeatherResponse) {
        val currentWeather = weather.list.firstOrNull() ?: return
        val weatherEntity = WeatherEntity(
            cityName = weather.city.name,
            temperature = currentWeather.main.temp,
            description = currentWeather.weather.firstOrNull()?.description ?: "",
            humidity = currentWeather.main.humidity,
            windSpeed = currentWeather.wind.speed,
            pressure = currentWeather.main.pressure,
            clouds = currentWeather.clouds.all,
            icon = currentWeather.weather.firstOrNull()?.icon ?: "",
            lastUpdated = System.currentTimeMillis()
        )
        repository.insertWeather(weatherEntity)
    }

    private fun convertToWeatherResponse(entity: WeatherEntity): WeatherResponse {
        val weatherItem = WeatherItem(
            dt = entity.lastUpdated / 1000,
            dt_txt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(entity.lastUpdated)),
            main = Main(entity.temperature, entity.pressure, entity.humidity),
            weather = listOf(Weather("Clear", entity.description, entity.icon)),
            wind = Wind(entity.windSpeed),
            clouds = Clouds(entity.clouds)
        )
        return WeatherResponse(listOf(weatherItem), City(entity.cityName, ""))
    }
}