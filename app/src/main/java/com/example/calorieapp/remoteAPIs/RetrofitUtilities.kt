package com.example.calorieapp.remoteAPIs

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    /**
     * Calorie ninjas retrofit
     */
    private val calorieNinjasHitRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.calorieninjas.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val calorieNinjasAPI: CalorieNinjasAPICalls by lazy {
        calorieNinjasHitRetrofit.create(CalorieNinjasAPICalls::class.java)
    }

    /**
     * Pixabay retrofit (from lectures)
     */
    private val pixabayRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://pixabay.com/") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val pixabayAPI: PixabayAPICalls by lazy {
        pixabayRetrofit.create(PixabayAPICalls::class.java)
    }
}