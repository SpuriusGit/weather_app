package com.example.weather

import com.example.weather.api.WeatherApiClient
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest

class ForecastServiceTest {

    private val server = MockWebServer()

    @AfterTest
    fun tearDown() = server.shutdown()

    private fun service() = ForecastService(
        WeatherApiClient.create(apiKey = "test-key", baseUrl = server.url("/").toString()),
    )

    private fun respondByCity(vararg responses: Pair<String, MockResponse>) {
        val byCity = responses.toMap()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                byCity[request.requestUrl?.queryParameter("q")]
                    ?: MockResponse().setResponseCode(404)
        }
    }

    @Test
    fun `drops today and maps tomorrow`() = runTest {
        server.enqueue(MockResponse().setBody(TestFixtures.twoDayResponse))

        val results = service().forecastFromTomorrow(listOf("Chisinau"))

        val success = assertIs<CityResult.Success>(results.single())
        assertEquals("Chisinau", success.resolvedName)
        assertEquals("Moldova", success.country)

        val tomorrow = success.days.single()
        assertEquals(LocalDate.of(2026, 9, 11), tomorrow.date)
        assertEquals(11.4, tomorrow.minTempC)
        assertEquals(23.8, tomorrow.maxTempC)
        assertEquals(58.0, tomorrow.humidityPercent)
        assertEquals(19.1, tomorrow.windSpeedKph)
    }

    @Test
    fun `wind direction comes from the windiest hour`() = runTest {
        server.enqueue(MockResponse().setBody(TestFixtures.twoDayResponse))

        val success = assertIs<CityResult.Success>(
            service().forecastFromTomorrow(listOf("Chisinau")).single(),
        )

        assertEquals("NNW", success.days.single().windDirection)
        assertEquals(337, success.days.single().windDegree)
    }

    @Test
    fun `sends the api key and requested day count`() = runTest {
        server.enqueue(MockResponse().setBody(TestFixtures.twoDayResponse))

        service().forecastFromTomorrow(listOf("Kyiv"))

        val url = server.takeRequest().requestUrl!!
        assertEquals("/v1/forecast.json", url.encodedPath)
        assertEquals("Kyiv", url.queryParameter("q"))
        assertEquals("test-key", url.queryParameter("key"))
        assertEquals("2", url.queryParameter("days"))
    }

    @Test
    fun `reports an unknown city without failing the run`() = runTest {
        respondByCity(
            "Chisinau" to MockResponse().setBody(TestFixtures.twoDayResponse),
            "Nowhereville" to MockResponse().setResponseCode(400).setBody(
                """{"error":{"code":1006,"message":"No matching location found."}}""",
            ),
        )

        val results = service().forecastFromTomorrow(listOf("Chisinau", "Nowhereville"))

        assertIs<CityResult.Success>(results[0])
        val failure = assertIs<CityResult.Failure>(results[1])
        assertTrue(failure.reason.contains("No matching location"), failure.reason)
        assertTrue(failure.reason.contains("1006"), failure.reason)
    }

    @Test
    fun `explains an invalid api key`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(401).setBody(
                """{"error":{"code":2006,"message":"API key provided is invalid"}}""",
            ),
        )

        val failure = assertIs<CityResult.Failure>(
            service().forecastFromTomorrow(listOf("Madrid")).single(),
        )
        assertTrue(failure.reason.contains("WEATHER_API_KEY"), failure.reason)
    }
}
