package com.aetnox.cooking.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.aetnox.cooking.ui.util.AppLocaleManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextDecoration
import com.aetnox.cooking.ui.util.CartManager
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.aetnox.cooking.ui.component.AiGenerationProgressBox
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.remote.model.RecipeMeta
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.domain.exporter.RecipeExporter
import com.aetnox.cooking.domain.exporter.ShoppingListExporter
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.domain.parser.RecipeImportParser
import com.aetnox.cooking.ui.component.ActiveCookingTicker
import com.aetnox.cooking.ui.component.CookingAppLogo
import com.aetnox.cooking.ui.component.OnboardingDialog
import com.aetnox.cooking.ui.component.RecipeStatusChip
import com.aetnox.cooking.ui.component.SpotlightTargetKey
import com.aetnox.cooking.ui.component.SpotlightWalkthrough
import com.aetnox.cooking.ui.component.TimeAndCountBadge
import com.aetnox.cooking.ui.component.ViewMode
import com.aetnox.cooking.ui.component.ViewModeToggle
import com.aetnox.cooking.ui.util.FormatUtils
import com.aetnox.cooking.ui.util.MediaViewerUtils
import com.aetnox.cooking.ui.util.OnboardingManager
import com.aetnox.cooking.ui.viewmodel.CloudCategory
import com.aetnox.cooking.ui.viewmodel.MainTab
import com.aetnox.cooking.ui.viewmodel.RecipeItemUiModel
import com.aetnox.cooking.ui.viewmodel.RecipeListUiState
import com.aetnox.cooking.ui.viewmodel.RecipeListViewModel
import com.aetnox.cooking.ui.viewmodel.SortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel,
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateToCookMode: (Long, String) -> Unit,
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
    var showCloudDownloadDialog by remember { mutableStateOf(false) }
    var showShoppingListDialog by remember { mutableStateOf(false) }
    val isAiDisabled = remember { ExperimentalSettingsManager.getSettings(context).aiDisabled }

    var showSortMenu by remember { mutableStateOf(false) }
    var showExpandedFilters by remember { mutableStateOf(false) }
    var tagSearchInput by remember { mutableStateOf("") }
    val currentViewMode = com.aetnox.cooking.ui.util.ViewModeManager.currentViewMode.value
    var fabExpanded by remember { mutableStateOf(false) }

    var userClosedBanner by remember { mutableStateOf(false) }

    // Локальное состояние текстовых полей поиска: держим текст на месте, чтобы курсор
    // не сбрасывался при обратном потоке uiState (combine на Dispatchers.Default).
    // В ViewModel уходит каждое изменение, а обратно поле синхронизируется только
    // при внешних изменениях (кнопка «Очистить», сброс фильтров).
    var localSearchQuery by remember { mutableStateOf(uiState.searchQuery) }
    var localIngredientQuery by remember { mutableStateOf(uiState.ingredientQuery) }
    var localCloudSearchQuery by remember { mutableStateOf(uiState.cloudSearchQuery) }

    LaunchedEffect(uiState.searchQuery) {
        if (uiState.searchQuery != localSearchQuery) localSearchQuery = uiState.searchQuery
    }
    LaunchedEffect(uiState.ingredientQuery) {
        if (uiState.ingredientQuery != localIngredientQuery) localIngredientQuery = uiState.ingredientQuery
    }
    LaunchedEffect(uiState.cloudSearchQuery) {
        if (uiState.cloudSearchQuery != localCloudSearchQuery) localCloudSearchQuery = uiState.cloudSearchQuery
    }

    val statusTabs: List<Pair<String?, String>> = listOf(
        null to AppLocaleManager.tr("Все", "All"),
        RecipeEntity.STATUS_PLANNED to AppLocaleManager.tr("В планах", "Planned"),
        RecipeEntity.STATUS_VERIFIED to AppLocaleManager.tr("Проверено", "Verified"),
        RecipeEntity.STATUS_NEEDS_WORK to AppLocaleManager.tr("Над ошибками", "Needs work"),
        RecipeEntity.STATUS_ARCHIVED to AppLocaleManager.tr("Архив", "Archive")
    )

    val activeFiltersCount = (if (uiState.searchQuery.isNotBlank()) 1 else 0) +
            (if (uiState.ingredientQuery.isNotBlank()) 1 else 0) +
            uiState.selectedTags.size +
            (if (uiState.statusFilter != null) 1 else 0) +
            (if (uiState.maxTimeMinutes != null) 1 else 0) +
            (if (uiState.onlyFavorites) 1 else 0) +
            (if (uiState.maxCalories != null) 1 else 0)

    val hasSampleDemoRecipe = remember(uiState.recipes) {
        uiState.recipes.any {
            it.recipe.title.contains("Образец", ignoreCase = true) ||
            it.recipe.title.contains("Брауни", ignoreCase = true) ||
            it.recipe.title.contains("Sample", ignoreCase = true) ||
            it.recipe.title.contains("Brownie", ignoreCase = true)
        }
    }

    val showOnboardingBanner = remember(uiState.isShowingOnboardingDialog, userClosedBanner, hasSampleDemoRecipe) {
        !OnboardingManager.isOnboardingCompleted(context) && !userClosedBanner && hasSampleDemoRecipe
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CookingAppLogo(
                                size = 32.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Text(
                                text = "Cooking",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        val cartCount = remember(uiState.recipes) {
                            uiState.recipes.count { it.recipe.isInCart }
                        }
                        IconButton(onClick = { showShoppingListDialog = true }) {
                            if (cartCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(cartCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingCart,
                                        contentDescription = AppLocaleManager.tr("🛒 Корзина покупок", "🛒 Shopping Cart"),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Icon(
                                    Icons.Outlined.ShoppingCart,
                                    contentDescription = AppLocaleManager.tr("🛒 Корзина покупок", "🛒 Shopping Cart")
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.onGloballyPositioned { coords ->
                                if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                                    val bounds = coords.boundsInWindow()
                                    if (targetBounds[SpotlightTargetKey.SORT_BUTTON] != bounds) {
                                        targetBounds[SpotlightTargetKey.SORT_BUTTON] = bounds
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.FilterList, contentDescription = AppLocaleManager.tr("Сортировка", "Sort"))
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
                                DropdownMenuItem(
                                    text = { Text(AppLocaleManager.tr("🛒 Список покупок", "🛒 Shopping List")) },
                                    onClick = {
                                        showSortMenu = false
                                        showShoppingListDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(AppLocaleManager.tr("🔑 Скачать из облака по коду", "🔑 Download from cloud by code")) },
                                    onClick = {
                                        showSortMenu = false
                                        showCloudDownloadDialog = true
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = onNavigateToBackup,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                                    val bounds = coords.boundsInWindow()
                                    if (targetBounds[SpotlightTargetKey.SETTINGS_BUTTON] != bounds) {
                                        targetBounds[SpotlightTargetKey.SETTINGS_BUTTON] = bounds
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = AppLocaleManager.tr("Настройки и Бэкап", "Settings & Backup"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.onGloballyPositioned { coords ->
                        if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                            val bounds = coords.boundsInWindow()
                            if (targetBounds[SpotlightTargetKey.FAB_BUTTON] != bounds) {
                                targetBounds[SpotlightTargetKey.FAB_BUTTON] = bounds
                            }
                        }
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
                                            text = AppLocaleManager.tr("✨ Оцифровать / Форматировать", "✨ Digitize / Format"),
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
                                        Icon(Icons.Default.AutoAwesome, contentDescription = AppLocaleManager.tr("Оцифровщик", "Digitizer"), tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
                                        text = AppLocaleManager.tr("Быстрый черновик", "Quick Draft"),
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
                                    Icon(Icons.Default.FlashOn, contentDescription = AppLocaleManager.tr("Черновик", "Draft"))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = AppLocaleManager.tr("Из буфера обмена", "From clipboard"),
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
                                            Toast.makeText(context, AppLocaleManager.tr("Буфер обмена пуст!", "Clipboard is empty!"), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = AppLocaleManager.tr("Импорт", "Import"))
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
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (fabExpanded) Icons.Default.Close else Icons.Default.Add,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (fabExpanded) AppLocaleManager.tr("Закрыть", "Close") else AppLocaleManager.tr("Создать", "Create"),
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
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .imePadding()
            ) {
                // Main Switcher Tabs (📖 Моя книга vs 🌐 Облачный каталог)
                TabRow(
                    selectedTabIndex = uiState.activeMainTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = uiState.activeMainTab == MainTab.LOCAL,
                        onClick = { viewModel.onMainTabChanged(MainTab.LOCAL, context) },
                        text = {
                            Text(
                                text = MainTab.LOCAL.label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                    Tab(
                        selected = uiState.activeMainTab == MainTab.CLOUD,
                        onClick = { viewModel.onMainTabChanged(MainTab.CLOUD, context) },
                        text = {
                            Text(
                                text = MainTab.CLOUD.label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (uiState.activeMainTab == MainTab.LOCAL) {
                    // --- TAB 1: LOCAL COOKBOOK ---

                    val selectedTabIndex = statusTabs.indexOfFirst { it.first == uiState.statusFilter }.coerceAtLeast(0)
                    PrimaryScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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

                    // Offline Network Connection Banner
                    val isOnline by com.aetnox.cooking.data.remote.NetworkObserver.observeNetwork(context)
                        .collectAsState(initial = com.aetnox.cooking.data.remote.NetworkObserver.isOnline(context))

                    if (!isOnline) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📡", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppLocaleManager.tr("Нет подключения к интернету. Приложение работает в автономном офлайн-режиме.", "No internet connection. App running in offline mode."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Maintenance Notice Banner
                    val startupState by com.aetnox.cooking.data.remote.AppStartupManager.startupState.collectAsState()
                    if (startupState.isMaintenance && isOnline) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppLocaleManager.tr("🛠 На сервере идут плановые технические работы. Оцифровка и ИИ-функции временно заблокированы. Ваша книга рецептов работает автономно.", "🛠 Planned server maintenance in progress. AI features temporarily disabled. Your cookbook works offline."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Server Unreachable / Diagnostic Error Banner
                    if (isOnline && !startupState.isMaintenance && !startupState.error.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🌐", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = startupState.error!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Active Cooking Resume Bar & Timer Ticker
                    ActiveCookingTicker(onNavigateToCookMode = { recipeId -> onNavigateToCookMode(recipeId, "STEP_BY_STEP") })

                    // Compact Search & View Toggle Header
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = localSearchQuery,
                                onValueChange = { newQuery ->
                                    localSearchQuery = newQuery
                                    viewModel.onSearchQueryChanged(newQuery)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .onGloballyPositioned { coords ->
                                        if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                                            val bounds = coords.boundsInWindow()
                                            if (targetBounds[SpotlightTargetKey.SEARCH_BAR] != bounds) {
                                                targetBounds[SpotlightTargetKey.SEARCH_BAR] = bounds
                                            }
                                        }
                                    },
                                placeholder = { Text(AppLocaleManager.tr("Поиск по названию...", "Search by title...")) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (localSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = AppLocaleManager.tr("Очистить", "Clear"))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            IconButton(
                                onClick = { viewModel.showAiSearchDialog() },
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = AppLocaleManager.tr("Умный ИИ-поиск", "Smart AI search"),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }

                            IconButton(
                                onClick = { showExpandedFilters = !showExpandedFilters },
                                modifier = Modifier
                                    .background(
                                        if (showExpandedFilters || activeFiltersCount > 0) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .onGloballyPositioned { coords ->
                                        if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                                            val bounds = coords.boundsInWindow()
                                            if (targetBounds[SpotlightTargetKey.FILTER_BUTTON] != bounds) {
                                                targetBounds[SpotlightTargetKey.FILTER_BUTTON] = bounds
                                            }
                                        }
                                    }
                            ) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = AppLocaleManager.tr("Фильтры", "Filters"),
                                    tint = if (showExpandedFilters || activeFiltersCount > 0) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (activeFiltersCount > 0) {
                                IconButton(
                                    onClick = { viewModel.clearAllFilters() },
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = AppLocaleManager.tr("Сбросить все фильтры", "Reset all filters"),
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Active Filters Chips Bar
                        if (activeFiltersCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
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
                                            label = { Text("⏱ ${AppLocaleManager.tr("До", "Under")} ${uiState.maxTimeMinutes} ${AppLocaleManager.tr("мин", "min")}") },
                                            trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                                if (uiState.onlyFavorites) {
                                    item {
                                        FilterChip(
                                            selected = true,
                                            onClick = { viewModel.onOnlyFavoritesToggled(false) },
                                            label = { Text("⭐ ${AppLocaleManager.tr("Избранное", "Favorites")}") },
                                            trailingIcon = { Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        )
                                    }
                                }
                                if (uiState.maxCalories != null) {
                                    item {
                                        FilterChip(
                                            selected = true,
                                            onClick = { viewModel.onMaxCaloriesFilterSelected(null) },
                                            label = { Text("🥗 ${AppLocaleManager.tr("До", "Under")} ${uiState.maxCalories} ${AppLocaleManager.tr("ккал", "kcal")}") },
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
                        }

                        // Expandable Filter Drawer
                        AnimatedVisibility(visible = showExpandedFilters) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "⏱ ${AppLocaleManager.tr("Максимальное время приготовления:", "Maximum cooking time:")}",
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
                                                label = { Text(AppLocaleManager.tr("Любое", "Any")) }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.maxTimeMinutes == 15,
                                                onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 15) null else 15) },
                                                label = { Text("⚡ ${AppLocaleManager.tr("До 15 мин", "Under 15 min")}") }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.maxTimeMinutes == 30,
                                                onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 30) null else 30) },
                                                label = { Text("⏱ ${AppLocaleManager.tr("До 30 мин", "Under 30 min")}") }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.maxTimeMinutes == 60,
                                                onClick = { viewModel.onMaxTimeFilterSelected(if (uiState.maxTimeMinutes == 60) null else 60) },
                                                label = { Text("🍲 ${AppLocaleManager.tr("До 1 часа", "Under 1 hour")}") }
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "⭐ ${AppLocaleManager.tr("Подборка рецептов:", "Recipe selection:")}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        item {
                                            FilterChip(
                                                selected = !uiState.onlyFavorites,
                                                onClick = { viewModel.onOnlyFavoritesToggled(false) },
                                                label = { Text(AppLocaleManager.tr("Все рецепты", "All recipes")) }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.onlyFavorites,
                                                onClick = { viewModel.onOnlyFavoritesToggled(!uiState.onlyFavorites) },
                                                label = { Text("⭐ ${AppLocaleManager.tr("Только Избранные", "Favorites Only")}") }
                                            )
                                        }
                                    }
                                }

                                Column {
                                    Text(
                                        text = "🥗 ${AppLocaleManager.tr("Калорийность (на 1 порцию):", "Calories (per serving):")}",
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
                                                label = { Text(AppLocaleManager.tr("Любая", "Any")) }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.maxCalories == 300,
                                                onClick = { viewModel.onMaxCaloriesFilterSelected(if (uiState.maxCalories == 300) null else 300) },
                                                label = { Text("🥗 ${AppLocaleManager.tr("До 300 ккал", "Under 300 kcal")}") }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = uiState.maxCalories == 500,
                                                onClick = { viewModel.onMaxCaloriesFilterSelected(if (uiState.maxCalories == 500) null else 500) },
                                                label = { Text("💪 ${AppLocaleManager.tr("До 500 ккал", "Under 500 kcal")}") }
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = localIngredientQuery,
                                    onValueChange = { newQuery ->
                                        localIngredientQuery = newQuery
                                        viewModel.onIngredientQueryChanged(newQuery)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text(AppLocaleManager.tr("Поиск по ингредиентам (мука, сыр, яйца)...", "Search ingredients (flour, cheese, eggs)...")) },
                                    leadingIcon = { Icon(Icons.Default.RestaurantMenu, contentDescription = null) },
                                    trailingIcon = {
                                        if (localIngredientQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onIngredientQueryChanged("") }) {
                                                Icon(Icons.Default.Clear, contentDescription = AppLocaleManager.tr("Очистить", "Clear"))
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedTextField(
                                    value = tagSearchInput,
                                    onValueChange = { tagSearchInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text(AppLocaleManager.tr("Поиск тегов...", "Search tags...")) },
                                    leadingIcon = { Icon(Icons.Default.LocalOffer, contentDescription = null) },
                                    trailingIcon = {
                                        if (tagSearchInput.isNotEmpty()) {
                                            IconButton(onClick = { tagSearchInput = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = AppLocaleManager.tr("Очистить", "Clear"))
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
                                        items(matchingTags, key = { it.id }) { tag ->
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

                    // Onboarding Banner
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
                                        text = AppLocaleManager.tr("👋 Обучение и рецепт-образец", "👋 Onboarding & Sample Recipe"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    IconButton(
                                        onClick = { userClosedBanner = true },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Закрыть", "Close"), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr(
                                        "Мы создали для вас образец «🍫 Шоколадный Брауни с вишней». Нажмите кнопку «Готовить» на карточке ниже или откройте интерактивный гид!",
                                        "We created a sample '🍫 Chocolate Cherry Brownie' for you. Tap 'Cook' on the card below or open the interactive guide!"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { viewModel.showOnboarding() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppLocaleManager.tr("🎓 Открыть интерактивное обучение", "🎓 Open Interactive Onboarding"), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Recipe Catalog Content List/Grid
                    if (uiState.isLoading && uiState.recipes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (uiState.recipes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = AppLocaleManager.tr("Ваша книга рецептов пока пуста", "Your cookbook is empty"),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = AppLocaleManager.tr(
                                        "Добавьте свой первый рецепт, загрузите проверенный образец Брауни или сбросьте фильтры поиска",
                                        "Add your first recipe, load the sample Brownie recipe, or reset search filters"
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { viewModel.addSampleDemoRecipe() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppLocaleManager.tr("Загрузить образец рецепта Брауни", "Load Sample Brownie Recipe"))
                                }
                            }
                        }
                    } else {
                        when (currentViewMode) {
                            ViewMode.LIST -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.recipeUiModels, key = { it.recipeWithDetails.recipe.id }) { item ->
                                        var cookButtonModifier = Modifier as Modifier
                                        if (item.recipeWithDetails.recipe.title.contains("Брауни", ignoreCase = true) ||
                                            item.recipeWithDetails.recipe.title.contains("Образец", ignoreCase = true) ||
                                            item.recipeWithDetails.recipe.title.contains("Brownie", ignoreCase = true) ||
                                            item.recipeWithDetails.recipe.title.contains("Sample", ignoreCase = true)
                                        ) {
                                            cookButtonModifier = Modifier.onGloballyPositioned { coords ->
                                                if (showOnboardingBanner || uiState.isShowingOnboardingDialog) {
                                                    val bounds = coords.boundsInWindow()
                                                    if (targetBounds[SpotlightTargetKey.COOK_BUTTON] != bounds) {
                                                        targetBounds[SpotlightTargetKey.COOK_BUTTON] = bounds
                                                    }
                                                }
                                            }
                                        }

                                        RecipeCardItem(
                                            itemUiModel = item,
                                            onCardClick = { onNavigateToCookMode(item.recipeWithDetails.recipe.id, "OVERVIEW") },
                                            onCookClick = { onNavigateToCookMode(item.recipeWithDetails.recipe.id, "STEP_BY_STEP") },
                                            onEditClick = { onNavigateToEdit(item.recipeWithDetails.recipe.id) },
                                            onDeleteClick = { recipeToDelete = item.recipeWithDetails },
                                            onShareClick = { recipeToShare = item.recipeWithDetails },
                                            onFavoriteToggle = { viewModel.toggleRecipeFavorite(item.recipeWithDetails.recipe.id, item.recipeWithDetails.recipe.isFavorite) },
                                            onCartToggle = { viewModel.toggleRecipeCartStatus(item.recipeWithDetails.recipe.id, item.recipeWithDetails.recipe.isInCart) },
                                            modifierForCookButton = cookButtonModifier,
                                            onOpenMedia = { uriStr -> selectedFullscreenMediaUri = uriStr }
                                        )
                                    }
                                }
                            }

                            ViewMode.GRID -> {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(uiState.recipeUiModels, key = { it.recipeWithDetails.recipe.id }) { item ->
                                        RecipeGridCardItem(
                                            itemUiModel = item,
                                            onCardClick = { onNavigateToCookMode(item.recipeWithDetails.recipe.id, "OVERVIEW") },
                                            onCookClick = { onNavigateToCookMode(item.recipeWithDetails.recipe.id, "STEP_BY_STEP") },
                                            onEditClick = { onNavigateToEdit(item.recipeWithDetails.recipe.id) },
                                            onDeleteClick = { recipeToDelete = item.recipeWithDetails },
                                            onShareClick = { recipeToShare = item.recipeWithDetails },
                                            onFavoriteToggle = { viewModel.toggleRecipeFavorite(item.recipeWithDetails.recipe.id, item.recipeWithDetails.recipe.isFavorite) },
                                            onCartToggle = { viewModel.toggleRecipeCartStatus(item.recipeWithDetails.recipe.id, item.recipeWithDetails.recipe.isInCart) },
                                            onOpenMedia = { uriStr -> selectedFullscreenMediaUri = uriStr }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- TAB 2: CLOUD CATALOG & GLOBAL SEARCH ---

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        // Cloud Toast / Notification Banner
                        uiState.cloudMessage?.let { msg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (msg.startsWith("✅")) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.clearCloudMessage() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Закрыть", "Close"))
                                    }
                                }
                            }
                        }

                        // Cloud Search Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = localCloudSearchQuery,
                                onValueChange = { newQuery ->
                                    localCloudSearchQuery = newQuery
                                    viewModel.onCloudSearchQueryChanged(newQuery)
                                },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text(AppLocaleManager.tr("Поиск рецептов в облаке...", "Search recipes in cloud...")) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (localCloudSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onCloudSearchQueryChanged("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = AppLocaleManager.tr("Очистить", "Clear"))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp)
                            )

                            Button(
                                onClick = { viewModel.searchCloudRecipes(context, uiState.cloudSearchQuery) },
                                enabled = !uiState.isSearchingCloud && uiState.cloudSearchQuery.trim().length >= 2,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (uiState.isSearchingCloud) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                                } else {
                                    Text(AppLocaleManager.tr("Поиск", "Search"))
                                }
                            }
                        }

                        // Smart Access Code or Cloud ID Prompt Card
                        val trimmedQuery = uiState.cloudSearchQuery.trim()
                        val isAccessCodeFormat = !trimmedQuery.startsWith("cloud-", ignoreCase = true) && trimmedQuery.length in 6..16
                        val isCloudIdFormat = trimmedQuery.startsWith("cloud-", ignoreCase = true)

                        if (isAccessCodeFormat || isCloudIdFormat) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isAccessCodeFormat) AppLocaleManager.tr("🔑 Код доступа обнаружен!", "🔑 Access code detected!") else AppLocaleManager.tr("☁️ ID рецепта обнаружен!", "☁️ Recipe ID detected!"),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = AppLocaleManager.tr("Скачать рецепт «$trimmedQuery» из облака?", "Download recipe '$trimmedQuery' from cloud?"),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.downloadCloudRecipe(
                                                context = context,
                                                cloudId = trimmedQuery,
                                                onSuccess = {
                                                    viewModel.onCloudSearchQueryChanged("")
                                                    Toast.makeText(context, AppLocaleManager.tr("✅ Рецепт скачан в вашу книгу!", "✅ Recipe downloaded to your cookbook!"), Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, "❌ $err", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(AppLocaleManager.tr("Скачать", "Download"), style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = uiState.cloudCategory == CloudCategory.PUBLIC && uiState.cloudSearchResults.isEmpty(),
                                onClick = { viewModel.onCloudCategorySelected(CloudCategory.PUBLIC, context) },
                                label = { Text(AppLocaleManager.tr("🔥 Все открытые", "🔥 All Public")) },
                                leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )

                            FilterChip(
                                selected = uiState.cloudCategory == CloudCategory.MY_UPLOADS && uiState.cloudSearchResults.isEmpty(),
                                onClick = { viewModel.onCloudCategorySelected(CloudCategory.MY_UPLOADS, context) },
                                label = { Text(AppLocaleManager.tr("👤 Мои загрузки", "👤 My Uploads")) },
                                leadingIcon = { Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )

                            FilterChip(
                                selected = false,
                                onClick = { showCloudDownloadDialog = true },
                                label = { Text(AppLocaleManager.tr("🔑 По коду", "🔑 By Code")) },
                                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cloud Catalog List / Search Results
                        val currentCloudList = when {
                            uiState.cloudSearchResults.isNotEmpty() -> uiState.cloudSearchResults
                            uiState.cloudCategory == CloudCategory.MY_UPLOADS -> uiState.myCloudRecipes
                            else -> uiState.publicCloudRecipes
                        }

                        val isCurrentLoading = when {
                            uiState.isSearchingCloud -> true
                            uiState.cloudCategory == CloudCategory.MY_UPLOADS -> uiState.isLoadingMyCloud
                            else -> uiState.isLoadingPublicCloud
                        }

                        uiState.cloudSearchMessage?.let { searchMsg ->
                            Text(
                                text = searchMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        if (isCurrentLoading && currentCloudList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(strokeWidth = 3.dp)
                            }
                        } else if (currentCloudList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Cloud,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = if (uiState.cloudCategory == CloudCategory.MY_UPLOADS) AppLocaleManager.tr("У вас нет выгруженных в облако рецептов", "You have no uploaded recipes") else AppLocaleManager.tr("Рецепты не найдены", "No recipes found"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (uiState.cloudCategory == CloudCategory.MY_UPLOADS) AppLocaleManager.tr("Вы можете загрузить рецепт в облако через меню любой карточки рецепта.", "You can upload a recipe to the cloud from any recipe card menu.") else AppLocaleManager.tr("Попробуйте изменить поисковый запрос или нажать «Все открытые».", "Try changing your search query or tapping 'All Public'."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(currentCloudList, key = { it.recipeId }) { item ->
                                    val isDownloaded = remember(uiState.recipes, item) {
                                        uiState.recipes.any { local ->
                                            (local.recipe.cloudId != null && local.recipe.cloudId == item.recipeId) ||
                                            (local.recipe.cloudAccessCode != null && item.accessCode != null && local.recipe.cloudAccessCode == item.accessCode) ||
                                            local.recipe.title.equals(item.title, ignoreCase = true)
                                        }
                                    }
                                    val isDownloading = uiState.downloadingCloudIds.contains(item.recipeId)

                                    CloudRecipeItemCard(
                                        item = item,
                                        isDownloaded = isDownloaded,
                                        isDownloading = isDownloading,
                                        showDeleteButton = uiState.cloudCategory == CloudCategory.MY_UPLOADS,
                                        onDownloadClick = {
                                            viewModel.downloadCloudRecipeItem(context, item)
                                        },
                                        onDeleteClick = {
                                            viewModel.deleteMyCloudRecipe(context, item.recipeId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dialogs
            if (recipeToDelete != null) {
                AlertDialog(
                    onDismissRequest = { recipeToDelete = null },
                    title = { Text(AppLocaleManager.tr("Удалить рецепт?", "Delete Recipe?"), fontWeight = FontWeight.Bold) },
                    text = { Text(AppLocaleManager.tr("Вы уверены, что хотите удалить «${recipeToDelete?.recipe?.title}»? Это действие нельзя отменить.", "Are you sure you want to delete '${recipeToDelete?.recipe?.title}'? This action cannot be undone.")) },
                    confirmButton = {
                        Button(
                            onClick = {
                                recipeToDelete?.recipe?.id?.let { id ->
                                    viewModel.deleteRecipe(id)
                                }
                                recipeToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(AppLocaleManager.tr("Удалить", "Delete"))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { recipeToDelete = null }) {
                            Text(AppLocaleManager.tr("Отмена", "Cancel"))
                        }
                    }
                )
            }

            if (recipeToShare != null) {
                ShareRecipeDialog(
                    recipeWithDetails = recipeToShare!!,
                    viewModel = viewModel,
                    onDismiss = { recipeToShare = null }
                )
            }

            if (parsedRecipeToPreview != null) {
                AlertDialog(
                    onDismissRequest = { parsedRecipeToPreview = null },
                    title = { Text(AppLocaleManager.tr("Предпросмотр импортированного рецепта", "Imported Recipe Preview"), fontWeight = FontWeight.Bold) },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("${AppLocaleManager.tr("Название:", "Title:")} ${parsedRecipeToPreview?.recipe?.title}", fontWeight = FontWeight.Bold)
                            Text("${AppLocaleManager.tr("Ингредиентов:", "Ingredients:")} ${parsedRecipeToPreview?.ingredients?.size ?: 0}")
                            Text("${AppLocaleManager.tr("Шагов:", "Steps:")} ${parsedRecipeToPreview?.steps?.size ?: 0}")
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                parsedRecipeToPreview?.let { parsed ->
                                    viewModel.saveParsedRecipe(parsed) { savedId ->
                                        onNavigateToEdit(savedId)
                                    }
                                }
                                parsedRecipeToPreview = null
                            }
                        ) {
                            Text(AppLocaleManager.tr("Сохранить и открыть", "Save and Open"))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { parsedRecipeToPreview = null }) {
                            Text(AppLocaleManager.tr("Отмена", "Cancel"))
                        }
                    }
                )
            }

            if (showChefFormatDialog) {
                ChefFormatDialog(
                    viewModel = viewModel,
                    onDismiss = { showChefFormatDialog = false },
                    onNavigateToEdit = { savedId ->
                        showChefFormatDialog = false
                        onNavigateToEdit(savedId)
                    }
                )
            }

            if (showCloudDownloadDialog) {
                DownloadCloudRecipeDialog(
                    viewModel = viewModel,
                    onDismiss = { showCloudDownloadDialog = false },
                    onRecipeDownloaded = { localId ->
                        showCloudDownloadDialog = false
                        Toast.makeText(context, AppLocaleManager.tr("✅ Рецепт успешно скачан из облака!", "✅ Recipe downloaded successfully from cloud!"), Toast.LENGTH_SHORT).show()
                    }
                )
            }

            if (showShoppingListDialog) {
                ShoppingListDialog(
                    recipes = uiState.recipes,
                    onDismiss = { showShoppingListDialog = false },
                    onCartToggle = { recipeId ->
                        val r = uiState.recipes.find { it.recipe.id == recipeId }?.recipe
                        if (r != null) {
                            viewModel.toggleRecipeCartStatus(r.id, r.isInCart)
                        }
                    },
                    onClearCart = { viewModel.clearCart() }
                )
            }

            if (selectedFullscreenMediaUri != null) {
                com.aetnox.cooking.ui.util.FullscreenMediaDialog(
                    uriStr = selectedFullscreenMediaUri!!,
                    onDismiss = { selectedFullscreenMediaUri = null }
                )
            }

            if (uiState.isShowingOnboardingDialog) {
                OnboardingDialog(
                    onDismissRequest = { viewModel.dismissOnboarding(context) },
                    onOpenSampleRecipe = {
                        viewModel.openSampleRecipe { sampleId ->
                            onNavigateToCookMode(sampleId, "STEP_BY_STEP")
                        }
                    }
                )
            }

            if (uiState.isAiSearchDialogVisible) {
                AiSearchDialog(
                    uiState = uiState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.hideAiSearchDialog() },
                    onOpenLocalRecipe = { localId ->
                        viewModel.hideAiSearchDialog()
                        onNavigateToCookMode(localId, "OVERVIEW")
                    },
                    onOpenCloudRecipe = { cloudId ->
                        viewModel.hideAiSearchDialog()
                        viewModel.downloadCloudRecipe(
                            context = context,
                            cloudId = cloudId,
                            onSuccess = { localId ->
                                onNavigateToCookMode(localId, "OVERVIEW")
                            },
                            onError = { err ->
                                Toast.makeText(context, "❌ $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ChefFormatDialog(
    viewModel: RecipeListViewModel,
    onDismiss: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var rawText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppLocaleManager.tr("✨ Оцифровать рецепт ИИ", "✨ AI Recipe Digitizer"), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isGenerating) {
                    AiGenerationProgressBox(
                        isCompleted = isCompleted
                    )
                } else {
                    Text(
                        text = AppLocaleManager.tr(
                            "Вставьте произвольный текст рецепта с сайта, из сообщения или из книги:",
                            "Paste raw recipe text from website, chat message or book:"
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = rawText,
                        onValueChange = { rawText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text(AppLocaleManager.tr("Ингредиенты: 500г муки...\nШаги: 1. Смешать все...", "Ingredients: 500g flour...\nSteps: 1. Mix all...")) },
                        shape = RoundedCornerShape(12.dp)
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
                    if (rawText.isBlank()) return@Button
                    isGenerating = true
                    isCompleted = false
                    errorMessage = null

                    viewModel.generateRecipeWithGemini(
                        context = context,
                        prompt = rawText.trim(),
                        imageUriStr = null,
                        onSuccess = { parsed ->
                            isCompleted = true
                            viewModel.saveParsedRecipe(parsed) { savedId ->
                                coroutineScope.launch {
                                    delay(750)
                                    onDismiss()
                                    onNavigateToEdit(savedId)
                                }
                            }
                        },
                        onError = { err ->
                            isGenerating = false
                            isCompleted = false
                            errorMessage = err
                        }
                    )
                },
                enabled = !isGenerating && rawText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(AppLocaleManager.tr("Оцифровать", "Digitize"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) {
                Text(AppLocaleManager.tr("Отмена", "Cancel"))
            }
        }
    )
}

@Composable
fun CloudRecipeItemCard(
    item: RecipeMeta,
    isDownloaded: Boolean,
    isDownloading: Boolean,
    showDeleteButton: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (item.isPublic == 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = AppLocaleManager.tr("🌐 Открытый", "🌐 Public"),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = AppLocaleManager.tr("🔒 По коду", "🔒 By Code"),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (!item.tags.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(item.tags, key = { it }) { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "🏷️ $tag",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDownloaded) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppLocaleManager.tr("В вашей книге", "In Cookbook"),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showDeleteButton) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = AppLocaleManager.tr("Удалить из облака", "Delete from cloud"), tint = MaterialTheme.colorScheme.error)
                        }
                    }

                    FilledTonalButton(
                        onClick = onDownloadClick,
                        enabled = !isDownloading,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDownloaded) AppLocaleManager.tr("Скачать снова", "Redownload") else AppLocaleManager.tr("Скачать в книгу", "Download to Cookbook"), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadCloudRecipeDialog(
    viewModel: RecipeListViewModel,
    onDismiss: () -> Unit,
    onRecipeDownloaded: (Long) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var cloudIdInput by remember {
        val clipText = clipboardManager.getText()?.text?.trim().orEmpty()
        val isValidCode = clipText.startsWith("cloud-", ignoreCase = true) || clipText.length in 6..16
        mutableStateOf(if (isValidCode) clipText else "")
    }
    var isDownloading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppLocaleManager.tr("☁️ Скачать рецепт из облака", "☁️ Download Cloud Recipe"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = AppLocaleManager.tr(
                        "Введите или вставьте ID облачного рецепта (cloud-...) или 10-значный код доступа, полученный от автора:",
                        "Enter or paste cloud recipe ID (cloud-...) or 10-digit access code from author:"
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = cloudIdInput,
                    onValueChange = { cloudIdInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(AppLocaleManager.tr("ID рецепта или код доступа", "Recipe ID or Access Code")) },
                    placeholder = { Text(AppLocaleManager.tr("cloud-123-45678 или 8392018471", "cloud-123-45678 or 8392018471")) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (cloudIdInput.isBlank()) return@Button
                    isDownloading = true
                    viewModel.downloadCloudRecipe(
                        context = context,
                        cloudId = cloudIdInput,
                        onSuccess = { localId ->
                            isDownloading = false
                            onRecipeDownloaded(localId)
                        },
                        onError = { err ->
                            isDownloading = false
                            Toast.makeText(context, "❌ $err", Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isDownloading && cloudIdInput.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isDownloading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppLocaleManager.tr("Скачивание...", "Downloading..."))
                } else {
                    Text(AppLocaleManager.tr("Скачать рецепт", "Download Recipe"))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLocaleManager.tr("Отмена", "Cancel"))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShoppingListDialog(
    recipes: List<RecipeWithDetails>,
    onDismiss: () -> Unit,
    onCartToggle: (Long) -> Unit,
    onClearCart: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(Unit) {
        CartManager.init(context)
    }

    var customInputText by remember { mutableStateOf("") }
    var showCompletedAccordion by remember { mutableStateOf(false) }

    val cartRecipes = remember(recipes) {
        recipes.filter { it.recipe.isInCart }
    }

    val recipeServingsMap = CartManager.recipeServings.toMap()
    val consolidatedIngredients = remember(cartRecipes, recipeServingsMap) {
        ShoppingListExporter.consolidateIngredients(
            recipes = cartRecipes,
            recipeServingsMap = recipeServingsMap
        )
    }

    val customItems = CartManager.customItems.value

    val totalItemsCount = consolidatedIngredients.size + customItems.size
    val checkedIngredientsCount = consolidatedIngredients.count { CartManager.isKeyChecked(it.key) }
    val checkedCustomCount = customItems.count { it.isChecked }
    val checkedTotalCount = checkedIngredientsCount + checkedCustomCount

    val progress = if (totalItemsCount > 0) checkedTotalCount.toFloat() / totalItemsCount else 0f

    val activeIngredients = remember(consolidatedIngredients, CartManager.checkedKeys.toMap()) {
        consolidatedIngredients.filter { !CartManager.isKeyChecked(it.key) }
    }
    val completedIngredients = remember(consolidatedIngredients, CartManager.checkedKeys.toMap()) {
        consolidatedIngredients.filter { CartManager.isKeyChecked(it.key) }
    }
    val activeCustom = remember(customItems) {
        customItems.filter { !it.isChecked }
    }
    val completedCustom = remember(customItems) {
        customItems.filter { it.isChecked }
    }
    val completedCount = completedIngredients.size + completedCustom.size

    val shoppingText = remember(cartRecipes, recipeServingsMap, CartManager.checkedKeys.toMap(), customItems) {
        ShoppingListExporter.generateShoppingListText(
            recipes = cartRecipes,
            recipeServingsMap = recipeServingsMap,
            checkedKeys = CartManager.checkedKeys.keys,
            customItems = customItems
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .heightIn(max = 680.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocaleManager.tr("🛒 Корзина покупок", "🛒 Shopping Cart"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Закрыть", "Close"))
                    }
                }

                if (totalItemsCount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppLocaleManager.tr(
                                "Куплено $checkedTotalCount из $totalItemsCount (${(progress * 100).toInt()}%)",
                                "Purchased $checkedTotalCount of $totalItemsCount (${(progress * 100).toInt()}%)"
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (cartRecipes.isEmpty() && customItems.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🛒", style = MaterialTheme.typography.displayMedium)
                            Text(
                                text = AppLocaleManager.tr("Корзина пуста", "Cart is empty"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppLocaleManager.tr(
                                    "Нажимайте иконку 🛒 на карточках рецептов, чтобы добавить ингредиенты в единый список покупок!",
                                    "Tap the 🛒 icon on recipe cards to add ingredients to your shopping list!"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (recipes.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        recipes.forEach { onCartToggle(it.recipe.id) }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppLocaleManager.tr("Добавить все рецепты в корзину", "Add all recipes to cart"))
                                }
                            }
                        }
                    }
                } else {
                    // Celebration Banner when everything is purchased
                    if (checkedTotalCount == totalItemsCount && totalItemsCount > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎉", style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = AppLocaleManager.tr("Всё куплено!", "All items purchased!"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = AppLocaleManager.tr("Все ингредиенты отмечены в списке.", "All ingredients checked off in list."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Recipe selection chips with "Select All" & "Clear" options
                    if (recipes.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppLocaleManager.tr("Выберите рецепты:", "Select recipes:"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(
                                        onClick = {
                                            recipes.filter { !it.recipe.isInCart }.forEach { onCartToggle(it.recipe.id) }
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(AppLocaleManager.tr("Выбрать все", "Select all"), style = MaterialTheme.typography.labelSmall)
                                    }

                                    TextButton(
                                        onClick = {
                                            recipes.filter { it.recipe.isInCart }.forEach { onCartToggle(it.recipe.id) }
                                        },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(AppLocaleManager.tr("Сбросить", "Clear"), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(recipes, key = { it.recipe.id }) { item ->
                                    val isInCart = item.recipe.isInCart
                                    FilterChip(
                                        selected = isInCart,
                                        onClick = { onCartToggle(item.recipe.id) },
                                        label = {
                                            Text(
                                                item.recipe.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Celebration Banner when everything is purchased
                    if (checkedTotalCount == totalItemsCount && totalItemsCount > 0) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = AppLocaleManager.tr("Рецепты в корзине (${cartRecipes.size}):", "Recipes in cart (${cartRecipes.size}):"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(cartRecipes, key = { it.recipe.id }) { item ->
                                    val currentServings = CartManager.getRecipeServings(item.recipe.id, item.recipe.baseServings)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = item.recipe.title,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Servings decrement
                                            IconButton(
                                                onClick = {
                                                    val prev = (currentServings - 1.0).coerceAtLeast(1.0)
                                                    CartManager.setRecipeServings(context, item.recipe.id, prev)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Remove,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            Text(
                                                text = UnitConverter.formatAmount(currentServings, item.recipe.yieldUnit),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )

                                            // Servings increment
                                            IconButton(
                                                onClick = {
                                                    val next = currentServings + 1.0
                                                    CartManager.setRecipeServings(context, item.recipe.id, next)
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Add,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(4.dp))

                                            // Remove recipe from cart
                                            IconButton(
                                                onClick = { onCartToggle(item.recipe.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = AppLocaleManager.tr("Удалить из корзины", "Remove from cart"),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Custom Item Field
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customInputText,
                            onValueChange = { customInputText = it },
                            placeholder = { Text(AppLocaleManager.tr("+ Свой товар (напр. салфетки)", "+ Custom item (e.g. napkins)")) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(
                            onClick = {
                                CartManager.addCustomItem(context, customInputText)
                                customInputText = ""
                            },
                            enabled = customInputText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = AppLocaleManager.tr("Добавить", "Add"))
                        }
                    }

                    // Active Categorized Ingredients List (Supermarket Aisles)
                    if (activeIngredients.isNotEmpty()) {
                        val groupedActive = activeIngredients.groupBy { it.groupName }
                        groupedActive.forEach { (groupName, list) ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = groupName,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                list.sortedBy { it.name }.forEach { ing ->
                                    val formattedAmt = UnitConverter.formatAmount(ing.totalAmount, ing.unit)
                                    val displayUnit = UnitConverter.formatUnitForDisplay(ing.unit)

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { CartManager.toggleCheckedKey(context, ing.key) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = false,
                                                onCheckedChange = { CartManager.toggleCheckedKey(context, ing.key) }
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ing.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (ing.notes.isNotEmpty()) {
                                                    Text(
                                                        text = ing.notes.joinToString(", "),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }

                                            if (formattedAmt.isNotEmpty() && ing.totalAmount > 0) {
                                                Text(
                                                    text = "$formattedAmt $displayUnit",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Custom Items Section
                    if (activeCustom.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = AppLocaleManager.tr("📌 Свои покупки", "📌 Custom Purchases"),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            activeCustom.forEach { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { CartManager.toggleCustomItem(context, item.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = false,
                                            onCheckedChange = { CartManager.toggleCustomItem(context, item.id) }
                                        )

                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = { CartManager.removeCustomItem(context, item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = AppLocaleManager.tr("Удалить", "Remove"),
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Collapsible "Completed Items" Accordion
                    if (completedCount > 0) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCompletedAccordion = !showCompletedAccordion },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = AppLocaleManager.tr("✅ Куплено ($completedCount)", "✅ Purchased ($completedCount)"),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = if (showCompletedAccordion) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (showCompletedAccordion) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                completedIngredients.forEach { ing ->
                                    val formattedAmt = UnitConverter.formatAmount(ing.totalAmount, ing.unit)
                                    val displayUnit = UnitConverter.formatUnitForDisplay(ing.unit)

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { CartManager.toggleCheckedKey(context, ing.key) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = true,
                                                onCheckedChange = { CartManager.toggleCheckedKey(context, ing.key) }
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = ing.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    textDecoration = TextDecoration.LineThrough,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }

                                            if (formattedAmt.isNotEmpty() && ing.totalAmount > 0) {
                                                Text(
                                                    text = "$formattedAmt $displayUnit",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    textDecoration = TextDecoration.LineThrough,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                    }
                                }

                                completedCustom.forEach { item ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { CartManager.toggleCustomItem(context, item.id) },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = true,
                                                onCheckedChange = { CartManager.toggleCustomItem(context, item.id) }
                                            )

                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textDecoration = TextDecoration.LineThrough,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.weight(1f)
                                            )

                                            IconButton(
                                                onClick = { CartManager.removeCustomItem(context, item.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = AppLocaleManager.tr("Удалить", "Remove"),
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(shoppingText))
                            Toast.makeText(context, AppLocaleManager.tr("🛒 Список покупок скопирован!", "🛒 Shopping list copied!"), Toast.LENGTH_SHORT).show()
                        },
                        enabled = totalItemsCount > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppLocaleManager.tr("Скопировать", "Copy"))
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shoppingText)
                            }
                            context.startActivity(android.content.Intent.createChooser(sendIntent, AppLocaleManager.tr("Отправить список покупок", "Share shopping list")))
                        },
                        enabled = totalItemsCount > 0,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppLocaleManager.tr("Поделиться", "Share"))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (totalItemsCount > 0) {
                        TextButton(
                            onClick = { CartManager.clearCheckedKeys(context) }
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppLocaleManager.tr("Сбросить галочки", "Clear checks"))
                        }

                        TextButton(
                            onClick = {
                                CartManager.clearAll(context)
                                onClearCart()
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppLocaleManager.tr("Очистить корзину", "Clear cart"), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        },
        dismissButton = {}
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeCardItem(
    itemUiModel: com.aetnox.cooking.ui.viewmodel.RecipeItemUiModel,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCartToggle: () -> Unit = {},
    modifierForCookButton: Modifier = Modifier,
    onOpenMedia: (String) -> Unit = {}
) {
    val recipeWithDetails = itemUiModel.recipeWithDetails
    val recipe = recipeWithDetails.recipe
    val coverUri = itemUiModel.coverUri

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
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
                            model = coil.request.ImageRequest.Builder(LocalContext.current)
                                .data(coverUri)
                                .crossfade(true)
                                .build(),
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
                            text = recipe.title.ifBlank { AppLocaleManager.tr("Черновик заметки", "Note Draft") },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        RecipeStatusChip(status = recipe.status)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TimeAndCountBadge(
                        totalMinutes = itemUiModel.totalTimeMinutes,
                        ingredientsCount = itemUiModel.ingredientsCount
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
                        text = "${AppLocaleManager.tr("⚠️ К доработке:", "⚠️ Needs Improvement:")} ${recipe.postCookNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Tags flow
            if (itemUiModel.formattedTags.isNotEmpty() || !recipe.cloudAccessCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    itemUiModel.formattedTags.forEach { tagName ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tagName",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!recipe.cloudAccessCode.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${AppLocaleManager.tr("🔑 Код:", "🔑 Code:")} ${recipe.cloudAccessCode}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
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
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = AppLocaleManager.tr("Избранное", "Favorite"),
                            tint = if (recipe.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    IconButton(onClick = onCartToggle) {
                        Icon(
                            imageVector = if (recipe.isInCart) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                            contentDescription = AppLocaleManager.tr("Корзина", "Cart"),
                            tint = if (recipe.isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(Icons.Default.Share, contentDescription = AppLocaleManager.tr("Поделиться", "Share"))
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = AppLocaleManager.tr("Редактировать", "Edit"))
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = AppLocaleManager.tr("Удалить", "Delete"), tint = MaterialTheme.colorScheme.error)
                    }
                }

                Button(
                    onClick = onCookClick,
                    modifier = modifierForCookButton,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(AppLocaleManager.tr("Готовить", "Cook"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeGridCardItem(
    itemUiModel: com.aetnox.cooking.ui.viewmodel.RecipeItemUiModel,
    onCardClick: () -> Unit,
    onCookClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onCartToggle: () -> Unit = {},
    onOpenMedia: (String) -> Unit = {}
) {
    val recipeWithDetails = itemUiModel.recipeWithDetails
    val recipe = recipeWithDetails.recipe
    val coverUri = itemUiModel.coverUri
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (!coverUri.isNullOrBlank()) {
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(LocalContext.current)
                            .data(coverUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onOpenMedia(coverUri) }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(42.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }

                // Top Left: Status Chip
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    RecipeStatusChip(status = recipe.status)
                }

                // Top Right: Favorite Star, Cart & More Options Overflow
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onFavoriteToggle() }
                    ) {
                        Box(
                            modifier = Modifier.padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recipe.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = AppLocaleManager.tr("Избранное", "Favorite"),
                                tint = if (recipe.isFavorite) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onCartToggle() }
                    ) {
                        Box(
                            modifier = Modifier.padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (recipe.isInCart) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                                contentDescription = AppLocaleManager.tr("Корзина", "Cart"),
                                tint = if (recipe.isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { showMenu = true }
                        ) {
                            Box(
                                modifier = Modifier.padding(5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = AppLocaleManager.tr("Действия", "Actions"),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (recipe.isInCart) AppLocaleManager.tr("🛒 Удалить из корзины", "🛒 Remove from cart") else AppLocaleManager.tr("🛒 В корзину", "🛒 Add to cart")) },
                                onClick = {
                                    showMenu = false
                                    onCartToggle()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(AppLocaleManager.tr("✏️ Редактировать", "✏️ Edit")) },
                                onClick = {
                                    showMenu = false
                                    onEditClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(AppLocaleManager.tr("📤 Поделиться", "📤 Share")) },
                                onClick = {
                                    showMenu = false
                                    onShareClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(AppLocaleManager.tr("🗑️ Удалить", "🗑️ Delete"), color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }

                // Bottom Left on Image: Rating Badge
                if (recipe.rating > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${recipe.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = recipe.title.ifBlank { AppLocaleManager.tr("Черновик", "Draft") },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                val totalTime = itemUiModel.totalTimeMinutes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (totalTime > 0) "⏱ $totalTime ${AppLocaleManager.tr("мин", "min")}" else "🛒 ${itemUiModel.ingredientsCount} ${AppLocaleManager.tr("ингред.", "ingred.")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )

                    if (totalTime > 0) {
                        Text(
                            text = "🛒 ${itemUiModel.ingredientsCount} ${AppLocaleManager.tr("ингред.", "ingred.")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Needs Work Note Banner
                if (recipe.postCookNote.isNotBlank() && recipe.status == RecipeEntity.STATUS_NEEDS_WORK) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "⚠️ ${recipe.postCookNote}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Tags & Cloud Access Code Flow
                if (itemUiModel.formattedTags.isNotEmpty() || !recipe.cloudAccessCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        itemUiModel.formattedTags.take(2).forEach { tagName ->
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "#$tagName",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (!recipe.cloudAccessCode.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "🔑 ${recipe.cloudAccessCode}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = androidx.compose.ui.unit.TextUnit(9f, androidx.compose.ui.unit.TextUnitType.Sp),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = AppLocaleManager.tr("Поделиться", "Share"),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = AppLocaleManager.tr("Редактировать", "Edit"),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Button(
                        onClick = onCookClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            AppLocaleManager.tr("Готовить", "Cook"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShareRecipeDialog(
    recipeWithDetails: RecipeWithDetails,
    viewModel: RecipeListViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isUploading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppLocaleManager.tr("Поделиться рецептом", "Share recipe"), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "«${recipeWithDetails.recipe.title}»",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = AppLocaleManager.tr("Выберите удобный способ отправки:", "Choose transfer method:"),
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
                            context.startActivity(Intent.createChooser(shareIntent, AppLocaleManager.tr("Отправить рецепт текстом", "Share recipe text")))
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
                            Text(AppLocaleManager.tr("💬 Текстом (для сообщений и заметок)", "💬 As Text (for messages & notes)"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(AppLocaleManager.tr("Красивый читаемый текст с ингредиентами и шагами", "Formatted readable text with ingredients & steps"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                context.startActivity(Intent.createChooser(shareIntent, AppLocaleManager.tr("Отправить файл рецепта", "Share recipe file")))
                            } else {
                                Toast.makeText(context, AppLocaleManager.tr("Не удалось создать файл рецепта", "Failed to create recipe file"), Toast.LENGTH_SHORT).show()
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
                            Text(AppLocaleManager.tr("📁 Файлом рецепта .json (100% точность)", "📁 As .json File (100% precision)"), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(AppLocaleManager.tr("Передать файл другому пользователю приложения Cooking с сохранением всех деталей", "Send file to another Cooking app user with all details preserved"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }

                // Cloud Upload Option
                var isPrivateUpload by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isUploading) {
                                    isUploading = true
                                    viewModel.uploadRecipeToCloud(
                                        context = context,
                                        recipeId = recipeWithDetails.recipe.id,
                                        isPublic = !isPrivateUpload,
                                        onSuccess = { cloudId, accessCode ->
                                            isUploading = false
                                            val copyText = accessCode ?: cloudId
                                            clipboardManager.setText(AnnotatedString(copyText))
                                            val msg = if (accessCode != null) {
                                                AppLocaleManager.tr("🔒 Приватный рецепт загружен! Код доступа скопирован: $accessCode", "🔒 Private recipe uploaded! Access code copied: $accessCode")
                                            } else {
                                                AppLocaleManager.tr("☁️ Публичный рецепт загружен! ID скопирован: $cloudId", "☁️ Public recipe uploaded! ID copied: $cloudId")
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                            onDismiss()
                                        },
                                        onError = { err ->
                                            isUploading = false
                                            Toast.makeText(context, "❌ ${AppLocaleManager.tr("Ошибка загрузки:", "Upload error:")} $err", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPrivateUpload) AppLocaleManager.tr("🔒 Выгрузить в облако (Приватно по коду)", "🔒 Upload to Cloud (Private code)") else AppLocaleManager.tr("🌐 Выгрузить в открытый каталог", "🌐 Upload to Public Catalog"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = if (isPrivateUpload) AppLocaleManager.tr("Доступ будет только у обладателей 10-значного кода", "Accessible only with 10-digit access code") else AppLocaleManager.tr("Рецепт появится в общем поиске приложения для всех", "Recipe will appear in public search for everyone"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppLocaleManager.tr("Сделать доступным только по коду", "Make private via access code"),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Switch(
                                checked = isPrivateUpload,
                                onCheckedChange = { isPrivateUpload = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLocaleManager.tr("Закрыть", "Close"))
            }
        }
    )
}

@Composable
fun AiSearchDialog(
    uiState: RecipeListUiState,
    viewModel: RecipeListViewModel,
    onDismiss: () -> Unit,
    onOpenLocalRecipe: (Long) -> Unit,
    onOpenCloudRecipe: (String) -> Unit
) {
    val context = LocalContext.current
    var inputQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.initSessionObserver(context)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(AppLocaleManager.tr("Умный ИИ-поиск рецептов", "Smart AI Recipe Search"), fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Выбор области поиска (scope)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.aiSearchScope == "all",
                        onClick = { viewModel.setAiSearchScope("all") },
                        label = { Text(AppLocaleManager.tr("🌐 Везде", "🌐 Everywhere")) }
                    )
                    FilterChip(
                        selected = uiState.aiSearchScope == "local",
                        onClick = { viewModel.setAiSearchScope("local") },
                        label = { Text(AppLocaleManager.tr("📖 На телефоне", "📖 On Device")) }
                    )
                    FilterChip(
                        selected = uiState.aiSearchScope == "cloud",
                        onClick = { viewModel.setAiSearchScope("cloud") },
                        label = { Text(AppLocaleManager.tr("☁️ В облаке", "☁️ In Cloud")) }
                    )
                }

                // Переключатель режимов с прогнозом стоимости
                val singleCost = uiState.getSingleSearchCostForecast()
                val twoPhaseCost = uiState.getTwoPhaseSearchCostForecast()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = uiState.aiSearchMode == "auto",
                        onClick = { viewModel.setAiSearchMode("auto") },
                        label = { Text(AppLocaleManager.tr("🤖 Авто", "🤖 Auto")) }
                    )
                    FilterChip(
                        selected = uiState.aiSearchMode == "two_phase",
                        onClick = { viewModel.setAiSearchMode("two_phase") },
                        label = { Text("⚡ ${AppLocaleManager.tr("Двухфазный", "Two-phase")} (${FormatUtils.formatVitaminAi(twoPhaseCost, short = true)})") }
                    )
                    FilterChip(
                        selected = uiState.aiSearchMode == "single",
                        onClick = { viewModel.setAiSearchMode("single") },
                        label = { Text("🔍 ${AppLocaleManager.tr("Обычный", "Standard")} (${FormatUtils.formatVitaminAi(singleCost, short = true)})") }
                    )
                }

                // Чипы сформированного плана поиска (если двухфазный поиск активен)
                uiState.aiSearchPlanInfo?.let { plan ->
                    val hasKeywords = plan.keywords.isNotEmpty()
                    val hasIngredients = plan.ingredients.isNotEmpty()
                    val hasTime = plan.maxMinutes != null || plan.minMinutes != null

                    if (hasKeywords || hasIngredients || hasTime) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("Условия поиска из диалога:", "Search conditions from chat:"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    plan.keywords.forEach { kw ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "🔑 $kw",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    plan.ingredients.forEach { ing ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "🧀 $ing",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    val maxM = plan.maxMinutes
                                    if (maxM != null) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "⏱️ ${AppLocaleManager.tr("до", "under")} $maxM ${AppLocaleManager.tr("мин", "min")}",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    val minM = plan.minMinutes
                                    if (minM != null) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.tertiaryContainer,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            Text(
                                                text = "⏱️ ${AppLocaleManager.tr("от", "over")} $minM ${AppLocaleManager.tr("мин", "min")}",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (uiState.aiSearchError != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.aiSearchError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .height(280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (uiState.aiSearchMessages.isEmpty()) {
                        Text(
                            AppLocaleManager.tr(
                                "Напишите свои пожелания, например:\n«Ужин до 30 минут без духовки, есть грибы и сметана»",
                                "Type what you'd like to cook, e.g.:\n'Dinner under 30 minutes no oven, have mushrooms and sour cream'"
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    uiState.aiSearchMessages.forEach { msg ->
                        val isUser = msg.role == "user"
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = msg.content,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    if (uiState.isAiSearching) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text(AppLocaleManager.tr("ИИ подбирает подходящие рецепты...", "AI is selecting matching recipes..."), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (uiState.aiSearchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            AppLocaleManager.tr("Рекомендованные варианты:", "Recommended options:"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        uiState.aiSearchResults.forEach { result ->
                            val isLocal = result.source == "local"
                            Card(
                                onClick = {
                                    if (isLocal) {
                                        val localId = result.id.toLongOrNull() ?: 0L
                                        onOpenLocalRecipe(localId)
                                    } else {
                                        onOpenCloudRecipe(result.id)
                                    }
                                },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isLocal) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = result.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isLocal) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                        ) {
                                            Text(
                                                text = if (isLocal) AppLocaleManager.tr("📖 Локальный", "📖 Local") else AppLocaleManager.tr("☁️ Облако", "☁️ Cloud"),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isLocal) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (result.minutes > 0) {
                                        Text(
                                            text = "⏱️ ${result.minutes} ${AppLocaleManager.tr("мин", "min")}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (result.reason.isNotBlank()) {
                                        Text(
                                            text = result.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(AppLocaleManager.tr("Ингредиенты, время, диета...", "Ingredients, cook time, diet...")) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (inputQuery.isNotBlank()) {
                                val textToSend = inputQuery
                                inputQuery = ""
                                viewModel.sendAiSearchQuery(context, textToSend)
                            }
                        },
                        enabled = !uiState.isAiSearching && inputQuery.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = AppLocaleManager.tr("Отправить", "Send"))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                viewModel.clearAiSearch()
            }) {
                Text(AppLocaleManager.tr("Очистить чат", "Clear chat"))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.hideAiSearchDialog()
                onDismiss()
            }) {
                Text(AppLocaleManager.tr("Закрыть", "Close"))
            }
        }
    )
}
