package com.aetnox.cooking.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.RecipeTagCrossRef
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    fun getAllRecipesWithDetails(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipeWithDetailsById(id: Long): Flow<RecipeWithDetails?>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeWithDetailsByIdSync(id: Long): RecipeWithDetails?

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY updatedAt DESC")
    suspend fun getAllRecipesWithDetailsSync(): List<RecipeWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipeById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<StepEntity>)

    @Query("DELETE FROM steps WHERE recipeId = :recipeId")
    suspend fun deleteStepsForRecipe(recipeId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    suspend fun getAllTagsSync(): List<TagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeTagCrossRef(crossRef: RecipeTagCrossRef)

    @Query("DELETE FROM recipe_tag_cross_ref WHERE recipeId = :recipeId")
    suspend fun deleteRecipeTagCrossRefs(recipeId: Long)

    @Transaction
    suspend fun saveFullRecipe(
        recipe: RecipeEntity,
        ingredients: List<IngredientEntity>,
        steps: List<StepEntity>,
        tags: List<String>
    ): Long {
        val recipeId = if (recipe.id == 0L) {
            insertRecipe(recipe)
        } else {
            updateRecipe(recipe)
            recipe.id
        }

        // Update ingredients
        deleteIngredientsForRecipe(recipeId)
        val prepIngredients = ingredients.mapIndexed { index, ing ->
            ing.copy(recipeId = recipeId, orderIndex = index)
        }
        insertIngredients(prepIngredients)

        // Update steps
        deleteStepsForRecipe(recipeId)
        val prepSteps = steps.mapIndexed { index, step ->
            step.copy(recipeId = recipeId, stepNumber = index + 1)
        }
        insertSteps(prepSteps)

        // Update tags
        deleteRecipeTagCrossRefs(recipeId)
        for (tagName in tags.map { it.trim() }.filter { it.isNotEmpty() }) {
            var tag = getTagByName(tagName)
            if (tag == null) {
                val tagId = insertTag(TagEntity(name = tagName))
                tag = TagEntity(id = tagId, name = tagName)
            }
            if (tag.id > 0) {
                insertRecipeTagCrossRef(RecipeTagCrossRef(recipeId = recipeId, tagId = tag.id))
            }
        }

        return recipeId
    }

    @Transaction
    suspend fun clearAllData() {
        deleteIngredientsAll()
        deleteStepsAll()
        deleteRecipeTagCrossRefsAll()
        deleteRecipesAll()
    }

    @Transaction
    suspend fun replaceAllData(items: List<RecipeWithDetails>) {
        clearAllDataIncludingTags()
        for (item in items) {
            saveFullRecipe(
                recipe = item.recipe,
                ingredients = item.ingredients,
                steps = item.steps,
                tags = item.tags.map { it.name }
            )
        }
    }

    @Transaction
    suspend fun clearAllDataIncludingTags() {
        deleteIngredientsAll()
        deleteStepsAll()
        deleteRecipeTagCrossRefsAll()
        deleteRecipesAll()
        deleteTagsAll()
    }

    @Query("DELETE FROM tags")
    suspend fun deleteTagsAll()

    @Query("DELETE FROM ingredients")
    suspend fun deleteIngredientsAll()

    @Query("DELETE FROM steps")
    suspend fun deleteStepsAll()

    @Query("DELETE FROM recipe_tag_cross_ref")
    suspend fun deleteRecipeTagCrossRefsAll()

    @Query("DELETE FROM recipes")
    suspend fun deleteRecipesAll()
}
