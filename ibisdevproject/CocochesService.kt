package com.victoriaBermudez.ibisdevproject

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface CocochesService {

    @GET("car_listing_presentation")
    fun getAllCars(@Query("list_length") listLength: Int): Call<CarListing>




}
