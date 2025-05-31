package com.example.weatherapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.parseColor("#1383C3")

        // Try to get lat/long from intent extras
        val lat = intent.getStringExtra("lat")
        val long = intent.getStringExtra("long")

        if (!lat.isNullOrEmpty() && !long.isNullOrEmpty()) {
            getJsonDataByLatLong(lat, long)
        }

        // Setup Search Button click listener to search by city name
        binding.searchButton.setOnClickListener {
            val cityInput = binding.city.text.toString().trim()
            if (cityInput.isNotEmpty()) {
                getJsonDataByCity(cityInput)
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getJsonDataByLatLong(lat: String, long: String) {
        val API_KEY = "18f1ac197db0207d1278a738602b73b9"
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$long&appid=$API_KEY"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Toast.makeText(this, "Weather data received", Toast.LENGTH_SHORT).show()
                setValues(response)
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun getJsonDataByCity(city: String) {
        val API_KEY = "18f1ac197db0207d1278a738602b73b9"
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$API_KEY"

        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                Toast.makeText(this, "Weather data received", Toast.LENGTH_SHORT).show()
                setValues(response)
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun setValues(response: String) {
        try {
            val jsonObj = JSONObject(response)

            // City and coordinates
            val cityName = jsonObj.getString("name")
            val coordObj = jsonObj.getJSONObject("coord")
            val lat = coordObj.getDouble("lat")
            val lon = coordObj.getDouble("lon")

            // Weather array for weather type and description
            val weatherArray = jsonObj.getJSONArray("weather")
            val weatherMain = if (weatherArray.length() > 0) weatherArray.getJSONObject(0).getString("main") else "N/A"

            // Main object for temperature, pressure, humidity
            val mainObj = jsonObj.getJSONObject("main")
            val tempKelvin = mainObj.getDouble("temp")
            val tempMinKelvin = mainObj.getDouble("temp_min")
            val tempMaxKelvin = mainObj.getDouble("temp_max")
            val pressure = mainObj.getInt("pressure")
            val humidity = mainObj.getInt("humidity")

            // Convert Kelvin to Celsius
            val tempC = (tempKelvin - 273.15).toInt()
            val tempMinC = (tempMinKelvin - 273.15).toInt()
            val tempMaxC = (tempMaxKelvin - 273.15).toInt()

            // Wind object
            val windObj = jsonObj.getJSONObject("wind")
            val windSpeed = windObj.getDouble("speed")
            val windDeg = windObj.optInt("deg", -1)
            val windGust = windObj.optDouble("gust", 0.0)

            // Update UI elements
            binding.city.setText(cityName)  // EditText: use setText()
            binding.coordinates.text = "$lat, $lon"
            binding.weatherType.text = weatherMain
            binding.temp.text = "$tempC°C\nMin temp: $tempMinC°C\nMax temp: $tempMaxC°C"
            binding.pressure.text = "$pressure\nPressure"
            binding.humidity.text = "$humidity\nHumidity"
            binding.windSpeed.text = windSpeed.toString()
            binding.windDetails.text = "Wind Speed    Degree: $windDeg   Gust: $windGust"

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to parse weather data", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}
