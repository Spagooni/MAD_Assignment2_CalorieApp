package com.example.calorieapp

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calorieapp.mealsDatabase.Meal
import com.example.calorieapp.mealsDatabase.MealDAO
import com.example.calorieapp.mealsDatabase.bitmapToByteArray
import com.example.calorieapp.mealsDatabase.upscaleBitmap
import com.example.calorieapp.remoteAPIs.CalorieNinjasAPICalls
import com.example.calorieapp.remoteAPIs.RetrofitInstance
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CalorieAppViewModel : ViewModel() {
    /** DAO for database, should be provided when the viewmodel is instantiated */
    var mealDao: MealDAO? = null

    /** service for calorie ninjas API */
    private val calorieNinjasAPIService = RetrofitInstance.calorieNinjasAPI

    /** true from when saveMealToDB is called until saved in DB, INCLUDES FIREBASE UPLOAD */
    var insertingIntoDB by mutableStateOf(false)
    /** null when not uploading, 0.0 at start of upload and 1.0 at end */
    var imageUploadProgress by mutableStateOf<Float?>(null)

    /** insert completed meal object into database and reset UI fields */
    private fun saveToDatabase(mealDao: MealDAO, meal: Meal, successToast : () -> Unit,) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("Database", "attempting insert")
                mealDao.insert(meal) // Insert meal into database
                Log.d("Database", "Meal inserted with image URL: ${meal.photoUrl}")
                withContext(Dispatchers.Main) {
                    successToast()
                }
                currentMeal.reset() // Reset the current meal data if upload successful
            } catch (e: Exception) {
                Log.e("DatabaseError", "Error inserting meal: ${e.message}")
            } finally {
                insertingIntoDB = false
            }
        }
    }

    /**
     * Save the current meal to database if valid, does nothing if else.
     * Track progress with:
     * - insertingIntoDB (true during insert AND upload if photo available)
     * - imageUploadProgress (from 0.0 to 1.0 and null when not uploading)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveMealToDB(
        successToast : () -> Unit,
    ) {
        if (mealDao == null) {
            Log.e("Database", "ERROR cannot insert, no DAO provided")
            return // early return if no DAO provided
        }
        val safeMealDao: MealDAO = mealDao!!
        if (!currentMeal.isValid()) {
            Log.e("Database", "ERROR cannot insert, current UI meal not valid")
            return // early return if current UI meal not valid
        }
        insertingIntoDB = true
        Log.d("Database", "inside addToDatabase")

        /** create ingredients string from ingredients list */
        val ingredientsString = currentMeal.ingredients
            .joinToString(", ") { it.name }

        /** create new meal with no photo url */
        val newMeal = Meal(
            name = currentMeal.mealName,
            mealType = currentMeal.mealType,
            date = currentMeal.dateString,
            ingredients = ingredientsString,
            calories = currentMeal.totalCalories.toInt(),
            totalWeight = currentMeal.totalWeight.toInt(),
            totalProtein = currentMeal.totalProtein.toInt(),
            totalCarbs = currentMeal.totalCarbs.toInt(),
            totalFat = currentMeal.totalFats.toInt(),
            photo = null,
            photoUrl = null
        )

        /** If no image, upload straight away, else save image to firebase first */
        val mealPhotoByteArray = currentMeal.photo?.let { bitmapToByteArray(it) } // byteArray?
        if (mealPhotoByteArray == null) { // no photo for meal, direct insert
            saveToDatabase(mealDao = safeMealDao, meal = newMeal, successToast)
        } else {
            Log.e("DATABASE", "has photo, uploading with photo")
            newMeal.photo = mealPhotoByteArray // set bytearray field in DB
            uploadMealImageToFirebase(
                onUploadSuccess = { uriString ->
                    newMeal.photoUrl = uriString
                    saveToDatabase(mealDao = safeMealDao, meal = newMeal, successToast) // save with firebase URL
                },
                onUploadFail = {
                    saveToDatabase(mealDao = safeMealDao, meal = newMeal, successToast) // save anyways???
                }
            )
        }
    }

    /** scale image 3x up (to slow upload) and upload to firebase */
    private fun uploadMealImageToFirebase(
        onUploadSuccess: (String) -> Unit,
        onUploadFail: () -> Unit,
    ) {
        currentMeal.photo?.let { originalBitmap -> // snapshot stateful bitmap to prevent errors
            viewModelScope.launch {
                // Run the upscale operation in a background thread (Default dispatcher)
                val upscaledBitmap = withContext(Dispatchers.Default) {
                    upscaleBitmap(originalBitmap, scaleFactor = 3f)
                }
                val byteArray = withContext(Dispatchers.Default) {
                    bitmapToByteArray(upscaledBitmap) // Convert the upscaled bitmap to byte array
                }
                // Proceed to upload the image
                withContext(Dispatchers.IO) {
                    uploadImageToFirebase(
                        imageByteArray = byteArray,
                        name = currentMeal.mealName,
                        onUploadSuccess = onUploadSuccess,
                        onUploadFail = onUploadFail,
                    )
                }
            }
        }
    }

    private fun uploadImageToFirebase(
        imageByteArray: ByteArray,
        name: String,
        onUploadSuccess: (String) -> Unit,
        onUploadFail: () -> Unit,
    ) {
        imageUploadProgress = 0f
        val storageRef = Firebase.storage.reference.child(
                "images/$name.png")
        val uploadTask = storageRef.putBytes(imageByteArray)

        // Listen for the progress of the upload
        uploadTask.addOnProgressListener { taskSnapshot ->
            // Calculate progress percentage
            Log.d("FirebaseStorage", "Uploaded ${taskSnapshot.bytesTransferred} of ${taskSnapshot.totalByteCount} bytes")
            val progress = taskSnapshot.bytesTransferred.toDouble() / taskSnapshot.totalByteCount
            imageUploadProgress = progress.toFloat()// Progress is a float between 0 and 1
            Log.d("FirebaseStorage", "progress: ${progress * 100f} %")
        }.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                imageUploadProgress = null
                Log.d("FirebaseStorage", "Image uploaded successfully. URL: $uri")
                onUploadSuccess(uri.toString())
            }
        }.addOnFailureListener { e ->
            imageUploadProgress = null
            Log.e("FirebaseStorage", "Failed to get download URL: ${e.message}")
            onUploadFail()
        }
    }

    var currentMeal by mutableStateOf(InProgressMeal())
        private set

    /** autofill ingredient per-gram values based on API call */
    fun autofillIngredient(ingredient: MealIngredient) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                ingredient.autofill(calorieNinjasAPIService)
            }
        }
    }
}

