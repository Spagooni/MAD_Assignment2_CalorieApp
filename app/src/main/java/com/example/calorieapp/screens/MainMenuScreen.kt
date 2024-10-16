package com.example.calorieapp.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.calorieapp.CalorieAppViewModel
import com.example.calorieapp.Routes
import com.example.calorieapp.general.InsetContent
import com.example.calorieapp.previewHeightDp
import com.example.calorieapp.previewWidthDp
import com.example.calorieapp.ui.theme.CalorieAppTheme

@Composable
fun MainMenuScreen(navController: NavHostController,
                   shvm: CalorieAppViewModel = viewModel<CalorieAppViewModel>()
) {
    val orientation = LocalConfiguration.current.orientation
    InsetContent {
        when (orientation) {
            Configuration.ORIENTATION_PORTRAIT ->
                MainMenu_Portrait(navController = navController, shvm)
            else ->
                MainMenu_Portrait(navController = navController, shvm)
        }
    }
}


@Composable
fun MainMenu_Portrait(navController: NavHostController, shvm: CalorieAppViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Main Menu",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            // color = Color.White
        )
        Button(
            onClick = { navController.navigate(Routes.LOG_MEAL) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Log a Meal")
        }
        Button(
            onClick = { navController.navigate(Routes.LOGGED_MEALS) },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Logged Meals")
        }
    }
}

@Composable
fun MainMenu_Landscape() {
    Column(Modifier.fillMaxSize()) {
        Text("Landscape")
    }
}

// **********************************************************************************
// Previews:         (Ignoring large screens for now)
// **********************************************************************************
@Composable
fun MainMenuScreen_Preview() {
    val navController = rememberNavController()
    CalorieAppTheme {
        MainMenuScreen(navController = navController)
    }
}

@Preview(name = "5-inch Device Portrait",
    widthDp = previewWidthDp, heightDp = previewHeightDp, showBackground = true)
@Composable
fun MainMenuScreen_Preview5Inch() {
    MainMenuScreen_Preview()
}
@Preview(name = "5-inch Device Landscape",
    widthDp = previewHeightDp, heightDp = previewWidthDp, showBackground = true)
@Composable
fun MainMenuScreen_Preview5InchLand() {
    MainMenuScreen_Preview()
}