package com.aetnox.cooking.ui.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.domain.ai.GeminiSettingsManager
import com.aetnox.cooking.domain.net.AssistantThinkingMode
import com.aetnox.cooking.domain.net.DnsMode
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.AiPromptTemplates
import com.aetnox.cooking.ui.util.OnboardingManager
import com.aetnox.cooking.ui.viewmodel.BackupViewModel
import com.aetnox.cooking.ui.theme.AppTheme
import com.aetnox.cooking.ui.theme.ThemeManager
import com.aetnox.cooking.ui.theme.themePreviewSwatches
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonStr = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                }
                if (!jsonStr.isNullOrBlank()) {
                    viewModel.importFullBackup(jsonStr) {}
                }
            } catch (e: Exception) {
                // handle error in viewmodel
            }
        }
    }

    val importSingleRecipeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val jsonStr = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                    reader.readText()
                }
                if (!jsonStr.isNullOrBlank()) {
                    viewModel.importSingleRecipe(jsonStr) {}
                }
            } catch (e: Exception) {
                // handle error in viewmodel
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки приложения", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        val clipboardManager = LocalClipboardManager.current
        val currentTheme = ThemeManager.currentTheme.value
        val currentGeminiSettings = remember { GeminiSettingsManager.getSettings(context) }

        var selectedSettingsTab by remember { mutableIntStateOf(0) }
        var apiKeyInput by remember { mutableStateOf(currentGeminiSettings.apiKey) }
        var selectedModel by remember { mutableStateOf(currentGeminiSettings.model) }
        var customBaseUrlInput by remember { mutableStateOf(currentGeminiSettings.customBaseUrl) }
        var isApiKeyVisible by remember { mutableStateOf(false) }

        val currentNetSettings = remember { ExperimentalSettingsManager.getSettings(context) }
        var selectedDnsMode by remember { mutableStateOf(currentNetSettings.dnsMode) }
        var customDnsUrlInput by remember { mutableStateOf(currentNetSettings.customDnsUrl) }
        var cookAssistantEnabled by remember { mutableStateOf(currentNetSettings.cookAssistantEnabled) }
        var selectedThinkingMode by remember { mutableStateOf(currentNetSettings.assistantThinkingMode) }
        var aiDisabled by remember { mutableStateOf(currentNetSettings.aiDisabled) }

        val scope = rememberCoroutineScope()
        var settingsSaveJob by remember { mutableStateOf<Job?>(null) }

        // Сохраняем настройки не на каждый введённый символ, а через паузу ввода
        fun persistGeminiSettingsDebounced() {
            settingsSaveJob?.cancel()
            settingsSaveJob = scope.launch {
                delay(600)
                GeminiSettingsManager.saveSettings(
                    context = context,
                    apiKey = apiKeyInput,
                    model = selectedModel,
                    customBaseUrl = customBaseUrlInput
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedSettingsTab) {
                Tab(
                    selected = selectedSettingsTab == 0,
                    onClick = { selectedSettingsTab = 0 },
                    text = { Text("🤖 ИИ", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 1,
                    onClick = { selectedSettingsTab = 1 },
                    text = { Text("💾 Память", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 2,
                    onClick = { selectedSettingsTab = 2 },
                    text = { Text("🎨 Тема", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 3,
                    onClick = { selectedSettingsTab = 3 },
                    text = { Text("🧪 Эксперименты", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.message?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                uiState.error?.let { err ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = err,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                when (selectedSettingsTab) {
                    0 -> {
                        // TAB 0: AI & PROVIDER SETTINGS
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (aiDisabled) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "⚠️ ИИ полностью отключен в экспериментальных настройках. Чтобы вернуть работу ИИ-функций, отключите эту опцию во вкладке «Эксперименты».",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🤖 Настройки ИИ и Провайдера",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "Настройте подключения к Google AI Studio или вашему собственному прокси-провайдеру.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/app/apikey"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🌐 Получить бесплатный API-ключ (1 мин)")
                                }

                                // Custom Base URL Provider Field
                                Column {
                                    Text(
                                        text = "Ссылка провайдера (Base URL API):",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = customBaseUrlInput,
                                        onValueChange = {
                                            customBaseUrlInput = it
                                            persistGeminiSettingsDebounced()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("https://generativelanguage.googleapis.com") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                customBaseUrlInput = GeminiSettingsManager.DEFAULT_BASE_URL
                                                GeminiSettingsManager.saveSettings(
                                                    context = context,
                                                    apiKey = apiKeyInput,
                                                    model = selectedModel,
                                                    customBaseUrl = customBaseUrlInput
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Google AI Studio", style = MaterialTheme.typography.labelSmall)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                customBaseUrlInput = "https://gemini.chat-api.net"
                                                GeminiSettingsManager.saveSettings(
                                                    context = context,
                                                    apiKey = apiKeyInput,
                                                    model = selectedModel,
                                                    customBaseUrl = customBaseUrlInput
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Прокси без VPN", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                // API Key Field
                                OutlinedTextField(
                                    value = apiKeyInput,
                                    onValueChange = {
                                        apiKeyInput = it
                                        persistGeminiSettingsDebounced()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("API Key ИИ") },
                                    placeholder = { Text("Вставьте ключ AQ...") },
                                    singleLine = true,
                                    visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                            Icon(
                                                imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isApiKeyVisible) "Скрыть ключ" else "Показать ключ"
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )



                                // Model Selection & Auto-Fetching
                                Text(
                                    text = "Выбор нейросети / модели:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                // Auto-fetch Models Button
                                Button(
                                    onClick = {
                                        GeminiSettingsManager.saveSettings(
                                            context = context,
                                            apiKey = apiKeyInput,
                                            model = selectedModel,
                                            customBaseUrl = customBaseUrlInput
                                        )
                                        viewModel.fetchModels(context)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isFetchingModels,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isFetchingModels) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Загрузка моделей с сервера...")
                                    } else {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("🔄 Загрузить доступные модели с сервера")
                                    }
                                }

                                uiState.fetchedModelsMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                val displayModels = uiState.fetchedModels ?: GeminiSettingsManager.AVAILABLE_MODELS
                                displayModels.forEach { modelOption ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedModel == modelOption.id,
                                            onClick = {
                                                selectedModel = modelOption.id
                                                GeminiSettingsManager.saveSettings(
                                                    context = context,
                                                    apiKey = apiKeyInput,
                                                    model = selectedModel,
                                                    customBaseUrl = customBaseUrlInput
                                                )
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column {
                                            Text(
                                                text = modelOption.displayName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selectedModel == modelOption.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (modelOption.description.isNotBlank()) {
                                                Text(
                                                    text = modelOption.description,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = selectedModel,
                                    onValueChange = {
                                        selectedModel = it
                                        persistGeminiSettingsDebounced()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("Идентификатор модели вручную") },
                                    placeholder = { Text("gemini-3.8-flash") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Connection Test Button
                                Button(
                                    onClick = {
                                        val keyToUse = apiKeyInput.ifBlank { GeminiSettingsManager.DEFAULT_API_KEY }
                                        if (apiKeyInput.isBlank()) {
                                            apiKeyInput = keyToUse
                                        }
                                        GeminiSettingsManager.saveSettings(
                                            context = context,
                                            apiKey = keyToUse,
                                            model = selectedModel,
                                            customBaseUrl = customBaseUrlInput
                                        )
                                        viewModel.testGeminiConnection(context)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isTestingGemini && !aiDisabled,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isTestingGemini) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Проверка подключения...")
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Проверить подключение к ИИ")
                                    }
                                }

                                uiState.geminiTestMessage?.let { testMsg ->
                                    Text(
                                        text = testMsg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (testMsg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: STORAGE, BACKUP & EXPORT
                        // Restart Tutorial & Interface Guide Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🎓 Обучение и гид по интерфейсу",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Запустить подсвечивающую интерактивную экскурсию по кнопкам и полям главного экрана приложения",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        OnboardingManager.resetOnboarding(context)
                                        Toast.makeText(context, "🎓 Обучение включено! Возвращаемся на главный экран...", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Пройти обучение заново")
                                }
                            }
                        }

                        // AI Prompt Assistant Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🤖 Промпт для сторонних ИИ (ChatGPT / Claude)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Скопируйте промпт для ChatGPT / Claude. Вставьте его нейросети вместе с текстом или фото блюда, а затем нажмите «Импорт из буфера» на главном экране.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(AiPromptTemplates.AI_RECIPE_JSON_PROMPT))
                                        Toast.makeText(context, "🤖 Промпт для ИИ скопирован в буфер обмена!", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Скопировать промпт для ИИ")
                                }
                            }
                        }

                        // Restore Sample Demo Recipe
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Добавить рецепт-образец",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Добавить в книгу рецептов демонстрационный образец «Шоколадный Брауни с вишней» для тестирования кухонного режима",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { viewModel.addSampleDemoRecipe() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Загрузить рецепт-образец")
                                }
                            }
                        }

                        // Export full database
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Экспорт базы данных",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Создать файл резервной копии всей кулинарной книги (.json)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.exportFullBackup { jsonStr ->
                                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_TEXT, jsonStr)
                                                putExtra(Intent.EXTRA_SUBJECT, "Cooking_Backup_${System.currentTimeMillis()}.json")
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Сохранить/поделиться бэкапом"))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Экспортировать бэкап")
                                }
                            }
                        }

                        // Restore full database
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Восстановление из бэкапа",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Восстановить базу из файла резервной копии (.json)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { restoreBackupLauncher.launch("*/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Upload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Загрузить бэкап")
                                }
                            }
                        }

                        // Import single recipe JSON file
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Импорт одного рецепта из файла",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Добавить рецепт, полученный в виде файла .json от другого пользователя",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = { importSingleRecipeLauncher.launch("*/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.FileOpen, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Открыть файл рецепта")
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: THEME & APPEARANCE
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🎨 Тема оформления",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "Выберите настроение приложения — применяется мгновенно, без перезапуска.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                AppTheme.entries.forEach { theme ->
                                    val isSelected = currentTheme == theme
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { ThemeManager.setTheme(context, theme) }
                                            .padding(vertical = 6.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { ThemeManager.setTheme(context, theme) }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = theme.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = theme.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            themePreviewSwatches(theme).forEach { swatch ->
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(swatch)
                                                        .border(
                                                            1.dp,
                                                            MaterialTheme.colorScheme.outlineVariant,
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Text(
                                        text = "💡 Темы «Ночной шеф», «Шоколад» и «Неоновая кухня» всегда тёмные — остальные следуют системной настройке телефона.",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: EXPERIMENTAL SETTINGS
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🧪 Экспериментальные настройки",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "⚠️ Функции этого раздела находятся в разработке и могут работать нестабильно. В случае проблем просто отключите их.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Full AI Disable Toggle
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🚫 Полное отключение ИИ",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Полностью отключает все запросы к нейросети и скрывает все ИИ-функции во всём приложении.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = aiDisabled,
                                        onCheckedChange = {
                                            aiDisabled = it
                                            ExperimentalSettingsManager.saveAiDisabled(context, it)
                                        }
                                    )
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🌐 DNS (обход блокировок)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "DNS-over-HTTPS шифрует запросы доменных имён. Полезно, если провайдер блокирует доступ к серверу ИИ. Применяется сразу к сетевым запросам приложения.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                DnsMode.entries.forEach { mode ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = selectedDnsMode == mode,
                                            onClick = {
                                                selectedDnsMode = mode
                                                ExperimentalSettingsManager.saveDnsMode(context, mode)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = mode.label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (selectedDnsMode == mode) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                if (selectedDnsMode == DnsMode.CUSTOM) {
                                    OutlinedTextField(
                                        value = customDnsUrlInput,
                                        onValueChange = {
                                            customDnsUrlInput = it
                                            ExperimentalSettingsManager.saveCustomDnsUrl(context, it)
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Свой DoH URL") },
                                        placeholder = { Text("https://1.1.1.1/dns-query") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Button(
                                    onClick = { viewModel.testDnsResolution(context) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isTestingDns,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isTestingDns) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Проверка DNS...")
                                    } else {
                                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Проверить разрешение адресов")
                                    }
                                }

                                uiState.dnsTestMessage?.let { dnsMsg ->
                                    Text(
                                        text = dnsMsg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (dnsMsg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // AI Assistant Section
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = if (!aiDisabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "🤖 ИИ-ассистент при готовке",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!aiDisabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                                        )
                                        Text(
                                            text = if (!aiDisabled)
                                                "В режиме готовки появится кнопка ассистента: он видит рецепт и текущий шаг и отвечает на вопросы."
                                            else
                                                "Отключено (включено полное отключение ИИ).",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = cookAssistantEnabled && !aiDisabled,
                                        enabled = !aiDisabled,
                                        onCheckedChange = {
                                            cookAssistantEnabled = it
                                            ExperimentalSettingsManager.saveCookAssistantEnabled(context, it)
                                        }
                                    )
                                }

                                // Thinking (размышления модели) — виден только когда ассистент включён и ИИ не отключен
                                if (cookAssistantEnabled && !aiDisabled) {
                                    Text(
                                        text = "🧠 Размышления модели (thinking)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Модель может сначала рассуждать «про себя», прежде чем ответить. Размышления улучшают качество ответов, но тратят токены и время.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AssistantThinkingMode.entries.forEach { mode ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedThinkingMode == mode,
                                                onClick = {
                                                    selectedThinkingMode = mode
                                                    ExperimentalSettingsManager.saveAssistantThinkingMode(context, mode)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = mode.label,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (selectedThinkingMode == mode) FontWeight.Bold else FontWeight.Normal
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
    }
}
