package com.example.weather.api

import com.example.weather.model.ForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast.json")
    suspend fun forecast(
        @Query("q") query: String,
        @Query("days") days: Int,
        @Query("aqi") airQuality: String = "no",
        @Query("alerts") alerts: String = "no",
    ): ForecastResponse
}
