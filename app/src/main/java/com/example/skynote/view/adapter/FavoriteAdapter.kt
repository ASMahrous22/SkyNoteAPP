package com.example.skynote.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skynote.data.local.FavoriteLocation
import com.example.skynote.databinding.ItemFavoriteLocationBinding

class FavoriteAdapter(
    private var items: List<FavoriteLocation>,
    private val onDeleteClick: (Int, FavoriteLocation) -> Unit,
    private val onItemClick: (FavoriteLocation) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(val binding: ItemFavoriteLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnDelete.setOnClickListener {
                onDeleteClick(adapterPosition, items[adapterPosition])
            }
            binding.root.setOnClickListener {
                onItemClick(items[adapterPosition])
            }
        }

        fun bind(item: FavoriteLocation) {
            binding.tvLocationName.text = item.name
            binding.tvCoordinates.text = String.format("Latitude: %.4f, Longitude: %.4f", item.latitude, item.longitude)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<FavoriteLocation>) {
        items = newItems
        notifyDataSetChanged()
    }
}