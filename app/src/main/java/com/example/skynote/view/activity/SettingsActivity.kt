package com.example.skynote.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skynote.R
import com.example.skynote.databinding.ActivitySettingsBinding
import com.example.skynote.utils.PreferenceManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        // Load saved preferences
        binding.rgTemperature.check(getRadioButtonIdForTempUnit(preferenceManager.getTempUnit()))
        binding.rgWindSpeed.check(getRadioButtonIdForWindSpeedUnit(preferenceManager.getWindSpeedUnit()))
        binding.rgLanguage.check(getRadioButtonIdForLanguage(preferenceManager.getLanguage()))
        binding.rgLocation.check(getRadioButtonIdForLocationSource(preferenceManager.getLocationSource()))

        // Setting listeners
        binding.rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbCelsius -> preferenceManager.setTempUnit("metric")
                R.id.rbFahrenheit -> preferenceManager.setTempUnit("imperial")
                R.id.rbKelvin -> preferenceManager.setTempUnit("standard")
            }
        }

        binding.rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbMS -> preferenceManager.setWindSpeedUnit("m/s")
                R.id.rbMPH -> preferenceManager.setWindSpeedUnit("mph")
            }
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbEnglish -> preferenceManager.setLanguage("en")
                R.id.rbArabic -> preferenceManager.setLanguage("ar")
            }
            recreate() // Restart activity to apply language change
        }

        binding.rgLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbGPS -> preferenceManager.setLocationSource("gps")
                R.id.rbMap -> preferenceManager.setLocationSource("map")
            }
        }
    }

    private fun getRadioButtonIdForTempUnit(unit: String): Int = when (unit)
    {
        "metric" -> R.id.rbCelsius
        "imperial" -> R.id.rbFahrenheit
        "standard" -> R.id.rbKelvin
        else -> R.id.rbCelsius
    }

    private fun getRadioButtonIdForWindSpeedUnit(unit: String): Int = when (unit)
    {
        "mph" -> R.id.rbMPH
        else -> R.id.rbMS
    }

    private fun getRadioButtonIdForLanguage(lang: String): Int = when (lang)
    {
        "ar" -> R.id.rbArabic
        else -> R.id.rbEnglish
    }

    private fun getRadioButtonIdForLocationSource(source: String): Int = when (source)
    {
        "map" -> R.id.rbMap
        else -> R.id.rbGPS
    }
}