package com.example.skynote.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skynote.data.local.FavoriteLocation
import com.example.skynote.databinding.ActivityFavoritesBinding
import com.example.skynote.view.adapter.FavoriteAdapter
import com.example.skynote.viewmodel.FavoriteViewModel

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var adapter: FavoriteAdapter
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val name = data?.getStringExtra("name") ?: "New Location"
            val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = data?.getDoubleExtra("lon", 0.0) ?: 0.0
            val location = FavoriteLocation(name = name, latitude = lat, longitude = lon)
            viewModel.addFavorite(location)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = FavoriteAdapter(emptyList()) { position, location ->
            viewModel.removeFavorite(location)
        }
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favoritesRecyclerView.adapter = adapter

        // Observe favorites and update UI
        viewModel.loadFavorites().observe(this) { favorites ->
            if (favorites.isEmpty()) {
                binding.favoritesRecyclerView.visibility = android.view.View.GONE
                binding.emptyState.visibility = android.view.View.VISIBLE
            } else {
                binding.favoritesRecyclerView.visibility = android.view.View.VISIBLE
                binding.emptyState.visibility = android.view.View.GONE
                adapter.updateData(favorites)
            }
        }

        // FAB to add a new favorite
        binding.fabAddFavorite.setOnClickListener {
            val intent = Intent(this, MapSearchActivity::class.java)
            mapLauncher.launch(intent)
        }
    }
}