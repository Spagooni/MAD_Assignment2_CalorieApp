package com.example.calorieapp

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun LogScreen() {
    val context = LocalContext.current

    InsetContent {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Log a Meal",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                InputEditor { name, mealPhoto ->
                    addToDatabase(context, name, mealPhoto)
                }
            }
        }
    }
}



@Composable
fun InputEditor(onAddClicked: (String, Bitmap?) -> Unit) {
    var text by remember { mutableStateOf("") }
    var mealPhoto by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        mealPhoto = bitmap
    }

    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Enter new meal") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )

    Button(onClick = { cameraLauncher.launch() }) {
        Text(text = "Capture Image")
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

    Button(onClick = {
        if (text.isNotEmpty()) {
            onAddClicked(text, mealPhoto)
            text = ""
            mealPhoto = null
        }
    }) {
        Text(text = "Add to Database")
    }
}


fun bitmapToByteArray(bitmap: Bitmap?): ByteArray? {
    if (bitmap == null) {
        Log.e("BitmapError", "Bitmap is null")
        return null
    }
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun addToDatabase(context: Context, mealName: String, mealImage: Bitmap?) {
    val db = MealDatabase.getDatabase(context)
    val imageByteArray = bitmapToByteArray(mealImage)
    val newMeal = Meal(name = mealName, calories = 1, photo = imageByteArray)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            db.mealDAO().insert(newMeal)
            Log.d("Database", "Meal inserted: $newMeal")
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error inserting meal: ${e.message}")
        }
    }
}


