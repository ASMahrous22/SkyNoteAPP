package com.example.skynote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.skynote.data.model.WeatherResponse
import com.example.skynote.data.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = WeatherRepository(application)

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    fun fetchWeather(lat: Double, lon: Double, units: String, lang: String, apiKey: String)
    {
        viewModelScope.launch {
            val result = repository.getWeatherForecast(lat, lon, units, lang, apiKey)
            _weatherData.postValue(result)
        }
    }
}
