package com.example.skynote.view.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.example.skynote.data.model.WeatherResponse
import com.example.skynote.databinding.ActivityHomeBinding
import com.example.skynote.utils.LocationHelper
import com.example.skynote.utils.LocaleHelper
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
            Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show()
            loadLastKnownLocation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        preferenceManager = PreferenceManager(this)
        LocaleHelper.updateLocale(this, preferenceManager.getLanguage())
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        // Check for location from intent (e.g., from MapSearchActivity or SettingsActivity)
        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        if (lat != 0.0 && lon != 0.0) {
            lastLat = lat
            lastLon = lon
            saveLastKnownLocation(lat, lon)
            fetchWeatherData(lat, lon)
        } else {
            // Respect the location source preference
            if (preferenceManager.getLocationSource() == "gps") {
                requestLocationPermission()
            } else {
                loadLastKnownLocation()
            }
        }

        // Setup Swipe Refresh Layout
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (lastLat != 0.0 && lastLon != 0.0) {
                fetchWeatherData(lastLat, lastLon)
            } else {
                loadCachedWeatherData()
                Toast.makeText(this, R.string.no_location_available, Toast.LENGTH_SHORT).show()
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
            fetchWeatherData(lat, lon)
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
                fetchWeatherData(lastLat, lastLon)
            }
            else
            {
                Toast.makeText(this, R.string.failed_to_get_location, Toast.LENGTH_SHORT).show()
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
            fetchWeatherData(lastLat, lastLon)
        } else {
            loadCachedWeatherData()
            Toast.makeText(this, R.string.no_previous_location, Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchWeatherData(lat: Double, lon: Double) {
        if (isOnline()) {
            viewModel.fetchWeather(lat, lon, preferenceManager.getTempUnit(), preferenceManager.getLanguage(), apiKey)
        } else {
            loadCachedWeatherData()
            Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show()
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun observeWeatherData()
    {
        viewModel.weatherData.observe(this) { weather ->
            if (weather != null && weather.list.isNotEmpty())
            {
                // Cache the weather data
                preferenceManager.saveWeatherData(weather)

                // Current weather (first item)
                val current = weather.list[0]
                with(binding) {
                    tvCity.text = weather.city.name
                    tvTemp.text = "${current.main.temp.toInt()}°"
                    tvDesc.text = current.weather[0].description.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale(preferenceManager.getLanguage())) else it.toString()
                    }
                    tvDate.text = getString(R.string.tvDate, convertToDate(current.dt))
                    tvTime.text = getString(R.string.tvTime, convertToTime(current.dt))
                    tvHumidity.text = getString(R.string.tvHumidity, current.main.humidity.toString())
                    tvWind.text = getString(R.string.tvWind, current.wind.speed.toString())
                    tvPressure.text = getString(R.string.tvPressure, current.main.pressure.toString())
                    tvClouds.text = getString(R.string.tvClouds, current.clouds.all.toString())
                    tvLastUpdated.text = getString(R.string.tvLastUpdated, SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale(preferenceManager.getLanguage())).format(Date(current.dt * 1000)))
                    Glide.with(this@HomeActivity).load(getWeatherIcon(current.weather[0].icon)).into(ivWeatherIcon)
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
                loadCachedWeatherData()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility =
                if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun loadCachedWeatherData() {
        val cachedWeather = preferenceManager.getWeatherData()
        if (cachedWeather != null && cachedWeather.list.isNotEmpty()) {
            with(binding) {
                val current = cachedWeather.list[0]
                tvCity.text = cachedWeather.city.name
                tvTemp.text = "${current.main.temp.toInt()}°"
                tvDesc.text = current.weather[0].description.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale(preferenceManager.getLanguage())) else it.toString()
                }
                tvDate.text = getString(R.string.tvDate, convertToDate(current.dt))
                tvTime.text = getString(R.string.tvTime, convertToTime(current.dt))
                tvHumidity.text = getString(R.string.tvHumidity, current.main.humidity.toString())
                tvWind.text = getString(R.string.tvWind, current.wind.speed.toString())
                tvPressure.text = getString(R.string.tvPressure, current.main.pressure.toString())
                tvClouds.text = getString(R.string.tvClouds, current.clouds.all.toString())
                tvLastUpdated.text = getString(R.string.tvLastUpdated, SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale(preferenceManager.getLanguage())).format(Date(current.dt * 1000)))
                Glide.with(this@HomeActivity).load(getWeatherIcon(current.weather[0].icon)).into(ivWeatherIcon)

                // Today's forecast
                val todayForecast = cachedWeather.list.take(8)
                todayForecastAdapter.updateData(todayForecast)

                // 5-day forecast
                val fiveDayForecast = getFiveDayForecast(cachedWeather.list)
                fiveDayForecastAdapter.updateData(fiveDayForecast)
            }
        } else {
            Toast.makeText(this, R.string.no_cached_data, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getWeatherIcon(iconCode: String): Int {
        return when (iconCode) {
            "01d" -> R.drawable.weather_01d
            "01n" -> R.drawable.weather_01n
            "02d" -> R.drawable.weather_02d
            "02n" -> R.drawable.weather_02n
            "03d", "03n" -> R.drawable.weather_03
            "04d", "04n" -> R.drawable.weather_04
            "09d", "09n" -> R.drawable.weather_09
            "10d" -> R.drawable.weather_10d
            "10n" -> R.drawable.weather_10n
            "11d", "11n" -> R.drawable.weather_11
            "13d", "13n" -> R.drawable.weather_13
            "50d", "50n" -> R.drawable.weather_50
            else -> R.drawable.weather_default
        }
    }

    private fun getFiveDayForecast(items: List<WeatherItem>): List<WeatherItem>
    {
        return items.filter { it.dt_txt.contains("12:00:00") }.take(5)
    }

    private fun convertToDate(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale(preferenceManager.getLanguage()))
        return sdf.format(Date(timestamp * 1000))
    }

    private fun convertToTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("hh:mm a", Locale(preferenceManager.getLanguage()))
        return sdf.format(Date(timestamp * 1000))
    }
}