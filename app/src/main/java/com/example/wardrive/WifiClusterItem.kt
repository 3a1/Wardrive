package com.example.wardrive

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class WifiClusterItem(
    val wifi: WifiNetwork
) : ClusterItem {
    override fun getPosition() = LatLng(wifi.latitude ?: 0.0, wifi.longitude ?: 0.0)
    override fun getTitle() = wifi.ssid
    override fun getSnippet() = "RSSI: ${wifi.rssi} dBm | ${wifi.security}"

    override fun getZIndex(): Float? = null
}