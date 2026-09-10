package com.example.weather.format

import com.example.weather.CityResult
import com.example.weather.DailyForecast
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object ForecastTable {

    private val HEADER_DATE = DateTimeFormatter.ofPattern("EEE dd MMM yyyy", Locale.ENGLISH)
    private const val NO_VALUE = "—"

    private val METRICS: List<Metric> = listOf(
        Metric("Min Temperature (°C)") { format(it.minTempC, 1) },
        Metric("Max Temperature (°C)") { format(it.maxTempC, 1) },
        Metric("Humidity (%)") { format(it.humidityPercent, 0) },
        Metric("Wind Speed (kph)") { format(it.windSpeedKph, 1) },
        Metric("Wind Direction") { day ->
            day.windDegree?.let { "${day.windDirection} (${it}°)" } ?: day.windDirection
        },
    )

    private class Metric(val label: String, val read: (DailyForecast) -> String)

    fun render(results: List<CityResult>): String {
        val dates = results.filterIsInstance<CityResult.Success>()
            .flatMap { success -> success.days.map { it.date } }
            .distinct()
            .sorted()

        if (dates.isEmpty()) return renderFailuresOnly(results)

        val header = listOf("City", "Data point") + dates.map { it.format(HEADER_DATE) }
        val blocks = results.map { it.toRows(dates) }
        val widths = columnWidths(header, blocks.flatten())
        val alignRight = BooleanArray(widths.size) { it >= 2 }

        return buildString {
            appendLine(rule(widths, "┌", "┬", "┐"))
            appendLine(row(header, widths, BooleanArray(widths.size)))
            appendLine(rule(widths, "├", "┼", "┤"))

            blocks.forEachIndexed { index, block ->
                if (index > 0) appendLine(rule(widths, "├", "┼", "┤"))
                block.forEach { appendLine(row(it, widths, alignRight)) }
            }
            append(rule(widths, "└", "┴", "┘"))

            results.filterIsInstance<CityResult.Failure>().forEach {
                append("\n! ${it.city}: ${it.reason}")
            }
        }
    }

    private fun CityResult.toRows(dates: List<LocalDate>): List<List<String>> = when (this) {
        is CityResult.Failure ->
            listOf(listOf(city, "unavailable") + dates.map { NO_VALUE })

        is CityResult.Success -> {
            val byDate = days.associateBy { it.date }
            METRICS.mapIndexed { index, metric ->
                listOf(if (index == 0) "$resolvedName, $country" else "", metric.label) +
                    dates.map { date -> byDate[date]?.let(metric.read) ?: NO_VALUE }
            }
        }
    }

    private fun renderFailuresOnly(results: List<CityResult>): String =
        results.joinToString("\n") { result ->
            when (result) {
                is CityResult.Failure -> "! ${result.city}: ${result.reason}"
                is CityResult.Success -> "! ${result.city}: no forecast days returned"
            }
        }

    private fun columnWidths(header: List<String>, body: List<List<String>>): IntArray {
        val widths = IntArray(header.size) { header[it].width() }
        for (line in body) {
            line.forEachIndexed { i, cell ->
                if (i < widths.size) widths[i] = maxOf(widths[i], cell.width())
            }
        }
        return widths
    }

    private fun rule(widths: IntArray, left: String, mid: String, right: String): String =
        widths.joinToString(mid, prefix = left, postfix = right) { "─".repeat(it + 2) }

    private fun row(cells: List<String>, widths: IntArray, alignRight: BooleanArray): String =
        widths.indices.joinToString("│", prefix = "│", postfix = "│") { i ->
            val cell = cells.getOrElse(i) { "" }
            val padding = " ".repeat(maxOf(widths[i] - cell.width(), 0))
            if (alignRight[i]) " $padding$cell " else " $cell$padding "
        }

    private fun String.width(): Int = codePointCount(0, length)

    private fun format(value: Double, decimals: Int): String =
        String.format(Locale.ENGLISH, "%.${decimals}f", value)
}
