package com.example.weather.format

import com.example.weather.CityResult
import com.example.weather.DailyForecast
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ForecastTableTest {

    private val tomorrow = LocalDate.of(2026, 9, 11)

    private fun forecast(date: LocalDate = tomorrow) = DailyForecast(
        date = date,
        minTempC = 11.4,
        maxTempC = 23.8,
        humidityPercent = 58.0,
        windSpeedKph = 19.1,
        windDirection = "NNW",
        windDegree = 337,
        condition = "Patchy rain nearby",
    )

    private fun success(city: String, country: String) =
        CityResult.Success(city, city, country, listOf(forecast()))

    @Test
    fun `dates head the columns and cities fill the rows`() {
        val table = ForecastTable.render(
            listOf(success("Chisinau", "Moldova"), success("Madrid", "Spain")),
        )
        val lines = table.lines()

        assertTrue(lines[1].contains("Fri 11 Sep 2026"), table)
        assertTrue(table.contains("Chisinau, Moldova"), table)
        assertTrue(table.contains("Madrid, Spain"), table)

        for (label in listOf(
            "Min Temperature (°C)", "Max Temperature (°C)",
            "Humidity (%)", "Wind Speed (kph)", "Wind Direction",
        )) {
            assertTrue(table.contains(label), "missing row '$label' in\n$table")
        }
        assertTrue(table.contains("NNW (337°)"), table)
    }

    @Test
    fun `every rendered line is the same width`() {
        val table = ForecastTable.render(
            listOf(success("Chisinau", "Moldova"), CityResult.Failure("Atlantis", "not found")),
        )
        val rows = table.lines().filter { it.startsWith("│") || it.startsWith("┌") || it.startsWith("├") || it.startsWith("└") }

        assertEquals(1, rows.map { it.length }.distinct().size, table)
    }

    @Test
    fun `a failing city keeps its row and gets a note`() {
        val table = ForecastTable.render(
            listOf(
                success("Chisinau", "Moldova"),
                CityResult.Failure("Atlantis", "No matching location found. (code 1006)"),
            ),
        )

        assertTrue(table.contains("│ Atlantis"), table)
        assertTrue(table.contains("unavailable"), table)
        assertTrue(table.trimEnd().endsWith("! Atlantis: No matching location found. (code 1006)"), table)
    }

    @Test
    fun `several days become several columns`() {
        val dayAfter = tomorrow.plusDays(1)
        val table = ForecastTable.render(
            listOf(
                CityResult.Success(
                    "Kyiv", "Kyiv", "Ukraine",
                    listOf(forecast(tomorrow), forecast(dayAfter)),
                ),
            ),
        )

        assertTrue(table.lines()[1].contains("Fri 11 Sep 2026"), table)
        assertTrue(table.lines()[1].contains("Sat 12 Sep 2026"), table)
    }

    @Test
    fun `renders notes only when nothing could be fetched`() {
        val table = ForecastTable.render(listOf(CityResult.Failure("Madrid", "network error")))

        assertEquals("! Madrid: network error", table)
    }
}
