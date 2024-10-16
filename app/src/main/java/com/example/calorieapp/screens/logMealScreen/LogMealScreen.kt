package com.example.calorieapp.screens.logMealScreen

import android.app.DatePickerDialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.calorieapp.general.InsetContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorieapp.CalorieAppViewModel
import com.example.calorieapp.MealIngredient
import com.example.calorieapp.remoteAPIs.isOnline
import java.time.LocalDate

@Composable
fun LogMealScreen(shvm: CalorieAppViewModel) {
    val orientation = LocalConfiguration.current.orientation
    InsetContent {
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                LogMealScreen_Portrait(shvm)
            else ->
                LogMealScreen_Landscape(shvm)
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
/** purely for readability */
private fun LogMealScreenHeader(shvm: CalorieAppViewModel) {
    val context = LocalContext.current
    val meal = shvm.currentMeal

    var showDatePicker by remember { mutableStateOf(false) }
    // Show the DatePickerDialog when requested
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                // Handle the date selection
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                meal.date = selectedDate // Update the selected date
                showDatePicker = false // Close the dialog
            },
            meal.date.year,
            meal.date.monthValue - 1,
            meal.date.dayOfMonth
        ).show()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(end = 20.dp),
                text = "Date: ${shvm.currentMeal.dateString}",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Button(onClick = { showDatePicker = true }) {
                Text(text = "Change Date")
            }
        }

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

        Text(
            modifier = Modifier.padding(horizontal = 10.dp),
            text = "TOTAL: " +
                    "weight: ${meal.totalWeight} g, " +
                    "Calories: ${meal.totalCalories.toInt()} kcal,\n" +
                    "Fats: ${meal.totalFats.toInt()} g, " +
                    "protein: ${meal.totalProtein.toInt()} g, " +
                    "carbs: ${meal.totalCarbs.toInt()} g",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White
        )
    }
}

@Composable
/** bottom buttons, camera launcher and loading bars */
private fun LogMealScreenFooter(shvm: CalorieAppViewModel) {
    val context = LocalContext.current
    val meal = shvm.currentMeal

    // for camera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        meal.photo = bitmap
    }

    // Gallery launcher for getting images frm gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Convert the selected URI to a Bitmap and set it to the meal's photo
            // val bitmap = loadImageFromUri(context, it) // Create a function to load bitmap from URI
            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream?.let {
                // Decode the input stream into a Bitmap
                val bitmap = BitmapFactory.decodeStream(it)
                meal.photo = bitmap
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column {
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
                    onClick = { galleryLauncher.launch("image/*") }, // Launch the gallery picker
                    modifier = Modifier.weight(1f)
                ) { Text("Select from Gallery") }
            }
            Button(
                onClick = {
                    val isOnline = isOnline(context)
                    if (meal.isValid()) {
                        val mealName = meal.mealName // snapshot in case it changes
                        val calories = meal.totalCalories.toInt()
                        if (!isOnline) {
                            Toast.makeText(
                                context,
                                "Unable to upload to firebase, check internet connection...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        shvm.saveMealToDB(successToast = {
                            Toast.makeText(
                                context,
                                "Saved meal: $mealName, ($calories kcal)",
                                Toast.LENGTH_SHORT
                            ).show()
                        }, isOnline = isOnline)
                    } else {
                        Toast.makeText(
                            context,
                            "Provide meal name and a valid ingredient before saving",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !shvm.insertingIntoDB,
            ) {
                if (shvm.insertingIntoDB) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxHeight()
                            .size(25.dp)
                    )
                } else {
                    Text("Add to Database")
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            )
        }

        Spacer(modifier = Modifier.size(50.dp)) // space at the bottom of the scroll
    }
}

@Composable
fun IngredientsColumn(shvm: CalorieAppViewModel) {
    val meal = shvm.currentMeal
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        meal.ingredients.forEach { ingredient ->
            IngredientInputCard(
                ingredient = ingredient,
                onAutofill = { shvm.autofillIngredient(ingredient,
                    failToast = {
                        Toast.makeText(
                            context,
                            "Unable to autofill, check internet connection...",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    nothingReturnedToast = {
                        Toast.makeText(
                            context,
                            "No information for autofill, please enter manually",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                ) },
                onDelete = { meal.ingredients.remove(ingredient) },
                loading = ingredient.isLoading,
            )
        }
    }
}

@Composable
fun LogMealScreen_Portrait(shvm: CalorieAppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            modifier = Modifier.padding(top = 20.dp),
            text = "Log a Meal",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,

                ),
            // color = Color.White,
        )
        LogMealScreenHeader(shvm)
        IngredientsColumn(shvm)
        LogMealScreenFooter(shvm)
    }
}

@Composable
fun LogMealScreen_Landscape(shvm: CalorieAppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            modifier = Modifier.padding(top = 5.dp),
            text = "Log a Meal",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,

                ),
            // color = Color.White,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {

                LogMealScreenHeader(shvm)
                LogMealScreenFooter(shvm)
            }

            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                IngredientsColumn(shvm)
            }
        }
    }
}
