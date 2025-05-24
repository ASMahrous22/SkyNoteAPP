package com.example.skynote.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.skynote.databinding.ActivityHomeBinding
import com.example.skynote.utils.LocationHelper
import com.example.skynote.utils.PreferenceManager
import com.example.skynote.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity()
{
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    private val apiKey = "fd6158b89b201f0c3c08f53f3bded73f"
    private val MAP_REQUEST_CODE = 1001

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() )
    { isGranted: Boolean ->
        if (isGranted)
        {
            fetchUserLocation()
        }
        else
        {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        val units = preferenceManager.getTempUnit()
        val lang = preferenceManager.getLanguage()

        observeWeatherData()
        requestLocationPermission()

        binding.btnOpenMap.setOnClickListener{
            val intent = Intent(this, MapSearchActivity::class.java)
            startActivityForResult(intent, MAP_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK) {
            val lat = data?.getDoubleExtra("lat", 30.0444) ?: 30.0444
            val lon = data?.getDoubleExtra("lon", 31.2357) ?: 31.2357

            viewModel.fetchWeather(lat, lon, preferenceManager.getTempUnit(), preferenceManager.getLanguage(), apiKey)
        }
    }

    private fun requestLocationPermission()
    {
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchUserLocation()
            }
            else -> {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchUserLocation()
    {
        val locationHelper = LocationHelper(this)
        locationHelper.getLastKnownLocation { location: Location? ->
            if (location != null)
            {
                viewModel.fetchWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    units = preferenceManager.getTempUnit(),
                    lang = preferenceManager.getLanguage(),
                    apiKey = apiKey
                )
            }
            else
            {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun observeWeatherData()
    {
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null)
            {
                val current = weather.list[0]

                binding.tvCity.text = weather.city.name
                binding.tvTemp.text = "${current.main.temp.toInt()}°"
                binding.tvDesc.text = current.weather[0].description.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }

                binding.tvHumidity.text = "Humidity: ${current.main.humidity}%"
                binding.tvWind.text = "Wind: ${current.wind.speed} m/s"
                binding.tvPressure.text = "Pressure: ${current.main.pressure} hPa"
                binding.tvClouds.text = "Clouds: ${current.clouds.all}%"

                val date = convertToDate(current.dt)
                val time = convertToTime(current.dt)
                binding.tvDate.text = "Date: $date"
                binding.tvTime.text = "Time: $time"

                val iconCode = current.weather[0].icon
                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"
                Glide.with(this).load(iconUrl).into(binding.ivWeatherIcon)

            }
            else
            {
                Toast.makeText(this, "Failed to load weather", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isLoading.observe(this)
        {
            isLoading -> binding.progressBar.visibility =
                if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun convertToDate(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun convertToTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}
