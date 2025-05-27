package com.example.skynote.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.skynote.R
import com.example.skynote.view.activity.WeatherAlertsActivity

class WeatherAlertWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    companion object {
        const val CHANNEL_ID = "weather_alert_channel"
        const val NOTIFICATION_ID = 1001
        const val SOUND_NOTIFICATION_ID = 1002
        const val KEY_ALARM_TYPE = "alarm_type"
        const val KEY_DURATION = "duration_minutes"
        const val ACTION_STOP_SOUND = "com.example.skynote.STOP_SOUND"
    }

    private var ringtone: Ringtone? = null
    private val stopSoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("WeatherAlertWorker", "Received stop sound broadcast")
            synchronized(this@WeatherAlertWorker) {
                ringtone?.let {
                    if (it.isPlaying) {
                        it.stop()
                        Log.d("WeatherAlertWorker", "Sound stopped via broadcast")
                    } else {
                        Log.d("WeatherAlertWorker", "Sound was not playing when broadcast received")
                    }
                } ?: run {
                    Log.d("WeatherAlertWorker", "Ringtone reference is null")
                }
                // Cancel notifications
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID)
                notificationManager.cancel(SOUND_NOTIFICATION_ID)
                Log.d("WeatherAlertWorker", "Notifications cancelled via broadcast")
            }
        }
    }

    private lateinit var localBroadcastManager: LocalBroadcastManager

    override fun doWork(): Result {
        Log.d("WeatherAlertWorker", "Worker started executing")
        localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
        // Register the stop sound receiver
        val filter = IntentFilter(ACTION_STOP_SOUND)
        try {
            localBroadcastManager.registerReceiver(stopSoundReceiver, filter)
            Log.d("WeatherAlertWorker", "Stop sound receiver registered successfully with LocalBroadcastManager")
        } catch (e: Exception) {
            Log.e("WeatherAlertWorker", "Failed to register stop sound receiver: ${e.message}", e)
            // Continue execution even if receiver registration fails
        }

        val alarmType = inputData.getString(KEY_ALARM_TYPE) ?: "notification"
        val durationMinutes = inputData.getInt(KEY_DURATION, 5)
        Log.d("WeatherAlertWorker", "Alarm type: $alarmType, Duration: $durationMinutes minutes")

        when (alarmType) {
            "notification" -> showNotification()
            "alarm_sound" -> {
                playAlarmSound(durationMinutes)
                showSoundNotification()
            }
            else -> Log.w("WeatherAlertWorker", "Unknown alarm type: $alarmType")
        }

        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        // Cleanup: stop sound and unregister receiver
        synchronized(this) {
            ringtone?.let {
                if (it.isPlaying) {
                    it.stop()
                    Log.d("WeatherAlertWorker", "Sound stopped on worker stop")
                }
            } ?: run {
                Log.d("WeatherAlertWorker", "Ringtone reference is null on stop")
            }
            try {
                localBroadcastManager.unregisterReceiver(stopSoundReceiver)
                Log.d("WeatherAlertWorker", "Stop sound receiver unregistered")
            } catch (e: Exception) {
                Log.e("WeatherAlertWorker", "Failed to unregister stop sound receiver: ${e.message}", e)
            }
            // Cancel notifications
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
            notificationManager.cancel(SOUND_NOTIFICATION_ID)
            Log.d("WeatherAlertWorker", "Notifications cancelled on worker stop")
        }
    }

    private fun showNotification() {
        Log.d("WeatherAlertWorker", "Attempting to show notification")
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
            Log.d("WeatherAlertWorker", "Notification channel created")
        }

        // Create PendingIntent to open WeatherAlertsActivity
        val intent = Intent(applicationContext, WeatherAlertsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Weather Alert")
            .setContentText("Severe weather warning! Check the app for details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d("WeatherAlertWorker", "Notification sent with ID: $NOTIFICATION_ID")
    }

    private fun showSoundNotification() {
        Log.d("WeatherAlertWorker", "Attempting to show sound notification")
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
            Log.d("WeatherAlertWorker", "Notification channel created for sound alert")
        }

        // Create PendingIntent to open WeatherAlertsActivity
        val intent = Intent(applicationContext, WeatherAlertsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 1, intent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SkyNote Weather Alert")
            .setContentText("This is a weather alert sound from SkyNote. Check the app for details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(SOUND_NOTIFICATION_ID, notification)
        Log.d("WeatherAlertWorker", "Sound notification sent with ID: $SOUND_NOTIFICATION_ID")
    }

    private fun playAlarmSound(durationMinutes: Int) {
        Log.d("WeatherAlertWorker", "Playing alarm sound for $durationMinutes minutes")
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(applicationContext, alarmSound)
        ringtone?.play()

        // Stop the sound after durationMinutes
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            synchronized(this) {
                ringtone?.let {
                    if (it.isPlaying) {
                        it.stop()
                        Log.d("WeatherAlertWorker", "Alarm sound stopped after $durationMinutes minutes")
                        // Cancel the notification when the sound stops
                        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(SOUND_NOTIFICATION_ID)
                        Log.d("WeatherAlertWorker", "Sound notification cancelled")
                    }
                } ?: run {
                    Log.d("WeatherAlertWorker", "Ringtone reference is null on timed stop")
                }
            }
        }, durationMinutes * 60 * 1000L)
    }
}