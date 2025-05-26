package com.example.skynote.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
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
            val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
            Glide.with(holder.itemView.context).load(iconUrl).into(ivWeatherIcon)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<WeatherItem>)
    {
        items = newItems
        notifyDataSetChanged()
    }

    private fun convertToTime(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}