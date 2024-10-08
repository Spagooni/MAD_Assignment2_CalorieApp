package com.example.calorieapp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * layout composable to inset content ~ 5% of width and height on each side
 */
@Composable
fun InsetContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(modifier = modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(0.1f))
        Column(modifier = Modifier.fillMaxSize().weight(2f)) {
            Spacer(modifier = Modifier.weight(0.1f))
            Column(modifier = Modifier.fillMaxSize().weight(2f)) {
                content()
            }
            Spacer(modifier = Modifier.weight(0.1f))
        }
        Spacer(modifier = Modifier.weight(0.1f))
    }
}