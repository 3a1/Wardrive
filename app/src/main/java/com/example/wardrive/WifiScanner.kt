package com.example.wardrive

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat

class WifiScanner(private val context: Context, private val locationProvider: LocationProvider)
{
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val wifiMap = mutableMapOf<String, WifiNetwork>()

    private val wifiScanReceiver = object : BroadcastReceiver()
    {
        @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        override fun onReceive(context: Context?, intent: Intent?)
        {
            val success = intent?.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) == true
            if (success) collectScanResults()
        }
    }

    fun start()
    {
        context.registerReceiver(
            wifiScanReceiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        )
        startScan()
    }

    fun stop()
    {
        context.unregisterReceiver(wifiScanReceiver)
    }

    fun startScan()
    {
        wifiManager.startScan()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun collectScanResults()
    {
        val (lat, lon) = locationProvider.getLocation()
        val timestamp = System.currentTimeMillis()

        if (ActivityCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
            return

        wifiManager.scanResults.forEach { net ->
            val ssid = net.SSID.ifEmpty { "<Hidden>" }
            val security = when {
                "WPA3" in net.capabilities -> "WPA3"
                "WPA2" in net.capabilities -> "WPA2"
                "WPA" in net.capabilities -> "WPA"
                "WEP" in net.capabilities -> "WEP"
                else -> "OPEN"
            }

            wifiMap[net.BSSID] = WifiNetwork(net, locationProvider.getLocation(), timestamp)
        }
    }

    fun getAllNetworks(): List<WifiNetwork>
    {
        return wifiMap.values.toList()
    }
}
