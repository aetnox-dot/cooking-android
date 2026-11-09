package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.domain.ai.GeminiApiService
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.domain.parser.RecipeImportParser
import com.aetnox.cooking.domain.parser.StepTokenParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IngredientUiItem(
    val id: Long = 0,
    val name: String = "",
    val amountStr: String = "",
    val unit: String = "г",
    val selectedUnit: String = "г",
    val note: String = "",
    val groupName: String = ""
)

data class StepUiItem(
    val id: Long = 0,
    val stepNumber: Int = 1,
    val instructionText: String = "",
    val cursorIndex: Int = 0,
    val imageUris: List<String> = emptyList()
)

data class RecipeEditUiState(
    val recipeId: Long = 0,
    val isQuickDraft: Boolean = false,
    val title: String = "",
    val prepTimeMinutesStr: String = "",
    val cookTimeMinutesStr: String = "",
    val servingsEnabled: Boolean = true,
    val baseServingsStr: String = "4",
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
    val imageUris: List<String> = emptyList(),
    val showNutrition: Boolean = false,
    val caloriesStr: String = "",
    val proteinStr: String = "",
    val fatStr: String = "",
    val carbsStr: String = "",
    val ingredients: List<IngredientUiItem> = listOf(IngredientUiItem()),
    val steps: List<StepUiItem> = listOf(StepUiItem(stepNumber = 1)),
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false,
    val aiFillAnimationTrigger: Long = 0L
)

