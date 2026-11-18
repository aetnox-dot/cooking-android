package com.aetnox.cooking.ui.screen

import android.app.Activity
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.StepSegment
import com.aetnox.cooking.domain.parser.StepTokenParser
import com.aetnox.cooking.ui.component.CookAssistantSheet
import com.aetnox.cooking.ui.component.StarRatingBar
import com.aetnox.cooking.ui.component.StepTimerCard
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.FullscreenMediaDialog
import com.aetnox.cooking.ui.util.MediaViewerUtils
import com.aetnox.cooking.ui.viewmodel.CookModeViewModel
import com.aetnox.cooking.ui.viewmodel.StepTimerParser
import com.aetnox.cooking.ui.viewmodel.StepTimerState

enum class CookViewTab {
    STEP_BY_STEP, OVERVIEW
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CookModeScreen(
    viewModel: CookModeViewModel,
    initialTab: CookViewTab = CookViewTab.STEP_BY_STEP,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activeTab by remember(initialTab) { mutableStateOf(initialTab) }
    var showCustomServingsDialog by remember { mutableStateOf(false) }
    var customServingsInput by remember { mutableStateOf("") }
    var selectedFullscreenMediaUri by remember { mutableStateOf<String?>(null) }
    var showIngredientsSheetInStepMode by remember { mutableStateOf(false) }
    var showAssistant by remember { mutableStateOf(false) }

    // Экспериментальная функция: при выключенной настройке или отключённом ИИ UI ассистента не существует
    val netSettings = ExperimentalSettingsManager.getSettings(context)
    val cookAssistantEnabled = netSettings.cookAssistantEnabled && !netSettings.aiDisabled

    // KEEP SCREEN AWAKE EFFECT
    DisposableEffect(uiState.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (uiState.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val recipeDetails = uiState.recipeWithDetails
    if (recipeDetails == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (uiState.recipeNotFound) AppLocaleManager.tr("Рецепт не найден", "Recipe not found") else AppLocaleManager.tr("Загрузка рецепта...", "Loading recipe..."),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppLocaleManager.tr("Назад", "Back"))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.recipeNotFound) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("😕", style = MaterialTheme.typography.displayLarge)
                        Text(
                            AppLocaleManager.tr("Рецепт не найден.\nВозможно, он был удалён.", "Recipe not found.\nIt might have been deleted."),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onNavigateBack, shape = RoundedCornerShape(14.dp)) {
                            Text(AppLocaleManager.tr("Назад к списку", "Back to list"))
                        }
                    }
                } else {
                    CircularProgressIndicator(strokeWidth = 3.dp)
                }
            }
        }
        return
    }

    val recipe = recipeDetails.recipe
    val sortedSteps = remember(recipeDetails.steps) { recipeDetails.steps.sortedBy { it.stepNumber } }
    val totalSteps = sortedSteps.size
    val totalVirtualSteps = totalSteps + 1 // Step 0 = Prep Ingredients, Steps 1..N = Cooking steps

    LaunchedEffect(recipe.id, uiState.activeStepIndex, uiState.prepCheckedIngredients, uiState.stepCheckedTokens, uiState.currentServings) {
        com.aetnox.cooking.ui.util.ActiveCookingManager.saveProgress(
            context = context,
            recipeId = recipe.id,
            recipeTitle = recipe.title,
            activeStepIndex = uiState.activeStepIndex,
            totalSteps = sortedSteps.size,
            checkedIngredients = uiState.prepCheckedIngredients,
            stepCheckedTokens = uiState.stepCheckedTokens,
            currentServings = uiState.currentServings
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(recipe.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppLocaleManager.tr("Назад", "Back"))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            imageVector = if (recipe.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                            contentDescription = AppLocaleManager.tr("Избранное", "Favorite"),
                            tint = if (recipe.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = { viewModel.toggleCartStatus() }) {
                        Icon(
                            imageVector = if (recipe.isInCart) Icons.Default.ShoppingCart else Icons.Outlined.ShoppingCart,
                            contentDescription = AppLocaleManager.tr("Корзина", "Cart"),
                            tint = if (recipe.isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (cookAssistantEnabled) {
                        IconButton(onClick = { showAssistant = true }) {
                            Icon(Icons.Default.SmartToy, contentDescription = AppLocaleManager.tr("ИИ-ассистент", "AI Assistant"))
                        }
                    }
                    IconButton(onClick = { onNavigateToEdit(recipe.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = AppLocaleManager.tr("Редактировать рецепт", "Edit recipe"))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.LockClock,
                            contentDescription = null,
                            tint = if (uiState.keepScreenOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Switch(
                            checked = uiState.keepScreenOn,
                            onCheckedChange = { viewModel.setKeepScreenOn(it) }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (uiState.activeStepIndex == totalSteps) {
                Surface(
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.showPostCookDialog() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppLocaleManager.tr("Завершить готовку", "Finish Cooking"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
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
            // View Mode Tab Bar
            PrimaryTabRow(selectedTabIndex = if (activeTab == CookViewTab.STEP_BY_STEP) 0 else 1) {
                Tab(
                    selected = activeTab == CookViewTab.STEP_BY_STEP,
                    onClick = { activeTab = CookViewTab.STEP_BY_STEP },
                    text = { Text(AppLocaleManager.tr("👨‍🍳 Шаг за шагом", "👨‍🍳 Step-by-step"), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == CookViewTab.OVERVIEW,
                    onClick = { activeTab = CookViewTab.OVERVIEW },
                    text = { Text(AppLocaleManager.tr("📋 Обзор рецепта", "📋 Overview"), fontWeight = FontWeight.Bold) }
                )
            }

            if (activeTab == CookViewTab.STEP_BY_STEP) {
                // Interactive Step-by-Step Horizontal Pager View
                val pagerState = rememberPagerState(
                    initialPage = uiState.activeStepIndex.coerceIn(0, totalVirtualSteps - 1),
                    pageCount = { totalVirtualSteps }
                )

                LaunchedEffect(uiState.activeStepIndex) {
                    if (pagerState.currentPage != uiState.activeStepIndex) {
                        pagerState.animateScrollToPage(uiState.activeStepIndex.coerceIn(0, totalVirtualSteps - 1))
                    }
                }

                LaunchedEffect(pagerState.currentPage) {
                    if (uiState.activeStepIndex != pagerState.currentPage) {
                        viewModel.setActiveStepIndex(pagerState.currentPage)
                    }
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Horizontal Step Navigation Bar (Quick Jump Bar)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        items(count = totalVirtualSteps, key = { it }) { stepIdx ->
                            val isSelected = pagerState.currentPage == stepIdx
                            val label = if (stepIdx == 0) AppLocaleManager.tr("🛒 Подготовка", "🛒 Preparation") else AppLocaleManager.tr("Шаг $stepIdx", "Step $stepIdx")
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setActiveStepIndex(stepIdx) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val currentVirtualStep = page
                        val progressFraction = (currentVirtualStep + 1).toFloat() / totalVirtualSteps.toFloat()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                    // Progress Bar & Step Counter Header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (currentVirtualStep == 0) AppLocaleManager.tr("ПОДГОТОВКА ИНГРЕДИЕНТОВ", "INGREDIENTS PREPARATION") else AppLocaleManager.tr("ШАГ $currentVirtualStep ИЗ $totalSteps", "STEP $currentVirtualStep OF $totalSteps"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                if (currentVirtualStep > 0) {
                                    TextButton(onClick = { showIngredientsSheetInStepMode = !showIngredientsSheetInStepMode }) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (showIngredientsSheetInStepMode) AppLocaleManager.tr("Скрыть продукты", "Hide items") else AppLocaleManager.tr("🛒 Продукты", "🛒 Products"))
                                    }
                                }
                            }

                            if (uiState.keepScreenOn) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = AppLocaleManager.tr("💡 Экран включен и не погаснет в процессе готовки", "💡 Screen stays on during cooking"),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                            )

                            val totalPrepIngs = recipeDetails.ingredients.size
                            val checkedPrepIngs = uiState.prepCheckedIngredients.size
                            if (currentVirtualStep > 0 && checkedPrepIngs < totalPrepIngs) {
                                val missingCount = totalPrepIngs - checkedPrepIngs
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = AppLocaleManager.tr("ℹ️ На этапе подготовки не отмечено продуктов: $missingCount из $totalPrepIngs", "ℹ️ Ingredients unchecked in preparation: $missingCount of $totalPrepIngs"),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Collapsible Ingredients Checklist in Step Mode (if in step 1..N)
                    AnimatedVisibility(visible = showIngredientsSheetInStepMode && currentVirtualStep > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("🛒 Необходимые ингредиенты:", "🛒 Required ingredients:"),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                recipeDetails.ingredients.forEach { ing ->
                                    val isStepChecked = uiState.stepCheckedTokens.contains("${currentVirtualStep}_${ing.id}")
                                    IngredientChecklistItem(
                                        ingredient = ing,
                                        baseServings = recipe.baseServings,
                                        currentServings = uiState.currentServings,
                                        isChecked = isStepChecked,
                                        onToggleCheck = { viewModel.toggleStepIngredientChecked(currentVirtualStep, ing.id) },
                                        onSelectUnit = { newUnit ->
                                            viewModel.onUnitSelectedForIngredient(ing.id, newUnit)
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }

                    if (currentVirtualStep == 0) {
                        // VIRTUAL STEP 0: INITIAL "ПОДГОТОВЬТЕ ИНГРЕДИЕНТЫ:" CARD
                        val recipeImages = remember(recipe.imageUrisJson) {
                            try {
                                kotlinx.serialization.json.Json.decodeFromString<List<String>>(recipe.imageUrisJson)
                            } catch (_: Exception) { emptyList() }
                        }

                        if (recipeImages.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "${AppLocaleManager.tr("📷 Фотографии блюда", "📷 Dish photos")} (${recipeImages.size}):",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(recipeImages, key = { it }) { uriStr ->
                                            val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                                            Box(
                                                modifier = Modifier
                                                    .size(110.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable { selectedFullscreenMediaUri = uriStr },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = uriStr,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (isVideo) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = AppLocaleManager.tr("Видео", "Video"), tint = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "🛒 ${AppLocaleManager.tr("Подготовьте ингредиенты:", "Prepare ingredients:")}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Отмерьте и подготовьте все необходимые продукты перед началом готовки:", "Measure and prepare all required ingredients before cooking:"),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // PORTION SCALER BAR IN PREP STEP
                                if (recipe.servingsEnabled) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val yieldLabel = if (AppLocaleManager.isEnglish && recipe.yieldUnit == "порций") "servings" else recipe.yieldUnit
                                            Text(
                                                text = "${AppLocaleManager.tr("Порций", "Servings")}: ${UnitConverter.formatAmount(uiState.currentServings, "шт")} $yieldLabel",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(onClick = { viewModel.decrementServings() }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Remove, contentDescription = AppLocaleManager.tr("Меньше", "Less"))
                                                }
                                                IconButton(onClick = { viewModel.incrementServings() }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.Add, contentDescription = AppLocaleManager.tr("Больше", "More"))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // CHECKABLE INGREDIENTS LIST GROUPED BY SECTIONS
                                val groupedIngredients = remember(recipeDetails.ingredients) {
                                    recipeDetails.ingredients.groupBy { it.groupName.trim() }
                                }

                                groupedIngredients.forEach { (groupName, list) ->
                                    if (groupName.isNotEmpty()) {
                                        Text(
                                            text = "📌 $groupName",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    }
                                    list.forEach { ingredient ->
                                        IngredientChecklistItem(
                                            ingredient = ingredient,
                                            baseServings = recipe.baseServings,
                                            currentServings = uiState.currentServings,
                                            isChecked = uiState.checkedIngredients.contains(ingredient.id),
                                            onToggleCheck = { viewModel.toggleIngredientChecked(ingredient.id) },
                                            onSelectUnit = { newUnit ->
                                                viewModel.onUnitSelectedForIngredient(ingredient.id, newUnit)
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    } else {
                        // VIRTUAL STEP 1..N: ACTUAL STEP INSTRUCTIONS
                        val currentStep = sortedSteps[currentVirtualStep - 1]
                        val currentStepNum = currentStep.stepNumber
                        val timerState = uiState.stepTimers[currentStepNum]
                        val parsedSeconds = StepTimerParser.parseDurationInSeconds(currentStep.instructionText)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                val stepCheckedIngs = remember(uiState.stepCheckedTokens, recipeDetails.ingredients, currentStepNum) {
                                    recipeDetails.ingredients.filter { uiState.stepCheckedTokens.contains("${currentStepNum}_${it.id}") }.map { it.id }.toSet()
                                }

                                StepTextWithBadges(
                                    instructionText = currentStep.instructionText,
                                    ingredients = recipeDetails.ingredients,
                                    baseServings = recipe.baseServings,
                                    currentServings = uiState.currentServings,
                                    checkedIngredients = stepCheckedIngs,
                                    onToggleIngredientCheck = { ingId -> viewModel.toggleStepIngredientChecked(currentStepNum, ingId) }
                                )

                                // Step Timer if defined or running
                                val durationToUse = timerState?.totalSeconds ?: parsedSeconds
                                if (durationToUse != null) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    StepTimerCard(
                                        secondsRemaining = timerState?.secondsRemaining ?: durationToUse,
                                        isRunning = timerState?.isRunning == true,
                                        isFinished = timerState?.isFinished == true,
                                        onStartPause = { viewModel.startOrPauseStepTimer(context, currentStepNum, durationToUse) },
                                        onReset = { viewModel.resetStepTimer(context, currentStepNum) },
                                        onAddSeconds = { addSecs ->
                                            viewModel.addTimeToStepTimer(context, currentStepNum, addSecs)
                                        }
                                    )
                                }

                                // Step Image Illustrations
                                val stepImages = remember(currentStep.imageUrisJson) {
                                    try {
                                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(currentStep.imageUrisJson)
                                    } catch (_: Exception) { emptyList() }
                                }
                                if (stepImages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(stepImages, key = { it }) { uriStr ->
                                            val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                                            Box(
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { selectedFullscreenMediaUri = uriStr },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = uriStr,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                if (isVideo) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.PlayArrow, contentDescription = AppLocaleManager.tr("Видео", "Video"), tint = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Navigation Buttons (PREVIOUS & NEXT)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.setActiveStepIndex(currentVirtualStep - 1) },
                            enabled = currentVirtualStep > 0,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(AppLocaleManager.tr("Назад", "Back"))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (currentVirtualStep < totalVirtualSteps - 1) {
                                    viewModel.setActiveStepIndex(currentVirtualStep + 1)
                                } else {
                                    viewModel.showPostCookDialog()
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (currentVirtualStep == 0) AppLocaleManager.tr("Начать готовку ▶", "Start cooking ▶") else if (currentVirtualStep < totalVirtualSteps - 1) AppLocaleManager.tr("Вперед", "Next") else AppLocaleManager.tr("Готово", "Done")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }
        }
    } else {
                // OVERVIEW MODE (Scrollable Full Recipe View)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // RAW NOTES CARD
                    if (recipe.rawNotes.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📝 ${AppLocaleManager.tr("Быстрая заметка черновика", "Quick draft note")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = recipe.rawNotes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    // CHEF'S CHEAT SHEET CARD («Шпаргалка шефа»)
                    if (recipe.chefHeatingType.isNotBlank() || recipe.chefTemperature.isNotBlank() ||
                        recipe.chefTiming.isNotBlank() || recipe.chefReadinessSign.isNotBlank() ||
                        recipe.prepTimeMinutes > 0 || recipe.cookTimeMinutes > 0
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "👨‍🍳 ${AppLocaleManager.tr("Шпаргалка шефа", "Chef's cheat sheet")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                if (recipe.chefHeatingType.isNotBlank()) {
                                    Text(
                                        text = "🔥 ${AppLocaleManager.tr("Нагрев", "Heating")}: ${recipe.chefHeatingType}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if (recipe.chefTemperature.isNotBlank()) {
                                    Text(
                                        text = "🌡 ${AppLocaleManager.tr("Температура", "Temperature")}: ${recipe.chefTemperature}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if (recipe.chefTiming.isNotBlank() || recipe.cookTimeMinutes > 0) {
                                    val timeText = recipe.chefTiming.ifBlank { "${recipe.cookTimeMinutes} ${AppLocaleManager.tr("мин", "min")}" }
                                    Text(
                                        text = "⏱ ${AppLocaleManager.tr("Тайминг", "Timing")}: $timeText",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if (recipe.chefReadinessSign.isNotBlank()) {
                                    Text(
                                        text = "✨ ${AppLocaleManager.tr("Признак готовности", "Readiness sign")}: ${recipe.chefReadinessSign}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }

                    // NUTRITION (КБЖУ) CARD
                    if (recipe.calories != null || recipe.protein != null || recipe.fat != null || recipe.carbs != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "📊 ${AppLocaleManager.tr("Пищевая ценность (на порцию)", "Nutritional Value (per serving)")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val calWord = AppLocaleManager.tr("ккал", "kcal")
                                val gWord = AppLocaleManager.tr("г", "g")
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    recipe.calories?.let {
                                        Text("🔥 ${it.toInt()} $calWord", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    recipe.protein?.let {
                                        Text("🥩 ${AppLocaleManager.tr("Б", "P")}: ${UnitConverter.formatAmount(it, "г")}$gWord", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    recipe.fat?.let {
                                        Text("🧈 ${AppLocaleManager.tr("Ж", "F")}: ${UnitConverter.formatAmount(it, "г")}$gWord", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    recipe.carbs?.let {
                                        Text("🌾 ${AppLocaleManager.tr("У", "C")}: ${UnitConverter.formatAmount(it, "г")}$gWord", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    // DYNAMIC PORTION SCALER
                    if (recipe.servingsEnabled) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = AppLocaleManager.tr("Масштабирование порций", "Servings scaling"),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val yieldLabel = if (AppLocaleManager.isEnglish && recipe.yieldUnit == "порций") "servings" else recipe.yieldUnit
                                        Text(
                                            text = "${AppLocaleManager.tr("Базово", "Base")}: ${UnitConverter.formatAmount(recipe.baseServings, "шт")} $yieldLabel",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { viewModel.decrementServings() }) {
                                            Icon(Icons.Default.Remove, contentDescription = AppLocaleManager.tr("Меньше", "Less"))
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                .clickable {
                                                    customServingsInput = UnitConverter.formatAmount(uiState.currentServings, AppLocaleManager.tr("шт", "pcs"))
                                                    showCustomServingsDialog = true
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            val yieldLabel = if (AppLocaleManager.isEnglish && recipe.yieldUnit == "порций") "servings" else recipe.yieldUnit
                                            Text(
                                                text = "${UnitConverter.formatAmount(uiState.currentServings, AppLocaleManager.tr("шт", "pcs"))} $yieldLabel",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(onClick = { viewModel.incrementServings() }) {
                                            Icon(Icons.Default.Add, contentDescription = AppLocaleManager.tr("Больше", "More"))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${AppLocaleManager.tr("Пресеты", "Presets")}:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    listOf(0.5 to "½x", 1.0 to "1x", 2.0 to "2x", 3.0 to "3x").forEach { (mult, label) ->
                                        val targetServings = (recipe.baseServings * mult).coerceAtLeast(0.5)
                                        val isSelected = (uiState.currentServings - targetServings).let { kotlin.math.abs(it) < 0.05 }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            modifier = Modifier.clickable { viewModel.setCustomServings(targetServings) }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // INGREDIENT CHECKLIST WITH GROUPS
                    Column {
                        Text(
                            text = "🛒 ${AppLocaleManager.tr("Чек-лист ингредиентов", "Ingredients Checklist")}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val groupedIngredients = remember(recipeDetails.ingredients) {
                            recipeDetails.ingredients.groupBy { it.groupName.trim() }
                        }

                        groupedIngredients.forEach { (groupName, list) ->
                            if (groupName.isNotEmpty()) {
                                Text(
                                    text = "📌 $groupName",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            list.forEach { ingredient ->
                                IngredientChecklistItem(
                                    ingredient = ingredient,
                                    baseServings = recipe.baseServings,
                                    currentServings = uiState.currentServings,
                                    isChecked = uiState.checkedIngredients.contains(ingredient.id),
                                    onToggleCheck = { viewModel.toggleIngredientChecked(ingredient.id) },
                                    onSelectUnit = { newUnit ->
                                        viewModel.onUnitSelectedForIngredient(ingredient.id, newUnit)
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }

                    // FULL STEPS LIST IN OVERVIEW MODE
                    Column {
                        Text(
                            text = "👨‍🍳 ${AppLocaleManager.tr("Шаги приготовления", "Cooking steps")} (${sortedSteps.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        sortedSteps.forEachIndexed { index, step ->
                            val timerState = uiState.stepTimers[step.stepNumber]
                            val isActive = (index + 1) == uiState.activeStepIndex

                            StepCardWithBadges(
                                stepNumber = step.stepNumber,
                                instructionText = step.instructionText,
                                imageUrisJson = step.imageUrisJson,
                                ingredients = recipeDetails.ingredients,
                                baseServings = recipe.baseServings,
                                currentServings = uiState.currentServings,
                                checkedIngredients = uiState.checkedIngredients,
                                timerState = timerState,
                                isActiveStep = isActive,
                                onSelectStep = { viewModel.setActiveStepIndex(index + 1) },
                                onStartOrPauseTimer = { duration -> viewModel.startOrPauseStepTimer(context, step.stepNumber, duration) },
                                onResetTimer = { viewModel.resetStepTimer(context, step.stepNumber) },
                                onAddTimeToTimer = { addSecs -> viewModel.addTimeToStepTimer(context, step.stepNumber, addSecs) },
                                onToggleIngredientCheck = { ingId -> viewModel.toggleIngredientChecked(ingId) },
                                onSelectMedia = { uriStr -> selectedFullscreenMediaUri = uriStr }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }

    // Direct Portions Keypad Input Dialog
    if (showCustomServingsDialog) {
        AlertDialog(
            onDismissRequest = { showCustomServingsDialog = false },
            title = { Text(AppLocaleManager.tr("Введите количество порций", "Enter number of servings")) },
            text = {
                val yieldLabel = if (AppLocaleManager.isEnglish && recipe.yieldUnit == "порций") "servings" else recipe.yieldUnit
                OutlinedTextField(
                    value = customServingsInput,
                    onValueChange = { customServingsInput = it },
                    label = { Text("${AppLocaleManager.tr("Порции", "Servings")} ($yieldLabel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val num = customServingsInput.replace(',', '.').toDoubleOrNull()
                        if (num != null && num > 0) {
                            viewModel.setCustomServings(num)
                        }
                        showCustomServingsDialog = false
                    }
                ) {
                    Text(AppLocaleManager.tr("Применить", "Apply"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomServingsDialog = false }) {
                    Text(AppLocaleManager.tr("Отмена", "Cancel"))
                }
            }
        )
    }

    // POST-COOKING DIALOG
    if (uiState.showPostCookDialog) {
        PostCookDialog(
            currentStatus = recipe.status,
            currentRating = recipe.rating,
            currentPostNote = recipe.postCookNote,
            onDismiss = { viewModel.hidePostCookDialog() },
            onConfirm = { status, rating, note ->
                viewModel.finishCooking(status, rating, note) {
                    com.aetnox.cooking.ui.util.ActiveCookingManager.clearSession(context, recipe.id)
                    onNavigateBack()
                }
            }
        )
    }

    // FULLSCREEN MEDIA VIEWER DIALOG
    selectedFullscreenMediaUri?.let { uriStr ->
        FullscreenMediaDialog(
            uriStr = uriStr,
            onDismiss = { selectedFullscreenMediaUri = null }
        )
    }

    // AI COOK ASSISTANT (экспериментальная функция, только при включённой настройке)
    if (showAssistant && cookAssistantEnabled) {
        CookAssistantSheet(
            viewModel = viewModel,
            onDismiss = { showAssistant = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepTextWithBadges(
    instructionText: String,
    ingredients: List<IngredientEntity>,
    baseServings: Double,
    currentServings: Double,
    checkedIngredients: Set<Long> = emptySet(),
    onToggleIngredientCheck: (Long) -> Unit = {}
) {
    val segments = remember(instructionText) { StepTokenParser.parseSegments(instructionText) }

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is StepSegment.TextSegment -> {
                    Text(
                        text = segment.text,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
                is StepSegment.IngredientTokenSegment -> {
                    val tokenClean = segment.ingredientName.trim()

                    val exactGroupMatch = ingredients.find {
                        "${it.name} (${it.groupName})".trim().equals(tokenClean, ignoreCase = true)
                    }

                    val matchedIngs = if (exactGroupMatch != null) {
                        listOf(exactGroupMatch)
                    } else {
                        ingredients.filter {
                            it.name.trim().equals(tokenClean, ignoreCase = true) ||
                            "${it.name} (${it.groupName})".trim().equals(tokenClean, ignoreCase = true)
                        }
                    }

                    val isChecked = matchedIngs.isNotEmpty() && matchedIngs.all { checkedIngredients.contains(it.id) }

                    val badgeText = if (matchedIngs.isNotEmpty()) {
                        val firstIng = matchedIngs.first()
                        val checkPrefix = if (isChecked) "✓ " else ""
                        val amtStr = UnitConverter.scaleAndFormatAmount(
                            amount = firstIng.amount,
                            fromUnit = firstIng.unit,
                            toUnit = firstIng.selectedUnit,
                            baseServings = baseServings,
                            currentServings = currentServings,
                            ingredientName = firstIng.name,
                            customGramsPerUnit = firstIng.gramsPerUnit
                        )
                        val displayUnit = UnitConverter.formatUnitForDisplay(firstIng.selectedUnit)
                        if (amtStr.isNotEmpty()) {
                            "$checkPrefix${firstIng.name} ($amtStr $displayUnit)"
                        } else {
                            "$checkPrefix${firstIng.name}"
                        }
                    } else {
                        segment.ingredientName
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                        else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .clickable {
                                matchedIngs.forEach { onToggleIngredientCheck(it.id) }
                            }
                    ) {
                        Text(
                            text = badgeText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isChecked) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DottedLeaderLine(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Canvas(modifier = modifier.height(1.dp)) {
        val dotRadius = 1.2.dp.toPx()
        val spacing = 7.dp.toPx()
        var currentX = dotRadius
        val y = size.height / 2
        while (currentX < size.width - dotRadius) {
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(currentX, y)
            )
            currentX += spacing
        }
    }
}

@Composable
fun IngredientChecklistItem(
    ingredient: IngredientEntity,
    baseServings: Double,
    currentServings: Double,
    isChecked: Boolean,
    onToggleCheck: () -> Unit,
    onSelectUnit: (String) -> Unit
) {
    var showUnitMenu by remember { mutableStateOf(false) }

    val formattedAmount = UnitConverter.scaleAndFormatAmount(
        amount = ingredient.amount,
        fromUnit = ingredient.unit,
        toUnit = ingredient.selectedUnit,
        baseServings = baseServings,
        currentServings = currentServings,
        ingredientName = ingredient.name,
        customGramsPerUnit = ingredient.gramsPerUnit
    )
    val displayUnit = UnitConverter.formatUnitForDisplay(ingredient.selectedUnit)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleCheck() },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.surfaceContainerLowest
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { onToggleCheck() }
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isChecked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                    )

                    if (formattedAmount.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        DottedLeaderLine(
                            modifier = Modifier.weight(1f),
                            color = if (isChecked) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                if (ingredient.note.isNotBlank()) {
                    Text(
                        text = ingredient.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (formattedAmount.isNotEmpty()) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isChecked) MaterialTheme.colorScheme.surfaceContainerHigh
                        else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showUnitMenu = true }
                    ) {
                        Text(
                            text = "$formattedAmount $displayUnit",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                            color = if (isChecked) MaterialTheme.colorScheme.outline
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    val availableUnits = remember(ingredient.unit, ingredient.name, ingredient.gramsPerUnit, AppLocaleManager.isEnglish) {
                        val supported = UnitConverter.getSupportedUnits()
                        supported.filter { targetUnit ->
                            val normTarget = UnitConverter.normalizeUnit(targetUnit)
                            UnitConverter.canConvert(
                                fromUnit = ingredient.unit,
                                toUnit = normTarget,
                                ingredientName = ingredient.name,
                                customGramsPerUnit = ingredient.gramsPerUnit
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        availableUnits.forEach { targetUnit ->
                            DropdownMenuItem(
                                text = { Text(targetUnit) },
                                onClick = {
                                    val normUnit = UnitConverter.normalizeUnit(targetUnit)
                                    onSelectUnit(normUnit)
                                    showUnitMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StepCardWithBadges(
    stepNumber: Int,
    instructionText: String,
    imageUrisJson: String = "[]",
    ingredients: List<IngredientEntity>,
    baseServings: Double,
    currentServings: Double,
    checkedIngredients: Set<Long> = emptySet(),
    timerState: StepTimerState? = null,
    isActiveStep: Boolean = false,
    onSelectStep: () -> Unit = {},
    onStartOrPauseTimer: (Int) -> Unit = {},
    onResetTimer: () -> Unit = {},
    onAddTimeToTimer: (Int) -> Unit = {},
    onToggleIngredientCheck: (Long) -> Unit = {},
    onSelectMedia: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val segments = remember(instructionText) { StepTokenParser.parseSegments(instructionText) }
    val parsedSeconds = remember(instructionText) { StepTimerParser.parseDurationInSeconds(instructionText) }
    val stepImages = remember(imageUrisJson) {
        try {
            kotlinx.serialization.json.Json.decodeFromString<List<String>>(imageUrisJson)
        } catch (_: Exception) {
            emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectStep() },
        shape = RoundedCornerShape(14.dp),
        border = if (isActiveStep) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isActiveStep) MaterialTheme.colorScheme.surfaceContainerHigh
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isActiveStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = if (isActiveStep) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                segments.forEach { segment ->
                    when (segment) {
                        is StepSegment.TextSegment -> {
                            Text(
                                text = segment.text,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                        }
                        is StepSegment.IngredientTokenSegment -> {
                            val tokenClean = segment.ingredientName.trim()

                            val exactGroupMatch = ingredients.find {
                                "${it.name} (${it.groupName})".trim().equals(tokenClean, ignoreCase = true)
                            }

                            val matchedIngs = if (exactGroupMatch != null) {
                                listOf(exactGroupMatch)
                            } else {
                                ingredients.filter {
                                    it.name.trim().equals(tokenClean, ignoreCase = true) ||
                                    "${it.name} (${it.groupName})".trim().equals(tokenClean, ignoreCase = true)
                                }
                            }

                            val isChecked = matchedIngs.isNotEmpty() && matchedIngs.all { checkedIngredients.contains(it.id) }

                            val badgeText = if (matchedIngs.isNotEmpty()) {
                                val firstIng = matchedIngs.first()
                                val checkPrefix = if (isChecked) "✓ " else ""
                                val displayUnit = UnitConverter.formatUnitForDisplay(firstIng.selectedUnit)
                                if (matchedIngs.size == 1) {
                                    val amtStr = UnitConverter.scaleAndFormatAmount(
                                        amount = firstIng.amount,
                                        fromUnit = firstIng.unit,
                                        toUnit = firstIng.selectedUnit,
                                        baseServings = baseServings,
                                        currentServings = currentServings,
                                        ingredientName = firstIng.name,
                                        customGramsPerUnit = firstIng.gramsPerUnit
                                    )
                                    val displayName = if (firstIng.groupName.isNotBlank() && exactGroupMatch != null) {
                                        "${firstIng.name} (${firstIng.groupName})"
                                    } else {
                                        firstIng.name
                                    }
                                    if (amtStr.isNotEmpty()) {
                                        "$checkPrefix$displayName ($amtStr $displayUnit)"
                                    } else {
                                        "$checkPrefix$displayName"
                                    }
                                } else {
                                    val amtStrList = matchedIngs.mapNotNull { ing ->
                                        val amt = UnitConverter.scaleAndFormatAmount(
                                            amount = ing.amount,
                                            fromUnit = ing.unit,
                                            toUnit = ing.selectedUnit,
                                            baseServings = baseServings,
                                            currentServings = currentServings,
                                            ingredientName = ing.name,
                                            customGramsPerUnit = ing.gramsPerUnit
                                        )
                                        val u = UnitConverter.formatUnitForDisplay(ing.selectedUnit)
                                        if (amt.isNotEmpty()) "$amt $u" else null
                                    }
                                    if (amtStrList.isNotEmpty()) {
                                        "$checkPrefix${firstIng.name} (${amtStrList.joinToString(" + ")})"
                                    } else {
                                        "$checkPrefix${firstIng.name}"
                                    }
                                }
                            } else {
                                segment.ingredientName
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isChecked) MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .clickable {
                                        matchedIngs.forEach { onToggleIngredientCheck(it.id) }
                                    }
                            ) {
                                Text(
                                    text = badgeText,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (isChecked) MaterialTheme.colorScheme.outline
                                    else MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // STEP TIMER UI
            if (parsedSeconds != null || timerState != null) {
                val durationToUse = timerState?.totalSeconds ?: parsedSeconds ?: 600
                Spacer(modifier = Modifier.height(10.dp))
                StepTimerCard(
                    secondsRemaining = timerState?.secondsRemaining ?: durationToUse,
                    isRunning = timerState?.isRunning == true,
                    isFinished = timerState?.isFinished == true,
                    onStartPause = { onStartOrPauseTimer(durationToUse) },
                    onReset = onResetTimer,
                    onAddSeconds = { addSecs -> onAddTimeToTimer(addSecs) }
                )
            }

            // STEP ILLUSTRATIONS GALLERY
            if (stepImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(stepImages, key = { it }) { uriStr ->
                        val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectMedia(uriStr) },
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = uriStr,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (isVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayArrow,
                                        contentDescription = AppLocaleManager.tr("Видео", "Video"),
                                        tint = Color.White
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

@Composable
fun PostCookDialog(
    currentStatus: String,
    currentRating: Int,
    currentPostNote: String,
    onDismiss: () -> Unit,
    onConfirm: (status: String, rating: Int, postCookNote: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var selectedRating by remember { mutableIntStateOf(currentRating) }
    var note by remember { mutableStateOf(currentPostNote) }

    val statusOptions = remember {
        listOf(
            RecipeEntity.STATUS_VERIFIED to AppLocaleManager.tr("Проверено (Отличный рецепт)", "Verified (Great recipe)"),
            RecipeEntity.STATUS_NEEDS_WORK to AppLocaleManager.tr("Работа над ошибками", "Needs work"),
            RecipeEntity.STATUS_ARCHIVED to AppLocaleManager.tr("Архив", "Archive")
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(decorFitsSystemWindows = false),
        title = { Text(AppLocaleManager.tr("🎉 Приготовление завершено!", "🎉 Cooking Completed!"), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(AppLocaleManager.tr("Выберите итоговый статус рецепта:", "Select final recipe status:"), fontWeight = FontWeight.SemiBold)

                statusOptions.forEach { (statusKey, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStatus = statusKey }
                    ) {
                        RadioButton(
                            selected = selectedStatus == statusKey,
                            onClick = { selectedStatus = statusKey }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(AppLocaleManager.tr("Оценка блюда:", "Rate dish:"), fontWeight = FontWeight.SemiBold)

                StarRatingBar(
                    rating = selectedRating,
                    onRatingChanged = { selectedRating = it },
                    starSize = 28
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AppLocaleManager.tr("Заметка по результату (что изменить на будущее)", "Post-cook note (what to improve next time)")) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedStatus, selectedRating, note) }) {
                Text(AppLocaleManager.tr("Сохранить результат", "Save result"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppLocaleManager.tr("Отмена", "Cancel"))
            }
        }
    )
}
