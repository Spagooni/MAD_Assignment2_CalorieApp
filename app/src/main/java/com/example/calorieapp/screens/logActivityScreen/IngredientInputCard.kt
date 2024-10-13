package com.example.calorieapp.screens.logActivityScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calorieapp.CalorieAppViewModel
import com.example.calorieapp.MealIngredient
import com.example.calorieapp.general.NumberField
import com.example.calorieapp.mealsDatabase.Meal

@Preview
@Composable
fun IngredientInput_Preview() {
    val ingredient = MealIngredient()
    IngredientInput(ingredient = ingredient, onAutofill = {}, onDelete = {})
}

@Composable
fun IngredientInput(
    ingredient: MealIngredient,
    onAutofill: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NumberField(
                    value = ingredient.weight,
                    onValueChange = { ingredient.weight = it },
                    label = { Text("Weight (g)") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = ingredient.name,
                    onValueChange = { ingredient.name = it },
                    label = { Text("Ingredient Name") },
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 8.dp)
                )
            }

            IngredientInputRow(
                value = ingredient.kcalPerGram, total = ingredient.totalKcal,
                quantityName = "Calories",
                onValueChange = { ingredient.kcalPerGram = it },
            )
            IngredientInputRow(
                value = ingredient.fatsPerGram, total = ingredient.totalFats,
                quantityName = "Fats",
                onValueChange = { ingredient.fatsPerGram = it },
            )
            IngredientInputRow(
                value = ingredient.proteinPerGram, total = ingredient.totalProtein,
                quantityName = "Protein",
                onValueChange = { ingredient.proteinPerGram = it },
            )
            IngredientInputRow(
                value = ingredient.carbsPerGram, total = ingredient.totalCarbs,
                quantityName = "Carbs",
                onValueChange = { ingredient.carbsPerGram = it },
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onAutofill,
                    modifier = Modifier
                        .weight(2f)
                        .padding(end = 8.dp)
                ) {
                    Text("Autofill")
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}


@Preview
@Composable
private fun IngredientInputRow(
    value: String = "0.0",
    total: String = "0.0",
    quantityName: String = "Calories",
    // unit: String? = null,
    onValueChange: (String) -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        NumberField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("$quantityName/g") },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Total $quantityName: $total",// + unit?.let{" $it"},
            modifier = Modifier.padding(start = 8.dp),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}