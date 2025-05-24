package com.example.skynote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.skynote.data.model.WeatherResponse
import com.example.skynote.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = WeatherRepository(application)

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun fetchWeather(lat: Double, lon: Double, units: String, lang: String, apiKey: String)
    {
        viewModelScope.launch {
            _isLoading.value = true
            val response = repository.getWeatherForecast(lat, lon, units, lang, apiKey)
            _weatherData.value = response
            _isLoading.value = false
        }
    }
}
