package com.aetnox.cooking.data.repository

import com.aetnox.cooking.data.dao.RecipeDao
import com.aetnox.cooking.data.database.SampleData
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.exporter.RecipeExporter
import com.aetnox.cooking.ui.util.AppLogger
import kotlinx.coroutines.flow.Flow

class RecipeRepository(private val recipeDao: RecipeDao) {

    val allRecipes: Flow<List<RecipeWithDetails>> = recipeDao.getAllRecipesWithDetails()
    val allTags: Flow<List<TagEntity>> = recipeDao.getAllTags()

    fun getRecipeById(id: Long): Flow<RecipeWithDetails?> {
        return recipeDao.getRecipeWithDetailsById(id)
    }

    suspend fun getRecipeByIdSync(id: Long): RecipeWithDetails? {
        return recipeDao.getRecipeWithDetailsByIdSync(id)
    }

    suspend fun saveRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        tags: List<String>
    ): Long {
        AppLogger.d("REPO", "saveRecipe: title='${recipe.title}', ingredientsCount=${ingredients.size}, stepsCount=${steps.size}")
        return recipeDao.saveFullRecipe(recipe, ingredients, steps, tags)
    }

    suspend fun addSampleDemoRecipe(): Long {
        AppLogger.i("REPO", "addSampleDemoRecipe: creating sample brownie recipe")
        val sample = SampleData.createSampleRecipeData()
        return recipeDao.saveFullRecipe(
            recipe = sample.recipe,
            ingredients = sample.ingredients,
            steps = sample.steps,
            tags = sample.tags
        )
    }

    suspend fun getOrCreateSampleRecipeId(): Long {
        val all = recipeDao.getAllRecipesWithDetailsSync()
        val existingSample = all.find {
            it.recipe.title.contains("Брауни", ignoreCase = true) ||
            it.tags.any { tag -> tag.name.equals("Образец", ignoreCase = true) }
        }
        val id = existingSample?.recipe?.id ?: addSampleDemoRecipe()
        AppLogger.d("REPO", "getOrCreateSampleRecipeId: resultId=$id")
        return id
    }

    suspend fun updateRecipeFavorite(recipeId: Long, isFavorite: Boolean) {
        AppLogger.d("REPO", "updateRecipeFavorite: recipeId=$recipeId, isFavorite=$isFavorite")
        recipeDao.updateFavoriteStatus(recipeId, isFavorite)
    }

    suspend fun updateRecipeCartStatus(recipeId: Long, isInCart: Boolean) {
        AppLogger.d("REPO", "updateRecipeCartStatus: recipeId=$recipeId, isInCart=$isInCart")
        recipeDao.updateCartStatus(recipeId, isInCart)
    }

    suspend fun clearCartRecipes() {
        AppLogger.d("REPO", "clearCartRecipes")
        recipeDao.clearAllCartRecipes()
    }

    suspend fun updateRecipeCloudInfo(recipeId: Long, cloudId: String?, cloudAccessCode: String?) {
        recipeDao.updateCloudInfo(recipeId, cloudId, cloudAccessCode)
    }

    suspend fun updateRecipeStatusAndRating(
        recipeId: Long,
        status: String,
        rating: Int,
        postCookNote: String
    ) {
        val recipeWithDetails = recipeDao.getRecipeWithDetailsByIdSync(recipeId) ?: return
        val updated = recipeWithDetails.recipe.copy(
            status = status,
            rating = rating,
            postCookNote = postCookNote,
            updatedAt = System.currentTimeMillis()
        )
        recipeDao.updateRecipe(updated)
    }

    suspend fun updateIngredientSelectedUnit(ingredientId: Long, recipeId: Long, selectedUnit: String) {
        val recipeWithDetails = recipeDao.getRecipeWithDetailsByIdSync(recipeId) ?: return
        val updatedIngredients = recipeWithDetails.ingredients.map { ing ->
            if (ing.id == ingredientId) ing.copy(selectedUnit = selectedUnit) else ing
        }
        recipeDao.insertIngredients(updatedIngredients)
    }

    suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteRecipeById(id)
    }

    suspend fun exportFullBackupJson(): String {
        val all = recipeDao.getAllRecipesWithDetailsSync()
        return RecipeExporter.exportDatabaseToJson(all)
    }

    suspend fun exportSingleRecipeJson(recipeId: Long): String {
        val recipeWithDetails = recipeDao.getRecipeWithDetailsByIdSync(recipeId)
            ?: throw IllegalStateException("Рецепт №$recipeId не найден")
        return RecipeExporter.exportSingleRecipeToJson(recipeWithDetails)
    }

    suspend fun importFullBackupJson(jsonStr: String) {
        val imported = RecipeExporter.importDatabaseFromJson(jsonStr)
        // Атомарно: либо вся база заменена, либо осталась прежней (парсинг — до транзакции)
        recipeDao.replaceAllData(imported)
    }

    suspend fun importSingleRecipeJson(jsonStr: String): Long {
        val imported = RecipeExporter.importSingleRecipeFromJson(jsonStr)
        return recipeDao.saveFullRecipe(
            recipe = imported.recipe,
            ingredients = imported.ingredients,
            steps = imported.steps,
            tags = imported.tags.map { it.name }
        )
    }

    suspend fun uploadCloudBackup(context: android.content.Context): Result<com.aetnox.cooking.data.remote.model.CloudBackupUploadResponse> {
        return try {
            val all = recipeDao.getAllRecipesWithDetailsSync()
            if (all.size > 500) {
                return Result.failure(IllegalStateException("В вашей базе данных ${all.size} рецептов. Облачный бэкап поддерживает максимум 500 рецептов."))
            }
            val dtos = all.map { RecipeExporter.recipeToBackupDto(it) }
            val repo = com.aetnox.cooking.data.remote.BackendRepository(context.applicationContext)
            repo.uploadCloudBackup(dtos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreCloudBackup(context: android.content.Context): Result<Int> {
        return try {
            val repo = com.aetnox.cooking.data.remote.BackendRepository(context.applicationContext)
            val res = repo.getCloudBackup().getOrThrow()
            if (!res.exists || res.recipes.isEmpty()) {
                return Result.failure(IllegalStateException("Облачный бэкап отсутствует"))
            }
            val imported = res.recipes.map { RecipeExporter.backupDtoToRecipeWithDetails(it) }
            recipeDao.replaceAllData(imported)
            Result.success(imported.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCloudBackupStatus(context: android.content.Context): Result<com.aetnox.cooking.data.remote.model.CloudBackupGetResponse> {
        return try {
            val repo = com.aetnox.cooking.data.remote.BackendRepository(context.applicationContext)
            repo.getCloudBackup()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyProposedPatch(
        recipeId: Long,
        patch: com.aetnox.cooking.data.remote.model.ProposedUpdatePatchDto
    ): RecipeWithDetails {
        val current = recipeDao.getRecipeWithDetailsByIdSync(recipeId)
            ?: throw IllegalStateException("Рецепт №$recipeId не найден")

        val r = current.recipe

        fun isSummaryText(text: String?): Boolean {
            if (text.isNullOrBlank()) return true
            val trimmed = text.trim()
            if (patch.changeSummary?.trim()?.equals(trimmed, ignoreCase = true) == true) return true
            val lower = trimmed.lowercase()
            return lower.startsWith("изменил") || lower.startsWith("обновил") ||
                    lower.startsWith("по запросу") || lower.startsWith("пользователь") ||
                    lower.startsWith("добавил") || lower.startsWith("удалил")
        }

        val newNotes = if ((patch.notes != null) && !isSummaryText(patch.notes)) patch.notes else r.notes
        val newPostCookNote = if ((patch.postCookNote != null) && !isSummaryText(patch.postCookNote)) patch.postCookNote else r.postCookNote

        val updatedRecipe = r.copy(
            title = patch.title?.ifBlank { null } ?: r.title,
            postCookNote = newPostCookNote,
            notes = newNotes,
            chefHeatingType = patch.chefHeatingType ?: r.chefHeatingType,
            chefTemperature = patch.chefTemperature ?: r.chefTemperature,
            chefTiming = patch.chefTiming ?: r.chefTiming,
            status = patch.suggestedStatus?.ifBlank { null } ?: r.status,
            updatedAt = System.currentTimeMillis()
        )

        val updatedIngredients = current.ingredients.map { ing -> ing.copy() }.toMutableList()

        patch.ingredientAmounts?.forEach { ingPatch ->
            val patchName = ingPatch.name.trim()
            if (patchName.isNotBlank()) {
                val matchIdx = updatedIngredients.indexOfFirst {
                    it.name.contains(patchName, ignoreCase = true) || patchName.contains(it.name, ignoreCase = true)
                }
                if (matchIdx != -1) {
                    val oldIng = updatedIngredients[matchIdx]
                    val parsed = com.aetnox.cooking.domain.parser.RecipeImportParser.parseAmountAndUnit(ingPatch.newAmount, baseAmount = oldIng.amount)
                    val newAmount = if (parsed.amount > 0.0) parsed.amount else oldIng.amount
                    val newUnit = parsed.unit?.ifBlank { null } ?: oldIng.unit
                    val newSelectedUnit = parsed.unit?.ifBlank { null } ?: oldIng.selectedUnit

                    val summaryLower = (patch.changeSummary ?: "").lowercase()
                    val patchAmountLower = ingPatch.newAmount.lowercase()
                    val shouldClearNote = summaryLower.contains("очистил") || summaryLower.contains("убрал описание") ||
                            summaryLower.contains("без описания") || patchAmountLower.contains("очисти")

                    val finalNote = if (shouldClearNote) ""
                                    else if (parsed.extraNote.isNullOrBlank() || parsed.extraNote.lowercase().contains("меньше") || parsed.extraNote.lowercase().contains("больше")) oldIng.note
                                    else listOfNotNull(oldIng.note.ifBlank { null }, parsed.extraNote).joinToString("; ")

                    updatedIngredients[matchIdx] = oldIng.copy(
                        amount = newAmount,
                        unit = newUnit,
                        selectedUnit = newSelectedUnit,
                        note = finalNote
                    )
                }
            }
        }

        recipeDao.saveFullRecipe(
            recipe = updatedRecipe,
            ingredients = updatedIngredients,
            steps = current.steps,
            tags = current.tags.map { it.name }
        )

        return recipeDao.getRecipeWithDetailsByIdSync(recipeId)!!
    }
}
