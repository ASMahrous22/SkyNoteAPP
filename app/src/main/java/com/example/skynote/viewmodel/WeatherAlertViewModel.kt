package com.example.skynote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.skynote.data.local.WeatherAlert
import com.example.skynote.data.repository.WeatherAlertRepository
import kotlinx.coroutines.launch

class WeatherAlertViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherAlertRepository(application)
    private val _alerts = MutableLiveData<List<WeatherAlert>>()
    val alerts: LiveData<List<WeatherAlert>> get() = _alerts

    fun loadAlerts() {
        viewModelScope.launch {
            val alerts = repository.getAllAlerts()
            _alerts.postValue(alerts)
        }
    }

    fun addAlert(alert: WeatherAlert) {
        viewModelScope.launch {
            repository.insertAlert(alert)
            loadAlerts()
        }
    }

    fun updateAlert(alert: WeatherAlert) {
        viewModelScope.launch {
            repository.updateAlert(alert)
            loadAlerts()
        }
    }

    fun deleteAlert(alert: WeatherAlert) {
        viewModelScope.launch {
            repository.deleteAlert(alert)
            loadAlerts()
        }
    }
}