package com.example.calorieapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorieapp.remoteAPIs.CalorieNinjasAPICalls
import com.example.calorieapp.remoteAPIs.CalorieNinjasResponseModel
import com.example.calorieapp.remoteAPIs.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalorieAppViewModel : ViewModel() {
    /** service for calorie ninjas API */
    private val calorieNinjasAPIService = RetrofitInstance.calorieNinjasAPI

    /** MutableStateFlow to hold the current response */
    private val _calorieNinjasResponse = MutableStateFlow<CalorieNinjasResponseModel?>(null)
    val calorieNinjasResponse: StateFlow<CalorieNinjasResponseModel?>
        get() = _calorieNinjasResponse

    /** MutableStateFlow to hold error messages */
    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    /** MutableStateFlow to hold loading state */
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading


    /**
     * Function to perform the network call to calorie ninjas API
     */
    fun fetchCalories(searchQuery: String) {
        _loading.value = true // set loading true while making network call

        /**
         * scope coroutine to the view model so it is auto cancelled
         * when the view model lifecycle ends
         */
        viewModelScope.launch {
            try {
                /** input/output on the IO thread */
                val searchResult: CalorieNinjasResponseModel =
                    withContext(Dispatchers.IO) {
                        calorieNinjasAPIService.getSearchResponse(searchQuery)
                    }
                _calorieNinjasResponse.value = searchResult // set result in view model
            } catch (e: Exception) {
                _errorMessage.value = "Error getting calories: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    var currentMeal by mutableStateOf(InProgressMeal())
        private set

    /** autofill ingredient per-gram values based on API call */
    fun autofillIngredient(ingredient: MealIngredient) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ingredient.autofill(calorieNinjasAPIService)
            }
        }
    }
}

/** Class to store the current meal being logged */
class InProgressMeal() {
    var mealName by mutableStateOf("")
    var mealType by mutableStateOf("")
    var ingredients = mutableStateListOf<MealIngredient>() // initially empty

    val totalWeight: Double get() = ingredients.sumOf { it.weight.toDoubleOrNull() ?: 0.0 }
    val totalCalories: Double get() = ingredients.sumOf { it.totalKcal.toDoubleOrNull() ?: 0.0 }
    val totalProtein: Double get() = ingredients.sumOf { it.totalKcal.toDoubleOrNull() ?: 0.0 }
    val totalFats: Double get() = ingredients.sumOf { it.totalKcal.toDoubleOrNull() ?: 0.0 }
    val totalCarbs: Double get() = ingredients.sumOf { it.totalKcal.toDoubleOrNull() ?: 0.0 }

    fun isValid(): Boolean {
        if(mealName.isNotEmpty() && mealType.isNotEmpty() && ingredients.isNotEmpty()) {
            return (!(ingredients.map { it.isValid() }.contains(false)))
        }
        return false
    }

    fun reset() {
        mealName = ""
        mealType = ""
        ingredients.clear()
    }
}

/** Class to hold ingredients and relevant info */
class MealIngredient() {
    var name by mutableStateOf("")
    var weight by mutableStateOf("")
    var kcalPer100g by mutableStateOf("")
    var fatsPer100g by mutableStateOf("")
    var proteinPer100g by mutableStateOf("")
    var carbsPer100g by mutableStateOf("")

    /** computed properties */
    private fun safeScaleByWeight(value: String): String {
        weight.toDoubleOrNull()?.let { weightDouble ->
            value.toDoubleOrNull()?.let { valueDouble ->
                return "%.2f".format(weightDouble / 100.0 * valueDouble)
            }
        }
        return "-"
    }
    val totalKcal: String get() = safeScaleByWeight(kcalPer100g)
    val totalFats: String get() = safeScaleByWeight(fatsPer100g)
    val totalProtein: String get() = safeScaleByWeight(proteinPer100g)
    val totalCarbs: String get() = safeScaleByWeight(carbsPer100g)

    fun isValid(): Boolean {
        return name.isNotEmpty() &&
            totalKcal != "-" &&
            totalFats != "-" &&
            totalProtein != "-" &&
            totalCarbs != "-"
    }

    /** API call stuff */
    // Loading state to track whether autofill request is in progress
    var isLoading by mutableStateOf(false)

    /**
     * Autofill function to fetch data from the API.
     * This updates the loading state before and after the network call.
     */
    suspend fun autofill(apiService: CalorieNinjasAPICalls) {
        try {
            isLoading = true // Set loading to true when request starts
            val response =
                apiService.getSearchResponse(name) // Query the API based on the ingredient name

            response.items.firstOrNull()?.let { item ->
                val servingSize = item.servingSizeGrams
                kcalPer100g = "%.2f".format(item.caloriesPerServe / (servingSize / 100.0))
                fatsPer100g = "%.2f".format(item.fatTotalPerServe / (servingSize / 100.0))
                proteinPer100g = "%.2f".format(item.proteinPerServe / (servingSize / 100.0))
                carbsPer100g = "%.2f".format(item.carbsPerServe / (servingSize / 100.0))
            }
        } catch (e: Exception) {
            Log.e("API", "Error fetching data: ${e.message}") // Handle the error here
        } finally {
            isLoading = false // Set loading to false when request finishes (either success or failure)
        }
    }
}


