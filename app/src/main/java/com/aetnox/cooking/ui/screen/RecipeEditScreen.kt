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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
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
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var focusedStepIndex by remember { mutableIntStateOf(0) }
    var newTagInput by remember { mutableStateOf("") }
    var selectedFullscreenMediaUri by remember { mutableStateOf<String?>(null) }
    var showGeminiDialog by remember { mutableStateOf(false) }
    val isAiDisabled = remember { ExperimentalSettingsManager.getSettings(context).aiDisabled }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var newGroupInput by remember { mutableStateOf("") }

    var lastTrigger by remember { mutableStateOf(0L) }
    var showAiSuccessBanner by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(uiState.aiFillAnimationTrigger) {
        if (uiState.aiFillAnimationTrigger > 0L && uiState.aiFillAnimationTrigger != lastTrigger) {
            lastTrigger = uiState.aiFillAnimationTrigger
            showAiSuccessBanner = true
            kotlinx.coroutines.delay(4000)
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
                        if (uiState.recipeId > 0) "Редактирование" else "Новый рецепт",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (!isAiDisabled) {
                        IconButton(onClick = { showGeminiDialog = true }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Заполнить с помощью Шеф-Помощника", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = {
                        val clipText = clipboardManager.getText()?.text
                        if (!clipText.isNullOrBlank()) {
                            viewModel.importFromClipboardText(clipText)
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, contentDescription = "Импорт из буфера")
                    }
                    Button(
                        onClick = {
                            if (!uiState.isQuickDraft && uiState.title.isBlank()) {
                                Toast.makeText(context, "Введите название блюда!", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.saveRecipe(onSaved = onRecipeSaved)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Сохранить")
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
                    text = { Text("Визуальный редактор") }
                )
                Tab(
                    selected = uiState.isQuickDraft,
                    onClick = { viewModel.setQuickDraftMode(true) },
                    text = { Text("Быстрый черновик") }
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
                        text = "⚡ Быстрый черновик",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Сохраняйте сырые заметки, ссылки, фото и видео на бегу. Рецепт автоматически получит статус «В планах».",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Название блюда (опционально)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.rawNotes,
                        onValueChange = { viewModel.updateRawNotes(it) },
                        label = { Text("Свободные заметки / текст рецепта") },
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
                        Text("📷 Прикрепить фото / видео из галереи")
                    }

                    OutlinedTextField(
                        value = uiState.sourceUrl,
                        onValueChange = { viewModel.updateSourceUrl(it) },
                        label = { Text("Ссылка на источник (URL)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Attached Images & Videos Section
                    if (uiState.imageUris.isNotEmpty()) {
                        Column {
                            Text(
                                text = "Прикрепленные медиа (${uiState.imageUris.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.imageUris) { uriStr ->
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
                                                    contentDescription = "Видео",
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
                                                contentDescription = "Удалить",
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
                                        text = "✨ Поля оцифрованы Шеф-Помощником!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Сгруппировано ${uiState.ingredients.size} ингредиентов по секциям и добавлено ${uiState.steps.size} шагов.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                    // AI Assistant Card with Instructions & Quick Actions
                    if (!isAiDisabled) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🤖 Автозаполнение через ИИ (ChatGPT / Claude)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "1️⃣ Нажмите «Промпт для ИИ» ниже.\n2️⃣ Отправьте его нейросети вместе с текстом/фото.\n3️⃣ Скопируйте ответ ИИ и нажмите «Вставить ответ».\n\n💡 Совет: Если у рецепта есть составные части (тесто, крем, соус), напомните нейросети: «Раздели ингредиенты по секциям!»",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(AiPromptTemplates.AI_RECIPE_JSON_PROMPT))
                                            Toast.makeText(context, "🤖 Промпт для ИИ скопирован в буфер обмена!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("1. Промпт для ИИ", style = MaterialTheme.typography.labelMedium)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val clipText = clipboardManager.getText()?.text
                                            if (!clipText.isNullOrBlank()) {
                                                viewModel.importFromClipboardText(clipText)
                                                Toast.makeText(context, "✨ Данные из буфера загружены в редактор!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Буфер обмена пуст!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("2. Вставить ответ", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Title & Time
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Название блюда *") },
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
                            label = { Text("Подготовка (мин)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = uiState.cookTimeMinutesStr,
                            onValueChange = { viewModel.updateCookTime(it) },
                            label = { Text("Готовка (мин)") },
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
                                text = "⏱ Общее время: $totalMins мин (Подготовка $prepMins мин + Готовка $cookMins мин)",
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
                                    text = "Расчет порций",
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
                                        label = { Text("Базовое кол-во") },
                                        modifier = Modifier.weight(1f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = uiState.yieldUnit,
                                        onValueChange = { viewModel.updateYieldUnit(it) },
                                        label = { Text("Единица выхода") },
                                        placeholder = { Text("порций, штук") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }

                    // Tags
                    Column {
                        Text(
                            text = "Теги и категории",
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
                                            contentDescription = "Удалить",
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
                                text = "Похожие теги в базе:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(suggestedTags) { tag ->
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
                                placeholder = { Text("Поиск / новый тег...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                viewModel.addTag(newTagInput)
                                newTagInput = ""
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Добавить тег")
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
                                text = "Ингредиенты (${uiState.ingredients.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Row {
                                TextButton(onClick = { showAddGroupDialog = true }) {
                                    Icon(Icons.Default.Folder, contentDescription = null)
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("＋ Секция")
                                }
                                TextButton(onClick = { viewModel.addIngredient() }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Добавить")
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
                                                    contentDescription = "Удалить группу",
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
                                            text = if (groupName.isNotBlank()) "Добавить в «$groupName»" else "Добавить ингредиент",
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
                                text = "Шаги приготовления (${uiState.steps.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.addStep() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Добавить шаг")
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
                                        text = "Нажмите на ингредиент для вставки токена в шаг №${focusedStepIndex + 1}:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(validIngredients) { ing ->
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
                                            text = "Шаг ${stepItem.stepNumber}",
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
                                                    contentDescription = "Переместить вверх",
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
                                                    contentDescription = "Переместить вниз",
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
                                                Text("Фото", style = MaterialTheme.typography.labelSmall)
                                            }

                                            if (uiState.steps.size > 1) {
                                                IconButton(
                                                    onClick = { viewModel.removeStep(index) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Удалить шаг",
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
                                        placeholder = { Text("Опишите действия...") },
                                        modifier = Modifier.fillMaxWidth(),
                                        maxLines = 5
                                    )

                                    // STEP ATTACHED ILLUSTRATION THUMBNAILS
                                    if (stepItem.imageUris.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(stepItem.imageUris) { uriStr ->
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
                                                            Icon(Icons.Default.PlayArrow, contentDescription = "Видео", tint = Color.White, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.removeStepImageUri(index, uriStr) },
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), CircleShape)
                                                            .size(22.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Удалить", modifier = Modifier.size(12.dp))
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
                                        text = "📊 Пищевая ценность (КБЖУ на порцию)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (hasNutrition) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "🔥 ${uiState.caloriesStr.ifBlank { "0" }} ккал | 🥩 Б: ${uiState.proteinStr.ifBlank { "0" }}г | 🧈 Ж: ${uiState.fatStr.ifBlank { "0" }}г | 🌾 У: ${uiState.carbsStr.ifBlank { "0" }}г",
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
                                            label = { Text("Ккал") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = uiState.proteinStr,
                                            onValueChange = { viewModel.updateProtein(it) },
                                            label = { Text("Белки (г)") },
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
                                            label = { Text("Жиры (г)") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = uiState.carbsStr,
                                            onValueChange = { viewModel.updateCarbs(it) },
                                            label = { Text("Углеводы (г)") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )
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
                                text = "Параметры готовки («Шпаргалка шефа»)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = uiState.chefHeatingType,
                                    onValueChange = { viewModel.updateChefHeatingType(it) },
                                    label = { Text("Тип нагрева") },
                                    placeholder = { Text("Духовка / Плита") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.chefTemperature,
                                    onValueChange = { viewModel.updateChefTemperature(it) },
                                    label = { Text("Температура") },
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
                                    label = { Text("Тайминг") },
                                    placeholder = { Text("25 мин") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = uiState.chefReadinessSign,
                                    onValueChange = { viewModel.updateChefReadinessSign(it) },
                                    label = { Text("Признак готовности") },
                                    placeholder = { Text("Золотистая корочка") },
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
                                    text = "Прикрепленные медиа (${uiState.imageUris.size})",
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
                                    Text("Галерея")
                                }
                            }

                            if (uiState.imageUris.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(uiState.imageUris) { uriStr ->
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
                                                        contentDescription = "Видео",
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
                                                    contentDescription = "Удалить",
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
                        label = { Text("Заметки, кулинарные хитрости, бренды") },
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
            title = { Text("Добавить секцию ингредиентов") },
            text = {
                OutlinedTextField(
                    value = newGroupInput,
                    onValueChange = { newGroupInput = it },
                    label = { Text("Название секции (напр. Для теста, Для крема)") },
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
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGroupDialog = false }) {
                    Text("Отмена")
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
    val uiState by viewModel.uiState.collectAsState()
    var promptInput by remember(uiState.title) {
        mutableStateOf(if (uiState.title.isNotBlank()) "Заполни и сгруппируй ингредиенты и шаги для: ${uiState.title}" else "")
    }
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
                promptInput = "Распознай и отформатируй рецепт с прикрепленного фото кулинарной книги"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isGenerating) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("👨‍🍳 Шеф-Помощник редактора", fontWeight = FontWeight.Bold)
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
                    text = "Шеф-Помощник автоматически разбьет ингредиенты по секциям (для теста, для соуса и т.д.), рассчитает время и расставит таймеры в шагах.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Action Chips
                Column {
                    Text(
                        text = "💡 Быстрый сценарий:",
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
                                promptInput = "Сгруппируй все ингредиенты по кулинарным секциям и структурируй шаги"
                            },
                            label = { Text("🛒 Сгруппировать ингредиенты") },
                            enabled = !isGenerating
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                promptInput = "Рассчитай общее время приготовления, параметры нагрева и расставь таймеры в минутах к шагам"
                            },
                            label = { Text("⏱ Добавить таймеры") },
                            enabled = !isGenerating
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            label = { Text("📷 Фото кулинарной книги") },
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
                    placeholder = { Text("Введите название или текст рецепта...") },
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
                        errorMessage = "Введите название блюда или прикрепите фото!"
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
                            onDismiss()
                            Toast.makeText(context, "Поля рецепта структурированы Шеф-Помощником!", Toast.LENGTH_SHORT).show()
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
                Text("Заполнить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) {
                Text("Отмена")
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
    var showUnitMenu by remember { mutableStateOf(false) }
    var showNoteInput by remember { mutableStateOf(false) } // HIDDEN BY DEFAULT
    var showGroupInput by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    var dragTotalY by remember { mutableFloatStateOf(0f) }

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
                    placeholder = { Text("Ингредиент (название)") },
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
                        Text(item.unit, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        UnitConverter.SUPPORTED_UNITS.forEach { unitStr ->
                            DropdownMenuItem(
                                text = { Text(unitStr) },
                                onClick = {
                                    onUpdate(item.copy(unit = unitStr, selectedUnit = unitStr))
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
                        contentDescription = "Зажмите и потяните",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp)
                    )

                    // Spacious Amount Field
                    OutlinedTextField(
                        value = item.amountStr,
                        onValueChange = { onUpdate(item.copy(amountStr = it)) },
                        placeholder = { Text("Кол-во") },
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
                                text = if (item.groupName.isNotBlank()) item.groupName else "Секция",
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
                                text = if (item.note.isNotBlank()) "Заметка *" else "+Заметка",
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
                        contentDescription = "Удалить",
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
                    placeholder = { Text("Секция (напр. Для теста, Для крема)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            if (showNoteInput) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = item.note,
                    onValueChange = { onUpdate(item.copy(note = it)) },
                    placeholder = { Text("Примечание (напр. просеять, по вкусу)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
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

