package com.example.calorieapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data object Routes{
    const val MAIN_MENU = "mainMenu"
    const val PROFILES = "editProfiles"
    const val LOGGED_MEALS = "changeDiskColors"
    const val LOG_MEAL = "gameStart/1Player"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.MAIN_MENU) {
        composable(Routes.MAIN_MENU) {MainMenuScreen(navController) }
//        composable(Routes.EDIT_PROFILES) { EditProfilesScreen(sharedViewModel) }
//        composable(Routes.CHANGE_DISK_COLORS) { ChangeDisksScreen(sharedViewModel) }
//        composable(Routes.START_GAME_MENU_1P) { Start1PGameScreen(navController, sharedViewModel) }
//        composable(Routes.START_GAME_MENU_2P) { Start2PGameScreen(navController, sharedViewModel) }


    }
}