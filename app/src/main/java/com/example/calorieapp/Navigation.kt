package com.example.calorieapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calorieapp.mealsDatabase.MealDatabase
import com.example.calorieapp.screens.logActivityScreen.LogMealScreen
import com.example.calorieapp.screens.LoggedMealsScreen
import com.example.calorieapp.screens.MainMenuScreen

data object Routes{
    const val MAIN_MENU = "mainMenu"
    const val LOGGED_MEALS = "loggedMeals"
    const val LOG_MEAL = "logMeal"
}

@Composable
fun AppNavigation() {
    // instantiate nav controller and view model
    val navController = rememberNavController()
    val viewModel = viewModel<CalorieAppViewModel>()

    // provide meal database DAO to viewmodel
    val context = LocalContext.current
    val mealDao = MealDatabase.getDatabase(context).mealDAO()
    viewModel.mealDao = mealDao

    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {
        composable(Routes.MAIN_MENU) { MainMenuScreen(navController) }
        composable(Routes.LOG_MEAL) { LogMealScreen(shvm = viewModel) }
        composable(Routes.LOGGED_MEALS) { LoggedMealsScreen() }
    }
}