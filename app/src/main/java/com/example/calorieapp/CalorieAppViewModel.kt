package com.example.calorieapp

import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
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
import okhttp3.ResponseBody

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
}