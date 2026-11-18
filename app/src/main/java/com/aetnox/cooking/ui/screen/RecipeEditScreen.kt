package com.aetnox.cooking.ui.screen

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.AiPromptTemplates
import com.aetnox.cooking.ui.component.AiGenerationProgressBox
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.FullscreenMediaDialog
import com.aetnox.cooking.ui.util.MediaViewerUtils
import com.aetnox.cooking.ui.viewmodel.IngredientUiItem
import com.aetnox.cooking.ui.viewmodel.RecipeEditViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeEditScreen(
    viewModel: RecipeEditViewModel,
    onNavigateBack: () -> Unit,
    onRecipeSaved: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var focusedStepIndex by remember { mutableIntStateOf(0) }
    var newTagInput by remember { mutableStateOf("") }
    var selectedFullscreenMediaUri by remember { mutableStateOf<String?>(null) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    val isAiDisabled = remember { ExperimentalSettingsManager.getSettings(context).aiDisabled }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupInput by remember { mutableStateOf("") }

    var lastTrigger by remember { mutableLongStateOf(0L) }
    var showAiSuccessBanner by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.aiFillAnimationTrigger) {
        if (uiState.aiFillAnimationTrigger > 0L && uiState.aiFillAnimationTrigger != lastTrigger) {
            lastTrigger = uiState.aiFillAnimationTrigger
            showAiSuccessBanner = true
            delay(4000)
            showAiSuccessBanner = false
        }
    }

    // Native Modern Android Photo & Video Gallery Picker for Quick Draft
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val persistentPath = MediaViewerUtils.saveUriToInternalStorage(context, uri)
            viewModel.addImageUri(persistentPath)
        }
    }

    // Media Picker for Step Illustrations
    var targetStepIndexForPicker by remember { mutableIntStateOf(-1) }
    val stepMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        if (targetStepIndexForPicker >= 0) {
            uris.forEach { uri ->
                val persistentPath = MediaViewerUtils.saveUriToInternalStorage(context, uri)
                viewModel.addStepImageUri(targetStepIndexForPicker, persistentPath)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.recipeId > 0) AppLocaleManager.tr("Редактирование", "Edit Recipe") else AppLocaleManager.tr("Новый рецепт", "New Recipe"),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = AppLocaleManager.tr("Назад", "Back"))
                    }
                },
                actions = {
                    if (!isAiDisabled) {
                        IconButton(onClick = { showGeminiDialog = true }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = AppLocaleManager.tr("Заполнить с помощью Шеф-Помощника", "Fill with Chef Assistant"), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Button(
                        onClick = {
                            if (!uiState.isQuickDraft && uiState.title.isBlank()) {
                                Toast.makeText(context, AppLocaleManager.tr("Введите название блюда!", "Enter recipe title!"), Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveRecipe(onSaved = onRecipeSaved)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppLocaleManager.tr("Сохранить", "Save"))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Mode Selector Tabs
            PrimaryTabRow(selectedTabIndex = if (uiState.isQuickDraft) 1 else 0) {
                Tab(
                    selected = !uiState.isQuickDraft,
                    onClick = { viewModel.setQuickDraftMode(false) },
                    text = { Text(AppLocaleManager.tr("Визуальный редактор", "Visual Editor")) }
                )
                Tab(
                    selected = uiState.isQuickDraft,
                    onClick = { viewModel.setQuickDraftMode(true) },
                    text = { Text(AppLocaleManager.tr("Быстрый черновик", "Quick Draft")) }
                )
            }

            if (uiState.isQuickDraft) {
                // Quick Draft UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⚡ ${AppLocaleManager.tr("Быстрый черновик", "Quick Draft")}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = AppLocaleManager.tr("Сохраняйте сырые заметки, ссылки, фото и видео на бегу. Рецепт автоматически получит статус «В планах».", "Save raw notes, links, photos and videos on the go. The recipe will automatically be set to 'Planned' status."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text(AppLocaleManager.tr("Название блюда (опционально)", "Recipe title (optional)")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.rawNotes,
                        onValueChange = { viewModel.updateRawNotes(it) },
                        label = { Text(AppLocaleManager.tr("Свободные заметки / текст рецепта", "Free notes / recipe text")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        maxLines = 10
                    )

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(AppLocaleManager.tr("📷 Прикрепить фото / видео из галереи", "📷 Attach photo / video from gallery"))
                    }

                    OutlinedTextField(
                        value = uiState.sourceUrl,
                        onValueChange = { viewModel.updateSourceUrl(it) },
                        label = { Text(AppLocaleManager.tr("Ссылка на источник (URL)", "Source URL")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Attached Images & Videos Section
                    if (uiState.imageUris.isNotEmpty()) {
                        Column {
                            Text(
                                text = "${AppLocaleManager.tr("Прикрепленные медиа", "Attached media")} (${uiState.imageUris.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.imageUris, key = { it }) { uriStr ->
                                    val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
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
                                        IconButton(
                                            onClick = { viewModel.removeImageUri(uriStr) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    CircleShape
                                                )
                                                .size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = AppLocaleManager.tr("Удалить", "Delete"),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Full Visual Editor
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI Success Sparkling Banner
                    AnimatedVisibility(
                        visible = showAiSuccessBanner,
                        enter = slideInVertically { -it } + fadeIn(),
                        exit = slideOutVertically { -it } + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "✨ ${AppLocaleManager.tr("Поля оцифрованы Шеф-Помощником!", "Fields digitized by Chef Assistant!")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = AppLocaleManager.tr("Сгруппировано ${uiState.ingredients.size} ингредиентов по секциям и добавлено ${uiState.steps.size} шагов.", "Grouped ${uiState.ingredients.size} ingredients into sections and added ${uiState.steps.size} steps."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                    // Title & Time
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text(AppLocaleManager.tr("Название блюда *", "Recipe title *")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.prepTimeMinutesStr,
                            onValueChange = { viewModel.updatePrepTime(it) },
                            label = { Text(AppLocaleManager.tr("Подготовка (мин)", "Prep (min)")) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.cookTimeMinutesStr,
                            onValueChange = { viewModel.updateCookTime(it) },
                            label = { Text(AppLocaleManager.tr("Готовка (мин)", "Cook (min)")) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    val prepMins = uiState.prepTimeMinutesStr.toIntOrNull() ?: 0
                    val cookMins = uiState.cookTimeMinutesStr.toIntOrNull() ?: 0
                    val totalMins = prepMins + cookMins
                    if (totalMins > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "⏱ ${AppLocaleManager.tr("Общее время", "Total time")}: $totalMins ${AppLocaleManager.tr("мин", "min")} (${AppLocaleManager.tr("Подготовка", "Prep")} $prepMins ${AppLocaleManager.tr("мин", "min")} + ${AppLocaleManager.tr("Готовка", "Cook")} $cookMins ${AppLocaleManager.tr("мин", "min")})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Portions
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
                                Text(
                                    text = AppLocaleManager.tr("Расчет порций", "Servings calculation"),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Switch(
                                    checked = uiState.servingsEnabled,
                                    onCheckedChange = { viewModel.updateServingsEnabled(it) }
                                )
                            }

                            if (uiState.servingsEnabled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = uiState.baseServingsStr,
                                        onValueChange = { viewModel.updateBaseServings(it) },
                                        label = { Text(AppLocaleManager.tr("Базовое кол-во", "Base count")) },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = uiState.yieldUnit,
                                        onValueChange = { viewModel.updateYieldUnit(it) },
                                        label = { Text(AppLocaleManager.tr("Единица выхода", "Yield unit")) },
                                        placeholder = { Text(AppLocaleManager.tr("порций, штук", "servings, pieces")) },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Favorite Toggle Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateFavorite(!uiState.isFavorite) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isFavorite) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (uiState.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = AppLocaleManager.tr("Избранный рецепт", "Favorite recipe"),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = if (uiState.isFavorite) AppLocaleManager.tr("Помещен в ваш топ избранных блюд", "Added to your favorite recipes") else AppLocaleManager.tr("Добавить в подборку любимых рецептов", "Add to favorite recipes collection"),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isFavorite,
                                onCheckedChange = { viewModel.updateFavorite(it) }
                            )
                        }
                    }

                    // Tags
                    Column {
                        Text(
                            text = AppLocaleManager.tr("Теги и категории", "Tags & Categories"),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.tags.forEach { tag ->
                                AssistChip(
                                    onClick = { viewModel.removeTag(tag) },
                                    label = { Text("#$tag") },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = AppLocaleManager.tr("Удалить", "Remove"),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }

                        // SUGGESTIONS OF EXISTING TAGS (FILTERED BY newTagInput)
                        val suggestedTags = remember(uiState.availableTags, uiState.tags, newTagInput) {
                            uiState.availableTags.filter { dbTag ->
                                !uiState.tags.any { it.equals(dbTag, ignoreCase = true) } &&
                                (newTagInput.isBlank() || dbTag.contains(newTagInput, ignoreCase = true))
                            }
                        }

                        if (suggestedTags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = AppLocaleManager.tr("Похожие теги в базе:", "Matching tags in database:"),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(suggestedTags, key = { it }) { tag ->
                                    AssistChip(
                                        onClick = {
                                            viewModel.addTag(tag)
                                            newTagInput = ""
                                        },
                                        label = { Text("+$tag") }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = newTagInput,
                                onValueChange = { newTagInput = it },
                                placeholder = { Text(AppLocaleManager.tr("Поиск / новый тег...", "Search / new tag...")) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                viewModel.addTag(newTagInput)
                                newTagInput = ""
                            }) {
                                Icon(Icons.Default.Add, contentDescription = AppLocaleManager.tr("Добавить тег", "Add tag"))
                            }
                        }
                    }

                    // GROUPED INGREDIENTS BLOCKS
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${AppLocaleManager.tr("Ингредиенты", "Ingredients")} (${uiState.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                TextButton(onClick = { showAddGroupDialog = true }) {
                                    Icon(Icons.Default.Folder, contentDescription = null)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(AppLocaleManager.tr("＋ Секция", "＋ Section"))
                                }
                                TextButton(onClick = { viewModel.addIngredient() }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text(AppLocaleManager.tr("Добавить", "Add"))
                                }
                            }
                        }

                        val groupedMap = remember(uiState.ingredients) {
                            uiState.ingredients.groupBy { it.groupName.trim() }
                        }

                        groupedMap.forEach { (groupName, list) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (groupName.isNotBlank()) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📌 $groupName",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { viewModel.removeIngredientGroup(groupName) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = AppLocaleManager.tr("Удалить группу", "Remove section"),
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }

                                    list.forEach { ingItem ->
                                        val globalIndex = uiState.ingredients.indexOf(ingItem)
                                        IngredientRowEdit(
                                            item = ingItem,
                                            isFirst = globalIndex == 0,
                                            isLast = globalIndex == uiState.ingredients.size - 1,
                                            onUpdate = { updated ->
                                                viewModel.updateIngredient(globalIndex, updated)
                                            },
                                            onDelete = {
                                                viewModel.removeIngredient(globalIndex)
                                            },
                                            onMoveUp = { viewModel.moveIngredientUp(globalIndex) },
                                            onMoveDown = { viewModel.moveIngredientDown(globalIndex) }
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }

                                    TextButton(
                                        onClick = { viewModel.addIngredient(groupName = groupName) },
                                        modifier = Modifier.align(Alignment.Start)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (groupName.isNotBlank()) AppLocaleManager.tr("Добавить в «$groupName»", "Add to \"$groupName\"") else AppLocaleManager.tr("Добавить ингредиент", "Add ingredient"),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // STEP INPUT WITH STEP MEDIA ILLUSTRATIONS, CONTEXT BAR AND CURSOR TRACKING
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${AppLocaleManager.tr("Шаги приготовления", "Cooking steps")} (${uiState.steps.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.addStep() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text(AppLocaleManager.tr("Добавить шаг", "Add step"))
                            }
                        }

                        // CONTEXT BAR WITH GROUP-QUALIFIED INGREDIENT CHIPS FOR TOKEN INSERTION
                        val validIngredients = uiState.ingredients.filter { it.name.isNotBlank() }
                        if (validIngredients.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = AppLocaleManager.tr("Нажмите на продукт, чтобы добавить его в описание шага №${focusedStepIndex + 1}:", "Tap product to insert it into step #${focusedStepIndex + 1} text:"),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(validIngredients, key = { it.name + "_" + it.groupName }) { ing ->
                                            val displayName = if (ing.groupName.isNotBlank()) "${ing.name} (${ing.groupName})" else ing.name
                                            AssistChip(
                                                onClick = {
                                                    viewModel.insertIngredientTokenIntoStep(
                                                        stepIndex = focusedStepIndex,
                                                        ingredientName = displayName
                                                    )
                                                },
                                                label = { Text("+$displayName") }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        uiState.steps.forEachIndexed { index, stepItem ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${AppLocaleManager.tr("Шаг", "Step")} ${stepItem.stepNumber}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = { viewModel.moveStepUp(index) },
                                                enabled = index > 0,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowUpward,
                                                    contentDescription = AppLocaleManager.tr("Переместить вверх", "Move up"),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.moveStepDown(index) },
                                                enabled = index < uiState.steps.size - 1,
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.ArrowDownward,
                                                    contentDescription = AppLocaleManager.tr("Переместить вниз", "Move down"),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            TextButton(onClick = {
                                                targetStepIndexForPicker = index
                                                stepMediaPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                                )
                                            }) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(AppLocaleManager.tr("Фото", "Photo"), style = MaterialTheme.typography.labelSmall)
                                            }

                                            if (uiState.steps.size > 1) {
                                                IconButton(
                                                    onClick = { viewModel.removeStep(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = AppLocaleManager.tr("Удалить шаг", "Delete step"),
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    var stepTextFieldValue by remember(stepItem.instructionText) {
                                        mutableStateOf(
                                            TextFieldValue(
                                                text = stepItem.instructionText,
                                                selection = TextRange(stepItem.cursorIndex.coerceIn(0, stepItem.instructionText.length))
                                            )
                                        )
                                    }

                                    OutlinedTextField(
                                        value = stepTextFieldValue,
                                        onValueChange = { newValue ->
                                            stepTextFieldValue = newValue
                                            focusedStepIndex = index
                                            viewModel.updateStep(index, newValue.text, newValue.selection.start)
                                        },
                                        placeholder = { Text(AppLocaleManager.tr("Опишите действия...", "Describe instructions...")) },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 5
                                    )

                                    // STEP ATTACHED ILLUSTRATION THUMBNAILS
                                    if (stepItem.imageUris.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(stepItem.imageUris, key = { it }) { uriStr ->
                                                val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                                                Box(
                                                    modifier = Modifier
                                                        .size(70.dp)
                                                        .clip(RoundedCornerShape(6.dp))
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
                                                                .size(24.dp)
                                                                .background(Color.Black.copy(alpha = 0.6f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Default.PlayArrow, contentDescription = AppLocaleManager.tr("Видео", "Video"), tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.removeStepImageUri(index, uriStr) },
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                                            .size(22.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = AppLocaleManager.tr("Удалить", "Delete"), modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // COLLAPSIBLE NUTRITION BLOCK (КБЖУ)
                    val hasNutrition = uiState.caloriesStr.isNotBlank() || uiState.proteinStr.isNotBlank() || uiState.fatStr.isNotBlank() || uiState.carbsStr.isNotBlank()

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleNutritionBlock() },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "📊 ${AppLocaleManager.tr("Пищевая ценность (КБЖУ на порцию)", "Nutritional Value (per serving)")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hasNutrition) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val calWord = AppLocaleManager.tr("ккал", "kcal")
                                        val pWord = AppLocaleManager.tr("Б", "P")
                                        val fWord = AppLocaleManager.tr("Ж", "F")
                                        val cWord = AppLocaleManager.tr("У", "C")
                                        val gWord = AppLocaleManager.tr("г", "g")
                                        Text(
                                            text = "🔥 ${uiState.caloriesStr.ifBlank { "0" }} $calWord | 🥩 $pWord: ${uiState.proteinStr.ifBlank { "0" }}$gWord | 🧈 $fWord: ${uiState.fatStr.ifBlank { "0" }}$gWord | 🌾 $cWord: ${uiState.carbsStr.ifBlank { "0" }}$gWord",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Icon(
                                    if (uiState.showNutrition) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = uiState.showNutrition) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = uiState.caloriesStr,
                                            onValueChange = { viewModel.updateCalories(it) },
                                            label = { Text(AppLocaleManager.tr("Ккал", "Kcal")) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = uiState.proteinStr,
                                            onValueChange = { viewModel.updateProtein(it) },
                                            label = { Text(AppLocaleManager.tr("Белки (г)", "Protein (g)")) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = uiState.fatStr,
                                            onValueChange = { viewModel.updateFat(it) },
                                            label = { Text(AppLocaleManager.tr("Жиры (г)", "Fat (g)")) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = uiState.carbsStr,
                                            onValueChange = { viewModel.updateCarbs(it) },
                                            label = { Text(AppLocaleManager.tr("Углеводы (г)", "Carbs (g)")) },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                    }

                                    if (!isAiDisabled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                val ingNames = uiState.ingredients.mapNotNull { it.name.ifBlank { null } }
                                                if (ingNames.isEmpty()) {
                                                    Toast.makeText(context, AppLocaleManager.tr("Сначала добавьте названия ингредиентов!", "Add ingredient names first!"), Toast.LENGTH_SHORT).show()
                                                } else {
                                                    showGeminiDialog = true
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(AppLocaleManager.tr("✨ Рассчитать КБЖУ через Шеф-Помощника", "✨ Calculate macros with Chef Assistant"), style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CHEF'S CHEAT SHEET FIELDS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = AppLocaleManager.tr("Параметры готовки («Шпаргалка шефа»)", "Cooking Parameters (\"Chef's Cheat Sheet\")"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.chefHeatingType,
                                    onValueChange = { viewModel.updateChefHeatingType(it) },
                                    label = { Text(AppLocaleManager.tr("Тип нагрева", "Heating type")) },
                                    placeholder = { Text(AppLocaleManager.tr("Духовка / Плита", "Oven / Stove")) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.chefTemperature,
                                    onValueChange = { viewModel.updateChefTemperature(it) },
                                    label = { Text(AppLocaleManager.tr("Температура", "Temperature")) },
                                    placeholder = { Text("180 °C") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.chefTiming,
                                    onValueChange = { viewModel.updateChefTiming(it) },
                                    label = { Text(AppLocaleManager.tr("Тайминг", "Timing")) },
                                    placeholder = { Text(AppLocaleManager.tr("25 мин", "25 min")) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.chefReadinessSign,
                                    onValueChange = { viewModel.updateChefReadinessSign(it) },
                                    label = { Text(AppLocaleManager.tr("Признак готовности", "Readiness sign")) },
                                    placeholder = { Text(AppLocaleManager.tr("Золотистая корочка", "Golden brown crust")) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    // ATTACHED MEDIA SECTION IN VISUAL EDITOR
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
                                Text(
                                    text = "${AppLocaleManager.tr("Прикрепленные медиа", "Attached media")} (${uiState.imageUris.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedButton(onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                    )
                                }) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppLocaleManager.tr("Галерея", "Gallery"))
                                }
                            }

                            if (uiState.imageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(uiState.imageUris, key = { it }) { uriStr ->
                                        val isVideo = MediaViewerUtils.isVideoUri(uriStr, context)
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
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
                                            IconButton(
                                                onClick = { viewModel.removeImageUri(uriStr) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .background(
                                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                        CircleShape
                                                    )
                                                    .size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = AppLocaleManager.tr("Удалить", "Delete"),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // NOTES
                    OutlinedTextField(
                        value = uiState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text(AppLocaleManager.tr("Заметки, кулинарные хитрости, бренды", "Notes, culinary tips, brands")) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 5
                    )
                }
            }
        }
    }

    // Add Ingredient Section / Group Dialog
    if (showAddGroupDialog) {
        AlertDialog(
            onDismissRequest = { showAddGroupDialog = false },
            title = { Text(AppLocaleManager.tr("Добавить секцию ингредиентов", "Add ingredient section")) },
            text = {
                OutlinedTextField(
                    value = newGroupInput,
                    onValueChange = { newGroupInput = it },
                    label = { Text(AppLocaleManager.tr("Название секции (напр. Для теста, Для крема)", "Section name (e.g. For dough, For cream)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val group = newGroupInput.trim()
                        if (group.isNotBlank()) {
                            viewModel.addIngredient(groupName = group)
                            newGroupInput = ""
                        }
                        showAddGroupDialog = false
                    }
                ) {
                    Text(AppLocaleManager.tr("Добавить", "Add"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text(AppLocaleManager.tr("Отмена", "Cancel"))
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

    if (showGeminiDialog && !isAiDisabled) {
        ChefEditorDialog(
            viewModel = viewModel,
            onDismiss = { showGeminiDialog = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChefEditorDialog(
    viewModel: RecipeEditViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    val defaultPrompt = remember(uiState.title) {
        if (uiState.title.isNotBlank()) {
            AppLocaleManager.tr("Заполни и сгруппируй ингредиенты и шаги для: ${uiState.title}", "Fill and group ingredients and steps for: ${uiState.title}")
        } else ""
    }
    var promptInput by remember(defaultPrompt) { mutableStateOf(defaultPrompt) }
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
                promptInput = AppLocaleManager.tr("Распознай и отформатируй рецепт с прикрепленного фото кулинарной книги", "Recognize and format recipe from attached cookbook photo")
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("👨‍🍳 ${AppLocaleManager.tr("Шеф-Помощник редактора", "Editor Chef Assistant")}", fontWeight = FontWeight.Bold)
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
                if (isGenerating) {
                    AiGenerationProgressBox(
                        isCompleted = isCompleted
                    )
                } else {
                    Text(
                        text = AppLocaleManager.tr("Шеф-Помощник автоматически разбьет ингредиенты по секциям (для теста, для соуса и т.д.), рассчитает время и расставит таймеры в шагах.", "Chef Assistant will automatically group ingredients into sections (for dough, for sauce, etc.), calculate time and set timers in steps."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Action Chips
                    Column {
                        Text(
                            text = "💡 ${AppLocaleManager.tr("Быстрый сценарий:", "Quick scenario:")}",
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
                                    promptInput = AppLocaleManager.tr("Сгруппируй все ингредиенты по кулинарным секциям и структурируй шаги", "Group all ingredients by culinary sections and structure steps")
                                },
                                label = { Text(AppLocaleManager.tr("🛒 Сгруппировать ингредиенты", "🛒 Group ingredients")) },
                                enabled = !isGenerating
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    promptInput = AppLocaleManager.tr("Рассчитай общее время приготовления, параметры нагрева и расставь таймеры в минутах к шагам", "Calculate total cooking time, heating params and set timers in steps")
                                },
                                label = { Text(AppLocaleManager.tr("⏱ Добавить таймеры", "⏱ Add timers")) },
                                enabled = !isGenerating
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                label = { Text(AppLocaleManager.tr("📷 Фото кулинарной книги", "📷 Cookbook photo")) },
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
                        placeholder = { Text(AppLocaleManager.tr("Введите название или текст рецепта...", "Enter recipe title or text...")) },
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
                            Text(if (selectedImageUri != null) AppLocaleManager.tr("Изменить фото", "Change photo") else AppLocaleManager.tr("Прикрепить фото", "Attach photo"))
                        }

                        if (selectedImageUri != null) {
                            TextButton(
                                onClick = { selectedImageUri = null },
                                enabled = !isGenerating
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(AppLocaleManager.tr("Удалить", "Remove"), style = MaterialTheme.typography.labelSmall)
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
                                contentDescription = AppLocaleManager.tr("Прикрепленный снимок рецепта", "Attached recipe image"),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
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
                        errorMessage = AppLocaleManager.tr("Введите название блюда или прикрепите фото!", "Enter recipe title or attach photo!")
                        return@Button
                    }
                    isGenerating = true
                    isConnected = false
                    isCompleted = false
                    errorMessage = null
                    viewModel.generateWithGemini(
                        context = context,
                        prompt = promptInput,
                        imageUriStr = selectedImageUri,
                        onConnected = {
                            isConnected = true
                        },
                        onSuccess = {
                            isCompleted = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(750)
                                onDismiss()
                                Toast.makeText(context, AppLocaleManager.tr("Поля рецепта структурированы Шеф-Помощником!", "Recipe fields structured by Chef Assistant!"), Toast.LENGTH_SHORT).show()
                            }
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
                Text(AppLocaleManager.tr("Заполнить", "Fill in"))
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
fun IngredientRowEdit(
    item: IngredientUiItem,
    isFirst: Boolean,
    isLast: Boolean,
    onUpdate: (IngredientUiItem) -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val context = LocalContext.current
    var showUnitMenu by remember { mutableStateOf(false) }
    var showNoteInput by remember { mutableStateOf(false) } // HIDDEN BY DEFAULT
    var showGroupInput by remember { mutableStateOf(false) }

    val isDensityEnabled = remember { ExperimentalSettingsManager.getSettings(context).densityHintsEnabled }
    val densityInfo = remember(item.name, isDensityEnabled) {
        if (isDensityEnabled && item.name.isNotBlank()) UnitConverter.findDensity(item.name) else null
    }

    val haptic = LocalHapticFeedback.current
    var dragTotalY by remember { mutableFloatStateOf(0f) }

    val currentUnits = remember(AppLocaleManager.isEnglish) {
        UnitConverter.getSupportedUnits()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(isFirst, isLast) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        dragTotalY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragTotalY += dragAmount.y
                        val threshold = 120f
                        if (dragTotalY > threshold && !isLast) {
                            onMoveDown()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            dragTotalY = 0f
                        } else if (dragTotalY < -threshold && !isFirst) {
                            onMoveUp()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            dragTotalY = 0f
                        }
                    },
                    onDragEnd = { dragTotalY = 0f },
                    onDragCancel = { dragTotalY = 0f }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Line 1: Extra Wide Name Field (75% width) + Unit Selector (25% width)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = item.name,
                    onValueChange = { onUpdate(item.copy(name = it)) },
                    placeholder = { Text(AppLocaleManager.tr("Ингредиент (название)", "Ingredient (name)")) },
                    modifier = Modifier.weight(3f),
                    singleLine = true
                )

                // Unit Selector Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showUnitMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Text(UnitConverter.formatUnitForDisplay(item.unit), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        currentUnits.forEach { unitStr ->
                            DropdownMenuItem(
                                text = { Text(unitStr) },
                                onClick = {
                                    val normUnit = UnitConverter.normalizeUnit(unitStr)
                                    onUpdate(item.copy(unit = normUnit, selectedUnit = normUnit))
                                    showUnitMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Line 2: Spacious Amount Input Field + Drag Handle + Group/Note Badges + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Drag handle
                    Icon(
                        Icons.Default.Reorder,
                        contentDescription = AppLocaleManager.tr("Зажмите и потяните", "Hold and drag"),
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp)
                    )

                    // Spacious Amount Field
                    OutlinedTextField(
                        value = item.amountStr,
                        onValueChange = { onUpdate(item.copy(amountStr = it)) },
                        placeholder = { Text(AppLocaleManager.tr("Кол-во", "Qty")) },
                        modifier = Modifier.width(110.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    // Group Badge / Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.groupName.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.clickable { showGroupInput = !showGroupInput }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (item.groupName.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.groupName.ifBlank { AppLocaleManager.tr("＋ Секция", "＋ Section") },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.groupName.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    // Note Badge / Button
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (item.note.isNotBlank()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.clickable { showNoteInput = !showNoteInput }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (item.note.isNotBlank()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.note.isNotBlank()) AppLocaleManager.tr("Заметка *", "Note *") else AppLocaleManager.tr("＋ Заметка", "＋ Note"),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (item.note.isNotBlank()) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = AppLocaleManager.tr("Удалить", "Delete"),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (showGroupInput) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = item.groupName,
                    onValueChange = { onUpdate(item.copy(groupName = it)) },
                    placeholder = { Text(AppLocaleManager.tr("Секция (например: Для теста, Для крема, Для соуса)", "Section (e.g.: For dough, For cream, For sauce)")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (showNoteInput) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = item.note,
                    onValueChange = { onUpdate(item.copy(note = it)) },
                    placeholder = { Text(AppLocaleManager.tr("Примечание (например: просеять, комнатной температуры)", "Note (e.g.: sifted, room temperature)")) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (densityInfo != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    val tbspWord = AppLocaleManager.tr("ст. л.", "tbsp")
                    val tspWord = AppLocaleManager.tr("ч. л.", "tsp")
                    val cupWord = AppLocaleManager.tr("стакан", "cup")
                    val gWord = AppLocaleManager.tr("г", "g")
                    Text(
                        text = "💡 1 $tbspWord = ${densityInfo.gPerTbsp.toInt()}$gWord | 1 $tspWord = ${densityInfo.gPerTsp.toInt()}$gWord | 1 $cupWord = ${densityInfo.gPerCup.toInt()}$gWord",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AiFillHighlightCard(
    isTriggered: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isGlowing by remember { mutableStateOf(false) }

    LaunchedEffect(isTriggered) {
        if (isTriggered) {
            isGlowing = true
            delay(2800)
            isGlowing = false
        }
    }

    val glowAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isGlowing) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .then(
                if (glowAlpha > 0.01f) {
                    Modifier.border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha),
                                MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
                } else Modifier
            )
    ) {
        content()
    }
}
