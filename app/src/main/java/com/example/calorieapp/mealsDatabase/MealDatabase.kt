package com.example.calorieapp.mealsDatabase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Database(entities = [Meal::class], version = 5)
abstract class MealDatabase : RoomDatabase() {
    abstract fun mealDAO(): MealDAO


    companion object {
        @Volatile
        private var INSTANCE: MealDatabase? = null

        fun getDatabase(context: Context): MealDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MealDatabase::class.java,
                    "contact_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
/** Function to upscale a bitmap by a scale factor */
fun upscaleBitmap(bitmap: Bitmap, scaleFactor: Float): Bitmap {
    val width = (bitmap.width * scaleFactor).toInt()
    val height = (bitmap.height * scaleFactor).toInt()
    return Bitmap.createScaledBitmap(bitmap, width, height, true)
}


/** helper function to convert byte array to bitmap */
fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
/** helper function to convert bitmap to byte array */
fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream) // PNG or JPEG format
    return stream.toByteArray()
}
