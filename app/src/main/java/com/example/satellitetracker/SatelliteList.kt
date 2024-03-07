package com.example.satellitetracker

import DatabaseHelper
import ListItemAdapter
import android.app.Activity
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.satellitetracker.models.ListItem

class SatelliteList : Activity() {

    private lateinit var adapter: ListItemAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var search: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.satellite_list)

        recycler = findViewById(R.id.sat_list)
        search = findViewById(R.id.search_bar)

        val dbHelper = DatabaseHelper(this)
        dbHelper.openDatabase()

        val satellites: MutableList<ListItem> = dbHelper.getSatellites()

        val sat_info_intent = Intent(this, SatelliteInfo::class.java)

        adapter = ListItemAdapter(satellites)
        adapter.setOnItemClickListener(object : ListItemAdapter.onItemClickListener {
            override fun onItemClick(model: ListItem) {
                sat_info_intent.putExtra("operator", model.operator)
                sat_info_intent.putExtra("name", model.name)
                sat_info_intent.putExtra("norad", model.norad)
                sat_info_intent.putExtra("user", model.user)
                sat_info_intent.putExtra("userLat", intent.getFloatExtra("userLat", 0F))
                sat_info_intent.putExtra("userLng", intent.getFloatExtra("userLng", 0F))
                sat_info_intent.putExtra("userAlt", intent.getFloatExtra("userAlt", 0F))

                if (checkNetworkConnection())
                    startActivity(sat_info_intent)
                else
                    Toast.makeText(this@SatelliteList, "Jesteś offline", Toast.LENGTH_SHORT).show()
            }
        })
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)

        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter.filter(query)

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)

                return true
            }
        })
    }

    private fun checkNetworkConnection(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals(
                    "WIFI",
                    ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedWifi = true
            if (ni.typeName.equals(
                    "MOBILE",
                    ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
    }
}