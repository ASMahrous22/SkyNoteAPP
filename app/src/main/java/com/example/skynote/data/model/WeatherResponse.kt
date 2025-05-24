package com.example.skynote.data.model

data class WeatherResponse(
    val list: List<WeatherItem>,
    val city: City
)

data class WeatherItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val clouds: Clouds
)

data class Main(
    val temp: Double,
    val pressure: Double,
    val humidity: Int
)

data class Weather(
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)

data class City(
    val name: String,
    val country: String
)

data class Clouds(
    val all: Int
)

//@Entity(tableName = "favorite_locations")
//data class FavoriteLocation(
//    @PrimaryKey val cityName: String,
//    val latitude: Double,
//    val longitude: Double
//)