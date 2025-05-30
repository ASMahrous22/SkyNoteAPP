package com.example.skynote.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteLocation::class, WeatherAlert::class, WeatherEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase()
{
    abstract fun favoriteDao(): FavoriteDao
    abstract fun weatherAlertDao(): WeatherAlertDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase
        {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}