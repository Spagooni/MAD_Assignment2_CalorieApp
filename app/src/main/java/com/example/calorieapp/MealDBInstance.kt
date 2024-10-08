package com.example.calorieapp

import android.content.Context
import androidx.room.Room

class MealDBInstance {
    private var database: MealDatabase? = null

    fun getDatabase(context: Context): MealDatabase {
        if (database == null) {
            synchronized(this) {
                if (database == null) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        MealDatabase::class.java,
                        "app_database"
                    )
                        .allowMainThreadQueries()
                        .build()
                }
            }
        }
        return database!!
    }
}