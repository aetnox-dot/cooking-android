package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.domain.ai.CookAssistantTools
import com.aetnox.cooking.domain.ai.GeminiApiService
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
        val regex = Regex("""(\d+(?:[\.,]\d+)?)\s*(мин|минут|минуты|сек|секунд|секунды|ч|час|часа|часов)""", RegexOption.IGNORE_CASE)
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
            String.format("%d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }
}

data class CookModeUiState(
    val recipeWithDetails: RecipeWithDetails? = null,
    val keepScreenOn: Boolean = true,
    val currentServings: Double = 4.0,
    val notesExpanded: Boolean = false,
    val checkedIngredients: Set<Long> = emptySet(),
    val stepTimers: Map<Int, StepTimerState> = emptyMap(),
    val activeStepIndex: Int = 0,
    val showPostCookDialog: Boolean = false,
    val isLoading: Boolean = true,
    val assistantMessages: List<AssistantMessage> = emptyList(),
    val isAssistantLoading: Boolean = false,
    val assistantError: String? = null
)

data class TimerSuggestion(
    val seconds: Int,
    val label: String,
    val isStarted: Boolean = false
)

data class AssistantMessage(
    val isUser: Boolean,
    val text: String,
    val timerSuggestion: TimerSuggestion? = null
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
    }

    private fun loadRecipe() {
        viewModelScope.launch {
            repository.getRecipeById(recipeId).collect { recipeDetails ->
                if (recipeDetails != null) {
                    val baseServings = recipeDetails.recipe.baseServings
                    val currServings = if (_uiState.value.isLoading) baseServings else _uiState.value.currentServings

                    _uiState.value = _uiState.value.copy(
                        recipeWithDetails = recipeDetails,
                        currentServings = currServings,
                        isLoading = false
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
        val current = _uiState.value.checkedIngredients
        val next = if (current.contains(ingredientId)) current - ingredientId else current + ingredientId
        _uiState.value = _uiState.value.copy(checkedIngredients = next)
    }

    fun setActiveStepIndex(index: Int) {
        _uiState.value = _uiState.value.copy(activeStepIndex = index)
    }

    fun startOrPauseStepTimer(stepNumber: Int, durationSeconds: Int) {
        val currentMap = _uiState.value.stepTimers.toMutableMap()
        val currentState = currentMap[stepNumber] ?: StepTimerState(
            stepNumber = stepNumber,
            totalSeconds = durationSeconds,
            secondsRemaining = durationSeconds
        )

        if (currentState.isRunning) {
            // Pause timer
            timerJobs[stepNumber]?.cancel()
            currentMap[stepNumber] = currentState.copy(isRunning = false)
            _uiState.value = _uiState.value.copy(stepTimers = currentMap)
        } else {
            // Start / Resume timer
            val stateToRun = if (currentState.isFinished || currentState.secondsRemaining <= 0) {
                currentState.copy(secondsRemaining = currentState.totalSeconds, isRunning = true, isFinished = false)
            } else {
                currentState.copy(isRunning = true, isFinished = false)
            }
            currentMap[stepNumber] = stateToRun
            _uiState.value = _uiState.value.copy(stepTimers = currentMap)

            timerJobs[stepNumber]?.cancel()
            timerJobs[stepNumber] = viewModelScope.launch {
                var remaining = stateToRun.secondsRemaining
                while (remaining > 0) {
                    delay(1000L)
                    remaining -= 1
                    val updated = _uiState.value.stepTimers.toMutableMap()
                    val timer = updated[stepNumber] ?: break
                    if (remaining <= 0) {
                        updated[stepNumber] = timer.copy(secondsRemaining = 0, isRunning = false, isFinished = true)
                        _uiState.value = _uiState.value.copy(stepTimers = updated)
                        break
                    } else {
                        updated[stepNumber] = timer.copy(secondsRemaining = remaining)
                        _uiState.value = _uiState.value.copy(stepTimers = updated)
                    }
                }
            }
        }
    }

    fun resetStepTimer(stepNumber: Int) {
        timerJobs[stepNumber]?.cancel()
        val currentMap = _uiState.value.stepTimers.toMutableMap()
        val timer = currentMap[stepNumber] ?: return
        currentMap[stepNumber] = timer.copy(
            secondsRemaining = timer.totalSeconds,
            isRunning = false,
            isFinished = false
        )
        _uiState.value = _uiState.value.copy(stepTimers = currentMap)
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

        _uiState.value = _uiState.value.copy(
            assistantMessages = _uiState.value.assistantMessages + AssistantMessage(true, trimmed),
            isAssistantLoading = true,
            assistantError = null
        )

        viewModelScope.launch {
            try {
                val details = _uiState.value.recipeWithDetails
                    ?: throw IllegalStateException("Рецепт ещё загружается")
                val servings = _uiState.value.currentServings
                val stepIndex = _uiState.value.activeStepIndex
                val history = _uiState.value.assistantMessages
                    .filter { !it.text.startsWith("🔧") }
                    .takeLast(4)

                // Агентный цикл: модель сама запрашивает данные через инструменты,
                // весь рецепт в промпт не передаётся
                var prompt = CookAssistantTools.buildInitialPrompt(details, servings, stepIndex, history, trimmed)
                var answer: String? = null
                var navigateTo: Int? = null
                var pendingTimerSuggestion: TimerSuggestion? = null
                var iterations = 0

                while (answer == null && iterations < CookAssistantTools.MAX_TOOL_ITERATIONS) {
                    val reply = GeminiApiService.generateAssistantReply(context, prompt)
                    val call = CookAssistantTools.parseToolCall(reply)

                    if (call == null) {
                        answer = reply.trim()
                        break
                    }

                    iterations++
                    val result = CookAssistantTools.execute(call, details, servings)
                    result.newStepIndex?.let { navigateTo = it }
                    result.timerSuggestionSeconds?.let { sec ->
                        pendingTimerSuggestion = TimerSuggestion(
                            seconds = sec,
                            label = result.timerSuggestionLabel ?: "Таймер"
                        )
                    }

                    // Показываем в чате, каким инструментом пользуется агент
                    _uiState.value = _uiState.value.copy(
                        assistantMessages = _uiState.value.assistantMessages +
                            AssistantMessage(false, "🔧 ${call.name}${if (call.arg.isNotBlank()) " (${call.arg})" else ""}")
                    )

                    prompt = CookAssistantTools.appendToolResult(prompt, call, result)
                }

                navigateTo?.let { setActiveStepIndex(it) }

                _uiState.value = _uiState.value.copy(
                    assistantMessages = _uiState.value.assistantMessages +
                        AssistantMessage(
                            isUser = false,
                            text = answer ?: "Не удалось собрать ответ. Попробуйте переформулировать вопрос.",
                            timerSuggestion = pendingTimerSuggestion
                        ),
                    isAssistantLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAssistantLoading = false,
                    assistantError = e.localizedMessage ?: "Ошибка ассистента"
                )
            }
        }
    }

    fun startTimerFromSuggestion(message: AssistantMessage) {
        val suggestion = message.timerSuggestion ?: return
        val targetStep = if (_uiState.value.activeStepIndex > 0) _uiState.value.activeStepIndex else 1
        startOrPauseStepTimer(targetStep, suggestion.seconds)

        val updated = _uiState.value.assistantMessages.map { msg ->
            if (msg == message && msg.timerSuggestion != null) {
                msg.copy(timerSuggestion = msg.timerSuggestion.copy(isStarted = true))
            } else {
                msg
            }
        }
        _uiState.value = _uiState.value.copy(assistantMessages = updated)
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
}
