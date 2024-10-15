package com.example.calorieapp.general

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType

/**
 * layout composable to inset content ~ 5% of width and height on each side
 */
@Composable
fun InsetContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.LightGray)
    ) {
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

/**
 * TextField wrapper which performs input validation for numbers
 *
 * NOTE "", "." and "-" are still considered valid and must
 * be handled by the parent
 */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable() (() -> Unit)? = null,
    modifier: Modifier,
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        singleLine = true,
        onValueChange = {
            // input validation to ensure valid entries only
            if (it.isEmpty() || it == "-" || it == ".") onValueChange(it)
            else onValueChange(when (it.toDoubleOrNull()) {
                null -> value //old value
                else -> it})
        },
        label = label,
        modifier = modifier,
        enabled = enabled,
    )
}