package com.example.satellitetracker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY


class MainActivity : Activity() {

    private lateinit var btn_refresh: Button
    private lateinit var btn_list: Button
    private lateinit var btn_flashlight: Button
    private lateinit var text_gps_lat: TextView
    private lateinit var text_gps_lng: TextView
    private lateinit var text_gps_alt: TextView

    private var userLat: Float = 0.0f
    private var userLng: Float = 0.0f
    private var userAlt: Float = 0.0f

    private lateinit var camManager: CameraManager
    private lateinit var camId: String
    private var torchStatus: Boolean = false

    private lateinit var listIntent: Intent

    private val LOC_COARSE_PERM = android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val LOC_FINE_PERM = android.Manifest.permission.ACCESS_FINE_LOCATION
    private val CAM_PERM = android.Manifest.permission.CAMERA

    private var PERM_TOAST: Boolean = false;

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.main_layout)

        listIntent = Intent(this, SatelliteList::class.java)

        btn_refresh = findViewById(R.id.btn_refresh)
        btn_list = findViewById(R.id.btn_list)
        btn_flashlight = findViewById(R.id.btn_flashlight)
        text_gps_lat = findViewById(R.id.text_gps_lat)
        text_gps_lng = findViewById(R.id.text_gps_lng)
        text_gps_alt = findViewById(R.id.text_gps_alt)

        camManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            camId = camManager.cameraIdList[0]
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }

        requestRuntimePermission()

        val locClient = LocationServices.getFusedLocationProviderClient(this)

        setLocalizationInfo(locClient)

        btn_refresh.setOnClickListener {
            locClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY,
                null
                )
            setLocalizationInfo(locClient)
            Toast.makeText(this, "Odświeżono lokalizację", Toast.LENGTH_SHORT).show();
            Log.d("Klik", "btn_refresh")
        }

        btn_list.setOnClickListener {
            Log.d("Klik", "btn_list")

            startActivity(listIntent)
        }

        btn_flashlight.setOnClickListener {
            Log.d("Klik", "btn_flashlight")

            torch()
        }
    }

    override fun onResume() {
        super.onResume()

        requestRuntimePermission()
    }

    private fun torch() {
        if (torchStatus) {
            torchStatus = false
            camManager.setTorchMode(camId, torchStatus)
        }
        else {
            torchStatus = true
            camManager.setTorchMode(camId, torchStatus)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setLocalizationInfo(locationProvider: FusedLocationProviderClient) {
        locationProvider.lastLocation.addOnSuccessListener {
            if (it != null) {
                userLat = it.latitude.toFloat()
                userLng = it.longitude.toFloat()
                userAlt = it.altitude.toFloat()

                text_gps_lat.text = userLat.toString()
                text_gps_lng.text = userLng.toString()
                text_gps_alt.text = userAlt.toString() + "m"

                listIntent.putExtra("userLat", userLat)
                listIntent.putExtra("userLng", userLng)
                listIntent.putExtra("userAlt", userAlt)

            } else {
                text_gps_lat.text = userLat.toString()
                text_gps_lng.text = userLng.toString()
                text_gps_alt.text = userAlt.toString() + "m"

                Toast.makeText(this, "Nie udało się pobrać lokalizacji", Toast.LENGTH_SHORT).show()

                Log.d("BLAD", "Ostatnia lokalizacja byla nullem")
            }
        }
    }

    private fun requestRuntimePermission() {
        if (checkPerms()) {
            Toast.makeText(this, "Przyznano odpowiednie uprawnienia", Toast.LENGTH_SHORT).show()
            PERM_TOAST = true;
        } else {
            requestPermissions(
                arrayOf(
                    LOC_COARSE_PERM,
                    LOC_FINE_PERM,
                    CAM_PERM,
                ),
                100)
        }
    }

    private fun checkPerms(): Boolean {
        return (
                checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Przyznano odpowiednie uprawnienia", Toast.LENGTH_SHORT).show()
            } else {
                val errorIntent = Intent(this, NoPermissions::class.java)
                PERM_TOAST = false;
                startActivity(errorIntent)
            }
        }
    }
}