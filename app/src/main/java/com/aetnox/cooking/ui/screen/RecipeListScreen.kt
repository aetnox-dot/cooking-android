package com.aetnox.cooking.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.exporter.RecipeExporter
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.domain.parser.RecipeImportParser
import com.aetnox.cooking.ui.component.AiGenerationProgressBox
import com.aetnox.cooking.ui.component.RecipeStatusChip
import com.aetnox.cooking.ui.component.SpotlightTargetKey
import com.aetnox.cooking.ui.component.SpotlightWalkthrough
import com.aetnox.cooking.ui.component.StarRatingBar
import com.aetnox.cooking.ui.component.TimeAndCountBadge
import com.aetnox.cooking.ui.component.ViewMode
import com.aetnox.cooking.ui.component.ViewModeToggle
import com.aetnox.cooking.ui.util.FullscreenMediaDialog
import com.aetnox.cooking.ui.util.MediaViewerUtils
import com.aetnox.cooking.ui.util.OnboardingManager
import com.aetnox.cooking.ui.viewmodel.RecipeListViewModel
import com.aetnox.cooking.ui.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel,
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateToCookMode: (Long) -> Unit,
    onNavigateToBackup: () -> Unit,
    onQuickDraftClick: () -> Unit,
    onImportClipboardClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val targetBounds = remember { mutableStateMapOf<SpotlightTargetKey, Rect>() }

    LaunchedEffect(Unit) {
        viewModel.checkOnboardingOnLaunch(context)
    }

    var recipeToDelete by remember { mutableStateOf<RecipeWithDetails?>(null) }
    var recipeToShare by remember { mutableStateOf<RecipeWithDetails?>(null) }
    var parsedRecipeToPreview by remember { mutableStateOf<ParsedRecipe?>(null) }
    var selectedFullscreenMediaUri by remember { mutableStateOf<String?>(null) }
    var showChefFormatDialog by remember { mutableStateOf(false) }
    val isAiDisabled = remember { ExperimentalSettingsManager.getSettings(context).aiDisabled }

    var showSortMenu by remember { mutableStateOf(false) }
    var showExpandedFilters by remember { mutableStateOf(false) }
    var tagSearchInput by remember { mutableStateOf("") }
    var currentViewMode by remember { mutableStateOf(ViewMode.LIST) }
    var fabExpanded by remember { mutableStateOf(false) }

    var userClosedBanner by remember { mutableStateOf(false) }

    val statusTabs = listOf(
        null to "Все",
        RecipeEntity.STATUS_PLANNED to "В планах",
        RecipeEntity.STATUS_VERIFIED to "Проверено",
        RecipeEntity.STATUS_NEEDS_WORK to "Над ошибками",
        RecipeEntity.STATUS_ARCHIVED to "Архив"
    )

    val activeFiltersCount = (if (uiState.searchQuery.isNotBlank()) 1 else 0) +
            (if (uiState.ingredientQuery.isNotBlank()) 1 else 0) +
            uiState.selectedTags.size +
            (if (uiState.statusFilter != null) 1 else 0) +
            (if (uiState.maxTimeMinutes != null) 1 else 0) +
            (if (uiState.minRating > 0) 1 else 0) +
            (if (uiState.maxCalories != null) 1 else 0)

    val hasSampleDemoRecipe = remember(uiState.recipes) {
        uiState.recipes.any { it.recipe.title.contains("Образец", ignoreCase = true) || it.recipe.title.contains("Брауни", ignoreCase = true) }
    }

    val showOnboardingBanner = remember(uiState.isShowingOnboardingDialog, userClosedBanner, hasSampleDemoRecipe) {
        !OnboardingManager.isOnboardingCompleted(context) && !userClosedBanner && hasSampleDemoRecipe
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Книга рецептов", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        val totalCount = uiState.statusCounts[null] ?: 0
                        val verifiedCount = uiState.statusCounts[RecipeEntity.STATUS_VERIFIED] ?: 0
                        Text(
                            text = "Рецептов: $totalCount • Проверено: $verifiedCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier.onGloballyPositioned { coords ->
                            targetBounds[SpotlightTargetKey.SORT_BUTTON] = coords.boundsInWindow()
                        }
                    ) {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Сортировка")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label) },
                                    onClick = {
                                        viewModel.onSortModeSelected(mode)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = onNavigateToBackup,
                        modifier = Modifier.onGloballyPositioned { coords ->
                            targetBounds[SpotlightTargetKey.SETTINGS_BUTTON] = coords.boundsInWindow()
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки и Бэкап")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.onGloballyPositioned { coords ->
                    targetBounds[SpotlightTargetKey.FAB_BUTTON] = coords.boundsInWindow()
                }
            ) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isAiDisabled) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = "✨ Оцифровать / Форматировать",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                SmallFloatingActionButton(
                                    onClick = {
                                        fabExpanded = false
                                        showChefFormatDialog = true
                                    },
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = "Оцифровщик", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "Быстрый черновик",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    onQuickDraftClick()
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = "Черновик")
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = "Из буфера обмена",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            SmallFloatingActionButton(
                                onClick = {
                                    fabExpanded = false
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        val parsed = RecipeImportParser.parse(clipText)
                                        parsedRecipeToPreview = parsed
                                    } else {
                                        Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Импорт")
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = {
                        if (fabExpanded) {
                            fabExpanded = false
                        } else {
                            onNavigateToEdit(null)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (fabExpanded) "Закрыть" else "Создать",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Status filter scrollable row
            val selectedTabIndex = statusTabs.indexOfFirst { it.first == uiState.statusFilter }.coerceAtLeast(0)
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 12.dp
            ) {
                statusTabs.forEachIndexed { index, (status, label) ->
                    val count = uiState.statusCounts[status] ?: 0
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onStatusFilterSelected(status) },
                        text = {
                            Text(
                                text = "$label ($count)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Compact Search & View Toggle Header
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coords ->
                                targetBounds[SpotlightTargetKey.SEARCH_BAR] = coords.boundsInWindow()
                            },
                        placeholder = { Text("Поиск по названию...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    IconButton(
                        onClick = { showExpandedFilters = !showExpandedFilters },
                        modifier = Modifier
                            .background(
                                if (showExpandedFilters || activeFiltersCount > 0) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .onGloballyPositioned { coords ->
                                targetBounds[SpotlightTargetKey.FILTER_BUTTON] = coords.boundsInWindow()
                            }
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Фильтры",
                            tint = if (showExpandedFilters || activeFiltersCount > 0) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    ViewModeToggle(
                        currentMode = currentViewMode,
                        onModeSelected = { currentViewMode = it },
                        modifier = Modifier.onGloballyPositioned { coords ->
                            targetBounds[SpotlightTargetKey.VIEW_TOGGLE] = coords.boundsInWindow()
                        }
                    )
                }

                // Active Filters Chips Bar
                if (activeFiltersCount > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.ingredientQuery.isNotBlank()) {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onIngredientQueryChanged("") },
                                        label = { Text("🛒 ${uiState.ingredientQuery}") },
                                        trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                            if (uiState.maxTimeMinutes != null) {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onMaxTimeFilterSelected(null) },
                                        label = { Text("⏱ До ${uiState.maxTimeMinutes} мин") },
                                        trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                            if (uiState.minRating > 0) {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onMinRatingFilterSelected(0) },
                                        label = { Text("⭐ ${uiState.minRating}.0+") },
                                        trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                            if (uiState.maxCalories != null) {
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onMaxCaloriesFilterSelected(null) },
                                        label = { Text("🥗 До ${uiState.maxCalories} ккал") },
                                        trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                            uiState.selectedTags.forEach { tagName ->
                                item {
                                    FilterChip(
                                        selected = true,
                                        onClick = { viewModel.onTagToggled(tagName) },
                                        label = { Text("#$tagName") },
                                        trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                    )
                                }
                            }
                        }

                        TextButton(onClick = { viewModel.clearAllFilters() }) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Сбросить", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // Expandable Filter Drawer with Time, Rating, Calories and Ingredients
                AnimatedVisibility(visible = showExpandedFilters) {
                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Max Time Filter Row
                        Column {
                            Text(
                                text = "⏱ Максимальное время приготовления:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = uiState.maxTimeMinutes == null,
                                        onClick = { viewModel.onMaxTimeFilterSelected(null) },
                                        label = { Text("Любое") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.maxTimeMinutes == 15,
                                        onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 15) null else 15) },
                                        label = { Text("⚡ До 15 мин") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.maxTimeMinutes == 30,
                                        onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 30) null else 30) },
                                        label = { Text("⏱ До 30 мин") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.maxTimeMinutes == 60,
                                        onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 60) null else 60) },
                                        label = { Text("🍲 До 1 часа") }
                                    )
                                }
                            }
                        }

                        // Rating Filter Row
                        Column {
                            Text(
                                text = "⭐ Минимальный рейтинг:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = uiState.minRating == 0,
                                        onClick = { viewModel.onMinRatingFilterSelected(0) },
                                        label = { Text("Любой") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.minRating == 4,
                                        onClick = { viewModel.onMinRatingFilterSelected(if (uiState.minRating == 4) 0 else 4) },
                                        label = { Text("⭐ 4.0+") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.minRating == 5,
                                        onClick = { viewModel.onMinRatingFilterSelected(if (uiState.minRating == 5) 0 else 5) },
                                        label = { Text("⭐⭐⭐⭐⭐ 5.0") }
                                    )
                                }
                            }
                        }

                        // Calories Filter Row
                        Column {
                            Text(
                                text = "🥗 Калорийность (на 1 порцию):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item {
                                    FilterChip(
                                        selected = uiState.maxCalories == null,
                                        onClick = { viewModel.onMaxCaloriesFilterSelected(null) },
                                        label = { Text("Любая") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.maxCalories == 300,
                                        onClick = { viewModel.onMaxCaloriesFilterSelected(if (uiState.maxCalories == 300) null else 300) },
                                        label = { Text("🥗 До 300 ккал") }
                                    )
                                }
                                item {
                                    FilterChip(
                                        selected = uiState.maxCalories == 500,
                                        onClick = { viewModel.onMaxCaloriesFilterSelected(if (uiState.maxCalories == 500) null else 500) },
                                        label = { Text("💪 До 500 ккал") }
                                    )
                                }
                            }
                        }

                        // Ingredient Search Input
                        OutlinedTextField(
                            value = uiState.ingredientQuery,
                            onValueChange = { viewModel.onIngredientQueryChanged(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Поиск по ингредиентам (мука, сыр, яйца)...") },
                            leadingIcon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.ingredientQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onIngredientQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Tag Search Input
                        OutlinedTextField(
                            value = tagSearchInput,
                            onValueChange = { tagSearchInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Поиск тегов...") },
                            leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                            trailingIcon = {
                                if (tagSearchInput.isNotEmpty()) {
                                    IconButton(onClick = { tagSearchInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        val matchingTags = remember(uiState.allTags, tagSearchInput) {
                            if (tagSearchInput.isBlank()) uiState.allTags
                            else uiState.allTags.filter { it.name.contains(tagSearchInput, ignoreCase = true) }
                        }

                        if (matchingTags.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(matchingTags) { tag ->
                                    val isSelected = uiState.selectedTags.contains(tag.name)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.onTagToggled(tag.name) },
                                        label = { Text("#${tag.name}") }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ONBOARDING WELCOME BANNER CARD (Linked to onboarding status)
            if (showOnboardingBanner) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👋 Обучение и рецепт-образец",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            IconButton(
                                onClick = { userClosedBanner = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Мы создали для вас образец «🍫 Шоколадный Брауни с вишней». Нажмите кнопку «Готовить», чтобы проверить шаг подготовки ингредиентов, встроенные таймеры и масштабитор порций!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            // Recipe Catalog Content
            if (uiState.recipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            modifier = Modifier
                                .height(64.dp)
                                .width(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Рецепты не найдены",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Добавьте новый рецепт, сбросьте фильтр или добавьте образец",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onNavigateToEdit(null) }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Создать")
                                }
                                Button(onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        val parsed = RecipeImportParser.parse(clipText)
                                        parsedRecipeToPreview = parsed
                                    } else {
                                        Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Из буфера")
                                }
                            }

                            Button(
                                onClick = { viewModel.addSampleDemoRecipe() },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer).run {
                                    androidx.compose.material3.ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Загрузить рецепт-образец (Брауни)")
                            }
                        }
                    }
                }
            } else {
                when (currentViewMode) {
                    ViewMode.LIST -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.recipes, key = { it.recipe.id }) { recipeWithDetails ->
                                val isFirstItem = recipeWithDetails.recipe.id == uiState.recipes.first().recipe.id
                                RecipeCardItem(
                                    recipeWithDetails = recipeWithDetails,
                                    onCardClick = { onNavigateToCookMode(recipeWithDetails.recipe.id) },
                                    onCookClick = { onNavigateToCookMode(recipeWithDetails.recipe.id) },
                                    onEditClick = { onNavigateToEdit(recipeWithDetails.recipe.id) },
                                    onDeleteClick = { recipeToDelete = recipeWithDetails },
                                    onShareClick = { recipeToShare = recipeWithDetails },
                                    onRatingChanged = { rating ->
                                        viewModel.updateRecipeRating(recipeWithDetails.recipe.id, rating)
                                    },
                                    onOpenMedia = { uriStr -> selectedFullscreenMediaUri = uriStr },
                                    modifierForCookButton = if (isFirstItem) {
                                        Modifier.onGloballyPositioned { coords ->
                                            targetBounds[SpotlightTargetKey.COOK_BUTTON] = coords.boundsInWindow()
                                        }
                                    } else Modifier
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                    ViewMode.GRID -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.recipes, key = { it.recipe.id }) { recipeWithDetails ->
                                RecipeGridCardItem(
                                    recipeWithDetails = recipeWithDetails,
                                    onCardClick = { onNavigateToCookMode(recipeWithDetails.recipe.id) },
                                    onCookClick = { onNavigateToCookMode(recipeWithDetails.recipe.id) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    // Share Options Dialog
    recipeToShare?.let { item ->
        ShareRecipeDialog(
            recipeWithDetails = item,
            onDismiss = { recipeToShare = null }
        )
    }

    // Interactive Import Preview Modal
    parsedRecipeToPreview?.let { parsed ->
        ImportPreviewDialog(
            parsedRecipe = parsed,
            onDismiss = { parsedRecipeToPreview = null },
            onConfirmSave = {
                viewModel.saveParsedRecipe(parsed) { savedId ->
                    parsedRecipeToPreview = null
                    Toast.makeText(context, "Рецепт «${parsed.recipe.title}» успешно сохранен!", Toast.LENGTH_LONG).show()
                }
            },
            onOpenInEditor = {
                viewModel.saveParsedRecipe(parsed) { savedId ->
                    parsedRecipeToPreview = null
                    onNavigateToEdit(savedId)
                }
            }
        )
    }

    // Delete confirmation dialog
    recipeToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { recipeToDelete = null },
            title = { Text("Удалить рецепт?") },
            text = { Text("Вы уверены, что хотите удалить «${item.recipe.title}»?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecipe(item.recipe.id)
                        recipeToDelete = null
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    selectedFullscreenMediaUri?.let { uriStr ->
        FullscreenMediaDialog(
            uriStr = uriStr,
            onDismiss = { selectedFullscreenMediaUri = null }
        )
    }

    if (showChefFormatDialog && !isAiDisabled) {
        ChefFormatDialog(
            viewModel = viewModel,
            onDismiss = { showChefFormatDialog = false },
            onRecipeGenerated = { parsedRecipe ->
                showChefFormatDialog = false
                parsedRecipeToPreview = parsedRecipe
            },
            onNavigateToSettings = {
                showChefFormatDialog = false
                onNavigateToBackup()
            }
        )
    }

    if (uiState.isShowingOnboardingDialog) {
        SpotlightWalkthrough(
            targetBounds = targetBounds,
            onDismissRequest = { viewModel.dismissOnboarding(context) },
            onOpenSampleRecipe = {
                viewModel.openSampleRecipe { sampleId ->
                    onNavigateToCookMode(sampleId)
                }
            }
        )
    }
}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCardItem(
    recipeWithDetails: RecipeWithDetails,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onRatingChanged: (Int) -> Unit,
    modifierForCookButton: Modifier = Modifier,
    onOpenMedia: (String) -> Unit = {}
) {
    val recipe = recipeWithDetails.recipe
    val totalTime = recipe.prepTimeMinutes + recipe.cookTimeMinutes

    val coverUri = remember(recipe.imageUrisJson, recipeWithDetails.steps) {
        try {
            val list = kotlinx.serialization.json.Json.decodeFromString<List<String>>(recipe.imageUrisJson)
            if (list.isNotEmpty()) list.first() else {
                recipeWithDetails.steps.mapNotNull { step ->
                    try {
                        val stepImgs = kotlinx.serialization.json.Json.decodeFromString<List<String>>(step.imageUrisJson)
                        stepImgs.firstOrNull()
                    } catch (e: Exception) { null }
                }.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                if (!coverUri.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenMedia(coverUri) }
                    ) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = recipe.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = recipe.title.ifBlank { "Черновик заметки" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        RecipeStatusChip(status = recipe.status)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    TimeAndCountBadge(
                        totalMinutes = totalTime,
                        ingredientsCount = recipeWithDetails.ingredients.size
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    StarRatingBar(
                        rating = recipe.rating,
                        onRatingChanged = onRatingChanged,
                        starSize = 16
                    )
                }
            }

            if (recipe.postCookNote.isNotBlank() && recipe.status == RecipeEntity.STATUS_NEEDS_WORK) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "⚠️ К доработке: ${recipe.postCookNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tags flow
            if (recipeWithDetails.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    recipeWithDetails.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${tag.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = "Поделиться")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
                    }
                }

                Button(
                    onClick = onCookClick,
                    modifier = modifierForCookButton,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Готовить", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RecipeGridCardItem(
    recipeWithDetails: RecipeWithDetails,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit
) {
    val recipe = recipeWithDetails.recipe
    val totalTime = recipe.prepTimeMinutes + recipe.cookTimeMinutes

    val coverUri = remember(recipe.imageUrisJson, recipeWithDetails.steps) {
        try {
            val list = kotlinx.serialization.json.Json.decodeFromString<List<String>>(recipe.imageUrisJson)
            if (list.isNotEmpty()) list.first() else {
                recipeWithDetails.steps.mapNotNull { step ->
                    try {
                        val stepImgs = kotlinx.serialization.json.Json.decodeFromString<List<String>>(step.imageUrisJson)
                        stepImgs.firstOrNull()
                    } catch (e: Exception) { null }
                }.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (!coverUri.isNullOrBlank()) {
                    AsyncImage(
                        model = coverUri,
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                ) {
                    RecipeStatusChip(status = recipe.status)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recipe.title.ifBlank { "Черновик" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (totalTime > 0) {
                    Text(
                        text = "⏱ $totalTime мин • 🛒 ${recipeWithDetails.ingredients.size} ингред.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "🛒 ${recipeWithDetails.ingredients.size} ингред.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onCookClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Готовить", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun ShareRecipeDialog(
    recipeWithDetails: RecipeWithDetails,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Поделиться рецептом", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "«${recipeWithDetails.recipe.title}»",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Выберите удобный способ отправки:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val text = RecipeExporter.formatHumanReadableText(recipeWithDetails)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, recipeWithDetails.recipe.title)
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Отправить рецепт текстом"))
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("💬 Текстом (для сообщений и заметок)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Красивый читаемый текст с ингредиентами и шагами", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val fileUri = RecipeExporter.createRecipeShareFileUri(context, recipeWithDetails)
                            if (fileUri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, fileUri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Отправить файл рецепта"))
                            } else {
                                Toast.makeText(context, "Не удалось создать файл рецепта", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("📁 Файлом рецепта .json (100% точность)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Передать файл другому пользователю приложения Cooking с сохранением всех деталей", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPreviewDialog(
    parsedRecipe: ParsedRecipe,
    onDismiss: () -> Unit,
    onConfirmSave: () -> Unit,
    onOpenInEditor: () -> Unit
) {
    val scrollState = rememberScrollState()
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Предпросмотр импорта", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✨ Распознан рецепт: «${parsedRecipe.recipe.title}»",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "🛒 Ингредиенты (${parsedRecipe.ingredients.size}):",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val grouped = remember(parsedRecipe.ingredients) {
                            parsedRecipe.ingredients.groupBy { it.groupName.trim() }
                        }

                        grouped.forEach { (groupName, list) ->
                            if (groupName.isNotBlank()) {
                                Text(
                                    text = "📌 $groupName",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                )
                            }
                            list.forEach { ing ->
                                val amtStr = if (ing.amount > 0) " — ${ing.amount} ${ing.unit}" else ""
                                val noteStr = if (ing.note.isNotBlank()) " (${ing.note})" else ""
                                Text(
                                    text = "• ${ing.name}$amtStr$noteStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(start = if (groupName.isNotBlank()) 8.dp else 0.dp)
                                )
                            }
                        }
                    }
                }

                if (parsedRecipe.steps.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "👨‍🍳 Шаги приготовления (${parsedRecipe.steps.size}):",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            parsedRecipe.steps.take(4).forEach { step ->
                                Text(
                                    text = "${step.stepNumber}. ${step.instructionText}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            if (parsedRecipe.steps.size > 4) {
                                Text(
                                    text = "И ещё ${parsedRecipe.steps.size - 4} шагов...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                if (parsedRecipe.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        parsedRecipe.tags.forEach { tag ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#$tag", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
            }
        },
        confirmButton = {
            Button(onClick = onConfirmSave) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Сохранить")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onOpenInEditor) {
                    Text("В редактор")
                }
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChefFormatDialog(
    viewModel: RecipeListViewModel,
    onDismiss: () -> Unit,
    onRecipeGenerated: (ParsedRecipe) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var promptInput by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val persistentPath = MediaViewerUtils.saveUriToInternalStorage(context, uri)
            selectedImageUri = persistentPath
            if (promptInput.isBlank()) {
                promptInput = "Распознай и точного оцифруй рецепт с прикрепленного фото кулинарной книги"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("👨‍🍳 Шеф-Оцифровщик рецептов", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Загрузите фото кулинарной книги/заметки или вставьте сырой текст рецепта для автоматической оцифровки и разбивки по секциям:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Scenario Chips
                Column {
                    Text(
                        text = "💡 Быстрый выбор:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            label = { Text("📷 Выбрать фото рецепта") },
                            enabled = !isGenerating
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    promptInput = clipText
                                } else {
                                    Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = { Text("📋 Вставить из буфера") },
                            enabled = !isGenerating
                        )
                    }
                }

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 130.dp),
                    placeholder = { Text("Вставьте сырой текст рецепта или прикрепите фото книги...") },
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isGenerating
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isGenerating
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (selectedImageUri != null) "Изменить фото" else "Прикрепить фото")
                    }

                    if (selectedImageUri != null) {
                        TextButton(
                            onClick = { selectedImageUri = null },
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Удалить", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                selectedImageUri?.let { uriStr ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        AsyncImage(
                            model = uriStr,
                            contentDescription = "Прикрепленный снимок рецепта",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (isGenerating) {
                    AiGenerationProgressBox(
                        isCompleted = isCompleted
                    )
                }

                errorMessage?.let { err ->
                    Text(
                        text = "⚠️ $err",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (promptInput.isBlank() && selectedImageUri == null) {
                        errorMessage = "Вставьте текст рецепта или прикрепите фото!"
                        return@Button
                    }
                    isGenerating = true
                    isConnected = false
                    isCompleted = false
                    errorMessage = null
                    viewModel.generateRecipeWithGemini(
                        context = context,
                        prompt = promptInput.ifBlank { "Распознай и точно оцифруй рецепт с прикрепленного фото" },
                        imageUriStr = selectedImageUri,
                        onConnected = {
                            isConnected = true
                        },
                        onSuccess = { parsed ->
                            isCompleted = true
                            onRecipeGenerated(parsed)
                        },
                        onError = { err ->
                            isGenerating = false
                            isConnected = false
                            isCompleted = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isGenerating
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Оцифровать")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onNavigateToSettings, enabled = !isGenerating) {
                    Text("Настройки")
                }
                TextButton(onClick = onDismiss, enabled = !isGenerating) {
                    Text("Отмена")
                }
            }
        }
    )
}



