# Weather Forecast

A Kotlin command-line app that prints tomorrow's [WeatherAPI.com](https://www.weatherapi.com/)
forecast for Chisinau, Madrid, Kyiv and Amsterdam as a table — dates across the columns,
cities down the rows.

```
┌────────────────────────┬──────────────────────┬─────────────────┐
│ City                   │ Data point           │ Fri 11 Sep 2026 │
├────────────────────────┼──────────────────────┼─────────────────┤
│ Chisinau, Moldova      │ Min Temperature (°C) │             9.2 │
│                        │ Max Temperature (°C) │            17.4 │
│                        │ Humidity (%)         │              46 │
│                        │ Wind Speed (kph)     │            26.9 │
│                        │ Wind Direction       │      SSW (202°) │
├────────────────────────┼──────────────────────┼─────────────────┤
│ Madrid, Spain          │ Min Temperature (°C) │            11.0 │
│                        │ ...                  │                 │
└────────────────────────┴──────────────────────┴─────────────────┘
```

## Stack

Kotlin 2.0 · Gradle (Kotlin DSL, with wrapper) · Retrofit 2 + OkHttp ·
kotlinx.serialization · Coroutines · JUnit 5 + MockWebServer.

## Running it

You need a JDK (17 or newer) on your `PATH` and a free WeatherAPI.com key from
<https://www.weatherapi.com/signup.aspx>. Then, from a fresh clone:

```bash
export WEATHER_API_KEY=your_key_here
./gradlew run
```

The table is printed to STDOUT. Nothing else needs installing: the wrapper downloads
Gradle itself, and the foojay resolver in `settings.gradle.kts` provisions a JDK 21
toolchain for compilation if your JDK is a different version. The first run takes a
couple of minutes while Gradle and the dependencies download; later runs are seconds.

To run it as a plain binary instead:

```bash
./gradlew installDist
WEATHER_API_KEY=your_key_here ./build/install/weather-forecast/bin/weather-forecast
```

### Options

```
  -c, --city <name>     City to report; repeat for several.
                        Default: Chisinau, Madrid, Kyiv, Amsterdam
  -d, --days <n>        Forecast days starting tomorrow (1-2, free plan). Default: 1
  -k, --api-key <key>   WeatherAPI.com key. Default: the WEATHER_API_KEY environment
                        variable, which is the preferred way to pass it.
      --base-url <url>  Override the API base URL (for testing).
  -v, --verbose         Log requests, with the key masked.
  -h, --help            Show this message.
```

Pass options through Gradle with `--args`:

```bash
./gradlew run --args="--city Lisbon --city Porto --days 2 --verbose"
```

## Tests

```bash
./gradlew test
```

The API is stubbed with OkHttp's MockWebServer, so the suite runs offline and needs no key.

## Notes on the data

- **The next day.** WeatherAPI counts *today* as day 1 of its forecast window, so the app
  requests one extra day and drops every day up to and including the location's local
  today. `--days 2` therefore adds the day after tomorrow — the free plan's 3-day window
  is the ceiling.
- **Wind direction** is the one data point the API does not publish on its daily summary;
  it exists only per hour. The app reports the direction recorded at the hour that
  produced the day's maximum wind speed, so the speed and direction shown describe the
  same moment rather than being averaged apart from each other.
- **Humidity** is the API's daily average (`avghumidity`); **wind speed** is the daily
  maximum (`maxwind_kph`).
- Cities are fetched **concurrently**, and one city failing (unknown name, quota, network)
  leaves its row marked `unavailable` with the reason footnoted, rather than killing the
  run. The exit code is non-zero only if *no* city could be fetched.

## Layout

```
src/main/kotlin/com/example/weather/
├── Main.kt                    entry point: options → service → table
├── ForecastService.kt         concurrent fetch, "tomorrow onward" windowing
├── DailyForecast.kt           domain model and per-city result type
├── ApiErrorReader.kt          WeatherAPI error envelopes → readable messages
├── api/                       Retrofit service + client (key injected by interceptor)
├── cli/                       argument parsing
├── format/ForecastTable.kt    box-drawn table renderer
└── model/                     API payload DTOs
```

The API key is added by an OkHttp interceptor, so no call site handles it, and `--verbose`
masks it in the request log.
# weather_app
