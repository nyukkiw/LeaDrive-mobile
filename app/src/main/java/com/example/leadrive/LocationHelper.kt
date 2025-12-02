package com.example.leadrive

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*

@SuppressLint("MissingPermission")
fun getUserLocation(context: Context, callback: (Double, Double) -> Unit) {

    val fused = LocationServices.getFusedLocationProviderClient(context)

    fused.lastLocation.addOnSuccessListener { loc: Location? ->
        if (loc != null) {
            callback(loc.latitude, loc.longitude)
        } else {
            callback(0.0, 0.0)
        }
    }
}
