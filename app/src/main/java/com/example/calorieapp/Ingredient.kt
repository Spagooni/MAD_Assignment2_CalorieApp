package com.example.calorieapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class Ingredient(
    var weight: String = "",
    var name: String = "",
    var caloriesPerGram: String = "",
    var proteinPerGram: String = "",
    var fatPerGram: String = "",
    var carbsPerGram: String = ""
) {
    var weightState by mutableStateOf(weight)
    var nameState by mutableStateOf(name)
    var caloriesPerGramState by mutableStateOf(caloriesPerGram)
    var proteinPerGramState by mutableStateOf(proteinPerGram)
    var fatPerGramState by mutableStateOf(fatPerGram)
    var carbsPerGramState by mutableStateOf(carbsPerGram)
}