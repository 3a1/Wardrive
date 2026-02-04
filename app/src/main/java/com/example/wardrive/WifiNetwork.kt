package com.example.wardrive

import android.net.wifi.ScanResult

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val security: String,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long
) {
    constructor(scanResult: ScanResult, location: Location?, timestamp: Long) : this(
        ssid = scanResult.SSID.ifEmpty { "<Hidden>" },
        bssid = scanResult.BSSID,
        rssi = scanResult.level,
        security = when {
            "WPA3" in scanResult.capabilities -> "WPA3"
            "WPA2" in scanResult.capabilities -> "WPA2"
            "WPA" in scanResult.capabilities -> "WPA"
            "WEP" in scanResult.capabilities -> "WEP"
            else -> "OPEN"
        },
        latitude = location?.lat,
        longitude = location?.lon,
        timestamp = timestamp
    )
}
