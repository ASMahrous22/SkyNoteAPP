package com.example.skynote.data.repository

import android.content.Context
import com.example.skynote.data.local.AppDatabase
import com.example.skynote.data.local.FavoriteLocation

class FavoriteRepository(context: Context)
{
    private val favoriteDao = AppDatabase.getDatabase(context).favoriteDao()

    fun getAllFavorites() = favoriteDao.getAllFavorites()

    suspend fun insertFavorite(location: FavoriteLocation)
    {
        favoriteDao.insertFavorite(location)
    }

    suspend fun deleteFavorite(location: FavoriteLocation)
    {
        favoriteDao.deleteFavorite(location)
    }
}
