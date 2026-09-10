package com.example.weather.api

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object WeatherApiClient {

    const val DEFAULT_BASE_URL: String = "https://api.weatherapi.com/"

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    fun create(
        apiKey: String,
        baseUrl: String = DEFAULT_BASE_URL,
        debugLogging: Boolean = false,
    ): WeatherApiService {
        val http = OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(apiKey))
            .apply { if (debugLogging) addInterceptor(redactingLogger()) }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.toHttpUrl())
            .client(http)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(WeatherApiService::class.java)
    }

    private class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val url = request.url.newBuilder().addQueryParameter("key", apiKey).build()
            return chain.proceed(request.newBuilder().url(url).build())
        }
    }

    private fun redactingLogger(): HttpLoggingInterceptor =
        HttpLoggingInterceptor { message ->
            println(message.replace(Regex("key=[^&\\s]+"), "key=***"))
        }.apply { level = HttpLoggingInterceptor.Level.BASIC }
}
