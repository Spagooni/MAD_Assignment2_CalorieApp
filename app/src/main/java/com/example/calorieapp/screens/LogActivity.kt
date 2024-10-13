package com.example.calorieapp.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.example.calorieapp.mealsDatabase.Meal
import com.example.calorieapp.general.InsetContent
import com.example.calorieapp.mealsDatabase.MealDatabase
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.unit.dp
import com.example.calorieapp.Ingredient
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun LogScreen() {
    val context = LocalContext.current


    var mealName by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf(listOf<Ingredient>()) }

    var totalCalories by remember { mutableStateOf(0) }
    var totalProtein by remember { mutableStateOf(0) }
    var totalFat by remember { mutableStateOf(0) }
    var totalCarbs by remember { mutableStateOf(0) }

    InsetContent {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log a Meal",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = mealName,
                        onValueChange = { mealName = it },
                        label = { Text("Meal Name") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    TextField(
                        value = mealType,
                        onValueChange = { mealType = it },
                        label = { Text("Meal Type") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                fun calculateTotals() {
                    totalCalories = ingredients.sumOf { it.weight.toIntOrNull()?.times(it.caloriesPerGram.toIntOrNull() ?: 0) ?: 0 }
                    totalProtein = ingredients.sumOf { it.weight.toIntOrNull()?.times(it.proteinPerGram.toIntOrNull() ?: 0) ?: 0 }
                    totalFat = ingredients.sumOf { it.weight.toIntOrNull()?.times(it.fatPerGram.toIntOrNull() ?: 0) ?: 0 }
                    totalCarbs = ingredients.sumOf { it.weight.toIntOrNull()?.times(it.carbsPerGram.toIntOrNull() ?: 0) ?: 0 }
                }

                Text(
                    text = "TOTAL: kcal: $totalCalories, fats: $totalFat, protein: $totalProtein, carbs: $totalCarbs",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 475.dp)
                        .padding(top = 16.dp)
                ) {
                    items(ingredients) { ingredient ->
                        IngredientInput(
                            ingredient = ingredient,
                            onIngredientChange = { updatedIngredient ->
                                ingredients = ingredients.map {
                                    if (it == ingredient) updatedIngredient else it
                                }
                                calculateTotals()
                            },
                            onAutofill = { },
                            onDelete = {
                                ingredients = ingredients.toMutableList().apply { remove(ingredient) }
                                calculateTotals()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        ingredients = ingredients + Ingredient()
                        calculateTotals()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Add Ingredient ")
                }

                var mealPhoto by remember { mutableStateOf<Bitmap?>(null) }

                val cameraLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicturePreview()
                ) { bitmap: Bitmap? ->
                    mealPhoto = bitmap
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { cameraLauncher.launch() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    ) {
                        Text("Take Picture")
                    }

                    Button(
                        onClick = {
                            if (mealName.isNotEmpty() && mealType.isNotEmpty() && ingredients.isNotEmpty() && mealPhoto != null) {
                                addToDatabase(context, mealName, mealType, ingredients, mealPhoto) { progress -> }
                                mealName = ""
                                mealType = ""
                                mealPhoto = null
                                ingredients = listOf()
                            } else {
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    ) {
                        Text("Add to Database")
                    }
                }

                mealPhoto?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(vertical = 16.dp)
                    )
                }
            }
        }
    }
}



@Composable
fun IngredientInput(
    ingredient: Ingredient,
    onIngredientChange: (Ingredient) -> Unit,
    onAutofill: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredient.weight,
                    onValueChange = { onIngredientChange(ingredient.copy(weight = it)) },
                    label = { Text("Weight (g)") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = ingredient.name,
                    onValueChange = { onIngredientChange(ingredient.copy(name = it)) },
                    label = { Text("Ingredient Name") },
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 8.dp)
                )
            }

            val totalCalories = (ingredient.weight.toIntOrNull() ?: 0) * (ingredient.caloriesPerGram.toIntOrNull() ?: 0)
            val totalProtein = (ingredient.weight.toIntOrNull() ?: 0) * (ingredient.proteinPerGram.toIntOrNull() ?: 0)
            val totalFat = (ingredient.weight.toIntOrNull() ?: 0) * (ingredient.fatPerGram.toIntOrNull() ?: 0)
            val totalCarbs = (ingredient.weight.toIntOrNull() ?: 0) * (ingredient.carbsPerGram.toIntOrNull() ?: 0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredient.caloriesPerGram,
                    onValueChange = { onIngredientChange(ingredient.copy(caloriesPerGram = it)) },
                    label = { Text("Calories/g") },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Total kcal: $totalCalories",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredient.proteinPerGram,
                    onValueChange = { onIngredientChange(ingredient.copy(proteinPerGram = it)) },
                    label = { Text("Protein/g") },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Total Protein: $totalProtein",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredient.fatPerGram,
                    onValueChange = { onIngredientChange(ingredient.copy(fatPerGram = it)) },
                    label = { Text("Fat/g") },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Total Fat: $totalFat",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextField(
                    value = ingredient.carbsPerGram,
                    onValueChange = { onIngredientChange(ingredient.copy(carbsPerGram = it)) },
                    label = { Text("Carbs/g") },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Total Carbs: $totalCarbs",
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAutofill,
                    modifier = Modifier
                        .weight(2f)
                        .padding(end = 8.dp)
                ) {
                    Text("Autofill")
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}



