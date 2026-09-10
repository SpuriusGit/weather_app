package com.example.weather

import com.example.weather.api.WeatherApiClient
import com.example.weather.model.ApiErrorResponse
import retrofit2.HttpException

class ApiErrorReader {

    fun describe(e: HttpException): String {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()
        val api = body?.let {
            runCatching { WeatherApiClient.json.decodeFromString<ApiErrorResponse>(it).error }
                .getOrNull()
        }

        if (api == null) return "HTTP ${e.code()} ${e.message()}"

        val hint = when (api.code) {
            1002, 2006 -> " - check WEATHER_API_KEY"
            2007, 2008 -> " - API key quota exhausted or disabled"
            else -> ""
        }
        return "${api.message} (code ${api.code})$hint"
    }
}
