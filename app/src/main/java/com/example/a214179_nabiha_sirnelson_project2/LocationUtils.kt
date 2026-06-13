package com.example.a214179_nabiha_sirnelson_project2

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import java.util.Locale

@SuppressLint("MissingPermission")
fun getLocation(context: Context, callback: (String) -> Unit) {

    val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->

        if (location != null) {

            val lat = location.latitude
            val lon = location.longitude

            callback("Lat: $lat, Lon: $lon")

        } else {
            callback("Location unavailable")
        }
    }
}
fun getCityName(context: Context, lat: Double, lon: Double): String {

    return try {

        val geocoder = Geocoder(context, Locale.getDefault())

        val addresses = geocoder.getFromLocation(lat, lon, 1)

        if (!addresses.isNullOrEmpty()) {

            val city = addresses[0].locality
            val state = addresses[0].adminArea
            val country = addresses[0].countryName

            "$city, $state, $country"
        } else {
            "Unknown location"
        }

    } catch (e: Exception) {
        "Location error"
    }
}