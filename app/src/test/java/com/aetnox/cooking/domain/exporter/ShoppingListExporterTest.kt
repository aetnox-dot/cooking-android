package com.aetnox.cooking.domain.exporter

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.ui.util.CustomCartItem
import org.junit.Assert.assertTrue
import org.junit.Test

class ShoppingListExporterTest {

    @Test
    fun testGenerateShoppingListTextConsolidatesIngredients() {
        val recipe1 = RecipeWithDetails(
            recipe = RecipeEntity(id = 1, title = "Брауни", baseServings = 4.0),
            ingredients = listOf(
                IngredientEntity(recipeId = 1, name = "Мука", amount = 200.0, unit = "г", selectedUnit = "г", groupName = "Для теста"),
                IngredientEntity(recipeId = 1, name = "Сахар", amount = 150.0, unit = "г", selectedUnit = "г", groupName = "Для теста")
            ),
            steps = emptyList(),
            tags = emptyList()
        )

        val recipe2 = RecipeWithDetails(
            recipe = RecipeEntity(id = 2, title = "Печенье", baseServings = 4.0),
            ingredients = listOf(
                IngredientEntity(recipeId = 2, name = "Мука", amount = 100.0, unit = "г", selectedUnit = "г", groupName = "Для теста"),
                IngredientEntity(recipeId = 2, name = "Яйца", amount = 2.0, unit = "шт", selectedUnit = "шт", groupName = "Общие")
            ),
            steps = emptyList(),
            tags = emptyList()
        )

        val text = ShoppingListExporter.generateShoppingListText(listOf(recipe1, recipe2))

        assertTrue(text.contains("🛒 СПИСОК ПОКУПОК"))
        assertTrue(text.contains("«Брауни»"))
        assertTrue(text.contains("«Печенье»"))
        assertTrue(text.contains("[ ] Мука — 300 г"))
        assertTrue(text.contains("[ ] Сахар — 150 г"))
        assertTrue(text.contains("[ ] Яйца — 2 шт"))
    }

    @Test
    fun testPortionScalingAndCheckedState() {
        val recipe = RecipeWithDetails(
            recipe = RecipeEntity(id = 10, title = "Паста", baseServings = 2.0),
            ingredients = listOf(
                IngredientEntity(recipeId = 10, name = "Спагетти", amount = 200.0, unit = "г", selectedUnit = "г", groupName = "Бакалея")
            ),
            steps = emptyList(),
            tags = emptyList()
        )

        // Scale servings to 4 (multiplier = 2x)
        val servingsMap = mapOf(10L to 4.0)
        val checkedKeys = setOf("спагетти_г")

        val text = ShoppingListExporter.generateShoppingListText(
            recipes = listOf(recipe),
            recipeServingsMap = servingsMap,
            checkedKeys = checkedKeys
        )

        assertTrue(text.contains("[v] Спагетти — 400 г"))
    }

    @Test
    fun testCustomShoppingItems() {
        val customItems = listOf(
            CustomCartItem(name = "Бумажные салфетки", isChecked = false),
            CustomCartItem(name = "Сок", isChecked = true)
        )

        val text = ShoppingListExporter.generateShoppingListText(
            recipes = emptyList(),
            customItems = customItems
        )

        assertTrue(text.contains("📌 Свои покупки:"))
        assertTrue(text.contains("[ ] Бумажные салфетки"))
        assertTrue(text.contains("[v] Сок"))
    }
}
