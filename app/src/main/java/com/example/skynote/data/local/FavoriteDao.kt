package com.example.skynote.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface FavoriteDao
{
    @Query("SELECT * FROM favorite_locations")
    fun getAllFavorites(): LiveData<List<FavoriteLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocation)

    @Delete
    suspend fun deleteFavorite(location: FavoriteLocation)
}
