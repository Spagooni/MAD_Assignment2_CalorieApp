package com.example.calorieapp.mealsDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
public interface MealDAO {
    @Insert
    suspend fun insert(vararg meal: Meal);

    @Update
    suspend fun update(vararg meal: Meal);

    @Delete
    suspend fun delete(vararg meal: Meal);

    @Query("SELECT * FROM meals")
    suspend fun getAll(): List<Meal>

    @Query("SELECT * FROM meals WHERE name = :studentName")
    suspend fun getMealByName(studentName: String) : List<Meal>

    @Query("SELECT * FROM meals WHERE id = :studentId")
    suspend fun getMealByID(studentId: Int) : Meal?

    @Query("SELECT * FROM meals WHERE meal_calories = :calories")
    suspend fun getCaloriesByID(calories: Int) : Meal?
}