class RecipeEditViewModel(
    private val repository: RecipeRepository,
    private val recipeIdToEdit: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeEditUiState())
    val uiState: StateFlow<RecipeEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allTags.collect { dbTags ->
                val names = dbTags.map { it.name }
                _uiState.value = _uiState.value.copy(availableTags = names)
            }
        }
        if (recipeIdToEdit != null && recipeIdToEdit > 0) {
            loadRecipe(recipeIdToEdit)
        } else {
            _uiState.value = _uiState.value.copy(isLoaded = true)
        }
    }

    private fun loadRecipe(id: Long) {
        viewModelScope.launch {
            val item = repository.getRecipeByIdSync(id)
            if (item != null) {
                val r = item.recipe
                val ingUi = item.ingredients.map { ing ->
                    IngredientUiItem(
                        id = ing.id,
                        name = ing.name,
                        amountStr = if (ing.amount > 0) UnitConverter.formatAmount(ing.amount, ing.unit) else "",
                        unit = ing.unit,
                        selectedUnit = ing.selectedUnit,
                        note = ing.note,
                        groupName = ing.groupName
                    )
                }
                val stepUi = item.steps.sortedBy { it.stepNumber }.map { s ->
                    val stepImages = try {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(s.imageUrisJson)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    StepUiItem(
                        id = s.id,
                        stepNumber = s.stepNumber,
                        instructionText = s.instructionText,
                        imageUris = stepImages
                    )
                }
                val imageList = try {
                    kotlinx.serialization.json.Json.decodeFromString<List<String>>(r.imageUrisJson)
                } catch (e: Exception) {
                    emptyList()
                }

                _uiState.value = RecipeEditUiState(
                    recipeId = r.id,
                    isQuickDraft = r.rawNotes.isNotBlank() && r.title.isBlank(),
                    title = r.title,
                    prepTimeMinutesStr = if (r.prepTimeMinutes > 0) r.prepTimeMinutes.toString() else "",
                    cookTimeMinutesStr = if (r.cookTimeMinutes > 0) r.cookTimeMinutes.toString() else "",
                    servingsEnabled = r.servingsEnabled,
                    baseServingsStr = UnitConverter.formatAmount(r.baseServings, "шт"),
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
                    imageUris = imageList,
                    showNutrition = r.calories != null || r.protein != null || r.fat != null || r.carbs != null,
                    caloriesStr = r.calories?.toString() ?: "",
                    proteinStr = r.protein?.toString() ?: "",
                    fatStr = r.fat?.toString() ?: "",
                    carbsStr = r.carbs?.toString() ?: "",
                    ingredients = if (ingUi.isEmpty()) listOf(IngredientUiItem()) else ingUi,
                    steps = if (stepUi.isEmpty()) listOf(StepUiItem(stepNumber = 1)) else stepUi,
                    tags = item.tags.map { it.name },
                    isLoaded = true
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoaded = true)
            }
        }
    }

    fun setQuickDraftMode(isDraft: Boolean) {
        _uiState.value = _uiState.value.copy(isQuickDraft = isDraft)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updatePrepTime(timeStr: String) {
        _uiState.value = _uiState.value.copy(prepTimeMinutesStr = timeStr)
    }

    fun updateCookTime(timeStr: String) {
        _uiState.value = _uiState.value.copy(cookTimeMinutesStr = timeStr)
    }

    fun updateServingsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(servingsEnabled = enabled)
    }

    fun updateBaseServings(servingsStr: String) {
        _uiState.value = _uiState.value.copy(baseServingsStr = servingsStr)
    }

    fun updateYieldUnit(unit: String) {
        _uiState.value = _uiState.value.copy(yieldUnit = unit)
    }

    fun updateRating(r: Int) {
        _uiState.value = _uiState.value.copy(rating = r)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateRawNotes(rawNotes: String) {
        _uiState.value = _uiState.value.copy(rawNotes = rawNotes)
    }

    fun updateSourceUrl(url: String) {
        _uiState.value = _uiState.value.copy(sourceUrl = url)
    }

    fun updateChefHeatingType(str: String) { _uiState.value = _uiState.value.copy(chefHeatingType = str) }
    fun updateChefTemperature(str: String) { _uiState.value = _uiState.value.copy(chefTemperature = str) }
    fun updateChefTiming(str: String) { _uiState.value = _uiState.value.copy(chefTiming = str) }
    fun updateChefReadinessSign(str: String) { _uiState.value = _uiState.value.copy(chefReadinessSign = str) }

    fun populateFromParsedRecipe(parsed: ParsedRecipe) {
        val r = parsed.recipe
        val ingUi = parsed.ingredients.map { ing ->
            IngredientUiItem(
                id = 0,
                name = ing.name,
                amountStr = if (ing.amount > 0) UnitConverter.formatAmount(ing.amount, ing.unit) else "",
                unit = ing.unit,
                selectedUnit = ing.selectedUnit,
                note = ing.note,
                groupName = ing.groupName
            )
        }
        val stepUi = parsed.steps.mapIndexed { idx, st ->
            StepUiItem(
                id = 0,
                stepNumber = idx + 1,
                instructionText = st.instructionText,
                imageUris = emptyList()
            )
        }
        _uiState.value = _uiState.value.copy(
            title = r.title,
            prepTimeMinutesStr = if (r.prepTimeMinutes > 0) r.prepTimeMinutes.toString() else "",
            cookTimeMinutesStr = if (r.cookTimeMinutes > 0) r.cookTimeMinutes.toString() else "",
            baseServingsStr = if (r.baseServings > 0) UnitConverter.formatAmount(r.baseServings, "порций") else "4",
            yieldUnit = r.yieldUnit.ifBlank { "порций" },
            chefHeatingType = r.chefHeatingType,
            chefTemperature = r.chefTemperature,
            chefTiming = r.chefTiming,
            chefReadinessSign = r.chefReadinessSign,
            notes = r.notes,
            caloriesStr = r.calories?.let { UnitConverter.formatAmount(it, "") } ?: "",
            proteinStr = r.protein?.let { UnitConverter.formatAmount(it, "") } ?: "",
            fatStr = r.fat?.let { UnitConverter.formatAmount(it, "") } ?: "",
            carbsStr = r.carbs?.let { UnitConverter.formatAmount(it, "") } ?: "",
            ingredients = ingUi.ifEmpty { listOf(IngredientUiItem()) },
            steps = stepUi.ifEmpty { listOf(StepUiItem(stepNumber = 1)) },
            tags = parsed.tags,
            aiFillAnimationTrigger = System.currentTimeMillis()
        )
    }

    fun generateWithGemini(
        context: Context,
        prompt: String,
        imageUriStr: String?,
        onConnected: () -> Unit = {},
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val parsed = GeminiApiService.generateRecipe(
                    context = context,
                    promptInput = prompt,
                    imageUriStr = imageUriStr,
                    onConnected = onConnected
                )
                populateFromParsedRecipe(parsed)
                onSuccess()
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Ошибка оцифровки ИИ")
            }
        }
    }

    fun toggleNutritionBlock() {
        _uiState.value = _uiState.value.copy(showNutrition = !_uiState.value.showNutrition)
    }

    fun updateCalories(str: String) { _uiState.value = _uiState.value.copy(caloriesStr = str) }
    fun updateProtein(str: String) { _uiState.value = _uiState.value.copy(proteinStr = str) }
    fun updateFat(str: String) { _uiState.value = _uiState.value.copy(fatStr = str) }
    fun updateCarbs(str: String) { _uiState.value = _uiState.value.copy(carbsStr = str) }

    fun addImageUri(uriStr: String) {
        val current = _uiState.value.imageUris
        if (!current.contains(uriStr)) {
            _uiState.value = _uiState.value.copy(imageUris = current + uriStr)
        }
    }

    fun removeImageUri(uriStr: String) {
        _uiState.value = _uiState.value.copy(imageUris = _uiState.value.imageUris - uriStr)
    }

    // Ingredient management
    fun addIngredient(groupName: String = "") {
        val current = _uiState.value.ingredients
        _uiState.value = _uiState.value.copy(ingredients = current + IngredientUiItem(groupName = groupName))
    }

    fun updateGroupName(oldName: String, newName: String) {
        val current = _uiState.value.ingredients.map { ing ->
            if (ing.groupName.trim() == oldName.trim()) ing.copy(groupName = newName.trim()) else ing
        }
        _uiState.value = _uiState.value.copy(ingredients = current)
    }

    fun removeIngredientGroup(groupName: String) {
        val current = _uiState.value.ingredients.filterNot { ing ->
            ing.groupName.trim() == groupName.trim()
        }
        _uiState.value = _uiState.value.copy(
            ingredients = if (current.isEmpty()) listOf(IngredientUiItem()) else current
        )
    }

    fun updateIngredient(index: Int, item: IngredientUiItem) {
        val current = _uiState.value.ingredients.toMutableList()
        if (index in current.indices) {
            current[index] = item
            _uiState.value = _uiState.value.copy(ingredients = current)
        }
    }

    fun removeIngredient(index: Int) {
        val current = _uiState.value.ingredients.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            _uiState.value = _uiState.value.copy(ingredients = current)
        }
    }

    fun moveIngredientUp(index: Int) {
        val current = _uiState.value.ingredients.toMutableList()
        if (index > 0 && index in current.indices) {
            val item = current.removeAt(index)
            current.add(index - 1, item)
            _uiState.value = _uiState.value.copy(ingredients = current)
        }
    }

    fun moveIngredientDown(index: Int) {
        val current = _uiState.value.ingredients.toMutableList()
        if (index in 0 until current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            _uiState.value = _uiState.value.copy(ingredients = current)
        }
    }

    // Step management
    fun addStep() {
        val current = _uiState.value.steps
        _uiState.value = _uiState.value.copy(
            steps = current + StepUiItem(stepNumber = current.size + 1)
        )
    }

    fun moveStepUp(index: Int) {
        val current = _uiState.value.steps.toMutableList()
        if (index > 0 && index in current.indices) {
            val item = current.removeAt(index)
            current.add(index - 1, item)
            val reindexed = current.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = reindexed)
        }
    }

    fun moveStepDown(index: Int) {
        val current = _uiState.value.steps.toMutableList()
        if (index in 0 until current.size - 1) {
            val item = current.removeAt(index)
            current.add(index + 1, item)
            val reindexed = current.mapIndexed { idx, s -> s.copy(stepNumber = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = reindexed)
        }
    }

    fun updateStep(index: Int, text: String, cursorIndex: Int = text.length) {
        val current = _uiState.value.steps.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(instructionText = text, cursorIndex = cursorIndex)
            _uiState.value = _uiState.value.copy(steps = current)
        }
    }

    fun addStepImageUri(stepIndex: Int, uriStr: String) {
        val current = _uiState.value.steps.toMutableList()
        if (stepIndex in current.indices) {
            val step = current[stepIndex]
            if (!step.imageUris.contains(uriStr)) {
                current[stepIndex] = step.copy(imageUris = step.imageUris + uriStr)
                _uiState.value = _uiState.value.copy(steps = current)
            }
        }
    }

    fun removeStepImageUri(stepIndex: Int, uriStr: String) {
        val current = _uiState.value.steps.toMutableList()
        if (stepIndex in current.indices) {
            val step = current[stepIndex]
            current[stepIndex] = step.copy(imageUris = step.imageUris - uriStr)
            _uiState.value = _uiState.value.copy(steps = current)
        }
    }

    fun insertIngredientTokenIntoStep(stepIndex: Int, ingredientName: String) {
        val current = _uiState.value.steps.toMutableList()
        if (stepIndex in current.indices) {
            val step = current[stepIndex]
            val (newText, newCursor) = StepTokenParser.insertToken(
                text = step.instructionText,
                selectionStart = step.cursorIndex,
                ingredientName = ingredientName
            )
            current[stepIndex] = step.copy(instructionText = newText, cursorIndex = newCursor)
            _uiState.value = _uiState.value.copy(steps = current)
        }
    }

    fun removeStep(index: Int) {
        val current = _uiState.value.steps.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            // Re-index steps
            val reindexed = current.mapIndexed { idx, item -> item.copy(stepNumber = idx + 1) }
            _uiState.value = _uiState.value.copy(steps = reindexed)
        }
    }

    // Tags
    fun addTag(tag: String) {
        val trimmed = tag.trim()
        if (trimmed.isNotBlank() && !_uiState.value.tags.contains(trimmed)) {
            _uiState.value = _uiState.value.copy(tags = _uiState.value.tags + trimmed)
        }
    }

    fun removeTag(tag: String) {
        _uiState.value = _uiState.value.copy(tags = _uiState.value.tags - tag)
    }

    // Safe Clipboard Import Preview
    fun importFromClipboardText(clipboardText: String) {
        val parsed = RecipeImportParser.parse(clipboardText)
        val r = parsed.recipe

        val ingItems = parsed.ingredients.map { ing ->
            IngredientUiItem(
                name = ing.name,
                amountStr = if (ing.amount > 0) UnitConverter.formatAmount(ing.amount, ing.unit) else "",
                unit = ing.unit,
                selectedUnit = ing.selectedUnit,
                note = ing.note,
                groupName = ing.groupName
            )
        }

        val stepItems = parsed.steps.map { s ->
            StepUiItem(stepNumber = s.stepNumber, instructionText = s.instructionText)
        }

        _uiState.value = _uiState.value.copy(
            isQuickDraft = false,
            title = r.title,
            prepTimeMinutesStr = if (r.prepTimeMinutes > 0) r.prepTimeMinutes.toString() else "",
            cookTimeMinutesStr = if (r.cookTimeMinutes > 0) r.cookTimeMinutes.toString() else "",
            baseServingsStr = UnitConverter.formatAmount(r.baseServings, "шт"),
            yieldUnit = r.yieldUnit,
            notes = r.notes,
            sourceUrl = r.sourceUrl,
            showNutrition = r.calories != null || r.protein != null || r.fat != null || r.carbs != null,
            caloriesStr = r.calories?.toString() ?: "",
            proteinStr = r.protein?.toString() ?: "",
            fatStr = r.fat?.toString() ?: "",
            carbsStr = r.carbs?.toString() ?: "",
            ingredients = if (ingItems.isEmpty()) listOf(IngredientUiItem()) else ingItems,
            steps = if (stepItems.isEmpty()) listOf(StepUiItem(stepNumber = 1)) else stepItems,
            tags = (_uiState.value.tags + parsed.tags).distinct()
        )
    }

    fun saveRecipe(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            val (amount, _) = RecipeImportParser.parseSafeAmount(state.baseServingsStr)
            val servingsVal = if (amount > 0) amount else 4.0

            val prepMinutes = state.prepTimeMinutesStr.toIntOrNull() ?: 0
            val cookMinutes = state.cookTimeMinutesStr.toIntOrNull() ?: 0

            val recipeTitle = if (state.isQuickDraft) {
                state.title.ifBlank { "Быстрая заметка ${System.currentTimeMillis() % 10000}" }
            } else {
                state.title.ifBlank { "Новый рецепт" }
            }

            val imagesJson = kotlinx.serialization.json.Json.encodeToString(state.imageUris)

            val recipe = RecipeEntity(
                id = state.recipeId,
                title = recipeTitle,
                prepTimeMinutes = prepMinutes,
                cookTimeMinutes = cookMinutes,
                servingsEnabled = state.servingsEnabled,
                baseServings = servingsVal,
                yieldUnit = state.yieldUnit.ifBlank { "порций" },
                status = state.status,
                rating = state.rating,
                postCookNote = state.postCookNote,
                chefHeatingType = state.chefHeatingType,
                chefTemperature = state.chefTemperature,
                chefTiming = state.chefTiming,
                chefReadinessSign = state.chefReadinessSign,
                notes = state.notes,
                rawNotes = state.rawNotes,
                sourceUrl = state.sourceUrl,
                imageUrisJson = imagesJson,
                calories = state.caloriesStr.toDoubleOrNull(),
                protein = state.proteinStr.toDoubleOrNull(),
                fat = state.fatStr.toDoubleOrNull(),
                carbs = state.carbsStr.toDoubleOrNull(),
                updatedAt = System.currentTimeMillis()
            )

            val ingredients = state.ingredients.mapIndexed { idx, item ->
                val (ingAmount, extraNote) = RecipeImportParser.parseSafeAmount(item.amountStr)
                val combinedNote = listOfNotNull(
                    item.note.ifBlank { null },
                    extraNote?.ifBlank { null }
                ).joinToString("; ")

                IngredientEntity(
                    id = item.id,
                    recipeId = state.recipeId,
                    name = item.name.ifBlank { "Ингредиент ${idx + 1}" },
                    amount = ingAmount,
                    unit = item.unit.ifBlank { "г" },
                    selectedUnit = item.selectedUnit.ifBlank { item.unit.ifBlank { "г" } },
                    note = combinedNote,
                    groupName = item.groupName,
                    orderIndex = idx
                )
            }

            val steps = state.steps.mapIndexed { idx, stepItem ->
                val stepImagesJson = kotlinx.serialization.json.Json.encodeToString(stepItem.imageUris)
                StepEntity(
                    id = stepItem.id,
                    recipeId = state.recipeId,
                    stepNumber = idx + 1,
                    instructionText = stepItem.instructionText,
                    imageUrisJson = stepImagesJson
                )
            }

            val savedId = repository.saveRecipe(recipe, ingredients, steps, state.tags)
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved(savedId)
        }
    }

    class Factory(
        private val repository: RecipeRepository,
        private val recipeIdToEdit: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeEditViewModel(repository, recipeIdToEdit) as T
        }
    }
}
