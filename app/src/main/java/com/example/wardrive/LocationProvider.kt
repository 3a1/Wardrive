package com.example.wardrive

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices

class LocationProvider(context: Context)
{
    private val client = LocationServices.getFusedLocationProviderClient(context)
    private var lastLocation: Location = Location(null, null)

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun update()
    {
        client.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) lastLocation = Location(loc)
        }
    }

    fun getLocation() = lastLocation
}