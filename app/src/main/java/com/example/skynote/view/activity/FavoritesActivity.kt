package com.example.skynote.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AlertDialog
import com.example.skynote.data.local.FavoriteLocation
import com.example.skynote.databinding.ActivityFavoritesBinding
import com.example.skynote.view.adapter.FavoriteAdapter
import com.example.skynote.viewmodel.FavoriteViewModel
import java.util.Locale

class FavoritesActivity : AppCompatActivity()
{
    private lateinit var binding: ActivityFavoritesBinding
    private val viewModel: FavoriteViewModel by viewModels()
    private lateinit var adapter: FavoriteAdapter
    private val mapLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == RESULT_OK)
        {
            val data = result.data
            //val name = data?.getStringExtra("name") ?: "Unknown Location"
            val lat = data?.getDoubleExtra("lat", 0.0) ?: 0.0
            val lon = data?.getDoubleExtra("lon", 0.0) ?: 0.0
            if (lat != 0.0 && lon != 0.0)
            {
                val name = getLocationName(lat, lon) ?: "$lat, $lon"
               //val location = FavoriteLocation(name = name, latitude = lat, longitude = lon)
                viewModel.addFavorite(FavoriteLocation(0, name, lat, lon))
            }
            else
            {
                Log.e("FavoritesActivity", "Invalid latitude or longitude: $lat, $lon")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        adapter = FavoriteAdapter(emptyList()) { position, location ->
            showDeleteConfirmation(position, location)
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

    private fun showDeleteConfirmation(position: Int, location: FavoriteLocation) {
        AlertDialog.Builder(this)
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to remove ${location.name} from favorites?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.removeFavorite(location)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun getLocationName(lat: Double, lon: Double): String?
    {
        return try
        {
            val geocoder = android.location.Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty())
            {
                addresses[0].locality ?: addresses[0].featureName
                ?: "${lat}, ${lon}"
            }
            else
            {
                "${lat}, ${lon}"
            }
        }
        catch (e: Exception)
        {
            Log.e("FavoriteLocalDataSource", "Geocoder error: ${e.message}")
            "${lat}, ${lon}"
        }
    }
}