package com.example.calorieapp.remoteAPIs

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/** note this really shouldn't be committed to a repo but who cares */
private const val calorieNinjasAPIKey = "TU2RQkRaruaMLT/87VmgKw==VbcUMVCAiKIyzUSg"

data class CalorieNinjasResponseModel(
    @SerializedName("items")
    val items: List<CalorieNinjasItem>
)
data class CalorieNinjasItem(
    @SerializedName("name") /** Name of item */
    val name: String,

    // serving size and calories
    @SerializedName("serving_size_g") /** serving size in grams */
    val servingSizeGrams: Float,

    @SerializedName("calories") /** kcal per serve (kj) */
    val caloriesPerServe: Float,

    // Macronutrients
    @SerializedName("fat_total_g") /** Total fats per serve in grams */
    val fatTotalPerServe: Float,

    @SerializedName("protein_g") /** Protein per serve in grams */
    val proteinPerServe: Float,

    @SerializedName("carbohydrates_total_g") /** Total carbohydrates per serve in grams */
    val carbsPerServe: Float,
)

interface CalorieNinjasAPICalls {
    @Headers("X-Api-Key: $calorieNinjasAPIKey") // Static header for all requests
    @GET("/v1/nutrition")
    suspend fun getSearchResponse(
        @Query("query") query: String
    ): CalorieNinjasResponseModel
}