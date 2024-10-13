package com.example.calorieapp.mealsDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    @ColumnInfo(name = "meal_calories") val calories: Int = 0,
    val photo: ByteArray?,
    var photoUrl: String?
)
