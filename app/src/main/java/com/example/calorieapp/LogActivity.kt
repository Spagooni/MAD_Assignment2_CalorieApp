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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                InputEditor { name, mealPhoto, onProgress ->
                    addToDatabase(context, name, mealPhoto, onProgress)
                }
            }
        }
    }
}

@Composable
fun InputEditor(onAddClicked: (String, Bitmap?, (Float) -> Unit) -> Unit) {
    var text by remember { mutableStateOf("") }
    var mealPhoto by remember { mutableStateOf<Bitmap?>(null) }
    var uploadProgress by remember { mutableStateOf<Float?>(null) }

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
            onAddClicked(text, mealPhoto) { progress ->
                uploadProgress = progress
            }
            text = ""
            mealPhoto = null
            uploadProgress = null
        }
    }) {
        Text(text = "Add to Database")
    }

    uploadProgress?.let { progress ->
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        )

        if (progress >= 100f) {
            CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                uploadProgress = null
            }
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

fun addToDatabase(context: Context, mealName: String, mealImage: Bitmap?, onProgress: (Float) -> Unit) {
    val db = MealDatabase.getDatabase(context)
    val imageByteArray = bitmapToByteArray(mealImage)

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

    val imageData = bitmapToByteArray(mealImage)
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
