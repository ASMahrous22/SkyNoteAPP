package com.example.skynote.view.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.skynote.data.local.WeatherAlert
import com.example.skynote.databinding.ItemWeatherAlertBinding
import com.example.skynote.worker.WeatherAlertWorker
import java.text.SimpleDateFormat
import java.util.*

class WeatherAlertAdapter(
    private var items: List<WeatherAlert>,
    private val onStop: (WeatherAlert) -> Unit,
    private val onDelete: (WeatherAlert) -> Unit
) : RecyclerView.Adapter<WeatherAlertAdapter.WeatherAlertViewHolder>() {

    inner class WeatherAlertViewHolder(val binding: ItemWeatherAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnStop.setOnClickListener {
                val alert = items[adapterPosition]
                onStop(alert)
                // Send broadcast to stop sound if alarm_sound type
                if (alert.alarmType == "alarm_sound") {
                    val intent = Intent(WeatherAlertWorker.ACTION_STOP_SOUND)
                    LocalBroadcastManager.getInstance(binding.root.context).sendBroadcast(intent)
                    Log.d("WeatherAlertAdapter", "Stop sound broadcast sent for alert ID: ${alert.id}")
                }
            }
            binding.btnDelete.setOnClickListener {
                showDeleteConfirmationDialog(binding.root.context, items[adapterPosition])
            }
        }

        fun bind(item: WeatherAlert) {
            binding.tvAlertTime.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.dateTime))
            binding.tvDuration.text = "Duration: ${item.durationMinutes} minutes"
            binding.tvAlarmType.text = "Type: ${item.alarmType.replace("_", " ").capitalize(Locale.getDefault())}"
            binding.btnStop.isEnabled = item.isActive
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherAlertViewHolder {
        val binding = ItemWeatherAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherAlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherAlertViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WeatherAlert>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun showDeleteConfirmationDialog(context: Context, alert: WeatherAlert) {
        AlertDialog.Builder(context)
            .setTitle("Delete Alert")
            .setMessage("Are you sure you want to delete this weather alert scheduled for ${
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(alert.dateTime))
            }?")
            .setPositiveButton("Delete") { _, _ ->
                onDelete(alert)
                // Send broadcast to stop sound if alarm_sound type
                if (alert.alarmType == "alarm_sound") {
                    val intent = Intent(WeatherAlertWorker.ACTION_STOP_SOUND)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    Log.d("WeatherAlertAdapter", "Stop sound broadcast sent for alert ID: ${alert.id} on delete")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}