package com.example.skynote.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class WeatherResponse(
    val list: List<Forecast>,
    val city: City
)

data class Forecast(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds,
    val dt_txt: String
)

data class Main(
    val temp: Float,
    val pressure: Int,
    val humidity: Int
)

data class Weather(
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Float
)

data class Clouds(
    val all: Int
)

data class City(
    val name: String
)

@Entity(tableName = "favorite_locations")
data class FavoriteLocation(
    @PrimaryKey val cityName: String,
    val latitude: Double,
    val longitude: Double
)