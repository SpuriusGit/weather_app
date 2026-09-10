package com.example.weather

import com.example.weather.api.WeatherApiClient
import com.example.weather.cli.CliException
import com.example.weather.cli.CliParser
import com.example.weather.format.ForecastTable
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking

private const val API_KEY_ENV = "WEATHER_API_KEY"

fun main(args: Array<String>) {
    val options = try {
        CliParser.parse(args)
    } catch (e: CliException) {
        System.err.println("weather-forecast: ${e.message}")
        System.err.println(CliParser.USAGE)
        exitProcess(2)
    }

    if (options.help) {
        println(CliParser.USAGE.trim())
        return
    }

    val apiKey = options.apiKey ?: System.getenv(API_KEY_ENV)
    if (apiKey.isNullOrBlank()) {
        System.err.println(
            "weather-forecast: no API key. Set $API_KEY_ENV or pass --api-key <key>.\n" +
                "Get a free key at https://www.weatherapi.com/signup.aspx",
        )
        exitProcess(2)
    }

    val api = WeatherApiClient.create(
        apiKey = apiKey,
        baseUrl = options.baseUrl ?: WeatherApiClient.DEFAULT_BASE_URL,
        debugLogging = options.verbose,
    )

    val results = runBlocking {
        ForecastService(api).forecastFromTomorrow(options.cities, options.days)
    }

    println(ForecastTable.render(results))

    if (results.none { it is CityResult.Success }) exitProcess(1)
}
