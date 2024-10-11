package com.example.calorieapp

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
public interface MealDAO {
    @Insert
    fun insert(vararg meal: Meal);

    @Update
    fun update(vararg meal: Meal);

    @Delete
    fun delete(vararg meal: Meal);

    @Query("SELECT * FROM meals")
    fun getAll(): List<Meal>

    @Query("SELECT * FROM meals WHERE name = :studentName")
    fun getMealByName(studentName: String) : List<Meal>

    @Query("SELECT * FROM meals WHERE id = :studentId")
    fun getMealByID(studentId: Int) : Meal?

    @Query("SELECT * FROM meals WHERE meal_calories = :calories")
    fun getCaloriesByID(calories: Int) : Meal?
}