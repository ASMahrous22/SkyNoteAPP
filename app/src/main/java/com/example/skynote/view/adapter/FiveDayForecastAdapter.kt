package com.example.skynote.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.skynote.data.model.WeatherItem
import com.example.skynote.databinding.ItemFiveDayForecastBinding
import java.text.SimpleDateFormat
import java.util.*

class FiveDayForecastAdapter(private var items: List<WeatherItem>
) : RecyclerView.Adapter<FiveDayForecastAdapter.FiveDayForecastViewHolder>()
{
    inner class FiveDayForecastViewHolder(val binding: ItemFiveDayForecastBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FiveDayForecastViewHolder
    {
        val binding = ItemFiveDayForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FiveDayForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FiveDayForecastViewHolder, position: Int)
    {
        val item = items[position]
        with(holder.binding) {
            tvDate.text = convertToDate(item.dt)
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

    private fun convertToDate(timestamp: Long): String
    {
        val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }
}