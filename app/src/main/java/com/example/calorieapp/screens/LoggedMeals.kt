package com.example.calorieapp.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.runtime.MutableState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.calorieapp.mealsDatabase.Meal
import com.example.calorieapp.general.InsetContent
import com.example.calorieapp.mealsDatabase.MealDatabase
import com.example.calorieapp.mealsDatabase.byteArrayToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection

@Composable
fun LoggedMealsScreen() {
    val context = LocalContext.current
    val mealList = remember { mutableStateOf<List<Meal>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val db = MealDatabase.getDatabase(context)
            mealList.value = db.mealDAO().getAll()
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error fetching contacts", e)
        }
    }
    
    InsetContent {
        LoggedMealsScreen_Portrait(mealList = mealList)
    }
}


@Composable
fun LoggedMealsScreen_Portrait(mealList: MutableState<List<Meal>>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(20.dp),
            text = "Logged Meals",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(mealList.value) {
                LoggedMealCard(meal = it)
            }
        }
    }
}

@Composable
fun LoggedMealCard(meal: Meal) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                meal.photo?.let { byteArray ->
                    val bitmap = byteArrayToBitmap(byteArray)
                    Text(text = "From Database: ",
                        fontSize = 10.sp)
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Meal Image from Database",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 16.dp)
                    )
                }

                meal.photoUrl?.let { uriString ->
                    val photoUri = Uri.parse(uriString)
                    Text(text = "From Firebase: ",
                        fontSize = 10.sp)
                    DisplayImageFromUri(photoUri)
                }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Name: ${meal.name}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = " ${meal.date}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                Text(
                    text = "Meal Type: ${meal.mealType}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Calories: ${meal.calories} kCal",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "Ingredients: ${meal.ingredients}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "Total Calories: ${meal.calories}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "Total Carbs: ${meal.totalCarbs}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "Total Protein: ${meal.totalProtein}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
                Text(
                    text = "Total Fat: ${meal.totalFat}g",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray
                    )
                )
            }
        }
    }
}


@Composable
fun DisplayImageFromUri(photoURI: Uri?) {
    val context = LocalContext.current
    val imageState = remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoURI) {
        if (photoURI != null) {
            try {
                val bitmap = loadImageFromUri(context, photoURI)
                imageState.value = bitmap
            } catch (e: Exception) {
                Log.e("ImageLoading", "Error loading image", e)
            }
        }
    }

    imageState.value?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Loaded Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .padding(end = 16.dp)
        )
    } ?: Text(text = "Image not found", modifier = Modifier.size(80.dp))
}

suspend fun loadImageFromUri(context: Context, uri: Uri): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            if (uri.scheme == "https" || uri.scheme == "http") {
                val connection = java.net.URL(uri.toString()).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()

                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } else {
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            Log.e("ImageError", "Error decoding image", e)
            null
        }
    }
}

