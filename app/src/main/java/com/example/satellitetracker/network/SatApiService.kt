package com.example.satellitetracker.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private val BASE_URL = "https://api.n2yo.com/rest/v1/satellite/"
private val API_KEY = "#"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

class SatApiService {

    interface SatApiService {
        @GET("tle/{id}?apiKey=")
        suspend fun getSatellite(@Path("id") satId: Int): String

        @GET("positions/{id}/{observer_lat}/{observer_lng}/{observer_alt}/1/&apiKey=")
        suspend fun getSatellitePosition(
            @Path("id") satId: Int,
            @Path("observer_lat") latitude: Float,
            @Path("observer_lng") longitude: Float,
            @Path("observer_alt") altitude: Float,
        ): String
    }

    object SatApi {
        val retrofitService: SatApiService by lazy {
            retrofit.create(SatApiService::class.java)
        }
    }
}