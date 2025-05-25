package com.example.skynote.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skynote.data.local.FavoriteLocation
import com.example.skynote.databinding.ItemFavoriteLocationBinding

class FavoriteLocationAdapter(
    private var items: List<FavoriteLocation>
) : RecyclerView.Adapter<FavoriteLocationAdapter.FavoriteViewHolder>()
{
    inner class FavoriteViewHolder(val binding: ItemFavoriteLocationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder
    {
        val binding = ItemFavoriteLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int)
    {
        val item = items[position]
        holder.binding.tvLocationName.text = item.name
        holder.binding.tvCoordinates.text = String.format("Latitude: %.4f, Longitude: %.4f", item.latitude, item.longitude)
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FavoriteLocation>)
    {
        items = newItems
        notifyDataSetChanged()
    }
}
