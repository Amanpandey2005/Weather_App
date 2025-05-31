package com.example.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class Splashscreen : AppCompatActivity() {

    private lateinit var mfusedlocation: FusedLocationProviderClient
    private val myrequestcode = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)

        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@Splashscreen, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)

        getLastLocation()
    }

    private fun locationEnable(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            myrequestcode
        )
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermission()) {
            if (locationEnable()) {
                mfusedlocation.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocation()
                    } else {
                        Log.i("Location", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                        Toast.makeText(this, "Lat: ${location.latitude}", Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent (this,MainActivity:: class.java)
                            intent.putExtra("lat", location.latitude.toString())
                            intent.putExtra("long", location.longitude.toString())
                            startActivity(intent)
                            finish()
                        },2000)

                    }
                }
            } else {
                Toast.makeText(this, "Please turn on your location", Toast.LENGTH_LONG).show()
            }
        } else {
            requestPermission()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
            fastestInterval = 500
            numUpdates = 1
        }

        mfusedlocation.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val lastLocation: Location? = result.lastLocation
            if (lastLocation != null) {
                Log.i("NewLocation", "Lat: ${lastLocation.latitude}, Lng: ${lastLocation.longitude}")
            }
            if (lastLocation != null) {
                Toast.makeText(this@Splashscreen, "New Lat: ${lastLocation.latitude}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ✅ MUST be inside the class and override the system method
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == myrequestcode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
