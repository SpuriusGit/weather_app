package com.example.weather

import java.time.LocalDate

data class DailyForecast(
    val date: LocalDate,
    val minTempC: Double,
    val maxTempC: Double,
    val humidityPercent: Double,
    val windSpeedKph: Double,
    val windDirection: String,
    val windDegree: Int?,
    val condition: String,
)

sealed interface CityResult {
    val city: String

    data class Success(
        override val city: String,
        val resolvedName: String,
        val country: String,
        val days: List<DailyForecast>,
    ) : CityResult

    data class Failure(
        override val city: String,
        val reason: String,
    ) : CityResult
}
