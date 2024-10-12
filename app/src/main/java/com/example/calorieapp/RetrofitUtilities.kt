package com.example.calorieapp

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

data class ResponseModel(
    @SerializedName("hits")
    val hits: List<Hit>
)
data class Hit(
    @SerializedName("largeImageURL")
    val largeImageURL: String
)

interface RemoteAPICalls {
    @GET("/api/")
    suspend fun getSearchResponse(
        @Query("key") apiKey: String,
        @Query("q") searchValue: String
    ): ResponseModel

    @GET
    suspend fun getImage(@Url imageUrl: String): ResponseBody
}

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://pixabay.com/") // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: RemoteAPICalls by lazy {
        retrofit.create(RemoteAPICalls::class.java)
    }
}