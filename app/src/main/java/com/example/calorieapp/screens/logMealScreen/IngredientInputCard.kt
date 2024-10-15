package com.example.calorieapp.screens.logMealScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calorieapp.MealIngredient
import com.example.calorieapp.general.InsetContent
import com.example.calorieapp.general.NumberField

@Preview(showBackground = true)
@Composable
fun IngredientInput_Preview() {
    val ingredient = MealIngredient()
    ingredient.name = "Sandwich"
    ingredient.weight = "100"
    ingredient.kcalPer100g = "10.5"
    ingredient.proteinPer100g = "2"
    InsetContent {
        IngredientInputCard(ingredient = ingredient, onAutofill = {}, onDelete = {},
            loading = true)
    }
}

@Composable
fun IngredientInputCard(
    ingredient: MealIngredient,
    onAutofill: () -> Unit,
    onDelete: () -> Unit,
    loading: Boolean,
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = ingredient.name,
                    onValueChange = { ingredient.name = it },
                    label = { if (ingredient.name.isEmpty())
                        Text("Ingredient Name",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold) },
                    modifier = Modifier.weight(0.6f),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    singleLine = true
                )
                NumberField(
                    value = ingredient.weight,
                    onValueChange = { ingredient.weight = it },
                    label = { Text("Weight (g)") },
                    modifier = Modifier.weight(0.4f)
                )
            }

            IngredientInputRow(
                value = ingredient.kcalPer100g, total = ingredient.totalKcal,
                quantityName = "Calories", unit = "kcal",
                onValueChange = { ingredient.kcalPer100g = it },
                enabled = !loading,
            )
            IngredientInputRow(
                value = ingredient.fatsPer100g, total = ingredient.totalFats,
                quantityName = "Fats",
                onValueChange = { ingredient.fatsPer100g = it },
                enabled = !loading,
            )
            IngredientInputRow(
                value = ingredient.proteinPer100g, total = ingredient.totalProtein,
                quantityName = "Protein",
                onValueChange = { ingredient.proteinPer100g = it },
                enabled = !loading,
            )
            IngredientInputRow(
                value = ingredient.carbsPer100g, total = ingredient.totalCarbs,
                quantityName = "Carbs",
                onValueChange = { ingredient.carbsPer100g = it },
                enabled = !loading,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = onAutofill,
                    modifier = Modifier.weight(2f),
                    enabled = !loading && ingredient.name != "",
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.fillMaxHeight().size(25.dp))
                    } else {
                        Text("Autofill")
                    }
                }

                Button(onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) { Text("Delete") }
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
    unit: String = "g",
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NumberField(
            value = if(enabled) value else "",
            onValueChange = onValueChange,
            label = { Text("$quantityName/100g") },
            modifier = Modifier.weight(0.6f),
            enabled = enabled,
        )
        Text(
            text = "Total $quantityName:\n" +
                    "$total ${if(total != "-") unit else ""}",
            modifier = Modifier.padding(start = 8.dp).weight(0.4f),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}