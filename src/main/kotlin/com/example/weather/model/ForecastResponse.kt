package com.example.weather.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponse(
    val location: Location,
    val forecast: Forecast,
)

@Serializable
data class Location(
    val name: String,
    val country: String,
    @SerialName("tz_id") val timeZoneId: String,
)

@Serializable
data class Forecast(
    @SerialName("forecastday") val forecastDays: List<ForecastDay>,
)

@Serializable
data class ForecastDay(
    val date: String,
    val day: Day,
    val hour: List<Hour> = emptyList(),
)

@Serializable
data class Day(
    @SerialName("mintemp_c") val minTempC: Double,
    @SerialName("maxtemp_c") val maxTempC: Double,
    @SerialName("maxwind_kph") val maxWindKph: Double,
    @SerialName("avghumidity") val avgHumidity: Double,
    val condition: Condition,
)

@Serializable
data class Hour(
    val time: String,
    @SerialName("wind_kph") val windKph: Double,
    @SerialName("wind_degree") val windDegree: Int,
    @SerialName("wind_dir") val windDir: String,
)

@Serializable
data class Condition(
    val text: String,
)

@Serializable
data class ApiErrorResponse(val error: ApiError)

@Serializable
data class ApiError(val code: Int, val message: String)
