package com.example.skynote.view.activity

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import com.example.skynote.R
import com.example.skynote.data.local.WeatherAlert
import com.example.skynote.databinding.ActivityWeatherAlertsBinding
import com.example.skynote.view.adapter.WeatherAlertAdapter
import com.example.skynote.viewmodel.WeatherAlertViewModel
import com.example.skynote.worker.WeatherAlertWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WeatherAlertsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWeatherAlertsBinding
    private val viewModel: WeatherAlertViewModel by viewModels()
    private lateinit var adapter: WeatherAlertAdapter

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Alerts may not work.", Toast.LENGTH_LONG).show()
        }
    }

    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
            Toast.makeText(this, "Exact alarm permission denied. Alerts may be delayed.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            exactAlarmPermissionLauncher.launch(intent)
        }

        adapter = WeatherAlertAdapter(emptyList(), onStop = { alert ->
            val updatedAlert = alert.copy(isActive = false)
            viewModel.updateAlert(updatedAlert)
            WorkManager.getInstance(this).cancelUniqueWork("WeatherAlert_${alert.id}")
            Log.d("WeatherAlertsActivity", "Stopped alert ID: ${alert.id}")
        }, onDelete = { alert ->
            viewModel.deleteAlert(alert)
            WorkManager.getInstance(this).cancelUniqueWork("WeatherAlert_${alert.id}")
            Log.d("WeatherAlertsActivity", "Deleted alert ID: ${alert.id}")
        })
        binding.weatherAlertsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.weatherAlertsRecyclerView.adapter = adapter

        viewModel.alerts.observe(this) { alerts ->
            if (alerts.isEmpty()) {
                binding.weatherAlertsRecyclerView.visibility = android.view.View.GONE
                binding.emptyState.visibility = android.view.View.VISIBLE
            } else {
                binding.weatherAlertsRecyclerView.visibility = android.view.View.VISIBLE
                binding.emptyState.visibility = android.view.View.GONE
                adapter.updateData(alerts)
            }
        }

        viewModel.loadAlerts()

        binding.fabAddAlert.setOnClickListener {
            showAddAlertDialog()
        }
    }

    private fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun showAddAlertDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_weather_alert, null)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val timePicker = dialogView.findViewById<TimePicker>(R.id.timePicker)
        val spinnerAlarmType = dialogView.findViewById<Spinner>(R.id.spinnerAlarmType)
        val spinnerDuration = dialogView.findViewById<Spinner>(R.id.spinnerDuration)

        // Set current date and time as default (05:48 PM EEST, May 27, 2025)
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MAY, 27, 17, 48) // Set to 05:48 PM EEST
        datePicker.init(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
            null
        )
        timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
        timePicker.minute = calendar.get(Calendar.MINUTE)

        // Setup alarm type spinner
        val alarmTypes = listOf("notification", "alarm_sound")
        spinnerAlarmType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, alarmTypes)

        // Setup duration spinner ( 1, 3, 5 minutes)
        val durations = listOf(1, 3, 5)
        spinnerDuration.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, durations)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Weather Alert")
            .setView(dialogView)
            .setPositiveButton("Set Alert") { _, _ ->
                val calendar = Calendar.getInstance()
                calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                calendar.set(Calendar.MINUTE, timePicker.minute)
                calendar.set(Calendar.SECOND, 0)

                val alertTime = calendar.timeInMillis
                val currentTime = System.currentTimeMillis()
                if (alertTime > currentTime) {
                    val alarmType = spinnerAlarmType.selectedItem.toString()
                    val duration = spinnerDuration.selectedItem.toString().toInt()
                    val alert = WeatherAlert(
                        dateTime = alertTime,
                        durationMinutes = duration,
                        alarmType = alarmType
                    )
                    viewModel.addAlert(alert)
                    scheduleAlert(alert)
                } else {
                    Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun scheduleAlert(alert: WeatherAlert) {
        val delay = alert.dateTime - System.currentTimeMillis()
        Log.d("WeatherAlertsActivity", "Scheduling alert with ID: ${alert.id}, delay: $delay ms")
        if (delay > 0) {
            val data = Data.Builder()
                .putString(WeatherAlertWorker.KEY_ALARM_TYPE, alert.alarmType)
                .putInt(WeatherAlertWorker.KEY_DURATION, alert.durationMinutes)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<WeatherAlertWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            val workManager = WorkManager.getInstance(this)
            workManager.enqueueUniqueWork("WeatherAlert_${alert.id}", ExistingWorkPolicy.REPLACE, workRequest)
            Log.d("WeatherAlertsActivity", "Work scheduled for alert ID: ${alert.id}")

            // Monitor WorkManager state
            workManager.getWorkInfosForUniqueWorkLiveData("WeatherAlert_${alert.id}")
                .observe(this) { workInfos ->
                    workInfos.forEach { workInfo ->
                        Log.d("WeatherAlertsActivity", "Work state for alert ID ${alert.id}: ${workInfo.state}")
                        if (workInfo.state.isFinished) {
                            Log.d("WeatherAlertsActivity", "Work finished with output: ${workInfo.outputData}")
                        }
                    }
                }
        } else {
            Log.w("WeatherAlertsActivity", "Delay is not positive, alert not scheduled: $delay")
        }
    }
}