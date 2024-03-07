package com.example.satellitetracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.satellitetracker.network.SatApiService
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import java.io.IOException

class SatelliteInfo: Activity() {

    private lateinit var data: String

    private lateinit var operator_text: TextView
    private lateinit var name_text: TextView
    private lateinit var norad_text: TextView
    private lateinit var user_text: TextView
    private lateinit var lat_text: TextView
    private lateinit var lng_text: TextView
    private lateinit var azi_text: TextView
    private lateinit var elev_text: TextView
    private lateinit var button_locate: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.satellite_info)

        button_locate = findViewById(R.id.button_locate)
        operator_text = findViewById(R.id.text_satinfo_operator)
        name_text = findViewById(R.id.text_satinfo_name)
        norad_text = findViewById(R.id.text_satinfo_norad)
        user_text = findViewById(R.id.text_satinfo_user)
        lng_text = findViewById(R.id.text_satinfo_lng)
        lat_text = findViewById(R.id.text_satinfo_lat)
        azi_text = findViewById(R.id.text_satinfo_azi)
        elev_text = findViewById(R.id.text_satinfo_elev)

        val userLat = intent.getFloatExtra("userLat", 0F)
        val userLng = intent.getFloatExtra("userLng", 0F)
        val userAlt = intent.getFloatExtra("userAlt", 0F)
        val operator = intent.getStringExtra("operator")
        val name = intent.getStringExtra("name")
        val norad = intent.getIntExtra("norad", 0)
        val user = intent.getStringExtra("user")

        val api = SatApiService.SatApi
        runBlocking {
            launch {
                try {
                    data = api.retrofitService.getSatellitePosition(
                        norad,
                        userLat,
                        userLng,
                        userAlt
                    )
                } catch (e: IOException) {
                    finish()
                }
            }
        }

        val parsedData = JSONObject(data)

        val lng = parsedData
            .getJSONArray("positions")
            .getJSONObject(0)
            .getDouble("satlongitude")
            .toFloat()

        val lat = parsedData
            .getJSONArray("positions")
            .getJSONObject(0)
            .getDouble("satlatitude")
            .toFloat()

        val azi = parsedData
            .getJSONArray("positions")
            .getJSONObject(0)
            .getDouble("azimuth")
            .toFloat()

        val elev = parsedData
            .getJSONArray("positions")
            .getJSONObject(0)
            .getDouble("elevation")
            .toFloat()

        val track = Intent(this, CameraTracking::class.java)

        button_locate.setOnClickListener {
            track.putExtra("azimuth", azi)
            track.putExtra("elevation", elev)

            startActivity(track)
        }

        operator_text.text = operator
        name_text.text = name
        norad_text.text = norad.toString()
        user_text.text = user
        lng_text.text = lng.toString()
        lat_text.text = lat.toString()
        azi_text.text = azi.toString()
        elev_text.text = elev.toString()
    }
}