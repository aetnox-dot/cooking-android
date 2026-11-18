package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.remote.BackendRepository
import com.aetnox.cooking.data.remote.model.ChatMessageDto
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.domain.ai.CookAssistantTools
import com.aetnox.cooking.domain.ai.GeminiApiService
import com.aetnox.cooking.ui.util.ActiveCookingManager
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StepTimerState(
    val stepNumber: Int,
    val totalSeconds: Int,
    val secondsRemaining: Int,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

object StepTimerParser {
    fun parseDurationInSeconds(text: String): Int? {
        val regex = Regex("""(\d+(?:[.,]\d+)?)\s*(мин|минут|минуты|сек|секунд|секунды|ч|час|часа|часов)""", RegexOption.IGNORE_CASE)
        val match = regex.find(text) ?: return null
        val numberStr = match.groupValues[1].replace(',', '.')
        val number = numberStr.toDoubleOrNull() ?: return null
        val unit = match.groupValues[2].lowercase()

        return when {
            unit.contains("сек") -> number.toInt().coerceAtLeast(1)
            unit.contains("ч") || unit.contains("час") -> (number * 3600).toInt().coerceAtLeast(1)
            else -> (number * 60).toInt().coerceAtLeast(1)
        }
    }

    fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(java.util.Locale.ROOT, "%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(java.util.Locale.ROOT, "%02d:%02d", mins, secs)
        }
    }
}

data class CookModeUiState(
    val recipeWithDetails: RecipeWithDetails? = null,
    val keepScreenOn: Boolean = true,
    val currentServings: Double = 4.0,
    val notesExpanded: Boolean = false,
    val prepCheckedIngredients: Set<Long> = emptySet(),
    val stepCheckedTokens: Set<String> = emptySet(),
    val stepTimers: Map<Int, StepTimerState> = emptyMap(),
    val activeStepIndex: Int = 0,
    val showPostCookDialog: Boolean = false,
    val isLoading: Boolean = true,
    val recipeNotFound: Boolean = false,
    val assistantMessages: List<AssistantMessage> = emptyList(),
    val isAssistantLoading: Boolean = false,
    val assistantError: String? = null
) {
    val checkedIngredients: Set<Long> get() = prepCheckedIngredients
}

data class TimerSuggestion(
    val seconds: Int,
    val label: String,
    val isStarted: Boolean = false
)

data class AssistantMessage(
    val isUser: Boolean,
    val text: String,
    val timerSuggestion: TimerSuggestion? = null,
    val proposedUpdate: com.aetnox.cooking.data.remote.model.ProposedUpdateDto? = null,
    val isApplied: Boolean = false,
    val isDismissed: Boolean = false
)