//@Composable
//fun InputEditor(onAddClicked: (String, Bitmap?, (Float) -> Unit) -> Unit) {
//    var text by remember { mutableStateOf("") }
//    var mealPhoto by remember { mutableStateOf<Bitmap?>(null) }
//    var uploadProgress by remember { mutableStateOf<Float?>(null) }
//
//    val cameraLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicturePreview()
//    ) { bitmap: Bitmap? ->
//        mealPhoto = bitmap
//    }
//
//    TextField(
//        value = text,
//        onValueChange = { text = it },
//        label = { Text("Enter new meal") },
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(bottom = 16.dp)
//    )
//
//    Button(onClick = { cameraLauncher.launch() }) {
//        Text(text = "Capture Image")
//    }
//
//    mealPhoto?.let { bitmap ->
//        Image(
//            bitmap = bitmap.asImageBitmap(),
//            contentDescription = "Captured Image",
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(250.dp)
//                .padding(vertical = 16.dp)
//        )
//    }
//
//    Button(onClick = {
//        if (text.isNotEmpty()) {
//            onAddClicked(text, mealPhoto) { progress ->
//                uploadProgress = progress
//            }
//            text = ""
//            mealPhoto = null
//            uploadProgress = null
//        }
//    }) {
//        Text(text = "Add to Database")
//    }
//
//    uploadProgress?.let { progress ->
//        LinearProgressIndicator(
//            progress = { progress / 100f },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 16.dp),
//        )
//
//        if (progress >= 100f) {
//            CoroutineScope(Dispatchers.Main).launch {
//                delay(1000)
//                uploadProgress = null
//            }
//        }
//    }
//}

fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? {
    if (bitmap == null) {
        Log.e("BitmapError", "Bitmap is null")
        return null
    }
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun addToDatabase(
    context: Context,
    mealName: String,
    mealType: String,
    ingredients: List<Ingredient>,
    mealImage: Bitmap?,
    onProgress: (Float) -> Unit
) {
    val db = MealDatabase.getDatabase(context)
    val imageByteArray = bitmapToByteArray(mealImage)

    val totalCalories = ingredients.sumOf { it.caloriesPerGram.toInt() }
    val totalWeight = ingredients.sumOf { it.weight.toInt() }
    val totalProtein = ingredients.sumOf { it.proteinPerGram.toInt() }
    val totalCarbs = ingredients.sumOf { it.carbsPerGram.toInt() }
    val totalFat = ingredients.sumOf { it.fatPerGram.toInt() }

    val ingredientsString = ingredients.joinToString(", ") { it.name }

    val newMeal = Meal(
        name = mealName,
        mealType = mealType,
        ingredients = ingredientsString,
        calories = totalCalories,
        totalWeight = totalWeight,
        totalProtein = totalProtein,
        totalCarbs = totalCarbs,
        totalFat = totalFat,
        photo = imageByteArray,
        photoUrl = null
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            db.mealDAO().insert(newMeal)
            Log.d("Database", "Meal inserted successfully")
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error inserting meal: ${e.message}")
        }
    }

    if (mealImage != null) {
        uploadImageToFirebase(mealName, mealImage, { downloadUrl ->
            val newMeal = Meal(name = mealName, calories = 1, photo = imageByteArray, photoUrl = downloadUrl)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    db.mealDAO().insert(newMeal)
                    Log.d("Database", "Meal inserted with image URL: $downloadUrl")
                } catch (e: Exception) {
                    Log.e("DatabaseError", "Error inserting meal: ${e.message}")
                }
            }
        }, { progress ->
            onProgress(progress)
        })
    } else {
        val newMeal = Meal(name = mealName, calories = 1, photo = imageByteArray, photoUrl = null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.mealDAO().insert(newMeal)
                Log.d("Database", "Meal inserted without image")
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error inserting meal: ${e.message}")
            }
        }
    }
}


fun uploadImageToFirebase(mealName: String, mealImage: Bitmap, onSuccess: (String) -> Unit, onProgress: (Float) -> Unit) {
    val storageRef = Firebase.storage.reference.child("images/$mealName.jpg")

    val imageData = com.example.calorieapp.mealsDatabase.bitmapToByteArray(mealImage)
    val uploadTask = storageRef.putBytes(imageData!!)

    uploadTask.addOnSuccessListener { taskSnapshot ->
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FirebaseStorage", "Image uploaded successfully. URL: $uri")
            onSuccess(uri.toString())
        }.addOnFailureListener { e ->
            Log.e("FirebaseStorage", "Failed to get download URL: ${e.message}")
        }
    }.addOnFailureListener { e ->
        Log.e("FirebaseStorage", "Image upload failed: ${e.message}")
    }.addOnProgressListener { taskSnapshot ->
        val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
        Log.d("FirebaseStorage", "Upload is $progress% done")
        onProgress(progress.toFloat())
    }
}
