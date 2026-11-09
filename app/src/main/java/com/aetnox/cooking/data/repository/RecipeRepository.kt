package com.aetnox.cooking.data.repository

import com.aetnox.cooking.data.dao.RecipeDao
import com.aetnox.cooking.data.database.SampleData
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.exporter.RecipeExporter
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
        return recipeDao.saveFullRecipe(recipe, ingredients, steps, tags)
    }

    suspend fun addSampleDemoRecipe(): Long {
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
        return existingSample?.recipe?.id ?: addSampleDemoRecipe()
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
}
