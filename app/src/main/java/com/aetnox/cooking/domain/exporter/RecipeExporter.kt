package com.aetnox.cooking.domain.exporter

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.converter.UnitConverter
import kotlinx.serialization.Serializable
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

    private val compactJson = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun formatHumanReadableText(
        recipeWithDetails: RecipeWithDetails,
        currentServings: Double = recipeWithDetails.recipe.baseServings,
        isEnglish: Boolean = com.aetnox.cooking.ui.util.AppLocaleManager.isEnglish
    ): String {
        val recipe = recipeWithDetails.recipe
        val sb = StringBuilder()

        sb.append("📖 ").append(recipe.title).append("\n\n")

        if ((recipe.prepTimeMinutes > 0) || (recipe.cookTimeMinutes > 0)) {
            if (isEnglish) {
                sb.append("⏱ Prep: ").append(recipe.prepTimeMinutes).append(" min | Cook: ").append(recipe.cookTimeMinutes).append(" min\n")
            } else {
                sb.append("⏱ Подготовка: ").append(recipe.prepTimeMinutes).append(" мин | Приготовление: ").append(recipe.cookTimeMinutes).append(" мин\n")
            }
        }

        if (recipe.servingsEnabled) {
            val pcs = UnitConverter.formatAmount(currentServings, "шт")
            val yieldUnit = if (isEnglish && recipe.yieldUnit == "порций") "servings" else recipe.yieldUnit
            if (isEnglish) {
                sb.append("🍽 Servings: ").append(pcs).append(" ").append(yieldUnit).append("\n")
            } else {
                sb.append("🍽 Порций: ").append(pcs).append(" ").append(yieldUnit).append("\n")
            }
        }

        val ingTitle = if (isEnglish) "\n🛒 Ingredients:\n" else "\n🛒 Ингредиенты:\n"
        sb.append(ingTitle)

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
                val displayUnit = UnitConverter.formatUnitForDisplay(ing.selectedUnit, isEnglish)
                sb.append("• ").append(ing.name)
                if (formattedAmt.isNotEmpty()) {
                    sb.append(" — ").append(formattedAmt).append(" ").append(displayUnit)
                }
                if (ing.note.isNotBlank()) {
                    sb.append(" (").append(ing.note).append(")")
                }
                sb.append("\n")
            }
        }

        if (recipeWithDetails.steps.isNotEmpty()) {
            val stepsTitle = if (isEnglish) "\n👨‍🍳 Cooking steps:\n" else "\n👨‍🍳 Приготовление:\n"
            sb.append(stepsTitle)
            recipeWithDetails.steps.sortedBy { it.stepNumber }.forEach { step ->
                val cleanInstruction = step.instructionText.replace(Regex("""@\[([^]]+)\]"""), "$1")
                sb.append(step.stepNumber).append(". ").append(cleanInstruction).append("\n")
            }
        }

        if (recipe.notes.isNotBlank()) {
            val notesTitle = if (isEnglish) "\n💡 Notes:\n" else "\n💡 Заметки:\n"
            sb.append(notesTitle).append(recipe.notes).append("\n")
        }

        if (recipe.sourceUrl.isNotBlank()) {
            val sourceTitle = if (isEnglish) "\n🔗 Source: " else "\n🔗 Источник: "
            sb.append(sourceTitle).append(recipe.sourceUrl).append("\n")
        }

        return sb.toString().trim()
    }

    fun exportSingleRecipeToJson(recipeWithDetails: RecipeWithDetails): String {
        val exportable = recipeToExportable(recipeWithDetails)
        return compactJson.encodeToString(exportable)
    }

    fun createRecipeShareFileUri(context: android.content.Context, recipeWithDetails: RecipeWithDetails): android.net.Uri? {
        return try {
            val jsonStr = exportSingleRecipeToJson(recipeWithDetails)
            val cacheDir = java.io.File(context.cacheDir, "recipes").apply { mkdirs() }
            val sanitizeName = recipeWithDetails.recipe.title
                .replace(Regex("[^a-zA-Zа-яА-Я0-9_]"), "_")
                .take(30)
                .ifBlank { "recipe" }
            val file = java.io.File(cacheDir, "$sanitizeName.json")
            file.writeText(jsonStr)
            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
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

    fun recipeToBackupDto(recipeWithDetails: RecipeWithDetails): com.aetnox.cooking.data.remote.model.BackupRecipeDto {
        val r = recipeWithDetails.recipe
        val validStatus = when (r.status) {
            RecipeEntity.STATUS_VERIFIED -> RecipeEntity.STATUS_VERIFIED
            RecipeEntity.STATUS_NEEDS_WORK -> RecipeEntity.STATUS_NEEDS_WORK
            RecipeEntity.STATUS_ARCHIVED -> RecipeEntity.STATUS_ARCHIVED
            else -> RecipeEntity.STATUS_PLANNED
        }
        return com.aetnox.cooking.data.remote.model.BackupRecipeDto(
            id = if (r.id > 0) r.id else null,
            title = r.title.ifBlank { "Рецепт" }.take(200),
            description = null,
            prepTimeMinutes = r.prepTimeMinutes.coerceIn(0, 10080),
            cookTimeMinutes = r.cookTimeMinutes.coerceIn(0, 10080),
            servingsEnabled = r.servingsEnabled,
            baseServings = r.baseServings.coerceIn(0.1, 999.0),
            yieldUnit = r.yieldUnit.ifBlank { "порций" }.take(30),
            status = validStatus,
            isFavorite = r.isFavorite,
            rating = r.rating.coerceIn(0, 5),
            postCookNote = r.postCookNote.take(2000),
            chefHeatingType = r.chefHeatingType.take(100),
            chefTemperature = r.chefTemperature.take(20),
            notes = r.notes.take(2000),
            calories = r.calories?.coerceIn(0.0, 100000.0),
            protein = r.protein?.coerceIn(0.0, 100000.0),
            fat = r.fat?.coerceIn(0.0, 100000.0),
            carbs = r.carbs?.coerceIn(0.0, 100000.0),
            cloudId = r.cloudId?.ifBlank { null }?.take(120),
            cloudAccessCode = r.cloudAccessCode?.ifBlank { null }?.take(20),
            createdAt = if (r.createdAt > 0L) r.createdAt else System.currentTimeMillis(),
            updatedAt = if (r.updatedAt > 0L) r.updatedAt else System.currentTimeMillis(),
            ingredients = recipeWithDetails.ingredients.take(150).map { ing ->
                com.aetnox.cooking.data.remote.model.BackupIngredientDto(
                    name = ing.name.ifBlank { "Ингредиент" },
                    amount = ing.amount.coerceAtLeast(0.0),
                    unit = ing.unit.ifBlank { "г" },
                    selectedUnit = ing.selectedUnit.ifBlank { ing.unit.ifBlank { "г" } },
                    gramsPerUnit = ing.gramsPerUnit,
                    note = ing.note,
                    groupName = ing.groupName
                )
            },
            steps = recipeWithDetails.steps.sortedBy { it.stepNumber }.take(100).map { it.instructionText },
            tags = recipeWithDetails.tags.take(30).map { it.name }
        )
    }

    fun backupDtoToRecipeWithDetails(dto: com.aetnox.cooking.data.remote.model.BackupRecipeDto): RecipeWithDetails {
        val recipe = RecipeEntity(
            id = dto.id ?: 0L,
            title = dto.title,
            prepTimeMinutes = dto.prepTimeMinutes,
            cookTimeMinutes = dto.cookTimeMinutes,
            servingsEnabled = dto.servingsEnabled,
            baseServings = dto.baseServings,
            yieldUnit = dto.yieldUnit,
            status = dto.status,
            isFavorite = dto.isFavorite,
            rating = dto.rating,
            postCookNote = dto.postCookNote,
            chefHeatingType = dto.chefHeatingType,
            chefTemperature = dto.chefTemperature,
            notes = dto.notes,
            calories = dto.calories,
            protein = dto.protein,
            fat = dto.fat,
            carbs = dto.carbs,
            cloudId = dto.cloudId,
            cloudAccessCode = dto.cloudAccessCode,
            createdAt = if (dto.createdAt > 0L) dto.createdAt else System.currentTimeMillis(),
            updatedAt = if (dto.updatedAt > 0L) dto.updatedAt else System.currentTimeMillis()
        )

        val ingredients = dto.ingredients.mapIndexed { index, ing ->
            IngredientEntity(
                recipeId = recipe.id,
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

        val steps = dto.steps.mapIndexed { index, stepText ->
            StepEntity(
                recipeId = recipe.id,
                stepNumber = index + 1,
                instructionText = stepText
            )
        }

        val tags = dto.tags.map { TagEntity(name = it) }

        return RecipeWithDetails(
            recipe = recipe,
            ingredients = ingredients,
            steps = steps,
            tags = tags
        )
    }
}
