package com.aetnox.cooking.domain.exporter

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeExporterTest {

    @Test
    fun testExportAndImportDatabaseRoundtrip() {
        val recipe = RecipeEntity(
            id = 1,
            title = "Омлет с сыром",
            prepTimeMinutes = 5,
            cookTimeMinutes = 10,
            baseServings = 2.0,
            yieldUnit = "порции"
        )
        val ingredients = listOf(
            IngredientEntity(id = 1, recipeId = 1, name = "Яйца", amount = 3.0, unit = "шт"),
            IngredientEntity(id = 2, recipeId = 1, name = "Сыр", amount = 50.0, unit = "г")
        )
        val steps = listOf(
            StepEntity(id = 1, recipeId = 1, stepNumber = 1, instructionText = "Взбейте яйца.")
        )
        val tags = listOf(TagEntity(id = 1, name = "Завтрак"))

        val details = RecipeWithDetails(recipe, ingredients, steps, tags)

        val jsonStr = RecipeExporter.exportDatabaseToJson(listOf(details))
        val importedList = RecipeExporter.importDatabaseFromJson(jsonStr)

        assertEquals(1, importedList.size)
        val imported = importedList[0]
        assertEquals("Омлет с сыром", imported.recipe.title)
        assertEquals(2, imported.ingredients.size)
        assertEquals("Яйца", imported.ingredients[0].name)
        assertEquals(1, imported.steps.size)
        assertEquals(1, imported.tags.size)
        assertEquals("Завтрак", imported.tags[0].name)
    }

    @Test
    fun testFormatHumanReadableText() {
        val recipe = RecipeEntity(
            id = 1,
            title = "Оладьи",
            baseServings = 4.0,
            yieldUnit = "порций"
        )
        val ingredients = listOf(
            IngredientEntity(id = 1, recipeId = 1, name = "Мука", amount = 250.0, unit = "г")
        )
        val steps = listOf(
            StepEntity(id = 1, recipeId = 1, stepNumber = 1, instructionText = "Смешайте ингредиенты.")
        )
        val details = RecipeWithDetails(recipe, ingredients, steps, emptyList())

        val text = RecipeExporter.formatHumanReadableText(details)
        assertTrue(text.contains("Оладьи"))
        assertTrue(text.contains("Мука"))
        assertTrue(text.contains("250"))
    }
}
