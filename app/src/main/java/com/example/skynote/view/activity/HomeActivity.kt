package com.example.skynote.view.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.skynote.R
import com.example.skynote.data.model.WeatherItem
import com.example.skynote.databinding.ActivityHomeBinding
import com.example.skynote.utils.LocationHelper
import com.example.skynote.utils.PreferenceManager
import com.example.skynote.view.adapter.FiveDayForecastAdapter
import com.example.skynote.view.adapter.TodayForecastAdapter
import com.example.skynote.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity()
{
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var todayForecastAdapter: TodayForecastAdapter
    private lateinit var fiveDayForecastAdapter: FiveDayForecastAdapter
    private val apiKey = "fd6158b89b201f0c3c08f53f3bded73f"
    private val MAP_REQUEST_CODE = 1001
    private var lastLat: Double = 0.0
    private var lastLon: Double = 0.0

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchUserLocation()
        } else {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            // Fallback to last known location if available
            loadLastKnownLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)

        // Initialize adapters
        todayForecastAdapter = TodayForecastAdapter(emptyList())
        fiveDayForecastAdapter = FiveDayForecastAdapter(emptyList())

        // Setup RecyclerViews
        binding.rvTodayForecast.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = todayForecastAdapter
        }
        binding.rvFiveDayForecast.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = fiveDayForecastAdapter
        }

        // Setup Bottom Navigation
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    val intent = Intent(this, MapSearchActivity::class.java)
                    startActivityForResult(intent, MAP_REQUEST_CODE)
                    true
                }
                R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                R.id.nav_alerts -> {
                    startActivity(Intent(this, WeatherAlertsActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Check for favorite location from intent
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        if (lat != 0.0 && lon != 0.0) {
            lastLat = lat
            lastLon = lon
            saveLastKnownLocation(lat, lon)
            viewModel.fetchWeather(lat, lon, preferenceManager.getTempUnit(), preferenceManager.getLanguage(), apiKey)
        } else {
            requestLocationPermission()
        }

        // Setup Swipe Refresh Layout
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (lastLat != 0.0 && lastLon != 0.0) {
                viewModel.fetchWeather(lastLat, lastLon, preferenceManager.getTempUnit(), preferenceManager.getLanguage(), apiKey)
                Toast.makeText(this, "Refreshing weather data...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No location available to refresh", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        binding.swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.skyblue))

        // Observe weather data and update UI
        observeWeatherData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK)
        {
            val lat = data?.getDoubleExtra("lat", 30.0444) ?: 30.0444
            val lon = data?.getDoubleExtra("lon", 31.2357) ?: 31.2357
            lastLat = lat
            lastLon = lon
            saveLastKnownLocation(lat, lon)
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
        locationHelper.getLastKnownLocation { location ->
            if (location != null)
            {
                lastLat = location.latitude
                lastLon = location.longitude
                saveLastKnownLocation(lastLat, lastLon)
                viewModel.fetchWeather(
                    lat = lastLat,
                    lon = lastLon,
                    units = preferenceManager.getTempUnit(),
                    lang = preferenceManager.getLanguage(),
                    apiKey = apiKey
                )
            }
            else
            {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                loadLastKnownLocation()
            }
        }
    }

    private fun saveLastKnownLocation(lat: Double, lon: Double) {
        preferenceManager.setLastLatitude(lat)
        preferenceManager.setLastLongitude(lon)
    }

    private fun loadLastKnownLocation() {
        lastLat = preferenceManager.getLastLatitude()
        lastLon = preferenceManager.getLastLongitude()
        if (lastLat != 0.0 && lastLon != 0.0) {
            viewModel.fetchWeather(lastLat, lastLon, preferenceManager.getTempUnit(), preferenceManager.getLanguage(), apiKey)
        } else {
            Toast.makeText(this, "No previous location found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeWeatherData()
    {
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null && weather.list.isNotEmpty())
            {
                // Current weather (first item)
                val current = weather.list[0]
                with(binding) {
                    tvCity.text = weather.city.name
                    tvTemp.text = "${current.main.temp.toInt()}°"
                    tvDesc.text = current.weather[0].description.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                    tvDate.text = "Date: ${convertToDate(current.dt)}"
                    tvTime.text = "Time: ${convertToTime(current.dt)}"
                    tvHumidity.text = "Humidity: ${current.main.humidity}%"
                    tvWind.text = "Wind: ${current.wind.speed} ${preferenceManager.getWindSpeedUnit()}"
                    tvPressure.text = "Pressure: ${current.main.pressure} hPa"
                    tvClouds.text = "Clouds: ${current.clouds.all}%"
                    val iconUrl = "https://openweathermap.org/img/wn/${current.weather[0].icon}@2x.png"
                    Glide.with(this@HomeActivity).load(iconUrl).into(ivWeatherIcon)
                }

                // Today's forecast (next 8 items for ~24 hours, 3-hour intervals)
                val todayForecast = weather.list.take(8)
                todayForecastAdapter.updateData(todayForecast)

                // 5-day forecast (one item per day)
                val fiveDayForecast = getFiveDayForecast(weather.list)
                fiveDayForecastAdapter.updateData(fiveDayForecast)
                binding.swipeRefreshLayout.isRefreshing = false
            }
            else
            {
                Toast.makeText(this, "Failed to load weather", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun getFiveDayForecast(items: List<WeatherItem>): List<WeatherItem>
    {
        return items.filter { it.dt_txt.contains("12:00:00") }.take(5)
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