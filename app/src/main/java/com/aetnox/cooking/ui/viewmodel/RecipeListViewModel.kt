package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.domain.ai.GeminiApiService
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.ui.util.OnboardingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val label: String) {
    RECENT("По дате обновления"),
    TITLE("По названию (А-Я)"),
    RATING("По рейтингу ⭐"),
    TIME_ASC("По времени (быстрые)"),
    CALORIES("По калориям")
}

private data class FilterOptions(
    val searchQuery: String,
    val ingredientQuery: String,
    val selectedTags: Set<String>,
    val statusFilter: String?,
    val maxTimeMinutes: Int?,
    val minRating: Int,
    val maxCalories: Int?,
    val sortMode: SortMode
)

data class RecipeListUiState(
    val recipes: List<RecipeWithDetails> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val statusCounts: Map<String?, Int> = emptyMap(),
    val searchQuery: String = "",
    val ingredientQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val statusFilter: String? = null, // null means All
    val maxTimeMinutes: Int? = null,
    val minRating: Int = 0,
    val maxCalories: Int? = null,
    val sortMode: SortMode = SortMode.RECENT,
    val isLoading: Boolean = false,
    val isShowingOnboardingDialog: Boolean = false
)

class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _ingredientQuery = MutableStateFlow("")
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _statusFilter = MutableStateFlow<String?>(null)
    private val _maxTimeMinutes = MutableStateFlow<Int?>(null)
    private val _minRating = MutableStateFlow(0)
    private val _maxCalories = MutableStateFlow<Int?>(null)
    private val _sortMode = MutableStateFlow(SortMode.RECENT)
    private val _showOnboardingDialog = MutableStateFlow(false)

    private val filterOptionsFlow = combine(
        _searchQuery,
        _ingredientQuery,
        _selectedTags,
        _statusFilter,
        _maxTimeMinutes,
        _minRating,
        _maxCalories,
        _sortMode
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        FilterOptions(
            searchQuery = args[0] as String,
            ingredientQuery = args[1] as String,
            selectedTags = args[2] as Set<String>,
            statusFilter = args[3] as String?,
            maxTimeMinutes = args[4] as Int?,
            minRating = args[5] as Int,
            maxCalories = args[6] as Int?,
            sortMode = args[7] as SortMode
        )
    }

    val uiState: StateFlow<RecipeListUiState> = combine(
        repository.allRecipes,
        repository.allTags,
        filterOptionsFlow,
        _showOnboardingDialog
    ) { allRecipes: List<RecipeWithDetails>, allTags: List<TagEntity>, filters: FilterOptions, showOnboarding: Boolean ->
        val counts = mutableMapOf<String?, Int>()
        counts[null] = allRecipes.size
        counts[RecipeEntity.STATUS_PLANNED] = allRecipes.count { it.recipe.status == RecipeEntity.STATUS_PLANNED }
        counts[RecipeEntity.STATUS_VERIFIED] = allRecipes.count { it.recipe.status == RecipeEntity.STATUS_VERIFIED }
        counts[RecipeEntity.STATUS_NEEDS_WORK] = allRecipes.count { it.recipe.status == RecipeEntity.STATUS_NEEDS_WORK }
        counts[RecipeEntity.STATUS_ARCHIVED] = allRecipes.count { it.recipe.status == RecipeEntity.STATUS_ARCHIVED }

        val filtered = allRecipes.filter { item ->
            val recipe = item.recipe
            val matchesTitle = filters.searchQuery.isBlank() || recipe.title.contains(filters.searchQuery, ignoreCase = true)
            val matchesStatus = filters.statusFilter == null || recipe.status == filters.statusFilter

            val totalTime = recipe.prepTimeMinutes + recipe.cookTimeMinutes
            val matchesTime = filters.maxTimeMinutes == null || totalTime <= filters.maxTimeMinutes

            val matchesRating = recipe.rating >= filters.minRating

            val caloriesVal = recipe.calories ?: 0.0
            val matchesCalories = filters.maxCalories == null || (caloriesVal > 0 && caloriesVal <= filters.maxCalories)

            val ingredientTerms = filters.ingredientQuery.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
            val matchesIngredient = ingredientTerms.isEmpty() || ingredientTerms.all { term ->
                item.ingredients.any { ing -> ing.name.contains(term, ignoreCase = true) }
            }

            val matchesTags = filters.selectedTags.isEmpty() || filters.selectedTags.all { selectedTag ->
                item.tags.any { it.name.equals(selectedTag, ignoreCase = true) }
            }

            matchesTitle && matchesStatus && matchesTime && matchesRating && matchesCalories && matchesIngredient && matchesTags
        }.sortedWith { a, b ->
            when (filters.sortMode) {
                SortMode.RECENT -> b.recipe.updatedAt.compareTo(a.recipe.updatedAt)
                SortMode.TITLE -> a.recipe.title.compareTo(b.recipe.title, ignoreCase = true)
                SortMode.RATING -> b.recipe.rating.compareTo(a.recipe.rating)
                SortMode.TIME_ASC -> (a.recipe.prepTimeMinutes + a.recipe.cookTimeMinutes)
                    .compareTo(b.recipe.prepTimeMinutes + b.recipe.cookTimeMinutes)
                SortMode.CALORIES -> (a.recipe.calories ?: 0.0).compareTo(b.recipe.calories ?: 0.0)
            }
        }

        RecipeListUiState(
            recipes = filtered,
            allTags = allTags,
            statusCounts = counts,
            searchQuery = filters.searchQuery,
            ingredientQuery = filters.ingredientQuery,
            selectedTags = filters.selectedTags,
            statusFilter = filters.statusFilter,
            maxTimeMinutes = filters.maxTimeMinutes,
            minRating = filters.minRating,
            maxCalories = filters.maxCalories,
            sortMode = filters.sortMode,
            isShowingOnboardingDialog = showOnboarding
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeListUiState(isLoading = true)
    )

    fun checkOnboardingOnLaunch(context: Context) {
        if (!OnboardingManager.isOnboardingCompleted(context)) {
            _showOnboardingDialog.value = true
        }
    }

    fun showOnboarding() {
        _showOnboardingDialog.value = true
    }

    fun dismissOnboarding(context: Context) {
        _showOnboardingDialog.value = false
        OnboardingManager.setOnboardingCompleted(context, true)
    }

    fun openSampleRecipe(onRecipeIdReady: (Long) -> Unit) {
        viewModelScope.launch {
            val sampleId = repository.getOrCreateSampleRecipeId()
            onRecipeIdReady(sampleId)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onIngredientQueryChanged(query: String) {
        _ingredientQuery.value = query
    }

    fun onTagToggled(tagName: String) {
        val current = _selectedTags.value
        _selectedTags.value = if (current.contains(tagName)) current - tagName else current + tagName
    }

    fun onStatusFilterSelected(status: String?) {
        _statusFilter.value = status
    }

    fun onMaxTimeFilterSelected(minutes: Int?) {
        _maxTimeMinutes.value = minutes
    }

    fun onMinRatingFilterSelected(rating: Int) {
        _minRating.value = rating
    }

    fun onMaxCaloriesFilterSelected(calories: Int?) {
        _maxCalories.value = calories
    }

    fun onSortModeSelected(sortMode: SortMode) {
        _sortMode.value = sortMode
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _ingredientQuery.value = ""
        _selectedTags.value = emptySet()
        _statusFilter.value = null
        _maxTimeMinutes.value = null
        _minRating.value = 0
        _maxCalories.value = null
    }

    fun addSampleDemoRecipe() {
        viewModelScope.launch {
            repository.addSampleDemoRecipe()
        }
    }

    fun saveParsedRecipe(parsed: ParsedRecipe, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val savedId = repository.saveRecipe(
                recipe = parsed.recipe,
                ingredients = parsed.ingredients,
                steps = parsed.steps,
                tags = parsed.tags
            )
            onSaved(savedId)
        }
    }

    fun generateRecipeWithGemini(
        context: Context,
        prompt: String,
        imageUriStr: String?,
        onConnected: () -> Unit = {},
        onSuccess: (ParsedRecipe) -> Unit,
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
                onSuccess(parsed)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Неизвестная ошибка оцифровки")
            }
        }
    }

    fun deleteRecipe(id: Long) {
        viewModelScope.launch {
            repository.deleteRecipe(id)
        }
    }

    fun updateRecipeRating(recipeId: Long, rating: Int) {
        viewModelScope.launch {
            val item = repository.getRecipeByIdSync(recipeId) ?: return@launch
            repository.updateRecipeStatusAndRating(
                recipeId = recipeId,
                status = item.recipe.status,
                rating = rating,
                postCookNote = item.recipe.postCookNote
            )
        }
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeListViewModel(repository) as T
        }
    }
}
