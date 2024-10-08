package com.example.calorieapp

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun LogScreen() {
    val context = LocalContext.current
    InsetContent {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                InputEditor { name -> addToDatabase(context, name) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputEditor(onAddClicked: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Enter new meal") },
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    )

    Button(onClick = {
        if (text.isNotEmpty()) {
            onAddClicked(text)
            text = ""
        }
    }) {
        Text(text = "Add to Database")
    }
}

fun addToDatabase(context: Context, mealName: String) {
    val db = MealDBInstance().getDatabase(context)
    val newMeal = Meal(name = mealName, calories = 1)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            db.mealDAO().insert(newMeal)
            println("Meal inserted: $newMeal")
        } catch (e: Exception) {
            println("Error inserting meal: ${e.message}")
        }
    }
}
