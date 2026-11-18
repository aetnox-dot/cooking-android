package com.aetnox.cooking.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.viewmodel.AssistantMessage
import com.aetnox.cooking.ui.viewmodel.CookModeViewModel
import com.aetnox.cooking.ui.viewmodel.StepTimerParser

/**
 * Чат с ИИ-ассистентом в режиме готовки.
 * Доступен по умолчанию в верхнем меню режима готовки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookAssistantSheet(
    viewModel: CookModeViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = remember {
        listOf(
            AppLocaleManager.tr("Что дальше?", "What's next?"),
            AppLocaleManager.tr("Совет по этому шагу", "Tip for this step"),
            AppLocaleManager.tr("Чем заменить ингредиент?", "Ingredient substitution?"),
            AppLocaleManager.tr("Сколько по времени осталось?", "How much time left?")
        )
    }

    LaunchedEffect(uiState.assistantMessages.size, uiState.isAssistantLoading) {
        val count = uiState.assistantMessages.size
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🤖 ${AppLocaleManager.tr("Ассистент шефа", "Chef Assistant")}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = AppLocaleManager.tr("Видит рецепт и текущий шаг. Отвечает коротко, чтобы беречь токены.", "Sees recipe and current step. Answers concisely to save tokens."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.assistantMessages.isEmpty() && !uiState.isAssistantLoading) {
                    item {
                        Text(
                            text = AppLocaleManager.tr("Спросите ассистента или выберите быстрый вопрос ниже 👇", "Ask assistant or pick a quick question below 👇"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(
                    items = uiState.assistantMessages,
                    key = { index, message -> "${index}_${message.text.hashCode()}" }
                ) { index, message ->
                    AssistantBubble(
                        message = message,
                        onStartTimer = { viewModel.startTimerFromSuggestion(context, it) },
                        onApplyUpdate = { viewModel.applyProposedUpdate(context, index) },
                        onDismissUpdate = { viewModel.dismissProposedUpdate(index) }
                    )
                }

                if (uiState.isAssistantLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = AppLocaleManager.tr("Ассистент думает...", "Assistant is thinking..."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            uiState.assistantError?.let { err ->
                Text(
                    text = "❌ $err",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                items(quickQuestions, key = { it }) { question ->
                    SuggestionChip(
                        onClick = { viewModel.askAssistant(context, question) },
                        label = { Text(question, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(AppLocaleManager.tr("Спросить о текущем шаге...", "Ask about current step...")) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (input.isNotBlank()) {
                            viewModel.askAssistant(context, input)
                            input = ""
                        }
                    },
                    enabled = input.isNotBlank() && !uiState.isAssistantLoading
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = AppLocaleManager.tr("Отправить", "Send"),
                        tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.padding(bottom = 14.dp))
        }
    }
}

@Composable
private fun AssistantBubble(
    message: AssistantMessage,
    onStartTimer: ((AssistantMessage) -> Unit)? = null,
    onApplyUpdate: (() -> Unit)? = null,
    onDismissUpdate: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (message.isUser) 14.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 14.dp
                ),
                color = if (message.isUser) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isUser) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            message.proposedUpdate?.let { prop ->
                ProposedUpdateCard(
                    proposedUpdate = prop,
                    isApplied = message.isApplied,
                    isDismissed = message.isDismissed,
                    onApply = { onApplyUpdate?.invoke() },
                    onDismiss = { onDismissUpdate?.invoke() }
                )
            }

            message.timerSuggestion?.let { suggestion ->
                Spacer(modifier = Modifier.size(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = suggestion.label,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${AppLocaleManager.tr("Время", "Time")}: ${StepTimerParser.formatTime(suggestion.seconds)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        if (suggestion.isStarted) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = AppLocaleManager.tr("✅ Запущен", "✅ Started"),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = { onStartTimer?.invoke(message) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppLocaleManager.tr("Запустить", "Start"), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProposedUpdateCard(
    proposedUpdate: com.aetnox.cooking.data.remote.model.ProposedUpdateDto,
    isApplied: Boolean,
    isDismissed: Boolean,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isDismissed) return

    val patch = proposedUpdate.patch
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🧑‍🍳 ${AppLocaleManager.tr("Рекомендация шефа по улучшению рецепта", "Chef's recommendation for recipe improvement")}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            proposedUpdate.summary?.let { summary ->
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            if (patch != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    patch.title?.let { newTitle ->
                        Text("• ${AppLocaleManager.tr("Название", "Title")}: $newTitle", style = MaterialTheme.typography.bodySmall)
                    }
                    patch.suggestedStatus?.let { newStatus ->
                        val statusLabel = when (newStatus) {
                            "NEEDS_WORK" -> AppLocaleManager.tr("🔧 Работа над ошибками", "🔧 Needs work")
                            "VERIFIED" -> AppLocaleManager.tr("✅ Проверено", "✅ Verified")
                            "PLANNED" -> AppLocaleManager.tr("📌 В планах", "📌 Planned")
                            "ARCHIVED" -> AppLocaleManager.tr("📦 Архив", "📦 Archive")
                            else -> newStatus
                        }
                        Text("• ${AppLocaleManager.tr("Статус", "Status")}: $statusLabel", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    patch.chefTemperature?.let { temp ->
                        Text("• ${AppLocaleManager.tr("Температура", "Temperature")}: $temp", style = MaterialTheme.typography.bodySmall)
                    }
                    patch.chefHeatingType?.let { mode ->
                        Text("• ${AppLocaleManager.tr("Режим нагрева", "Heating type")}: $mode", style = MaterialTheme.typography.bodySmall)
                    }
                    patch.postCookNote?.let { note ->
                        Text("• ${AppLocaleManager.tr("Заметка шефа", "Chef's note")}: $note", style = MaterialTheme.typography.bodySmall)
                    }
                    patch.notes?.let { notes ->
                        Text("• ${AppLocaleManager.tr("Заметки", "Notes")}: $notes", style = MaterialTheme.typography.bodySmall)
                    }
                    patch.ingredientAmounts?.forEach { ing ->
                        Text("• ${ing.name}: ${ing.newAmount}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isApplied) {
                    Text(
                        text = AppLocaleManager.tr("✅ Применено к рецепту", "✅ Applied to recipe"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(AppLocaleManager.tr("Отмена", "Dismiss"), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppLocaleManager.tr("Применить", "Apply"), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
