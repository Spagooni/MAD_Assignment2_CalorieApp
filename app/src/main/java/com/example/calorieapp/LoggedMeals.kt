package com.example.calorieapp

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.navigation.NavHostController
import androidx.compose.foundation.border
import androidx.compose.ui.unit.Dp

@Composable
fun LoggedMealsScreen(navController: NavHostController) {
    val context = LocalContext.current
    InsetContent {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Logged Meals",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                DisplaySavedMealsScreen()
            }
        }
    }
}

@Composable
fun DisplaySavedMealsScreen() {
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

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        items(mealList.value) { meal ->
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    meal.photo?.let { byteArray ->
                        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Meal Image",
                            modifier = Modifier
                                .size(80.dp)
                                .padding(end = 16.dp)
                        )
                    }
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Name: ${meal.name}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Calories: ${meal.calories}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    }
}
