package com.example.calorieapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ProfilesScreen(navController: NavHostController) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(64.dp)) {
        Text(text = "Nav to view all Profiles")
    }
}