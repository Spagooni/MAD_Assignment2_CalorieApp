package com.example.calorieapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorieapp.remoteAPIs.CalorieNinjasResponseModel
import com.example.calorieapp.remoteAPIs.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalorieAppViewModel : ViewModel() {
    /** MutableStateFlow to hold the current response */
    private val _calorieNinjasResponse = MutableStateFlow<CalorieNinjasResponseModel?>(null)
    val calorieNinjasResponse: StateFlow<CalorieNinjasResponseModel?>
        get() = _calorieNinjasResponse
    /** Set calorie ninjas response to null & reset loading and error flows */
    fun resetCalorieNinjasResponse() {
        _calorieNinjasResponse.value = null
        _errorMessage.value = ""
        _loading.value = false
    }

    /** MutableStateFlow to hold error messages */
    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    /** MutableStateFlow to hold loading state */
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    /** service for calorie ninjas API */
    private val calorieNinjasAPIService = RetrofitInstance.calorieNinjasAPI

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
    fun resetCurrentMeal() {
        currentMeal = InProgressMeal()
    }
}


/** Class to store the current meal being logged */
class InProgressMeal {
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
    var kcalPerGram by mutableStateOf("")
    var fatsPerGram by mutableStateOf("")
    var proteinPerGram by mutableStateOf("")
    var carbsPerGram by mutableStateOf("")

    /** computed properties */
    private fun safeScaleByWeight(value: String): String {
        weight.toDoubleOrNull()?.let { weightDouble ->
            value.toDoubleOrNull()?.let { valueDouble ->
                return "${weightDouble * valueDouble}"
            }
        }
        return "-"
    }
    val totalKcal: String get() = safeScaleByWeight(kcalPerGram)
    val totalFats: String get() = safeScaleByWeight(fatsPerGram)
    val totalProtein: String get() = safeScaleByWeight(proteinPerGram)
    val totalCarbs: String get() = safeScaleByWeight(carbsPerGram)

    fun isValid(): Boolean {
        return name.isNotEmpty() &&
            totalKcal != "-" &&
            totalFats != "-" &&
            totalProtein != "-" &&
            totalCarbs != "-"
    }

    /** autofill ingredient per-gram values based on API call */
    fun autofill() {
        // TODO replace with API call later
        kcalPerGram = "1"
        fatsPerGram = "1"
        proteinPerGram = "1"
        carbsPerGram = "1"
    }
}
