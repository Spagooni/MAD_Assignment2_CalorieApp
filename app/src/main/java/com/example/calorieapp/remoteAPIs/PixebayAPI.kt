package com.example.calorieapp.remoteAPIs

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

data class PixabayResponseModel(
    @SerializedName("hits")
    val hits: List<PixabayHit>
)
data class PixabayHit(
    @SerializedName("largeImageURL")
    val largeImageURL: String
)

interface PixabayAPICalls {
    @GET("/api/")
    suspend fun getSearchResponse(
        @Query("key") apiKey: String,
        @Query("q") searchValue: String
    ): PixabayResponseModel

    @GET
    suspend fun getImage(@Url imageUrl: String): ResponseBody
}