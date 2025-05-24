package com.example.skynote.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.skynote.R
import com.example.skynote.databinding.ActivitySettingsBinding
import com.example.skynote.utils.PreferenceManager

class SettingsActivity : AppCompatActivity()
{
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferenceManager(this)

        setupInitialSelections()
        setupListeners()
    }

    private fun setupInitialSelections()
    {
        // Temperature
        when (prefs.getTempUnit())
        {
            "metric" -> binding.rbCelsius.isChecked = true
            "imperial" -> binding.rbFahrenheit.isChecked = true
            "standard" -> binding.rbKelvin.isChecked = true
        }

        // Wind Speed
        when (prefs.getWindSpeedUnit())
        {
            "m/s" -> binding.rbMS.isChecked = true
            "mph" -> binding.rbMPH.isChecked = true
        }

        // Language
        when (prefs.getLanguage())
        {
            "en" -> binding.rbEnglish.isChecked = true
            "ar" -> binding.rbArabic.isChecked = true
        }

        // Location source
        when (prefs.getLocationSource())
        {
            "gps" -> binding.rbGPS.isChecked = true
            "map" -> binding.rbMap.isChecked = true
        }
    }

    private fun setupListeners()
    {
        binding.rgTemperature.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbCelsius -> prefs.setTempUnit("metric")
                R.id.rbFahrenheit -> prefs.setTempUnit("imperial")
                R.id.rbKelvin -> prefs.setTempUnit("standard")
            }
        }

        binding.rgWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbMS -> prefs.setWindSpeedUnit("m/s")
                R.id.rbMPH -> prefs.setWindSpeedUnit("mph")
            }
        }

        binding.rgLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbEnglish -> prefs.setLanguage("en")
                R.id.rbArabic -> prefs.setLanguage("ar")
            }
            // Restarting activity to update UI language
            recreate()
        }

        binding.rgLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId)
            {
                R.id.rbGPS -> prefs.setLocationSource("gps")
                R.id.rbMap -> prefs.setLocationSource("map")
            }
        }
    }
}
