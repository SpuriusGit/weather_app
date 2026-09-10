package com.example.weather.cli

data class CliOptions(
    val cities: List<String>,
    val days: Int,
    val apiKey: String?,
    val baseUrl: String?,
    val verbose: Boolean,
    val help: Boolean,
)

class CliException(message: String) : IllegalArgumentException(message)

object CliParser {

    val DEFAULT_CITIES: List<String> = listOf("Chisinau", "Madrid", "Kyiv", "Amsterdam")
    private const val MAX_DAYS = 3

    val USAGE: String = """
Usage: weather-forecast [options]

Prints tomorrow's WeatherAPI.com forecast as a table: dates in columns, cities in rows.

Options:
  -c, --city <name>     City to report; repeat for several.
                        Default: ${DEFAULT_CITIES.joinToString(", ")}
  -d, --days <n>        Forecast days starting tomorrow (1-2, free plan). Default: 1
  -k, --api-key <key>   WeatherAPI.com key. Default: the WEATHER_API_KEY environment
                        variable, which is the preferred way to pass it.
      --base-url <url>  Override the API base URL (for testing).
  -v, --verbose         Log requests, with the key masked.
  -h, --help            Show this message.
"""

    fun parse(args: Array<String>): CliOptions {
        val cities = mutableListOf<String>()
        var days = 1
        var apiKey: String? = null
        var baseUrl: String? = null
        var verbose = false
        var help = false

        var i = 0
        while (i < args.size) {
            when (val arg = args[i]) {
                "-c", "--city" -> cities += args.value(++i, arg)
                "-d", "--days" -> days = args.value(++i, arg).toIntOrNull()
                    ?: throw CliException("--days expects a number, got '${args[i]}'")

                "-k", "--api-key" -> apiKey = args.value(++i, arg)
                "--base-url" -> baseUrl = args.value(++i, arg)
                "-v", "--verbose" -> verbose = true
                "-h", "--help" -> help = true
                else -> throw CliException("unknown option '$arg'")
            }
            i++
        }

        if (days !in 1 until MAX_DAYS) {
            throw CliException("--days must be between 1 and ${MAX_DAYS - 1}")
        }

        return CliOptions(
            cities = cities.ifEmpty { DEFAULT_CITIES },
            days = days,
            apiKey = apiKey,
            baseUrl = baseUrl,
            verbose = verbose,
            help = help,
        )
    }

    private fun Array<String>.value(index: Int, option: String): String =
        getOrNull(index) ?: throw CliException("$option expects a value")
}
