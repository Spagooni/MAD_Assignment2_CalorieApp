package com.example.calorieapp

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody

class CalorieAppViewModel : ViewModel() {

    // MutableStateFlow to hold the image
    private val _imageBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val imageBitmap: StateFlow<android.graphics.Bitmap?> get() = _imageBitmap

    // MutableStateFlow to hold error messages
    private val _errorMessage = MutableStateFlow<String?>("")
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // MutableStateFlow to hold loading state
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading

    private val apiService = RetrofitInstance.api

    // Function to perform the network call
    fun fetchImage(apiKey: String, searchQuery: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val searchResult = withContext(Dispatchers.IO) {
                    apiService.getSearchResponse(apiKey, searchQuery)
                }

                val imageUrl = searchResult.hits.firstOrNull()?.largeImageURL ?: ""

                val imageResponse: ResponseBody = withContext(Dispatchers.IO) {
                    apiService.getImage(imageUrl)
                }

                val bitmap = BitmapFactory.decodeStream(imageResponse.byteStream())
                _imageBitmap.value = bitmap
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error loading image: ${e.message}"
                _imageBitmap.value = null
            } finally {
                _loading.value = false
            }
        }
    }
}