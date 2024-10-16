package com.example.calorieapp.screens

import android.content.Context
import android.content.res.Configuration
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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

const val dailyCaloriesLimit = 2000f
val sampleMeal = Meal(
    name = "test name",
    mealType = "lunch",
    date = "1/12/2000",
    ingredients = "ham, cheese",
    calories = 100,
    totalWeight = 100,
    totalProtein = 100,
    totalCarbs = 100,
    totalFat = 100,
    photo = null,
    photoUrl = null
)
val imageHeight = 80.dp

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
    /** Group meals by date */
    val groupedMeals = mealList.value.groupBy { meal ->
        meal.date
    }.toSortedMap(reverseOrder())
    
    InsetContent {
        LoggedMealsScreen_Portrait(groupedMeals = groupedMeals)
    }
}

@Preview
@Composable
fun LoggedMealsScreen_Preview() {
    InsetContent {
        /** Group meals by date */
        val groupedMeals = listOf(sampleMeal).groupBy { meal ->
            meal.date
        }
        LoggedMealsScreen_Portrait(groupedMeals = groupedMeals)
    }
}


@Composable
fun LoggedMealsScreen_Portrait(
    groupedMeals: Map<String, List<Meal>>,
) {
    val orientation = LocalConfiguration.current.orientation
    val isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT)
    val imageCache = remember { mutableMapOf<Uri, Bitmap?>() }
    val context = LocalContext.current

    // For each meal, fetch its image and save to the cache
    LaunchedEffect(groupedMeals) {
        Log.d("ImageLoading", "pre-fetching now, groups: ${groupedMeals.size}")
        groupedMeals.values.flatten().forEach { meal ->
            Log.d("ImageLoading", "pre-fetching meal: ${meal.name}")
            meal.photoUrl?.let { uriString ->
                Log.d("ImageLoading", "pre-fetching for uri: uriString")
                val photoURI = Uri.parse(uriString)
                // Load the image and cache it
                try {
                    val bitmap = loadImageFromUri(context, photoURI)
                    imageCache[photoURI] = bitmap // Cache the loaded image
                } catch (e: Exception) {
                    Log.e("ImageLoading", "Error pre-loading image $e")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(vertical = if (isPortrait) 20.dp else 5.dp),
            text = "Logged Meals",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White
        )
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Loop through each date group
            groupedMeals.forEach { (date, mealsForDate) ->
                val totalCalories = mealsForDate.sumOf { it.calories }
                val caloriesPercent =
                    "%.1f".format(100 * totalCalories / dailyCaloriesLimit)
                item {
                    Column {
                        Text(text = date,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold)
                        )// Header for the date

                        Text(text = "Total Calories: $totalCalories kcal\n" +
                                    "($caloriesPercent% of daily total = " +
                                    "${dailyCaloriesLimit.toInt()} kcal)"
                        )
                    }
                }
                items(mealsForDate) { meal -> // Meals for that date
                    LoggedMealCard(meal = meal, isPortrait = isPortrait, imageCache=imageCache)
                }
                item { HorizontalDivider(color = Color.Black) }
            }
            item { Spacer(modifier = Modifier.size(75.dp))}
        }
    }
}

@Preview
@Composable
fun LoggedMealCard_Preview() {
    val imageCache = remember { mutableMapOf<Uri, Bitmap?>() }
    InsetContent {
        LoggedMealCard(meal = sampleMeal, isPortrait = true, imageCache = imageCache)
    }
}

@Composable
fun LoggedMealCard(meal: Meal, isPortrait: Boolean, imageCache: MutableMap<Uri, Bitmap?>) {
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
            if (isPortrait) {
                VerticalPhotos(meal = meal, imageCache)
            } else {
                HorizontalPhotos(meal = meal, imageCache)
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
                    text = "Total Calories: ${meal.calories} kCal",
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
private fun HorizontalPhotos(meal: Meal, imageCache: MutableMap<Uri, Bitmap?>) {
    Row {
        meal.photo?.let { byteArray ->
            val bitmap = byteArrayToBitmap(byteArray)
            Text(text = "From Database: ",
                fontSize = 10.sp)
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Meal Image from Database",
                modifier = Modifier
                    .size(imageHeight)
                    .padding(end = 16.dp)
            )
        }

        meal.photoUrl?.let { uriString ->
            val photoUri = Uri.parse(uriString)
            Text(text = "From Firebase: ",
                fontSize = 10.sp)
            DisplayImageFromUri(photoUri, imageCache = imageCache)
        }
    }
}

@Composable
private fun VerticalPhotos(meal: Meal, imageCache: MutableMap<Uri, Bitmap?>) {
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
                    .size(imageHeight)
                    .padding(end = 16.dp)
            )
        }

        meal.photoUrl?.let { uriString ->
            val photoUri = Uri.parse(uriString)
            Text(text = "From Firebase: ",
                fontSize = 10.sp)
            DisplayImageFromUri(photoUri, imageCache = imageCache)
        }
    }
}


@Composable
fun DisplayImageFromUri(photoURI: Uri, imageCache: MutableMap<Uri, Bitmap?>) {
    val context = LocalContext.current
    val imageState = remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    Log.d("ImageLoading", "start, isLoading = true")

    LaunchedEffect(photoURI) {
        Log.d("ImageLoading", "launchedEffect, isLoading = true")
        if (imageCache.containsKey(photoURI)) {
            // If the image is already in the cache, use it
            imageState.value = imageCache[photoURI]
        } else {
            isLoading = true
            Log.e("ImageLoading", "cache miss!, size = ${imageCache.size}")
            // Otherwise, load the image and cache it
            try {
                val bitmap = loadImageFromUri(context, photoURI)
                imageState.value = bitmap
                imageCache[photoURI] = bitmap // Cache the loaded image
                isLoading = false
                Log.d("ImageLoading", "image? returned, isLoading=false")
            } catch (e: Exception) {
                Log.e("ImageLoading", "Error loading image $e")
                isLoading = false
                Log.d("ImageLoading", "caught error, isLoading=false")
            }
        }
    }

    imageState.value?.let { bitmap -> // if image returned and available, use that
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Loaded Image",
            // contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(imageHeight)
                .padding(end = 16.dp)
        )
    } ?:
    if (isLoading) {
        Box(
            modifier = Modifier.size(imageHeight),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxHeight()
                    .size(25.dp)
            )
        }
    } else {
        Text(
            "Unable to load image from firebase, check internet connection...",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.size(width = 150.dp, height = imageHeight) // use same height to prevent scrolling issues
        )
    }
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
            Log.e("ImageError", "Error decoding image $e")
            null
        }
    }
}