class CookModeViewModel(
    private val repository: RecipeRepository,
    private val recipeId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookModeUiState())
    val uiState: StateFlow<CookModeUiState> = _uiState.asStateFlow()

    private val timerJobs = mutableMapOf<Int, Job>()

    init {
        loadRecipe()
        observeActiveTimers()
    }

    private fun observeActiveTimers() {
        viewModelScope.launch {
            var lastMap: Map<Int, StepTimerState>? = null
            while (true) {
                val sess = ActiveCookingManager.getSessionForRecipe(recipeId)
                val map = sess?.activeTimers?.mapValues { (stepNum, timer) ->
                    StepTimerState(
                        stepNumber = stepNum,
                        totalSeconds = timer.totalSeconds,
                        secondsRemaining = timer.secondsRemaining,
                        isRunning = timer.isRunning,
                        isFinished = timer.isFinished
                    )
                } ?: emptyMap()

                // Пишем в state только при реальном изменении, чтобы не гонять рекомпозиции 3 раза в секунду
                if (map != lastMap) {
                    lastMap = map
                    _uiState.value = _uiState.value.copy(stepTimers = map)
                }
                delay(300L)
            }
        }
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            // Минимальная длительность экрана загрузки: сглаживает «прыжок» при открытии рецепта,
            // пока Compose инициализирует пейджер и декодирует изображения
            val minLoadingUntilMs = System.currentTimeMillis() + MIN_LOADING_MS
            var isFirstEmission = true

            repository.getRecipeById(recipeId).collect { recipeDetails ->
                if (recipeDetails != null) {
                    val savedSession = ActiveCookingManager.getSessionForRecipe(recipeId)
                    val baseServings = recipeDetails.recipe.baseServings
                    val currServings = if (_uiState.value.isLoading && savedSession != null) {
                        savedSession.currentServings
                    } else if (_uiState.value.isLoading) {
                        baseServings
                    } else {
                        _uiState.value.currentServings
                    }

                    val initialStep = if (_uiState.value.activeStepIndex > 0) {
                        _uiState.value.activeStepIndex
                    } else if (_uiState.value.isLoading && savedSession != null) {
                        savedSession.activeStepIndex
                    } else {
                        _uiState.value.activeStepIndex
                    }
                    val prepChecked = if (_uiState.value.isLoading && savedSession != null) savedSession.checkedIngredients else _uiState.value.prepCheckedIngredients
                    val stepTokens = if (_uiState.value.isLoading && savedSession != null) savedSession.stepCheckedTokens else _uiState.value.stepCheckedTokens

                    if (isFirstEmission) {
                        val nowMs = System.currentTimeMillis()
                        if (nowMs < minLoadingUntilMs) delay(minLoadingUntilMs - nowMs)
                    }

                    _uiState.value = _uiState.value.copy(
                        recipeWithDetails = recipeDetails,
                        currentServings = currServings,
                        activeStepIndex = initialStep,
                        prepCheckedIngredients = prepChecked,
                        stepCheckedTokens = stepTokens,
                        isLoading = false
                    )
                    isFirstEmission = false
                } else {
                    // Рецепт не найден (удалён/некорректный id) — не крутим спиннер бесконечно
                    _uiState.value = _uiState.value.copy(
                        recipeWithDetails = null,
                        isLoading = false,
                        recipeNotFound = true
                    )
                }
            }
        }
    }

    fun toggleKeepScreenOn() {
        _uiState.value = _uiState.value.copy(keepScreenOn = !_uiState.value.keepScreenOn)
    }

    fun setKeepScreenOn(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(keepScreenOn = enabled)
    }

    fun toggleFavorite() {
        val recipe = _uiState.value.recipeWithDetails?.recipe ?: return
        val next = !recipe.isFavorite
        viewModelScope.launch {
            repository.updateRecipeFavorite(recipe.id, next)
        }
    }

    fun toggleCartStatus() {
        val recipe = _uiState.value.recipeWithDetails?.recipe ?: return
        val next = !recipe.isInCart
        viewModelScope.launch {
            repository.updateRecipeCartStatus(recipe.id, next)
        }
    }

    fun incrementServings() {
        val next = _uiState.value.currentServings + 1.0
        _uiState.value = _uiState.value.copy(currentServings = next)
    }

    fun decrementServings() {
        val curr = _uiState.value.currentServings
        if (curr > 0.5) {
            val prev = (curr - 1.0).coerceAtLeast(0.5)
            _uiState.value = _uiState.value.copy(currentServings = prev)
        }
    }

    fun setCustomServings(servings: Double) {
        if (servings > 0) {
            _uiState.value = _uiState.value.copy(currentServings = servings)
        }
    }

    fun toggleNotesExpanded() {
        _uiState.value = _uiState.value.copy(notesExpanded = !_uiState.value.notesExpanded)
    }

    fun toggleIngredientChecked(ingredientId: Long) {
        val current = _uiState.value.prepCheckedIngredients
        val next = if (current.contains(ingredientId)) current - ingredientId else current + ingredientId
        AppLogger.d("COOK", "toggleIngredientChecked: ingredientId=$ingredientId, isNowChecked=${!current.contains(ingredientId)}")
        _uiState.value = _uiState.value.copy(prepCheckedIngredients = next)
    }

    fun toggleStepIngredientChecked(stepNumber: Int, ingredientId: Long) {
        val tokenKey = "${stepNumber}_$ingredientId"
        val current = _uiState.value.stepCheckedTokens
        val next = if (current.contains(tokenKey)) current - tokenKey else current + tokenKey
        AppLogger.d("COOK", "toggleStepIngredientChecked: step=$stepNumber, ingredientId=$ingredientId, isNowChecked=${!current.contains(tokenKey)}")
        _uiState.value = _uiState.value.copy(stepCheckedTokens = next)
    }

    fun setActiveStepIndex(index: Int) {
        AppLogger.d("COOK", "setActiveStepIndex: recipeId=$recipeId, targetStep=$index")
        _uiState.value = _uiState.value.copy(activeStepIndex = index)
    }

    fun startOrPauseStepTimer(context: Context, stepNumber: Int, durationSeconds: Int) {
        val recipeTitle = _uiState.value.recipeWithDetails?.recipe?.title ?: AppLocaleManager.tr("Рецепт", "Recipe")
        val totalSteps = _uiState.value.recipeWithDetails?.steps?.size ?: 1
        AppLogger.d("TIMER", "startOrPauseStepTimer: recipeId=$recipeId, step=$stepNumber, duration=${durationSeconds}s")
        ActiveCookingManager.saveProgress(
            context = context,
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            activeStepIndex = _uiState.value.activeStepIndex,
            totalSteps = totalSteps,
            checkedIngredients = _uiState.value.prepCheckedIngredients,
            stepCheckedTokens = _uiState.value.stepCheckedTokens,
            currentServings = _uiState.value.currentServings
        )
        ActiveCookingManager.startOrPauseTimer(
            context = context,
            recipeId = recipeId,
            stepNumber = stepNumber,
            durationSeconds = durationSeconds,
            label = AppLocaleManager.tr("Шаг $stepNumber", "Step $stepNumber")
        )
    }

    fun resetStepTimer(context: Context, stepNumber: Int) {
        AppLogger.d("TIMER", "resetStepTimer: recipeId=$recipeId, step=$stepNumber")
        ActiveCookingManager.resetTimer(context, recipeId, stepNumber)
    }

    /** Добавляет время к таймеру шага без переключения его состояния (запущен/пауза). */
    fun addTimeToStepTimer(context: Context, stepNumber: Int, secondsToAdd: Int) {
        AppLogger.d("TIMER", "addTimeToStepTimer: recipeId=$recipeId, step=$stepNumber, +${secondsToAdd}s")
        ActiveCookingManager.addSecondsToTimer(context, recipeId, stepNumber, secondsToAdd)
    }

    fun onUnitSelectedForIngredient(ingredientId: Long, selectedUnit: String) {
        viewModelScope.launch {
            repository.updateIngredientSelectedUnit(ingredientId, recipeId, selectedUnit)
        }
    }

    fun showPostCookDialog() {
        _uiState.value = _uiState.value.copy(showPostCookDialog = true)
    }

    fun hidePostCookDialog() {
        _uiState.value = _uiState.value.copy(showPostCookDialog = false)
    }

    fun finishCooking(
        status: String,
        rating: Int,
        postCookNote: String,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            repository.updateRecipeStatusAndRating(recipeId, status, rating, postCookNote)
            _uiState.value = _uiState.value.copy(showPostCookDialog = false)
            onFinished()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJobs.values.forEach { it.cancel() }
    }

    // ---- ИИ-ассистент во время готовки (экспериментальная функция, агентный режим) ----

    fun askAssistant(context: Context, question: String) {
        val trimmed = question.trim()
        if (trimmed.isEmpty() || _uiState.value.isAssistantLoading) return

        // Автоматическое применение предложенных правок при разборе полётов по фразам-командам
        val lower = trimmed.lowercase()
        val isAffirmativeCommand = lower.contains("сделай это") || lower.contains("примени") ||
                lower.contains("подтверждаю") || lower.contains("применить") ||
                lower.contains("обнови") || lower == "да" || lower == "сделай"

        if (isAffirmativeCommand) {
            val messages = _uiState.value.assistantMessages
            val pendingIndex = messages.indexOfLast { msg ->
                msg.proposedUpdate?.patch != null && !msg.isApplied && !msg.isDismissed
            }
            if (pendingIndex != -1) {
                applyProposedUpdate(context, pendingIndex)
            }
        }

        _uiState.value = _uiState.value.copy(
            assistantMessages = trimAssistantMessages(_uiState.value.assistantMessages + AssistantMessage(true, trimmed)),
            isAssistantLoading = true,
            assistantError = null
        )

        viewModelScope.launch {
            try {
                val (canUse, reason) = com.aetnox.cooking.data.remote.AppStartupManager.canUseNetworkAi(context, isAssistant = true)
                if (!canUse) {
                    _uiState.value = _uiState.value.copy(
                        assistantMessages = trimAssistantMessages(_uiState.value.assistantMessages + AssistantMessage(false, reason)),
                        isAssistantLoading = false
                    )
                    return@launch
                }

                val details = _uiState.value.recipeWithDetails
                    ?: throw IllegalStateException(AppLocaleManager.tr("Рецепт ещё загружается", "Recipe is still loading"))
                val servings = _uiState.value.currentServings
                val stepIndex = _uiState.value.activeStepIndex
                val history = _uiState.value.assistantMessages
                    .filter { !it.text.startsWith("🔧") }
                    .takeLast(12)

                // 1. Попытка запроса к бэкенду (POST /assistant/step) с поддержкой многошагового агентного цикла
                val repo = BackendRepository(context.applicationContext)
                val recipeContextStr = "${AppLocaleManager.tr("Название:", "Title:")} ${details.recipe.title}. ${AppLocaleManager.tr("Текущий шаг $stepIndex из ${details.steps.size}.", "Current step $stepIndex of ${details.steps.size}.")}"
                val currentChatDtos = (history.map {
                    ChatMessageDto(
                        role = if (it.isUser) "user" else "assistant",
                        content = it.text
                    )
                } + ChatMessageDto("user", trimmed)).toMutableList()

                var backendIterations = 0
                var backendFinalAnswer: String? = null
                var backendNavigateTo: Int? = null
                var backendPendingTimer: TimerSuggestion? = null
                var backendProposedUpdate: com.aetnox.cooking.data.remote.model.ProposedUpdateDto? = null
                var backendSuccess = false

                while (backendIterations < CookAssistantTools.MAX_TOOL_ITERATIONS) {
                    val backendRes = repo.assistantStep(recipeContextStr, currentChatDtos, allowRecipeUpdates = true)
                    if (backendRes.isFailure) {
                        break
                    }
                    backendSuccess = true
                    val stepResp = backendRes.getOrThrow()
                    if (stepResp.proposedUpdate != null) {
                        backendProposedUpdate = stepResp.proposedUpdate
                    }

                    val call = CookAssistantTools.parseToolCall(stepResp.content)
                    if (call != null || stepResp.replyType == "tool_call") {
                        if (call != null) {
                            backendIterations++
                            val toolResult = CookAssistantTools.execute(call, details, servings, stepIndex)
                            toolResult.newStepIndex?.let { backendNavigateTo = it }
                            toolResult.timerSuggestionSeconds?.let { sec ->
                                backendPendingTimer = TimerSuggestion(sec, toolResult.timerSuggestionLabel ?: AppLocaleManager.tr("Таймер", "Timer"))
                            }

                            _uiState.value = _uiState.value.copy(
                                assistantMessages = trimAssistantMessages(
                                    _uiState.value.assistantMessages + AssistantMessage(false, "🔧 ${call.name}${if (call.arg.isNotBlank()) " (${call.arg})" else ""}")
                                )
                            )

                            currentChatDtos.add(ChatMessageDto("assistant", stepResp.content))
                            currentChatDtos.add(ChatMessageDto("user", "${AppLocaleManager.tr("Результат инструмента", "Tool result")} «${call.name}»:\n${toolResult.text}\n${AppLocaleManager.tr("Дай итоговый ответ повару.", "Give final answer to chef.")}"))
                        } else {
                            backendFinalAnswer = stepResp.content.ifBlank { stepResp.proposedUpdate?.summary ?: "" }
                            break
                        }
                    } else {
                        backendFinalAnswer = stepResp.content.ifBlank { stepResp.proposedUpdate?.summary ?: "" }
                        break
                    }
                }

                if (backendSuccess && backendFinalAnswer != null) {
                    backendNavigateTo?.let { setActiveStepIndex(it) }
                    _uiState.value = _uiState.value.copy(
                        assistantMessages = trimAssistantMessages(
                            _uiState.value.assistantMessages + AssistantMessage(
                                isUser = false,
                                text = backendFinalAnswer,
                                timerSuggestion = backendPendingTimer,
                                proposedUpdate = backendProposedUpdate
                            )
                        ),
                        isAssistantLoading = false
                    )
                    return@launch
                }

                // 2. Локальный агентный цикл с прямому Gemini API (fallback)
                var prompt = CookAssistantTools.buildInitialPrompt(details, servings, stepIndex, history, trimmed)
                var answer: String? = null
                var navigateTo: Int? = null
                var pendingTimerSuggestion: TimerSuggestion? = null
                var iterations = 0

                while (iterations < CookAssistantTools.MAX_TOOL_ITERATIONS) {
                    val reply = GeminiApiService.generateAssistantReply(context, prompt)
                    val call = CookAssistantTools.parseToolCall(reply)

                    if (call == null) {
                        answer = reply.trim()
                        break
                    }

                    iterations++
                    val result = CookAssistantTools.execute(call, details, servings, stepIndex)
                    result.newStepIndex?.let { navigateTo = it }
                    result.timerSuggestionSeconds?.let { sec ->
                        pendingTimerSuggestion = TimerSuggestion(
                            seconds = sec,
                            label = result.timerSuggestionLabel ?: AppLocaleManager.tr("Таймер", "Timer")
                        )
                    }

                    _uiState.value = _uiState.value.copy(
                        assistantMessages = trimAssistantMessages(
                            _uiState.value.assistantMessages + AssistantMessage(false, "🔧 ${call.name}${if (call.arg.isNotBlank()) " (${call.arg})" else ""}")
                        )
                    )

                    prompt = CookAssistantTools.appendToolResult(prompt, call, result)
                }

                navigateTo?.let { setActiveStepIndex(it) }

                _uiState.value = _uiState.value.copy(
                    assistantMessages = trimAssistantMessages(
                        _uiState.value.assistantMessages + AssistantMessage(
                            isUser = false,
                            text = answer ?: AppLocaleManager.tr("Не удалось собрать ответ. Попробуйте переформулировать вопрос.", "Failed to assemble answer. Try rephrasing question."),
                            timerSuggestion = pendingTimerSuggestion
                        )
                    ),
                    isAssistantLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAssistantLoading = false,
                    assistantError = com.aetnox.cooking.data.remote.NetworkDiagnostics.formatErrorMessage(context, e)
                )
            }
        }
    }

    fun startTimerFromSuggestion(context: Context, message: AssistantMessage) {
        val suggestion = message.timerSuggestion ?: return
        val targetStep = if (_uiState.value.activeStepIndex > 0) _uiState.value.activeStepIndex else 1

        // Если таймер на этом шаге уже существует — просто добавляем время, а не переключаем старт/паузу
        val existingTimer = ActiveCookingManager.getSessionForRecipe(recipeId)?.activeTimers?.get(targetStep)
        if (existingTimer != null) {
            ActiveCookingManager.addSecondsToTimer(context, recipeId, targetStep, suggestion.seconds)
        } else {
            startOrPauseStepTimer(context, targetStep, suggestion.seconds)
        }

        val updated = _uiState.value.assistantMessages.map { msg ->
            if (msg == message && msg.timerSuggestion != null) {
                msg.copy(timerSuggestion = msg.timerSuggestion.copy(isStarted = true))
            } else {
                msg
            }
        }
        _uiState.value = _uiState.value.copy(assistantMessages = updated)
    }

    fun applyProposedUpdate(context: Context, messageIndex: Int) {
        val messages = _uiState.value.assistantMessages
        if (messageIndex !in messages.indices) return
        val msg = messages[messageIndex]
        val patch = msg.proposedUpdate?.patch ?: return

        viewModelScope.launch {
            try {
                val updatedDetails = repository.applyProposedPatch(recipeId, patch)
                val updatedMessages = _uiState.value.assistantMessages.toMutableList()
                updatedMessages[messageIndex] = msg.copy(isApplied = true)

                _uiState.value = _uiState.value.copy(
                    recipeWithDetails = updatedDetails,
                    assistantMessages = updatedMessages
                )

                val cloudId = updatedDetails.recipe.cloudId
                if (!cloudId.isNullOrBlank()) {
                    runCatching {
                        val jsonStr = repository.exportSingleRecipeJson(recipeId)
                        val repo = BackendRepository(context.applicationContext)
                        repo.uploadRecipe(recipeId = cloudId, payloadJson = jsonStr, isPublic = true)
                    }
                }

                android.widget.Toast.makeText(context, AppLocaleManager.tr("✅ Рецепт успешно обновлен по рекомендации шефа!", "✅ Recipe successfully updated per chef's recommendation!"), android.widget.Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "${AppLocaleManager.tr("❌ Ошибка обновления:", "❌ Update error:")} ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    fun dismissProposedUpdate(messageIndex: Int) {
        val messages = _uiState.value.assistantMessages
        if (messageIndex !in messages.indices) return
        val msg = messages[messageIndex]
        val updatedMessages = _uiState.value.assistantMessages.toMutableList()
        updatedMessages[messageIndex] = msg.copy(isDismissed = true)
        _uiState.value = _uiState.value.copy(assistantMessages = updatedMessages)
    }

    class Factory(
        private val repository: RecipeRepository,
        private val recipeId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CookModeViewModel(repository, recipeId) as T
        }
    }

    companion object {
        /** Минимальная длительность экрана загрузки при открытии рецепта, мс */
        private const val MIN_LOADING_MS = 200L
        private const val MAX_ASSISTANT_MESSAGES = 12

        fun trimAssistantMessages(messages: List<AssistantMessage>): List<AssistantMessage> {
            return if (messages.size > MAX_ASSISTANT_MESSAGES) {
                messages.drop(messages.size - MAX_ASSISTANT_MESSAGES)
            } else {
                messages
            }
        }
    }
}
