package com.example.wardrive

data class Location(val lat: Double?, val lon: Double?)
{
    constructor(androidLocation: android.location.Location) : this(
        lat = androidLocation.latitude,
        lon = androidLocation.longitude
    )
}