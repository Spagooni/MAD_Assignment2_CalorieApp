package com.example.calorieapp.screens.logActivityScreen

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.calorieapp.mealsDatabase.Meal
import com.example.calorieapp.general.InsetContent
import com.example.calorieapp.mealsDatabase.MealDatabase
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorieapp.CalorieAppViewModel
import com.example.calorieapp.InProgressMeal
import com.example.calorieapp.MealIngredient
import com.example.calorieapp.mealsDatabase.bitmapToByteArray
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun LogMealScreen(shvm: CalorieAppViewModel) {
    val orientation = LocalConfiguration.current.orientation
    InsetContent {
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                LogMealScreen_Portrait(shvm)
            else ->
                LogMealScreen_Portrait(shvm)
        }
    }
}

@Preview
@Composable
fun LogMealScreen_Preview() {
    val shvm = viewModel<CalorieAppViewModel>()
    shvm.insertingIntoDB = true
    shvm.currentMeal.ingredients.add(MealIngredient()) // add empty ingredient
    LogMealScreen(shvm)
}

@Composable
fun LogMealScreen_Portrait(shvm: CalorieAppViewModel) {
    val context = LocalContext.current
    val meal = shvm.currentMeal

    // for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        meal.photo = bitmap
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Log a Meal",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = meal.mealName,
                onValueChange = { meal.mealName = it },
                label = { Text("Meal Name") },
                modifier = Modifier
                    .weight(1f)
            )
            TextField(
                value = meal.mealType,
                onValueChange = { meal.mealType = it },
                label = { Text("Meal Type (optional)") },
                modifier = Modifier
                    .weight(1f)
            )
        }

        Text(modifier = Modifier.padding(horizontal = 10.dp),
            text = "TOTAL: " +
                    "weight: ${meal.totalWeight}, " +
                    "Calories: ${meal.totalCalories}, " +
                    "Fats: ${meal.totalFats}, " +
                    "protein: ${meal.totalProtein}, " +
                    "carbs: ${meal.totalCarbs}",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 90000.dp)
        ) {
            items(meal.ingredients) { ingredient ->
                IngredientInputCard(
                    ingredient = ingredient,
                    onAutofill = { shvm.autofillIngredient(ingredient) },
                    onDelete = { meal.ingredients.remove(ingredient) },
                    loading = ingredient.isLoading,
                )
            }
        }


        Column() {
            Button(
                onClick = { meal.ingredients.add(MealIngredient()) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add Ingredient ") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { cameraLauncher.launch() },
                    modifier = Modifier.weight(1f)
                ) { Text("Take Picture") }

                Button(
                    onClick = {
                        if (meal.isValid()) {
                            val mealName = meal.mealName // snapshot in case it changes
                            val calories = meal.totalCalories
                            shvm.saveMealToDB(successToast = {
                                Toast.makeText(context,
                                    "Saved meal: $mealName, ($calories kcal)",
                                    Toast.LENGTH_SHORT).show()
                            })
                        } else {
                            Toast.makeText(context,
                                "Provide meal name and a valid ingredient before saving",
                                Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !shvm.insertingIntoDB,
                ) {
                    if (shvm.insertingIntoDB) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxHeight().size(25.dp))
                    } else {
                        Text("Add to Database")
                    }
                }
            }
        }

        meal.photo?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(vertical = 16.dp)
            )
        }

        shvm.imageUploadProgress?.let { progress ->
            Text(text = "Uploading image. Progress: ${(progress * 100).toInt()}%")
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            )
        }

        Spacer(modifier = Modifier.size(50.dp)) // space at the bottom of the scroll
    }
}
