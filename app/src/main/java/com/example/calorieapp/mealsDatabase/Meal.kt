package com.example.calorieapp.mealsDatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.time.LocalDate

/**/
@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val mealType: String = "",
    @ColumnInfo(name = "meal_calories") val calories: Int = 0,
    val date: String = "",
    val ingredients: String = "",
    val totalWeight: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0,
    var photo: ByteArray?,
    var photoUrl: String?
)
