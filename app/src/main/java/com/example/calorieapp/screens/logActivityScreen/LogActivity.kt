package com.example.calorieapp.screens.logActivityScreen

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
import com.example.calorieapp.CalorieAppViewModel
import com.example.calorieapp.InProgressMeal
import com.example.calorieapp.MealIngredient
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun LogScreen(shvm: CalorieAppViewModel) {
    val context = LocalContext.current
    val meal = shvm.currentMeal

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
                        value = meal.mealName,
                        onValueChange = { meal.mealName = it },
                        label = { Text("Meal Name") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )
                    TextField(
                        value = meal.mealType,
                        onValueChange = { meal.mealType = it },
                        label = { Text("Meal Type") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }

                Text(
                    text = "TOTAL: " +
                            "weight: ${meal.totalWeight}, " +
                            "Calories: ${meal.totalCalories}, " +
                            "Fats: ${meal.totalFats}, " +
                            "protein: ${meal.totalProtein}, " +
                            "carbs: ${meal.totalCarbs}",
                    modifier = Modifier.padding(top = 16.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    // color = Color.White
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 475.dp)
                        .padding(top = 16.dp)
                ) {
                    items(meal.ingredients) { ingredient ->
                        IngredientInput(
                            ingredient = ingredient,
                            onAutofill = {ingredient.autofill()},
                            onDelete = {
                                meal.ingredients.remove(ingredient)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        meal.ingredients.add(MealIngredient())
                        // calculateTotals()
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
                            if (meal.isValid()) {
                                addToDatabase(context, meal, mealPhoto) { progress -> }
                                meal.reset()
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
    inProgressMeal: InProgressMeal,
    mealImage: Bitmap?,
    onProgress: (Float) -> Unit
) {
    val db = MealDatabase.getDatabase(context)
    val imageByteArray = bitmapToByteArray(mealImage)
    val ingredientsString = inProgressMeal.ingredients.joinToString(", ") { it.name }

    val newMeal = Meal(
        name = inProgressMeal.mealName,
        mealType = inProgressMeal.mealType,
        ingredients = ingredientsString,
        calories = inProgressMeal.totalCalories.toInt(),
        totalWeight = inProgressMeal.totalWeight.toInt(),
        totalProtein = inProgressMeal.totalProtein.toInt(),
        totalCarbs = inProgressMeal.totalCarbs.toInt(),
        totalFat = inProgressMeal.totalFats.toInt(),
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
        uploadImageToFirebase(inProgressMeal.mealName, mealImage, { downloadUrl ->
            val newMeal = Meal(name = inProgressMeal.mealName, calories = 1, photo = imageByteArray, photoUrl = downloadUrl)

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
        val newMeal = Meal(name = inProgressMeal.mealName, calories = 1, photo = imageByteArray, photoUrl = null)

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
