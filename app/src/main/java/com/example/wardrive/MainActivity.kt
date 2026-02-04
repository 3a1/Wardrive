package com.example.wardrive

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager

class MainActivity : AppCompatActivity(), OnMapReadyCallback
{
    private lateinit var wifiScanner: WifiScanner
    private lateinit var locationProvider: LocationProvider
    private var googleMap: GoogleMap? = null
    private var clusterManager: ClusterManager<WifiClusterItem>? = null

    private var scanning = false

    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable
    {
        @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
        override fun run() {
            updateMap()
            handler.postDelayed(this, 5000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissionsIfNeeded()

        locationProvider = LocationProvider(this)
        wifiScanner = WifiScanner(this, locationProvider)

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
            .getMapAsync(this)

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            scanning = !scanning
            toggleScanning(scanning, it as Button)
        }
    }

    override fun onResume()
    {
        super.onResume()
        if (scanning) startScanning()
    }

    override fun onPause()
    {
        super.onPause()
        stopScanning()
    }

    private fun toggleScanning(start: Boolean, btn: Button)
    {
        if (start) startScanning() else stopScanning()
        btn.text = if (start) "Stop" else "Start"
        btn.setBackgroundColor(
            resources.getColor(if (start) android.R.color.holo_red_light else android.R.color.holo_green_light)
        )
    }

    private fun startScanning()
    {
        wifiScanner.start()
        handler.post(updateRunnable)
    }

    private fun stopScanning()
    {
        wifiScanner.stop()
        handler.removeCallbacks(updateRunnable)
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun updateMap()
    {
        locationProvider.update()
        drawWifiOnMap()
        moveCameraToMe()
    }

    private fun drawWifiOnMap()
    {
        clusterManager?.let { cm ->
            cm.clearItems()
            wifiScanner.getAllNetworks().forEach { wifi ->
                if (wifi.latitude != null && wifi.longitude != null) {
                    cm.addItem(WifiClusterItem(wifi))
                }
            }
            cm.cluster()
        }
    }

    private fun moveCameraToMe()
    {
        val (lat, lon) = locationProvider.getLocation()
        if (lat != null && lon != null) {
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 16f))
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(map: GoogleMap)
    {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = true


        clusterManager = ClusterManager(this, map)

        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)

        clusterManager?.setOnClusterClickListener { cluster ->
            showWifiList(cluster.items.map { it.wifi })
            true
        }

        if (locationProvider.getLocation().lat != null) map.isMyLocationEnabled = true
    }

    private fun showWifiList(wifis: Collection<WifiNetwork>)
    {
        if (wifis.isEmpty()) return

        AlertDialog.Builder(this)
            .setTitle("Wi-Fi (${wifis.size})")
            .setItems(wifis.map { "${it.ssid}\nRSSI: ${it.rssi} dBm | ${it.security}" }.toTypedArray(), null)
            .setPositiveButton("OK", null)
            .show()
    }

    // Permissions
    private fun hasLocationPermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissionsIfNeeded()
    {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }
}
