package com.aetnox.cooking.domain.exporter

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.converter.UnitConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ExportableRecipe(
    val title: String,
    val prepTimeMinutes: Int = 0,
    val cookTimeMinutes: Int = 0,
    val servingsEnabled: Boolean = true,
    val baseServings: Double = 4.0,
    val yieldUnit: String = "порций",
    val status: String = RecipeEntity.STATUS_PLANNED,
    val rating: Int = 0,
    val postCookNote: String = "",
    val chefHeatingType: String = "",
    val chefTemperature: String = "",
    val chefTiming: String = "",
    val chefReadinessSign: String = "",
    val notes: String = "",
    val rawNotes: String = "",
    val sourceUrl: String = "",
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null,
    val ingredients: List<ExportableIngredient> = emptyList(),
    val steps: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

@Serializable
data class ExportableIngredient(
    val name: String,
    val amount: Double = 0.0,
    val unit: String = "г",
    val selectedUnit: String = unit,
    val gramsPerUnit: Double? = null,
    val note: String = "",
    val groupName: String = ""
)

@Serializable
data class FullDatabaseExport(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val recipes: List<ExportableRecipe>
)

object RecipeExporter {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun formatHumanReadableText(
        recipeWithDetails: RecipeWithDetails,
        currentServings: Double = recipeWithDetails.recipe.baseServings
    ): String {
        val recipe = recipeWithDetails.recipe
        val sb = StringBuilder()

        sb.append("📖 ").append(recipe.title).append("\n\n")

        if (recipe.prepTimeMinutes > 0 || recipe.cookTimeMinutes > 0) {
            sb.append("⏱ Подготовка: ").append(recipe.prepTimeMinutes).append(" мин | Приготовление: ").append(recipe.cookTimeMinutes).append(" мин\n")
        }

        if (recipe.servingsEnabled) {
            sb.append("🍽 Порций: ").append(UnitConverter.formatAmount(currentServings, "шт")).append(" ").append(recipe.yieldUnit).append("\n")
        }

        sb.append("\n🛒 Ингредиенты:\n")

        val grouped = recipeWithDetails.ingredients.groupBy { it.groupName.trim() }
        grouped.forEach { (groupName, list) ->
            if (groupName.isNotEmpty()) {
                sb.append("\n📌 ").append(groupName).append(":\n")
            }
            for (ing in list) {
                val formattedAmt = UnitConverter.scaleAndFormatAmount(
                    amount = ing.amount,
                    fromUnit = ing.unit,
                    toUnit = ing.selectedUnit,
                    baseServings = recipe.baseServings,
                    currentServings = currentServings,
                    ingredientName = ing.name,
                    customGramsPerUnit = ing.gramsPerUnit
                )
                sb.append("• ").append(ing.name)
                if (formattedAmt.isNotEmpty()) {
                    sb.append(" — ").append(formattedAmt).append(" ").append(ing.selectedUnit)
                }
                if (ing.note.isNotBlank()) {
                    sb.append(" (").append(ing.note).append(")")
                }
                sb.append("\n")
            }
        }

        if (recipeWithDetails.steps.isNotEmpty()) {
            sb.append("\n👨‍🍳 Приготовление:\n")
            recipeWithDetails.steps.sortedBy { it.stepNumber }.forEach { step ->
                val cleanInstruction = step.instructionText.replace(Regex("""@\[([^\]]+)\]"""), "$1")
                sb.append(step.stepNumber).append(". ").append(cleanInstruction).append("\n")
            }
        }

        if (recipe.notes.isNotBlank()) {
            sb.append("\n💡 Заметки:\n").append(recipe.notes).append("\n")
        }

        if (recipe.sourceUrl.isNotBlank()) {
            sb.append("\n🔗 Источник: ").append(recipe.sourceUrl).append("\n")
        }

        return sb.toString().trim()
    }

    fun exportSingleRecipeToJson(recipeWithDetails: RecipeWithDetails): String {
        val exportable = recipeToExportable(recipeWithDetails)
        return json.encodeToString(exportable)
    }

    fun createRecipeShareFileUri(context: android.content.Context, recipeWithDetails: RecipeWithDetails): android.net.Uri? {
        return try {
            val jsonStr = exportSingleRecipeToJson(recipeWithDetails)
            val cacheDir = java.io.File(context.cacheDir, "recipes").apply { mkdirs() }
            val sanitizeName = recipeWithDetails.recipe.title
                .replace(Regex("[^a-zA-Zа-яА-Я0-9_]"), "_")
                .take(30)
                .ifBlank { "recipe" }
            val file = java.io.File(cacheDir, "${sanitizeName}.json")
            file.writeText(jsonStr)
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    fun exportDatabaseToJson(recipes: List<RecipeWithDetails>): String {
        val exportList = recipes.map { recipeToExportable(it) }
        val dbExport = FullDatabaseExport(recipes = exportList)
        return json.encodeToString(dbExport)
    }

    fun importSingleRecipeFromJson(jsonStr: String): RecipeWithDetails {
        val exportable = json.decodeFromString<ExportableRecipe>(jsonStr)
        return exportableToRecipeWithDetails(exportable)
    }

    fun importDatabaseFromJson(jsonStr: String): List<RecipeWithDetails> {
        val dbExport = json.decodeFromString<FullDatabaseExport>(jsonStr)
        return dbExport.recipes.map { exportableToRecipeWithDetails(it) }
    }

    private fun recipeToExportable(recipeWithDetails: RecipeWithDetails): ExportableRecipe {
        val r = recipeWithDetails.recipe
        return ExportableRecipe(
            title = r.title,
            prepTimeMinutes = r.prepTimeMinutes,
            cookTimeMinutes = r.cookTimeMinutes,
            servingsEnabled = r.servingsEnabled,
            baseServings = r.baseServings,
            yieldUnit = r.yieldUnit,
            status = r.status,
            rating = r.rating,
            postCookNote = r.postCookNote,
            chefHeatingType = r.chefHeatingType,
            chefTemperature = r.chefTemperature,
            chefTiming = r.chefTiming,
            chefReadinessSign = r.chefReadinessSign,
            notes = r.notes,
            rawNotes = r.rawNotes,
            sourceUrl = r.sourceUrl,
            calories = r.calories,
            protein = r.protein,
            fat = r.fat,
            carbs = r.carbs,
            ingredients = recipeWithDetails.ingredients.map {
                ExportableIngredient(
                    name = it.name,
                    amount = it.amount,
                    unit = it.unit,
                    selectedUnit = it.selectedUnit,
                    gramsPerUnit = it.gramsPerUnit,
                    note = it.note,
                    groupName = it.groupName
                )
            },
            steps = recipeWithDetails.steps.sortedBy { it.stepNumber }.map { it.instructionText },
            tags = recipeWithDetails.tags.map { it.name }
        )
    }

    private fun exportableToRecipeWithDetails(exp: ExportableRecipe): RecipeWithDetails {
        val recipe = RecipeEntity(
            title = exp.title,
            prepTimeMinutes = exp.prepTimeMinutes,
            cookTimeMinutes = exp.cookTimeMinutes,
            servingsEnabled = exp.servingsEnabled,
            baseServings = exp.baseServings,
            yieldUnit = exp.yieldUnit,
            status = exp.status,
            rating = exp.rating,
            postCookNote = exp.postCookNote,
            chefHeatingType = exp.chefHeatingType,
            chefTemperature = exp.chefTemperature,
            chefTiming = exp.chefTiming,
            chefReadinessSign = exp.chefReadinessSign,
            notes = exp.notes,
            rawNotes = exp.rawNotes,
            sourceUrl = exp.sourceUrl,
            calories = exp.calories,
            protein = exp.protein,
            fat = exp.fat,
            carbs = exp.carbs
        )

        val ingredients = exp.ingredients.mapIndexed { index, ing ->
            IngredientEntity(
                recipeId = 0,
                name = ing.name,
                amount = ing.amount,
                unit = ing.unit,
                selectedUnit = ing.selectedUnit,
                gramsPerUnit = ing.gramsPerUnit,
                note = ing.note,
                groupName = ing.groupName,
                orderIndex = index
            )
        }

        val steps = exp.steps.mapIndexed { index, stepText ->
            StepEntity(
                recipeId = 0,
                stepNumber = index + 1,
                instructionText = stepText
            )
        }

        val tags = exp.tags.map { TagEntity(name = it) }

        return RecipeWithDetails(
            recipe = recipe,
            ingredients = ingredients,
            steps = steps,
            tags = tags
        )
    }
}
