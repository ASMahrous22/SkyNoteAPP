package com.example.skynote.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skynote.data.local.FavoriteLocation
import com.example.skynote.data.repository.FavoriteRepository
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = FavoriteRepository(application)
    val favorites: LiveData<List<FavoriteLocation>> = repository.getAllFavorites()

    fun loadFavorites(): LiveData<List<FavoriteLocation>> = favorites

    fun addFavorite(location: FavoriteLocation)
    {
        viewModelScope.launch {
            repository.insertFavorite(location)
        }
    }

    fun removeFavorite(location: FavoriteLocation)
    {
        viewModelScope.launch {
            repository.deleteFavorite(location)
        }
    }
}