/** Class to store the current meal being logged */
class InProgressMeal() {
    var photo by mutableStateOf<Bitmap?>(null)
    var mealName by mutableStateOf("")
    var mealType by mutableStateOf("")
    var ingredients = mutableStateListOf<MealIngredient>() // initially empty

    // for date
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    var date by mutableStateOf<LocalDate>(LocalDate.now())
    val dateString: String get() = date.format(formatter)

    val totalWeight: Double get() = ingredients.sumOf { it.weight.toDoubleOrNull() ?: 0.0 }
    val totalCalories: Double get() = ingredients.sumOf { it.totalKcal.toDoubleOrNull() ?: 0.0 }
    val totalProtein: Double get() = ingredients.sumOf { it.totalProtein.toDoubleOrNull() ?: 0.0 }
    val totalFats: Double get() = ingredients.sumOf { it.totalFats.toDoubleOrNull() ?: 0.0 }
    val totalCarbs: Double get() = ingredients.sumOf { it.totalCarbs.toDoubleOrNull() ?: 0.0 }

    fun isValid(): Boolean {
        if(mealName.isNotEmpty() && ingredients.isNotEmpty()) {
            return (!(ingredients.map { it.isValid() }.contains(false)))
        }
        return false
    }

    fun reset() {
        mealName = ""
        mealType = ""
        photo = null
        ingredients.clear()
    }
}

/** Class to hold ingredients and relevant info */
class MealIngredient() {
    var name by mutableStateOf("")
    var weight by mutableStateOf("")
    var kcalPer100g by mutableStateOf("")
    var fatsPer100g by mutableStateOf("")
    var proteinPer100g by mutableStateOf("")
    var carbsPer100g by mutableStateOf("")

    /** computed properties */
    private fun safeScaleByWeight(value: String): String {
        weight.toDoubleOrNull()?.let { weightDouble ->
            value.toDoubleOrNull()?.let { valueDouble ->
                return "%.2f".format(weightDouble / 100.0 * valueDouble)
            }
        }
        return "-"
    }
    val totalKcal: String get() = safeScaleByWeight(kcalPer100g)
    val totalFats: String get() = safeScaleByWeight(fatsPer100g)
    val totalProtein: String get() = safeScaleByWeight(proteinPer100g)
    val totalCarbs: String get() = safeScaleByWeight(carbsPer100g)

    fun isValid(): Boolean {
        return name.isNotEmpty() &&
            totalKcal != "-" &&
            totalFats != "-" &&
            totalProtein != "-" &&
            totalCarbs != "-"
    }

    /** API call stuff */
    // Loading state to track whether autofill request is in progress
    var isLoading by mutableStateOf(false)

    /**
     * Autofill function to fetch data from the API.
     * This updates the loading state before and after the network call.
     */
    suspend fun autofill(apiService: CalorieNinjasAPICalls) {
        try {
            isLoading = true // Set loading to true when request starts
            val response =
                apiService.getSearchResponse(name) // Query the API based on the ingredient name

            response.items.firstOrNull()?.let { item ->
                val servingSize = item.servingSizeGrams
                kcalPer100g = "%.2f".format(item.caloriesPerServe / (servingSize / 100.0))
                fatsPer100g = "%.2f".format(item.fatTotalPerServe / (servingSize / 100.0))
                proteinPer100g = "%.2f".format(item.proteinPerServe / (servingSize / 100.0))
                carbsPer100g = "%.2f".format(item.carbsPerServe / (servingSize / 100.0))
            }
        } catch (e: Exception) {
            Log.e("API", "Error fetching data: ${e.message}") // Handle the error here
        } finally {
            isLoading = false // Set loading to false when request finishes (either success or failure)
        }
    }
}


