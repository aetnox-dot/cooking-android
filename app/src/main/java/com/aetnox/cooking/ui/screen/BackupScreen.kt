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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.School
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import com.aetnox.cooking.ui.component.ViewMode
import com.aetnox.cooking.ui.theme.CustomThemeManager
import com.aetnox.cooking.ui.util.LogoColorOption
import com.aetnox.cooking.ui.util.AppLanguage
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.FormatUtils
import com.aetnox.cooking.ui.util.LogoManager
import com.aetnox.cooking.ui.util.LogoStyle
import com.aetnox.cooking.ui.util.ViewModeManager
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.domain.net.DnsMode
import com.aetnox.cooking.domain.net.ExperimentalSettingsManager
import com.aetnox.cooking.domain.parser.AiPromptTemplates
import com.aetnox.cooking.ui.theme.AppTheme
import com.aetnox.cooking.ui.theme.ThemeManager
import com.aetnox.cooking.ui.theme.themePreviewSwatches
import com.aetnox.cooking.ui.util.OnboardingManager
import com.aetnox.cooking.ui.viewmodel.BackupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: BackupViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.initSessionObserver(context)
    }

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
            } catch (_: Exception) {
                // handle error
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
                // handle error
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(AppLocaleManager.tr("Настройки приложения", "App Settings"), fontWeight = FontWeight.Bold) },
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
        val currentTheme = ThemeManager.currentTheme.value
        val coroutineScope = rememberCoroutineScope()
        val feedbackBringIntoViewRequester = remember { BringIntoViewRequester() }

        var selectedSettingsTab by remember { mutableIntStateOf(0) }

        var keyInputText by remember { mutableStateOf("") }
        var claimCodeInputText by remember { mutableStateOf("") }
        var cloudRecipeIdInputText by remember { mutableStateOf("") }
        var accessCodeInputText by remember { mutableStateOf("") }
        var feedbackInputText by remember { mutableStateOf("") }

        val currentNetSettings = remember { ExperimentalSettingsManager.getSettings(context) }
        var densityHintsEnabled by remember { mutableStateOf(currentNetSettings.densityHintsEnabled) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            PrimaryTabRow(selectedTabIndex = selectedSettingsTab) {
                Tab(
                    selected = selectedSettingsTab == 0,
                    onClick = { selectedSettingsTab = 0 },
                    text = { Text("👤 ${AppLocaleManager.tr("Аккаунт", "Account")}", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 1,
                    onClick = { selectedSettingsTab = 1 },
                    text = { Text("💾 ${AppLocaleManager.tr("Память", "Storage")}", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 2,
                    onClick = { selectedSettingsTab = 2 },
                    text = { Text("🎨 ${AppLocaleManager.tr("Оформление", "Appearance")}", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSettingsTab == 3,
                    onClick = { selectedSettingsTab = 3 },
                    text = { Text("🧪 ${AppLocaleManager.tr("Эксперименты", "Experiments")}", fontWeight = FontWeight.Bold) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val isOnline by com.aetnox.cooking.data.remote.NetworkObserver.observeNetwork(context)
                    .collectAsState(initial = com.aetnox.cooking.data.remote.NetworkObserver.isOnline(context))

                if (!isOnline) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = AppLocaleManager.tr("📡 Офлайн-режим: облачный поиск, активация ключей и перенос подписки временно недоступны.", "📡 Offline mode: cloud search, key activation and subscription transfer are temporarily unavailable."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }

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
                        // TAB 0: ACCOUNT & SUBSCRIPTION STATUS
                        val session = uiState.userSession

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
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppLocaleManager.tr("👤 Профиль и подписка", "👤 Profile & Subscription"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.refreshUserStatus(context) }) {
                                        Icon(Icons.Default.Refresh, contentDescription = AppLocaleManager.tr("Обновить", "Refresh"))
                                    }
                                }

                                if (session.isQuarantined) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                                    ) {
                                        Text(
                                            text = AppLocaleManager.tr("⚠️ Аккаунт находится в карантине (30 дней). Активна базовая лимитированная версия.", "⚠️ Account in quarantine (30 days). Basic limited version active."),
                                            modifier = Modifier.padding(12.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "Account ID: ${session.accountId.ifEmpty { AppLocaleManager.tr("Создаётся...", "Creating...") }}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${AppLocaleManager.tr("Тариф", "Plan")}: ${session.planName} (${AppLocaleManager.tr("Уровень", "Level")} ${session.planLevel})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val subUntilText = if (session.subscriptionUntil > 0) {
                                        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(session.subscriptionUntil * 1000))
                                    } else {
                                        AppLocaleManager.tr("Бессрочно / Free", "Lifetime / Free")
                                    }
                                    Text(
                                        text = "${AppLocaleManager.tr("Подписка активна до:", "Subscription active until:")} $subUntilText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Quotas Progress Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "📊 ${AppLocaleManager.tr("Лимиты и квоты тарифа", "Plan Limits & Quotas")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // AI Requests Monthly Quota
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(AppLocaleManager.tr("Витамины ИИ (Оцифровка + Ассистент)", "AI Vitamins (Digitization + Assistant)"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        if (session.aiRequestsMonthlyLimit > 0) {
                                            Text("${session.aiRequestsUsedThisMonth} / ${session.aiRequestsMonthlyLimit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(
                                        text = "${AppLocaleManager.tr("Остаток", "Remaining")}: ${FormatUtils.formatVitaminAi(session.aiRequestsRemaining)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (session.aiRequestsMonthlyLimit > 0) {
                                        val aiProgress = (session.aiRequestsUsedThisMonth.toFloat() / session.aiRequestsMonthlyLimit.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { aiProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }

                                // Photo Scan Monthly Quota
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(AppLocaleManager.tr("Сканирование по фото (в месяц)", "Photo Scans (per month)"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        if (session.photosMonthlyLimit > 0) {
                                            Text("${session.photosUsedThisMonth} / ${session.photosMonthlyLimit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(if (session.allowPhotos) AppLocaleManager.tr("Включено", "Enabled") else "🔒 " + AppLocaleManager.tr("Нужен Pro/Ultra", "Requires Pro/Ultra"), style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    Text(
                                        text = if (session.photosMonthlyLimit > 0) "${AppLocaleManager.tr("Остаток", "Remaining")}: ${session.photosRemaining}" else if (session.allowPhotos) AppLocaleManager.tr("Доступно по тарифу", "Included in plan") else AppLocaleManager.tr("0 фото", "0 photos"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (session.photosMonthlyLimit > 0) {
                                        val photoProgress = (session.photosUsedThisMonth.toFloat() / session.photosMonthlyLimit.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { photoProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }

                                // Cloud Storage Quota
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(AppLocaleManager.tr("Рецептов в облаке", "Cloud Recipes"), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        if (session.cloudLimit > 0) {
                                            Text("${session.cloudUsedTotal} / ${session.cloudLimit}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("${session.cloudUsedTotal} ${AppLocaleManager.tr("рецептов", "recipes")}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    if (session.cloudLimit > 0) {
                                        Text(
                                            text = "${AppLocaleManager.tr("Остаток", "Remaining")}: ${session.cloudRemaining} ${AppLocaleManager.tr("рецептов", "recipes")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val cloudProgress = (session.cloudUsedTotal.toFloat() / session.cloudLimit.toFloat()).coerceIn(0f, 1f)
                                        LinearProgressIndicator(
                                            progress = { cloudProgress },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }

                                if (session.monthlyResetsAt > 0L) {
                                    val resetDateText = SimpleDateFormat("dd.MM.yyyy HH:mm 'UTC'", Locale.US)
                                        .format(Date(session.monthlyResetsAt * 1000))
                                    Text(
                                        text = "📅 ${AppLocaleManager.tr("Сброс месячных квот:", "Monthly quotas reset:")} $resetDateText",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider()
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "⚖️ ${AppLocaleManager.tr("Расценки действий (витаминов ИИ):", "Action costs (AI vitamins):")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    val costs = session.aiCosts
                                    Text("• 🍂 ${AppLocaleManager.tr("Вопрос су-шефу:", "Question to sous-chef:")} ${FormatUtils.formatVitaminAi(costs.assistant, short = true)}", style = MaterialTheme.typography.labelSmall)
                                    Text("• 📖 ${AppLocaleManager.tr("Оцифровка текстом:", "Text digitization:")} ${FormatUtils.formatVitaminAi(costs.parse, short = true)}", style = MaterialTheme.typography.labelSmall)
                                    Text("• 📷 ${AppLocaleManager.tr("Оцифровка по фото:", "Photo digitization:")} ${FormatUtils.formatVitaminAi(costs.parsePhoto, short = true)} (+1 ${AppLocaleManager.tr("фото", "photo")})", style = MaterialTheme.typography.labelSmall)
                                    Text("• 🧭 ${AppLocaleManager.tr("Двухфазный поиск (план / финиш):", "Two-phase search (plan / finish):")} ${FormatUtils.formatVitaminAi(costs.searchPlan, short = true)} / ${FormatUtils.formatVitaminAi(costs.searchFinish, short = true)}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        // Activate License Key Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🔑 ${AppLocaleManager.tr("Активация ключа подписки", "Subscription Key Activation")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Введите ключ, приобретенный на маркетплейсе (например: RECIPE-7K9A-B2X4).", "Enter license key purchased from marketplace (e.g.: RECIPE-7K9A-B2X4)."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = keyInputText,
                                    onValueChange = { keyInputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(AppLocaleManager.tr("Ключ подписки", "License key")) },
                                    placeholder = { Text("RECIPE-XXXX-XXXX") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = { viewModel.activateKey(context, keyInputText) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isActivatingKey && keyInputText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isActivatingKey) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Активация...", "Activating..."))
                                    } else {
                                        Text(AppLocaleManager.tr("Активировать ключ", "Activate key"))
                                    }
                                }

                                uiState.keyActivationMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Transfer Subscription Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "📱 ${AppLocaleManager.tr("Перенос подписки на другой телефон", "Transfer Subscription to Another Device")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Перенесите купленную подписку на новое устройство с помощью 6-значного одноразового кода.", "Transfer your subscription to a new device using a 6-digit one-time code."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Button(
                                    onClick = { viewModel.createTransferCode(context) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isCreatingTransfer && session.planLevel >= 2,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isCreatingTransfer) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Генерация...", "Generating..."))
                                    } else {
                                        Text(AppLocaleManager.tr("Сгенерировать код переноса", "Generate transfer code"))
                                    }
                                }

                                uiState.transferCode?.let { code ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(AppLocaleManager.tr("Ваш код переноса:", "Your transfer code:"), style = MaterialTheme.typography.labelMedium)
                                            Text(
                                                text = code,
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = AppLocaleManager.tr("Код действителен 10 минут (${uiState.transferExpiresIn} сек)", "Code valid for 10 minutes (${uiState.transferExpiresIn} sec)"),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Text(AppLocaleManager.tr("Ввод кода на новом устройстве:", "Enter code on new device:"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                                OutlinedTextField(
                                    value = claimCodeInputText,
                                    onValueChange = { claimCodeInputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(AppLocaleManager.tr("6-значный код переноса", "6-digit transfer code")) },
                                    placeholder = { Text("849201") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = { viewModel.claimTransferCode(context, claimCodeInputText) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isClaimingTransfer && claimCodeInputText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isClaimingTransfer) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Активация...", "Activating..."))
                                    } else {
                                        Text(AppLocaleManager.tr("Активировать подписку по коду", "Claim subscription code"))
                                    }
                                }

                                uiState.transferMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Download Cloud Recipe Card (by Access Code or by ID)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🔑 ${AppLocaleManager.tr("Скачать рецепт из облака", "Download Cloud Recipe")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("1. По 10-значному коду доступа (приватный рецепт):", "1. By 10-digit access code (private recipe):"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = accessCodeInputText,
                                    onValueChange = { accessCodeInputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(AppLocaleManager.tr("10-значный код доступа", "10-digit access code")) },
                                    placeholder = { Text(AppLocaleManager.tr("например: Xx9Ab3a7K2", "e.g.: Xx9Ab3a7K2")) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.downloadRecipeByAccessCode(context, accessCodeInputText) { localId ->
                                            Toast.makeText(context, AppLocaleManager.tr("Рецепт успешно скачан по коду!", "Recipe downloaded successfully!"), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isDownloadingCloud && accessCodeInputText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isDownloadingCloud) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Скачивание...", "Downloading..."))
                                    } else {
                                        Text(AppLocaleManager.tr("Скачать по коду доступа", "Download by access code"))
                                    }
                                }

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Text(
                                    text = AppLocaleManager.tr("2. По ID рецепта в облаке:", "2. By cloud recipe ID:"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = cloudRecipeIdInputText,
                                    onValueChange = { cloudRecipeIdInputText = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(AppLocaleManager.tr("ID рецепта в облаке", "Cloud Recipe ID")) },
                                    placeholder = { Text("cloud-123-456") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                OutlinedButton(
                                    onClick = {
                                        viewModel.downloadCloudRecipe(context, cloudRecipeIdInputText) { localId ->
                                            Toast.makeText(context, AppLocaleManager.tr("Рецепт скачан из облака!", "Recipe downloaded from cloud!"), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isDownloadingCloud && cloudRecipeIdInputText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(AppLocaleManager.tr("Скачать по ID рецепта", "Download by Recipe ID"))
                                }

                                uiState.cloudDownloadMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Notice Card pointing to Main Screen Cloud Tab
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "🌐 ${AppLocaleManager.tr("Облачный каталог и Поиск", "Cloud Catalog & Search")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = AppLocaleManager.tr("Поиск, каталог открытых рецептов и управление вашими выгрузками доступны прямо на главном экране кулинарной книги на вкладке «🌐 Облачный каталог».", "Search, public catalog and uploaded recipes management are available directly on main screen in '🌐 Cloud Catalog' tab."),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        // Feedback Card (POST /feedback)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Feedback, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("💬 ${AppLocaleManager.tr("Обратная связь", "Feedback")}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }

                                Text(AppLocaleManager.tr("Напишите сообщение или отзыв разработчикам:", "Write a message or feedback to developers:"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                OutlinedTextField(
                                    value = feedbackInputText,
                                    onValueChange = { if (it.length <= 2000) feedbackInputText = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .bringIntoViewRequester(feedbackBringIntoViewRequester)
                                        .onFocusEvent { focusState ->
                                            if (focusState.isFocused) {
                                                coroutineScope.launch {
                                                    feedbackBringIntoViewRequester.bringIntoView()
                                                }
                                            }
                                        },
                                    label = { Text(AppLocaleManager.tr("Сообщение (до 2000 символов)", "Message (up to 2000 chars)")) },
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Button(
                                    onClick = {
                                        viewModel.sendFeedback(context, feedbackInputText)
                                        feedbackInputText = ""
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isSendingFeedback && feedbackInputText.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isSendingFeedback) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Отправка...", "Sending..."))
                                    } else {
                                        Text(AppLocaleManager.tr("Отправить отзыв", "Send feedback"))
                                    }
                                }

                                uiState.feedbackStatusMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Session & Account Security Card (POST /auth/logout)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text("⚙️ ${AppLocaleManager.tr("Сессия и безопасность", "Session & Security")}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                                OutlinedButton(
                                    onClick = {
                                        viewModel.logoutSession(context) {
                                            Toast.makeText(context, AppLocaleManager.tr("Сессия завершена. Создана новая авторизация.", "Session logged out. New authorization created."), Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !uiState.isLoggingOut,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (uiState.isLoggingOut) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.Logout, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(AppLocaleManager.tr("Выйти из сессии", "Sign out session"))
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: STORAGE, BACKUP & EXPORT
                        // Cloud Backup Card (v2: POST/GET /recipes/backup)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Cloud, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "☁️ ${AppLocaleManager.tr("Облачный бэкап аккаунта (CookIn v2)", "Account Cloud Backup (CookIn v2)")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Единый слепок вашей кулинарной книги в облаке. Хранит все рецепты, их статусы, избранность и заметки.", "Single cloud snapshot of your cookbook. Stores all recipes, statuses, favorites, and notes."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )

                                uiState.cloudBackupInfo?.let { info ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            if (info.exists) {
                                                val dateStr = info.createdAt?.let {
                                                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it * 1000))
                                                } ?: AppLocaleManager.tr("Неизвестно", "Unknown")
                                                Text("✅ ${AppLocaleManager.tr("Облачный бэкап есть (создан $dateStr)", "Cloud backup exists (created $dateStr)")}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                Text("${AppLocaleManager.tr("Рецептов в бэкапе:", "Recipes in backup:")} ${info.recipesCount}", style = MaterialTheme.typography.labelSmall)
                                            } else {
                                                Text("ℹ️ ${AppLocaleManager.tr("Облачный бэкап отсутствует", "No cloud backup found")}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.uploadCloudBackup(context) },
                                        modifier = Modifier.weight(1f),
                                        enabled = !uiState.isUploadingCloudBackup,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (uiState.isUploadingCloudBackup) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(AppLocaleManager.tr("Выгрузить", "Upload"))
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.restoreCloudBackup(context) {
                                                Toast.makeText(context, AppLocaleManager.tr("Облачный бэкап восстановлен!", "Cloud backup restored!"), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !uiState.isRestoringCloudBackup && (uiState.cloudBackupInfo?.exists == true),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (uiState.isRestoringCloudBackup) {
                                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(AppLocaleManager.tr("Восстановить", "Restore"))
                                        }
                                    }
                                }

                                uiState.cloudBackupStatusMessage?.let { msg ->
                                    Text(
                                        text = msg,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

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
                                        text = "🎓 ${AppLocaleManager.tr("Обучение и гид по интерфейсу", "Tutorial & Interface Walkthrough")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Запустить подсвечивающую интерактивную экскурсию по кнопкам и полям главного экрана приложения", "Launch highlighted interactive walkthrough for main screen buttons and fields"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        OnboardingManager.resetOnboarding(context)
                                        Toast.makeText(context, AppLocaleManager.tr("🎓 Обучение включено! Возвращаемся на главный экран...", "🎓 Tutorial enabled! Returning to main screen..."), Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppLocaleManager.tr("Пройти обучение заново", "Restart tutorial"))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("Добавить рецепт-образец", "Add sample recipe"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Добавить в книгу рецептов демонстрационный образец «Шоколадный Брауни с вишней» для тестирования кухонного режима", "Add demo 'Chocolate Cherry Brownie' sample to test cooking mode"),
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
                                    Text(AppLocaleManager.tr("Загрузить рецепт-образец", "Add sample recipe"))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("Экспорт базы данных", "Export Database"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Создать файл резервной копии всей кулинарной книги (.json)", "Create backup file of full cookbook (.json)"),
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
                                            context.startActivity(Intent.createChooser(sendIntent, AppLocaleManager.tr("Сохранить/поделиться бэкапом", "Save/share backup")))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(AppLocaleManager.tr("Экспортировать бэкап", "Export backup"))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("Восстановление из бэкапа", "Restore from Backup"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Восстановить базу из файла резервной копии (.json)", "Restore database from backup file (.json)"),
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
                                    Text(AppLocaleManager.tr("Загрузить бэкап", "Load backup"))
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = AppLocaleManager.tr("Импорт одного рецепта из файла", "Import Single Recipe from File"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = AppLocaleManager.tr("Добавить рецепт, полученный в виде файла .json от другого пользователя", "Add recipe received as a .json file from another user"),
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
                                    Text(AppLocaleManager.tr("Открыть файл рецепта", "Open recipe file"))
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
                                        text = AppLocaleManager.tr("🎨 Тема оформления", "🎨 Theme & Appearance"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr(
                                        "Выберите настроение приложения — применяется мгновенно, без перезапуска.",
                                        "Choose the app theme — applies instantly without restart."
                                    ),
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

                                if (currentTheme == AppTheme.CUSTOM) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(
                                                text = AppLocaleManager.tr("✨ Настройка кастомных акцентов", "✨ Custom Accent Color Setup"),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Text(
                                                text = AppLocaleManager.tr("Основной акцентный цвет:", "Primary Accent Color:"),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium
                                            )

                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(CustomThemeManager.presetColors) { color ->
                                                    val isSel = CustomThemeManager.customPrimary.value == color
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .border(
                                                                if (isSel) 3.dp else 1.dp,
                                                                if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                                CircleShape
                                                            )
                                                            .clickable { CustomThemeManager.setPrimaryColor(context, color) }
                                                    )
                                                }
                                            }

                                            Text(
                                                text = AppLocaleManager.tr("Дополнительный акцентный цвет:", "Secondary Accent Color:"),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Medium
                                            )

                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(CustomThemeManager.presetColors) { color ->
                                                    val isSel = CustomThemeManager.customSecondary.value == color
                                                    Box(
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                            .clip(CircleShape)
                                                            .background(color)
                                                            .border(
                                                                if (isSel) 3.dp else 1.dp,
                                                                if (isSel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant,
                                                                CircleShape
                                                            )
                                                            .clickable { CustomThemeManager.setSecondaryColor(context, color) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                                ) {
                                    Text(
                                        text = AppLocaleManager.tr(
                                            "💡 Темы «Ночной шеф», «Шоколад» и «Неоновая кухня» всегда тёмные — остальные следуют системной настройке телефона.",
                                            "💡 'Night Chef', 'Chocolate' and 'Neon Kitchen' themes are always dark — others follow system settings."
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Logo Customization Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = AppLocaleManager.tr("👨‍🍳 Логотип приложения", "👨‍🍳 App Logo"),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr(
                                        "Выберите значок логотипа и его цвет — отображается на главном экране Cooking:",
                                        "Select logo icon and color scheme displayed on Cooking main screen:"
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = AppLocaleManager.tr("Стиль оформления логотипа:", "Logo Style:"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(LogoStyle.entries.toTypedArray()) { style ->
                                        val isSel = LogoManager.currentStyle.value == style
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { LogoManager.setStyle(context, style) },
                                            label = { Text(style.label) }
                                        )
                                    }
                                }

                                Text(
                                    text = AppLocaleManager.tr("Цветовая гамма логотипа:", "Logo Color Palette:"),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(LogoColorOption.entries.toTypedArray()) { option ->
                                        val isSel = LogoManager.currentColorOption.value == option
                                        val displayColor = option.color ?: MaterialTheme.colorScheme.primary
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { LogoManager.setColorOption(context, option) },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clip(CircleShape)
                                                        .background(displayColor)
                                                )
                                            },
                                            label = { Text(option.label) }
                                        )
                                    }
                                }
                            }
                        }

                        // App Language Setting Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Public, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "🌐 ${AppLocaleManager.tr("Язык интерфейса / App Language", "App Language / Язык интерфейса")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Выберите язык приложения и локализации системных сообщений:", "Select application language and system notification localization:"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val currentLang = AppLocaleManager.currentLanguage.value

                                    AppLanguage.entries.forEach { lang ->
                                        val isSel = currentLang == lang
                                        FilledTonalButton(
                                            onClick = { AppLocaleManager.setLanguage(context, lang) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(lang.label, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }

                        // Launcher App Icon Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "📱 ${AppLocaleManager.tr("Иконка на рабочем столе и экране блокировки", "Home Screen & Lock Screen Icon")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Выберите значок приложения, который будет отображаться на рабочем столе, в лаунчере и на экране блокировки Android:", "Choose the app icon displayed on the home screen, launcher, and Android lock screen:"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(com.aetnox.cooking.ui.util.AppIconVariant.entries.toTypedArray()) { variant ->
                                        val isSel = com.aetnox.cooking.ui.util.AppIconManager.currentIconVariant.value == variant
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                com.aetnox.cooking.ui.util.AppIconManager.setAppIcon(context, variant)
                                                Toast.makeText(context, "📱 ${AppLocaleManager.tr("Иконка приложения установлена:", "App icon set:")} ${variant.label}", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text(variant.label, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                                        )
                                    }
                                }
                            }
                        }

                        // Tile View Mode Setting Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.GridView, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "📱 ${AppLocaleManager.tr("Отображение плиток рецептов", "Recipe Tiles View")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Выберите желаемый вид списка рецептов на главном экране:", "Choose preferred layout for recipe list on main screen:"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    val currentMode = ViewModeManager.currentViewMode.value

                                    FilledTonalButton(
                                        onClick = { ViewModeManager.setViewMode(context, ViewMode.LIST) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (currentMode == ViewMode.LIST) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.ViewList, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(AppLocaleManager.tr("Списком", "List"))
                                    }

                                    FilledTonalButton(
                                        onClick = { ViewModeManager.setViewMode(context, ViewMode.GRID) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (currentMode == ViewMode.GRID) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(AppLocaleManager.tr("Сеткой", "Grid"))
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: EXPERIMENTAL SETTINGS (Smart Density Hints)
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
                                        text = "🧪 ${AppLocaleManager.tr("Экспериментальные настройки", "Experimental Settings")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = AppLocaleManager.tr("Дополнительные функции для удобства работы с рецептами.", "Additional features for recipe convenience."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Science,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "💡 ${AppLocaleManager.tr("Умные подсказки плотности продуктов", "Smart Ingredient Density Hints")}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = AppLocaleManager.tr("В редакторе ингредиентов подсвечивает стандартные справочные единицы (например: 1 ст. л. = 15г).", "Highlights standard reference units in ingredient editor (e.g. 1 tbsp = 15g)."),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = densityHintsEnabled,
                                        onCheckedChange = {
                                            densityHintsEnabled = it
                                            ExperimentalSettingsManager.saveDensityHintsEnabled(context, it)
                                        }
                                    )
                                }
                            }
                        }

                        // App Version & Build Information Card
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
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ℹ️ ${AppLocaleManager.tr("Информация о приложении", "App Information")}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = AppLocaleManager.tr("Версия приложения", "App Version"),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    val appVersionText = remember(context) {
                                        try {
                                            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                                            val vName = pInfo.versionName ?: com.aetnox.cooking.BuildConfig.VERSION_NAME
                                            @Suppress("DEPRECATION")
                                            val vCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                                                pInfo.longVersionCode
                                            } else {
                                                pInfo.versionCode.toLong()
                                            }
                                            AppLocaleManager.tr("v$vName (сборка $vCode)", "v$vName (build $vCode)")
                                        } catch (_: Exception) {
                                            AppLocaleManager.tr(
                                                "v${com.aetnox.cooking.BuildConfig.VERSION_NAME} (сборка ${com.aetnox.cooking.BuildConfig.VERSION_CODE})",
                                                "v${com.aetnox.cooking.BuildConfig.VERSION_NAME} (build ${com.aetnox.cooking.BuildConfig.VERSION_CODE})"
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = appVersionText,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

        }
    }
}
