package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission")
fun Any.getLocation(latitude: (String?, String?) -> Unit) {
    var getLatitude: String? = ""
    var getLongitude: String? = ""
    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MyApp.context!!)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location1 : Location? ->
            if (location1 != null) {
                getLatitude = location1.latitude.toString()
                getLongitude = location1.longitude.toString()
            } else {
                val lm = MyApp.context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                var location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location == null) {
                    location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        getLatitude = location.latitude.toString()
                        getLongitude = location.longitude.toString()
                    }
                }
            }
            latitude(getLatitude, getLongitude)
        }
        .addOnFailureListener {
            Log.d("Tes", it.toString())
        }
}