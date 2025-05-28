package com.example.skynote.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skynote.R
import com.example.skynote.data.model.WeatherItem
import com.example.skynote.databinding.ItemTodayForecastBinding
import java.text.SimpleDateFormat
import java.util.*

class TodayForecastAdapter(private var items: List<WeatherItem>
) : RecyclerView.Adapter<TodayForecastAdapter.TodayForecastViewHolder>()
{
    inner class TodayForecastViewHolder(val binding: ItemTodayForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodayForecastViewHolder
    {
        val binding = ItemTodayForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodayForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TodayForecastViewHolder, position: Int)
    {
        val item = items[position]
        with(holder.binding) {
            tvTime.text = convertToTime(item.dt)
            tvTemp.text = "${item.main.temp.toInt()}°"
            tvDesc.text = item.weather[0].description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            Glide.with(holder.itemView.context).load(getWeatherIcon(item.weather[0].icon)).into(ivWeatherIcon)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WeatherItem>)
    {
        items = newItems
        notifyDataSetChanged()
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

    private fun convertToTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}