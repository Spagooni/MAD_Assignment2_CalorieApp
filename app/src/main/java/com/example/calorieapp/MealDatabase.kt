package com.example.calorieapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

// TODO if required later: (FROM PRAC 8)
//      boilerplate for using bytearrays/bitmaps from database:

        /** TODO: (retrieve bytearray, convert to bitmap and display)
            contact.photo?.let { byteArray ->
                val bitmap = byteArrayToBitmap(byteArray)
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured Image",
                    // modifier = Modifier
                    //     .fillMaxSize()
                    //     .height(250.dp)
                )
            }
         */

        /** TODO (convert bitmap to bytearray and save to DB)
        onSave = {
            vm.selectedContact?.let {
                val currentSelectedImageBitmap = vm.selectedImageBitmap
                if (currentSelectedImageBitmap != null) {
                    it.photo = bitmapToByteArray(currentSelectedImageBitmap)
                }
                dao.insert(it) // add to DB
                myContacts = dao.getAllContacts() // refresh DB in Ui
            }
        },
        */
