package com.example.a214179_nabiha_sirnelson_project2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

suspend fun getWeather(city: String): String {

    return withContext(Dispatchers.IO) {

        try {

            val apiKey = "c9de07195a72af0f6db3b14aab99e57c"
            val url =
                "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"

            val response = URL(url).readText()

            val json = JSONObject(response)

            val temp = json.getJSONObject("main").getDouble("temp")
            val condition = json.getJSONArray("weather")
                .getJSONObject(0)
                .getString("main")

            "$temp°C, $condition"

        } catch (e: Exception) {
            "Weather unavailable"
        }
    }
}