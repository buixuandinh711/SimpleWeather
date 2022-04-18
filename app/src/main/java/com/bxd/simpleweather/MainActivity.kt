package com.bxd.simpleweather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.bxd.simpleweather.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.log
import kotlin.math.sign

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        var currentLocation: String = ""

        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    Log.d("xyz", "Permission granted")
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        currentLocation =
                            location?.latitude.toString() + "," + location?.longitude.toString()
                        getWeatherProperty(currentLocation)
                    }
                }
                else -> {
                    Log.d("xyz", "Permission granted")
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                currentLocation =
                    location?.latitude.toString() + "," + location?.longitude.toString()
                getWeatherProperty(currentLocation)
            }
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getWeatherProperty(currentLocation: String) {
        WeatherApi.weatherService.getWeatherProperty(
            "e3273e78035247a6abe175630221704",
            currentLocation
        )
            .enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    val responseObj = JSONObject(response.body() ?: "")
                    val propertyObj = responseObj.getJSONObject("current");
                    val locationObj = responseObj.getJSONObject("location")
                    val locationName = "Location: " + locationObj.getString("name")
                    val temperature = "Temperature: " + propertyObj.getString("temp_c") + "°C"
                    val conditionObj = propertyObj.getJSONObject("condition")
                    val status = "Status: " + conditionObj.getString("text")
                    val iconURL = "https:" + conditionObj.getString("icon")
                    binding.location.text =  locationName
                    binding.weatherStatus.text =status
                    binding.temperature.text = temperature
                    Picasso.get().load(iconURL).fit().into(binding.weatherIcon)
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d("abc", t.message ?: "")
                }
            })
    }
}