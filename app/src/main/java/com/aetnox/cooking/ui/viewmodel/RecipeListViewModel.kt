package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.remote.BackendRepository
import com.aetnox.cooking.data.remote.model.RecipeMeta
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.domain.ai.GeminiApiService
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.AppLogger
import com.aetnox.cooking.ui.util.OnboardingManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(private val ruLabel: String, private val enLabel: String) {
    RECENT("По дате обновления", "By date updated"),
    TITLE("По названию (А-Я)", "By title (A-Z)"),
    FAVORITE("Сначала избранные ⭐", "Favorites first ⭐"),
    TIME_ASC("По времени (быстрые)", "By time (fastest)"),
    CALORIES("По калориям", "By calories");

    val label: String
        get() = com.aetnox.cooking.ui.util.AppLocaleManager.tr(ruLabel, enLabel)
}

enum class MainTab(private val ruLabel: String, private val enLabel: String) {
    LOCAL("📖 Моя книга", "📖 My Cookbook"),
    CLOUD("🌐 Облачный каталог", "🌐 Cloud Catalog");

    val label: String
        get() = com.aetnox.cooking.ui.util.AppLocaleManager.tr(ruLabel, enLabel)
}

enum class CloudCategory(private val ruLabel: String, private val enLabel: String) {
    PUBLIC("🔥 Открытые рецепты", "🔥 Public Recipes"),
    MY_UPLOADS("👤 Мои загрузки", "👤 My Uploads");

    val label: String
        get() = com.aetnox.cooking.ui.util.AppLocaleManager.tr(ruLabel, enLabel)
}

private data class FilterOptions(
    val searchQuery: String,
    val ingredientQuery: String,
    val selectedTags: Set<String>,
    val statusFilter: String?,
    val maxTimeMinutes: Int?,
    val onlyFavorites: Boolean,
    val maxCalories: Int?,
    val sortMode: SortMode
)

@androidx.compose.runtime.Immutable
data class RecipeItemUiModel(
    val recipeWithDetails: RecipeWithDetails,
    val coverUri: String?,
    val totalTimeMinutes: Int,
    val ingredientsCount: Int,
    val formattedTags: List<String>
)

data class RecipeListUiState(
    // Local Cookbook state
    val recipes: List<RecipeWithDetails> = emptyList(),
    val recipeUiModels: List<RecipeItemUiModel> = emptyList(),
    val allTags: List<TagEntity> = emptyList(),
    val statusCounts: Map<String?, Int> = emptyMap(),
    val searchQuery: String = "",
    val ingredientQuery: String = "",
    val selectedTags: Set<String> = emptySet(),
    val statusFilter: String? = null, // null means All
    val maxTimeMinutes: Int? = null,
    val onlyFavorites: Boolean = false,
    val maxCalories: Int? = null,
    val sortMode: SortMode = SortMode.RECENT,
    val isLoading: Boolean = false,
    val isShowingOnboardingDialog: Boolean = false,

    // Cloud Catalog state
    val activeMainTab: MainTab = MainTab.LOCAL,
    val cloudCategory: CloudCategory = CloudCategory.PUBLIC,
    val cloudSearchQuery: String = "",
    val cloudSearchResults: List<RecipeMeta> = emptyList(),
    val isSearchingCloud: Boolean = false,
    val publicCloudRecipes: List<RecipeMeta> = emptyList(),
    val isLoadingPublicCloud: Boolean = false,
    val myCloudRecipes: List<RecipeMeta> = emptyList(),
    val isLoadingMyCloud: Boolean = false,
    val downloadingCloudIds: Set<String> = emptySet(),
    val cloudMessage: String? = null,
    val cloudSearchMessage: String? = null,

    // AI Search state
    val isAiSearchDialogVisible: Boolean = false,
    val aiSearchScope: String = "all",
    val aiSearchMode: String = "auto", // "auto", "two_phase", "single"
    val aiSearchPlanInfo: com.aetnox.cooking.data.remote.model.AiSearchPlanDto? = null,
    val aiSearchMessages: List<com.aetnox.cooking.data.remote.model.ChatMessageDto> = emptyList(),
    val aiSearchResults: List<com.aetnox.cooking.data.remote.model.AiSearchResultItemDto> = emptyList(),
    val isAiSearching: Boolean = false,
    val aiSearchError: String? = null,
    val userSession: com.aetnox.cooking.data.remote.UserSessionData = com.aetnox.cooking.data.remote.UserSessionData()
) {
    fun getSingleSearchCostForecast(): Int {
        val count = recipes.size
        val weight = maxOf(1, kotlin.math.ceil(count / 60.0).toInt())
        return weight * userSession.aiCosts.searchSinglePer60
    }

    fun getTwoPhaseSearchCostForecast(): Int {
        return userSession.aiCosts.searchPlan + userSession.aiCosts.searchFinish
    }
}

class RecipeListViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _ingredientQuery = MutableStateFlow("")
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    private val _statusFilter = MutableStateFlow<String?>(null)
    private val _maxTimeMinutes = MutableStateFlow<Int?>(null)
    private val _onlyFavorites = MutableStateFlow(false)
    private val _maxCalories = MutableStateFlow<Int?>(null)
    private val _sortMode = MutableStateFlow(SortMode.RECENT)
    private val _showOnboardingDialog = MutableStateFlow(false)

    // Cloud StateFlows
    private val _activeMainTab = MutableStateFlow(MainTab.LOCAL)
    private val _cloudCategory = MutableStateFlow(CloudCategory.PUBLIC)
    private val _cloudSearchQuery = MutableStateFlow("")
    private val _cloudSearchResults = MutableStateFlow<List<RecipeMeta>>(emptyList())
    private val _isSearchingCloud = MutableStateFlow(false)
    private val _publicCloudRecipes = MutableStateFlow<List<RecipeMeta>>(emptyList())
    private val _isLoadingPublicCloud = MutableStateFlow(false)
    private val _myCloudRecipes = MutableStateFlow<List<RecipeMeta>>(emptyList())
    private val _isLoadingMyCloud = MutableStateFlow(false)
    private val _downloadingCloudIds = MutableStateFlow<Set<String>>(emptySet())
    private val _cloudMessage = MutableStateFlow<String?>(null)
    private val _cloudSearchMessage = MutableStateFlow<String?>(null)

    // AI Search StateFlows
    private val _userSession = MutableStateFlow(com.aetnox.cooking.data.remote.UserSessionData())
    private val _isAiSearchDialogVisible = MutableStateFlow(false)
    private val _aiSearchScope = MutableStateFlow("all")
    private val _aiSearchMode = MutableStateFlow("auto")
    private val _aiSearchPlanInfo = MutableStateFlow<com.aetnox.cooking.data.remote.model.AiSearchPlanDto?>(null)
    private val _aiSearchMessages = MutableStateFlow<List<com.aetnox.cooking.data.remote.model.ChatMessageDto>>(emptyList())
    private val _aiSearchResults = MutableStateFlow<List<com.aetnox.cooking.data.remote.model.AiSearchResultItemDto>>(emptyList())
    private val _isAiSearching = MutableStateFlow(false)
    private val _aiSearchError = MutableStateFlow<String?>(null)
    private val processedFinishTokens = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    private val coverUriCache = java.util.concurrent.ConcurrentHashMap<Long, Pair<Long, String?>>()
    private val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    private fun extractCoverUri(item: RecipeWithDetails): String? {
        return runCatching {
            val list = json.decodeFromString<List<String>>(item.recipe.imageUrisJson)
            if (list.isNotEmpty()) list.first() else {
                item.steps.mapNotNull { step ->
                    runCatching {
                        json.decodeFromString<List<String>>(step.imageUrisJson).firstOrNull()
                    }.getOrNull()
                }.firstOrNull()
            }
        }.getOrNull()
    }

    private val filterOptionsFlow = combine(
        _searchQuery,
        _ingredientQuery,
        _selectedTags,
        _statusFilter,
        _maxTimeMinutes,
        _onlyFavorites,
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
            onlyFavorites = args[5] as Boolean,
            maxCalories = args[6] as Int?,
            sortMode = args[7] as SortMode
        )
    }

    private val cloudStateFlow = combine(
        _activeMainTab,
        _cloudCategory,
        _cloudSearchQuery,
        _cloudSearchResults,
        _isSearchingCloud,
        _publicCloudRecipes,
        _isLoadingPublicCloud,
        _myCloudRecipes,
        _isLoadingMyCloud,
        _downloadingCloudIds,
        _cloudMessage,
        _cloudSearchMessage
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        mapOf(
            "activeMainTab" to args[0],
            "cloudCategory" to args[1],
            "cloudSearchQuery" to args[2],
            "cloudSearchResults" to args[3],
            "isSearchingCloud" to args[4],
            "publicCloudRecipes" to args[5],
            "isLoadingPublicCloud" to args[6],
            "myCloudRecipes" to args[7],
            "isLoadingMyCloud" to args[8],
            "downloadingCloudIds" to args[9],
            "cloudMessage" to args[10],
            "cloudSearchMessage" to args[11]
        )
    }

    private val aiSearchStateFlow = combine(
        _isAiSearchDialogVisible,
        _aiSearchScope,
        _aiSearchMode,
        _aiSearchMessages,
        _aiSearchResults,
        _isAiSearching,
        _aiSearchError,
        _aiSearchPlanInfo,
        _userSession
    ) { args: Array<Any?> ->
        mapOf(
            "isAiSearchDialogVisible" to args[0],
            "aiSearchScope" to args[1],
            "aiSearchMode" to args[2],
            "aiSearchMessages" to args[3],
            "aiSearchResults" to args[4],
            "isAiSearching" to args[5],
            "aiSearchError" to args[6],
            "aiSearchPlanInfo" to args[7],
            "userSession" to args[8]
        )
    }

    val uiState: StateFlow<RecipeListUiState> = combine(
        repository.allRecipes,
        repository.allTags,
        filterOptionsFlow,
        _showOnboardingDialog,
        cloudStateFlow,
        aiSearchStateFlow
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allRecipes = args[0] as List<RecipeWithDetails>
        @Suppress("UNCHECKED_CAST")
        val allTags = args[1] as List<TagEntity>
        val filters = args[2] as FilterOptions
        val showOnboarding = args[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val cloudMap = args[4] as Map<String, Any?>
        @Suppress("UNCHECKED_CAST")
        val aiSearchMap = args[5] as Map<String, Any?>

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

            val matchesFavorite = !filters.onlyFavorites || recipe.isFavorite

            val caloriesVal = recipe.calories ?: 0.0
            val matchesCalories = filters.maxCalories == null || (caloriesVal > 0 && caloriesVal <= filters.maxCalories)

            val ingredientTerms = filters.ingredientQuery.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
            val matchesIngredient = ingredientTerms.isEmpty() || ingredientTerms.all { term ->
                item.ingredients.any { ing -> ing.name.contains(term, ignoreCase = true) }
            }

            val matchesTags = filters.selectedTags.isEmpty() || filters.selectedTags.all { selectedTag ->
                item.tags.any { it.name.equals(selectedTag, ignoreCase = true) }
            }

            matchesTitle && matchesStatus && matchesTime && matchesFavorite && matchesCalories && matchesIngredient && matchesTags
        }.sortedWith { a, b ->
            when (filters.sortMode) {
                SortMode.RECENT -> b.recipe.updatedAt.compareTo(a.recipe.updatedAt)
                SortMode.TITLE -> a.recipe.title.compareTo(b.recipe.title, ignoreCase = true)
                SortMode.FAVORITE -> b.recipe.isFavorite.compareTo(a.recipe.isFavorite)
                SortMode.TIME_ASC -> (a.recipe.prepTimeMinutes + a.recipe.cookTimeMinutes)
                    .compareTo(b.recipe.prepTimeMinutes + b.recipe.cookTimeMinutes)
                SortMode.CALORIES -> (a.recipe.calories ?: 0.0).compareTo(b.recipe.calories ?: 0.0)
            }
        }

        val uiModels = filtered.map { item ->
            val recipe = item.recipe
            val cached = coverUriCache[recipe.id]
            val coverUri = if (cached != null && cached.first == recipe.updatedAt) {
                cached.second
            } else {
                val uri = extractCoverUri(item)
                coverUriCache[recipe.id] = Pair(recipe.updatedAt, uri)
                uri
            }

            RecipeItemUiModel(
                recipeWithDetails = item,
                coverUri = coverUri,
                totalTimeMinutes = recipe.prepTimeMinutes + recipe.cookTimeMinutes,
                ingredientsCount = item.ingredients.size,
                formattedTags = item.tags.map { it.name }
            )
        }

        @Suppress("UNCHECKED_CAST")
        RecipeListUiState(
            recipes = filtered,
            recipeUiModels = uiModels,
            allTags = allTags,
            statusCounts = counts,
            searchQuery = filters.searchQuery,
            ingredientQuery = filters.ingredientQuery,
            selectedTags = filters.selectedTags,
            statusFilter = filters.statusFilter,
            maxTimeMinutes = filters.maxTimeMinutes,
            onlyFavorites = filters.onlyFavorites,
            maxCalories = filters.maxCalories,
            sortMode = filters.sortMode,
            isLoading = false,
            isShowingOnboardingDialog = showOnboarding,

            activeMainTab = cloudMap["activeMainTab"] as MainTab,
            cloudCategory = cloudMap["cloudCategory"] as CloudCategory,
            cloudSearchQuery = cloudMap["cloudSearchQuery"] as String,
            cloudSearchResults = cloudMap["cloudSearchResults"] as List<RecipeMeta>,
            isSearchingCloud = cloudMap["isSearchingCloud"] as Boolean,
            publicCloudRecipes = cloudMap["publicCloudRecipes"] as List<RecipeMeta>,
            isLoadingPublicCloud = cloudMap["isLoadingPublicCloud"] as Boolean,
            myCloudRecipes = cloudMap["myCloudRecipes"] as List<RecipeMeta>,
            isLoadingMyCloud = cloudMap["isLoadingMyCloud"] as Boolean,
            downloadingCloudIds = cloudMap["downloadingCloudIds"] as Set<String>,
            cloudMessage = cloudMap["cloudMessage"] as String?,
            cloudSearchMessage = cloudMap["cloudSearchMessage"] as String?,

            isAiSearchDialogVisible = aiSearchMap["isAiSearchDialogVisible"] as Boolean,
            aiSearchScope = aiSearchMap["aiSearchScope"] as String,
            aiSearchMode = aiSearchMap["aiSearchMode"] as String,
            aiSearchMessages = aiSearchMap["aiSearchMessages"] as List<com.aetnox.cooking.data.remote.model.ChatMessageDto>,
            aiSearchResults = aiSearchMap["aiSearchResults"] as List<com.aetnox.cooking.data.remote.model.AiSearchResultItemDto>,
            isAiSearching = aiSearchMap["isAiSearching"] as Boolean,
            aiSearchError = aiSearchMap["aiSearchError"] as String?,
            aiSearchPlanInfo = aiSearchMap["aiSearchPlanInfo"] as com.aetnox.cooking.data.remote.model.AiSearchPlanDto?,
            userSession = aiSearchMap["userSession"] as com.aetnox.cooking.data.remote.UserSessionData
        )
    }.flowOn(kotlinx.coroutines.Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
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

    // --- Tab and Cloud Navigation ---

    fun onMainTabChanged(tab: MainTab, context: Context? = null) {
        _activeMainTab.value = tab
        if (tab == MainTab.CLOUD && context != null) {
            if (_publicCloudRecipes.value.isEmpty()) {
                loadPublicCloudRecipes(context)
            }
        }
    }

    fun onCloudCategorySelected(category: CloudCategory, context: Context? = null) {
        _cloudCategory.value = category
        if (category == CloudCategory.MY_UPLOADS && context != null) {
            if (_myCloudRecipes.value.isEmpty()) {
                loadMyCloudRecipes(context)
            }
        } else if (category == CloudCategory.PUBLIC && context != null) {
            if (_publicCloudRecipes.value.isEmpty()) {
                loadPublicCloudRecipes(context)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        AppLogger.d("SEARCH_DBG", "ViewModel onSearchQueryChanged: query='$query', previous='${_searchQuery.value}'")
        _searchQuery.value = query
    }

    fun onCloudSearchQueryChanged(query: String) {
        AppLogger.d("SEARCH_DBG", "ViewModel onCloudSearchQueryChanged: query='$query', previous='${_cloudSearchQuery.value}'")
        _cloudSearchQuery.value = query
        if (query.isBlank()) {
            _cloudSearchResults.value = emptyList()
            _cloudSearchMessage.value = null
        }
    }

    fun searchCloudRecipes(context: Context, query: String) {
        val trimmed = query.trim()
        AppLogger.d("NET", "searchCloudRecipes: query='$trimmed'")
        if (trimmed.length < 2) {
            _cloudSearchMessage.value = com.aetnox.cooking.ui.util.AppLocaleManager.tr("Введите минимум 2 символа для поиска", "Enter at least 2 characters to search")
            return
        }
        viewModelScope.launch {
            _isSearchingCloud.value = true
            _cloudSearchMessage.value = null
            val repo = BackendRepository(context.applicationContext)
            val result = repo.searchCloudRecipes(trimmed)
            result.onSuccess { res ->
                val msg = if (res.results.isEmpty()) com.aetnox.cooking.ui.util.AppLocaleManager.tr("Ничего не найдено по запросу «$trimmed»", "No recipes found for \"$trimmed\"") else null
                AppLogger.d("NET", "searchCloudRecipes success: count=${res.results.size}")
                _isSearchingCloud.value = false
                _cloudSearchResults.value = res.results
                _cloudSearchMessage.value = msg
            }.onFailure { err ->
                AppLogger.w("NET", "searchCloudRecipes failed: ${err.localizedMessage}", err)
                _isSearchingCloud.value = false
                _cloudSearchMessage.value = "❌ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Ошибка поиска:", "Search error:")} ${err.localizedMessage}"
            }
        }
    }

    fun loadPublicCloudRecipes(context: Context) {
        viewModelScope.launch {
            _isLoadingPublicCloud.value = true
            val repo = BackendRepository(context.applicationContext)
            val result = repo.listPublicRecipes()
            result.onSuccess { res ->
                _isLoadingPublicCloud.value = false
                _publicCloudRecipes.value = res.recipes
            }.onFailure {
                _isLoadingPublicCloud.value = false
            }
        }
    }

    fun loadMyCloudRecipes(context: Context) {
        viewModelScope.launch {
            _isLoadingMyCloud.value = true
            val repo = BackendRepository(context.applicationContext)
            val result = repo.listMyRecipes()
            result.onSuccess { res ->
                _isLoadingMyCloud.value = false
                _myCloudRecipes.value = res.recipes
            }.onFailure {
                _isLoadingMyCloud.value = false
            }
        }
    }

    fun downloadCloudRecipeItem(
        context: Context,
        item: RecipeMeta,
        onSuccess: (Long) -> Unit = {}
    ) {
        val idToUse = item.recipeId
        viewModelScope.launch {
            _downloadingCloudIds.value = _downloadingCloudIds.value + idToUse
            _cloudMessage.value = null
            val repo = BackendRepository(context.applicationContext)

            val codeToUse = item.accessCode
            val result = if (!codeToUse.isNullOrBlank()) {
                repo.getRecipeByAccessCode(codeToUse)
            } else {
                repo.getCloudRecipe(idToUse)
            }

            result.onSuccess { cloudResp ->
                try {
                    val localId = repository.importSingleRecipeJson(
                        when (cloudResp) {
                            is com.aetnox.cooking.data.remote.model.CloudRecipeResponse -> cloudResp.payloadJson
                            is com.aetnox.cooking.data.remote.model.CloudRecipeByCodeResponse -> cloudResp.payloadJson
                            else -> ""
                        }
                    )
                    val respRecipeId = when (cloudResp) {
                        is com.aetnox.cooking.data.remote.model.CloudRecipeResponse -> cloudResp.recipeId
                        is com.aetnox.cooking.data.remote.model.CloudRecipeByCodeResponse -> cloudResp.recipeId
                        else -> idToUse
                    }
                    repository.updateRecipeCloudInfo(localId, respRecipeId, codeToUse)
                    _downloadingCloudIds.value = _downloadingCloudIds.value - idToUse
                    _cloudMessage.value = "✅ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Рецепт «${item.title}» скачан в вашу книгу!", "Recipe \"${item.title}\" downloaded to your book!")}"
                    onSuccess(localId)
                } catch (e: Exception) {
                    _downloadingCloudIds.value = _downloadingCloudIds.value - idToUse
                    _cloudMessage.value = "❌ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Ошибка сохранения:", "Save error:")} ${e.localizedMessage}"
                }
            }.onFailure { err ->
                _downloadingCloudIds.value = _downloadingCloudIds.value - idToUse
                _cloudMessage.value = "❌ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Не удалось скачать:", "Download failed:")} ${err.localizedMessage}"
            }
        }
    }

    fun deleteMyCloudRecipe(context: Context, recipeId: String) {
        viewModelScope.launch {
            val repo = BackendRepository(context.applicationContext)
            val result = repo.deleteCloudRecipe(recipeId)
            result.onSuccess {
                _cloudMessage.value = "✅ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Рецепт удален из облака", "Recipe deleted from cloud")}"
                loadMyCloudRecipes(context)
            }.onFailure { err ->
                _cloudMessage.value = "❌ ${com.aetnox.cooking.ui.util.AppLocaleManager.tr("Ошибка удаления:", "Delete error:")} ${err.localizedMessage}"
            }
        }
    }

    fun clearCloudMessage() {
        _cloudMessage.value = null
    }

    // --- Local Filter Handlers ---

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

    fun onOnlyFavoritesToggled(onlyFav: Boolean) {
        _onlyFavorites.value = onlyFav
    }

    fun toggleRecipeFavorite(recipeId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateRecipeFavorite(recipeId, !currentIsFavorite)
        }
    }

    fun toggleRecipeCartStatus(recipeId: Long, currentIsInCart: Boolean) {
        viewModelScope.launch {
            repository.updateRecipeCartStatus(recipeId, !currentIsInCart)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCartRecipes()
        }
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
        _onlyFavorites.value = false
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
                onError(com.aetnox.cooking.data.remote.NetworkDiagnostics.formatErrorMessage(context, e))
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

    fun uploadRecipeToCloud(
        context: Context,
        recipeId: Long,
        isPublic: Boolean = true,
        onSuccess: (cloudId: String, accessCode: String?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val jsonStr = repository.exportSingleRecipeJson(recipeId)
                val item = repository.getRecipeByIdSync(recipeId)
                    ?: throw IllegalStateException(AppLocaleManager.tr("Рецепт не найден", "Recipe not found"))

                val cloudId = "cloud-${recipeId}-${System.currentTimeMillis() % 100000}"
                val repo = BackendRepository(context.applicationContext)

                val result = repo.uploadRecipe(
                    recipeId = cloudId,
                    payloadJson = jsonStr,
                    isPublic = isPublic
                )

                result.onSuccess { resp ->
                    repository.updateRecipeCloudInfo(recipeId, cloudId, resp.accessCode)
                    onSuccess(cloudId, resp.accessCode)
                }.onFailure { err ->
                    onError(err.localizedMessage ?: AppLocaleManager.tr("Ошибка загрузки в облако", "Cloud upload error"))
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: AppLocaleManager.tr("Ошибка подготовки рецепта", "Recipe preparation error"))
            }
        }
    }

    fun downloadRecipeByAccessCode(
        context: Context,
        accessCode: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val repo = BackendRepository(context.applicationContext)
                val result = repo.getRecipeByAccessCode(accessCode.trim())
                result.onSuccess { resp ->
                    val localId = repository.importSingleRecipeJson(resp.payloadJson)
                    repository.updateRecipeCloudInfo(localId, resp.recipeId, accessCode.trim())
                    onSuccess(localId)
                }.onFailure { err ->
                    onError(err.localizedMessage ?: AppLocaleManager.tr("Ошибка скачивания по коду доступа", "Error downloading by access code"))
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: AppLocaleManager.tr("Ошибка сохранения рецепта", "Error saving recipe"))
            }
        }
    }

    fun downloadCloudRecipe(
        context: Context,
        cloudId: String,
        onSuccess: (Long) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val repo = BackendRepository(context.applicationContext)
                val trimmed = cloudId.trim()
                if (trimmed.isBlank()) {
                    onError(AppLocaleManager.tr("Введите ID рецепта или код доступа", "Enter recipe ID or access code"))
                    return@launch
                }

                val isAccessCodeFormat = !trimmed.startsWith("cloud-", ignoreCase = true) && trimmed.length in 6..16

                if (isAccessCodeFormat) {
                    val codeResult = repo.getRecipeByAccessCode(trimmed)
                    if (codeResult.isSuccess) {
                        val resp = codeResult.getOrThrow()
                        val localId = repository.importSingleRecipeJson(resp.payloadJson)
                        repository.updateRecipeCloudInfo(localId, resp.recipeId, trimmed)
                        onSuccess(localId)
                        return@launch
                    }
                }

                val result = repo.getCloudRecipe(trimmed)
                result.onSuccess { resp ->
                    val localId = repository.importSingleRecipeJson(resp.payloadJson)
                    repository.updateRecipeCloudInfo(localId, resp.recipeId, null)
                    onSuccess(localId)
                }.onFailure { err ->
                    if (!isAccessCodeFormat) {
                        val fallback = repo.getRecipeByAccessCode(trimmed)
                        if (fallback.isSuccess) {
                            val resp = fallback.getOrThrow()
                            val localId = repository.importSingleRecipeJson(resp.payloadJson)
                            repository.updateRecipeCloudInfo(localId, resp.recipeId, trimmed)
                            onSuccess(localId)
                            return@launch
                        }
                    }
                    onError(err.localizedMessage ?: AppLocaleManager.tr("Ошибка скачивания рецепта из облака", "Error downloading recipe from cloud"))
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: AppLocaleManager.tr("Ошибка сохранения рецепта", "Error saving recipe"))
            }
        }
    }

    // --- ИИ Диалоговый поиск рецептов ---

    fun initSessionObserver(context: Context) {
        viewModelScope.launch {
            com.aetnox.cooking.data.remote.SessionManager.getSessionFlow(context).collect { session ->
                _userSession.value = session
            }
        }
    }

    fun showAiSearchDialog() {
        _isAiSearchDialogVisible.value = true
    }

    fun hideAiSearchDialog() {
        _isAiSearchDialogVisible.value = false
    }

    fun setAiSearchScope(scope: String) {
        _aiSearchScope.value = scope
    }

    fun setAiSearchMode(mode: String) {
        _aiSearchMode.value = mode
    }

    fun clearAiSearch() {
        _aiSearchMessages.value = emptyList()
        _aiSearchResults.value = emptyList()
        _aiSearchError.value = null
        _aiSearchPlanInfo.value = null
        processedFinishTokens.clear()
    }

    fun sendAiSearchQuery(context: Context, userQuery: String) {
        val trimmed = userQuery.trim()
        if (trimmed.isEmpty() || _isAiSearching.value) return

        val newMsg = com.aetnox.cooking.data.remote.model.ChatMessageDto(
            role = "user",
            content = trimmed.take(600)
        )
        val currentMessages = (_aiSearchMessages.value + newMsg).map {
            it.copy(content = it.content.take(600))
        }
        _aiSearchMessages.value = currentMessages
        _isAiSearching.value = true
        _aiSearchError.value = null

        viewModelScope.launch {
            try {
                val (canUse, reason) = com.aetnox.cooking.data.remote.AppStartupManager.canUseNetworkAi(context, isSearch = true)
                if (!canUse) {
                    _aiSearchError.value = reason
                    _isAiSearching.value = false
                    return@launch
                }

                val repo = BackendRepository(context.applicationContext)
                val scope = _aiSearchScope.value
                val currentRecipes = if (scope != "cloud") repository.allRecipes.first() else emptyList()
                val installedCloudIds = currentRecipes.mapNotNull { it.recipe.cloudId?.ifBlank { null } }

                val selectedMode = _aiSearchMode.value
                val useTwoPhase = when (selectedMode) {
                    "two_phase" -> true
                    "single" -> false
                    else -> currentRecipes.size > 60 // "auto" mode
                }

                if (useTwoPhase) {
                    var planResult = repo.aiSearchPlan(
                        scope = scope,
                        messages = currentMessages,
                        cloudIds = installedCloudIds
                    )

                    if (planResult.isSuccess) {
                        var planResponse = planResult.getOrThrow()
                        _aiSearchPlanInfo.value = planResponse.plan

                        var matches = filterLocalRecipesForPlan(currentRecipes, planResponse.plan).take(30)

                        val token = planResponse.searchToken
                        if (!processedFinishTokens.contains(token)) {
                            processedFinishTokens.add(token)
                            var finishResult = repo.aiSearchFinish(
                                searchToken = token,
                                messages = currentMessages,
                                limit = 10,
                                localMatches = matches
                            )

                            // On 404/410, single replan retry (новые SEARCH_SESSION_* + старые SESSION_* для совместимости)
                            val finishErrCode = (finishResult.exceptionOrNull() as? com.aetnox.cooking.data.remote.ApiException)?.errorCode
                            val isSessionError = finishErrCode == com.aetnox.cooking.data.remote.model.ApiErrorCodes.SEARCH_SESSION_NOT_FOUND ||
                                    finishErrCode == com.aetnox.cooking.data.remote.model.ApiErrorCodes.SEARCH_SESSION_EXPIRED ||
                                    finishErrCode == com.aetnox.cooking.data.remote.model.ApiErrorCodes.SESSION_NOT_FOUND ||
                                    finishErrCode == com.aetnox.cooking.data.remote.model.ApiErrorCodes.SESSION_EXPIRED
                            if (finishResult.isFailure && isSessionError) {
                                planResult = repo.aiSearchPlan(
                                    scope = scope,
                                    messages = currentMessages,
                                    cloudIds = installedCloudIds
                                )
                                if (planResult.isSuccess) {
                                    planResponse = planResult.getOrThrow()
                                    _aiSearchPlanInfo.value = planResponse.plan
                                    matches = filterLocalRecipesForPlan(currentRecipes, planResponse.plan).take(30)
                                    val retryToken = planResponse.searchToken
                                    if (!processedFinishTokens.contains(retryToken)) {
                                        processedFinishTokens.add(retryToken)
                                        finishResult = repo.aiSearchFinish(
                                            searchToken = retryToken,
                                            messages = currentMessages,
                                            limit = 10,
                                            localMatches = matches
                                        )
                                    }
                                }
                            }

                            if (finishResult.isSuccess) {
                                val res = finishResult.getOrThrow()
                                _isAiSearching.value = false
                                if (res.reply.isNotBlank()) {
                                    _aiSearchMessages.value = currentMessages + com.aetnox.cooking.data.remote.model.ChatMessageDto(role = "assistant", content = res.reply)
                                }
                                _aiSearchResults.value = res.results
                                return@launch
                            }
                        }
                    }
                }

                // Single search execution (or fallback if two-phase plan/finish failed)
                val localDtos = if (scope == "cloud") {
                    null
                } else {
                    currentRecipes.take(240).map { r ->
                        val ingNames = r.ingredients.joinToString(", ") { it.name }
                        val tagNames = r.tags.map { it.name }
                        val time = r.recipe.prepTimeMinutes + r.recipe.cookTimeMinutes
                        com.aetnox.cooking.data.remote.model.AiSearchLocalRecipeDto(
                            id = r.recipe.id.toString(),
                            title = r.recipe.title,
                            tags = tagNames,
                            minutes = time,
                            ingredients = ingNames,
                            status = r.recipe.status,
                            cloudId = r.recipe.cloudId
                        )
                    }
                }

                val singleResult = repo.aiSearch(
                    scope = scope,
                    limit = 10,
                    messages = currentMessages,
                    localRecipes = localDtos
                )

                singleResult.onSuccess { res ->
                    _isAiSearching.value = false
                    if (res.reply.isNotBlank()) {
                        _aiSearchMessages.value = currentMessages + com.aetnox.cooking.data.remote.model.ChatMessageDto(role = "assistant", content = res.reply)
                    }
                    _aiSearchResults.value = res.results
                }.onFailure { err ->
                    _isAiSearching.value = false
                    _aiSearchError.value = err.localizedMessage ?: AppLocaleManager.tr("Ошибка выполнения ИИ-поиска", "AI search error")
                }
            } catch (e: Exception) {
                _isAiSearching.value = false
                _aiSearchError.value = e.localizedMessage ?: AppLocaleManager.tr("Ошибка ИИ-поиска", "AI search error")
            }
        }
    }

    companion object {
        fun filterLocalRecipesForPlan(
            recipes: List<com.aetnox.cooking.data.relation.RecipeWithDetails>,
            plan: com.aetnox.cooking.data.remote.model.AiSearchPlanDto
        ): List<com.aetnox.cooking.data.remote.model.AiSearchLocalMatchDto> {
            return recipes.filter { r ->
                val totalMinutes = r.recipe.prepTimeMinutes + r.recipe.cookTimeMinutes
                if (plan.minMinutes != null && totalMinutes < plan.minMinutes) return@filter false
                if (plan.maxMinutes != null && totalMinutes > plan.maxMinutes) return@filter false

                val titleLower = r.recipe.title.lowercase()
                val notesLower = r.recipe.notes.lowercase()
                val tagLowers = r.tags.map { it.name.lowercase() }
                val ingNamesLower = r.ingredients.map { it.name.lowercase() }

                if (plan.keywords.isNotEmpty()) {
                    val hasKeywordMatch = plan.keywords.any { kw ->
                        val k = kw.lowercase().trim()
                        if (k.isBlank()) true else {
                            titleLower.contains(k) ||
                            notesLower.contains(k) ||
                            tagLowers.any { t -> t.contains(k) } ||
                            ingNamesLower.any { i -> i.contains(k) }
                        }
                    }
                    if (!hasKeywordMatch) return@filter false
                }

                if (plan.ingredients.isNotEmpty()) {
                    val hasIngMatch = plan.ingredients.all { reqIng ->
                        val target = reqIng.lowercase().trim()
                        target.isBlank() || ingNamesLower.any { i -> i.contains(target) }
                    }
                    if (!hasIngMatch) return@filter false
                }

                true
            }.map { r ->
                com.aetnox.cooking.data.remote.model.AiSearchLocalMatchDto(
                    id = r.recipe.id.toString(),
                    title = r.recipe.title,
                    status = r.recipe.status,
                    minutes = r.recipe.prepTimeMinutes + r.recipe.cookTimeMinutes,
                    ingredients = r.ingredients.joinToString(", ") { it.name }
                )
            }
        }
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RecipeListViewModel(repository) as T
        }
    }
}
