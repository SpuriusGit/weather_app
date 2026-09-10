package com.example.weather

object TestFixtures {

    val twoDayResponse: String = """
    {
      "location": {
        "name": "Chisinau", "region": "Chisinau", "country": "Moldova",
        "lat": 47.0056, "lon": 28.8575, "tz_id": "Europe/Chisinau",
        "localtime": "2026-09-10 13:25"
      },
      "current": { "temp_c": 21.0, "wind_kph": 12.2, "wind_dir": "NW" },
      "forecast": {
        "forecastday": [
          {
            "date": "2026-09-10",
            "day": {
              "maxtemp_c": 25.6, "mintemp_c": 13.0, "maxwind_kph": 15.5,
              "avghumidity": 51, "condition": { "text": "Sunny", "code": 1000 }
            },
            "hour": [
              { "time": "2026-09-10 12:00", "wind_kph": 15.5, "wind_degree": 300, "wind_dir": "WNW" }
            ]
          },
          {
            "date": "2026-09-11",
            "day": {
              "maxtemp_c": 23.8, "mintemp_c": 11.4, "maxwind_kph": 19.1,
              "avghumidity": 58, "condition": { "text": "Patchy rain nearby", "code": 1063 }
            },
            "hour": [
              { "time": "2026-09-11 09:00", "wind_kph": 10.4, "wind_degree": 220, "wind_dir": "SW" },
              { "time": "2026-09-11 15:00", "wind_kph": 19.1, "wind_degree": 337, "wind_dir": "NNW" },
              { "time": "2026-09-11 21:00", "wind_kph": 12.6, "wind_degree": 350, "wind_dir": "N" }
            ]
          }
        ]
      }
    }
    """.trimIndent()
}
