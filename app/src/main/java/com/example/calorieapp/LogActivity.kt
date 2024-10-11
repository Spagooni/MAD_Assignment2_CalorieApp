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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun LogScreen() {
    val context = LocalContext.current
    var mealPhoto by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        mealPhoto = bitmap
    }

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
                InputEditor { name ->
                    addToDatabase(context, name, mealPhoto)
                }

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
                    )
                }
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

@Composable
fun ThumbnailCaptureScreen() {
    var thumbnailImage by remember { mutableStateOf<Bitmap?>(null) }

    // This launcher is equivalent to ActivityResultLauncher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            thumbnailImage = bitmap
        } else {
            Log.e("CameraCapture", "Failed to capture image")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
    ) {
        Button(
            onClick = {
                cameraLauncher.launch()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Capture Image")
        }

        // Display the thumbnail if available
        thumbnailImage?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
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

