package com.example.weather

import com.example.weather.api.WeatherApiService
import com.example.weather.model.ForecastDay
import com.example.weather.model.ForecastResponse
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class ForecastService(
    private val api: WeatherApiService,
    private val errorReader: ApiErrorReader = ApiErrorReader(),
) {

    suspend fun forecastFromTomorrow(cities: List<String>, days: Int = 1): List<CityResult> =
        coroutineScope {
            cities
                .map { city -> async(Dispatchers.IO) { fetchCity(city, days) } }
                .awaitAll()
        }

    private suspend fun fetchCity(city: String, days: Int): CityResult = try {
        val response = api.forecast(query = city, days = days + 1)
        toResult(city, response, days)
    } catch (e: HttpException) {
        CityResult.Failure(city, errorReader.describe(e))
    } catch (e: IOException) {
        CityResult.Failure(city, "network error: ${e.message ?: e::class.simpleName}")
    }

    private fun toResult(city: String, response: ForecastResponse, days: Int): CityResult {
        val today = LocalDate.parse(response.forecast.forecastDays.first().date)
        val upcoming = response.forecast.forecastDays
            .filter { LocalDate.parse(it.date) > today }
            .take(days)

        if (upcoming.isEmpty()) {
            return CityResult.Failure(
                city,
                "the API returned no forecast day after ${today}; the free plan caps the " +
                    "forecast at 3 days",
            )
        }

        return CityResult.Success(
            city = city,
            resolvedName = response.location.name,
            country = response.location.country,
            days = upcoming.map { it.toDailyForecast() },
        )
    }
}

internal fun ForecastDay.toDailyForecast(): DailyForecast {
    val windiestHour = hour.maxByOrNull { it.windKph }
    return DailyForecast(
        date = LocalDate.parse(date),
        minTempC = day.minTempC,
        maxTempC = day.maxTempC,
        humidityPercent = day.avgHumidity,
        windSpeedKph = day.maxWindKph,
        windDirection = windiestHour?.windDir ?: "n/a",
        windDegree = windiestHour?.windDegree,
        condition = day.condition.text,
    )
}
