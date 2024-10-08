package com.example.calorieapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data object Routes{
    const val MAIN_MENU = "mainMenu"
    const val PROFILES = "profiles"
    const val LOGGED_MEALS = "loggedMeals"
    const val LOG_MEAL = "logMeal"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {
        composable(Routes.MAIN_MENU) {MainMenuScreen(navController) }
        composable(Routes.LOG_MEAL) { LogScreen() }
        composable(Routes.LOGGED_MEALS) { LoggedMealsScreen(navController) }
        composable(Routes.PROFILES) { ProfilesScreen(navController) }
    }
}