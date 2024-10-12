package com.example.calorieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.calorieapp.ui.theme.CalorieAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
        enableEdgeToEdge()
        setContent {
            CalorieAppTheme {
                AppNavigation()
            }
        }
    }
}

const val previewWidthDp = 360
const val previewHeightDp = 740
const val previewTabletWidthDp = 800
const val previewTabletHeightDp = 1280

// **********************************************************************************
// Previews:         (Ignoring large screens for now)
// **********************************************************************************
@Composable
fun CalorieAppPreview() {
    CalorieAppTheme {
        AppNavigation()
    }
}

@Preview(name = "5-inch Device Portrait",
    widthDp = previewWidthDp, heightDp = previewHeightDp, showBackground = true)
@Composable
fun App_Preview5Inch() {
    CalorieAppPreview()
}
@Preview(name = "5-inch Device Landscape",
    widthDp = previewHeightDp, heightDp = previewWidthDp, showBackground = true)
@Composable
fun App_Preview5InchLand() {
    CalorieAppPreview()
}

// TODO check if we need these, my friend got 100% with no tablet layouts
@Preview(name = "10-inch Tablet Portrait",
    widthDp = previewTabletWidthDp, heightDp = previewTabletHeightDp, showBackground = true)
@Composable
fun App_Preview10InchTablet() {
    CalorieAppPreview()
}
@Preview(name = "10-inch Tablet Landscape",
    widthDp = previewTabletHeightDp, heightDp = previewTabletWidthDp, showBackground = true)
@Composable
fun App_Preview10InchTabletLand() {
    CalorieAppPreview()
}