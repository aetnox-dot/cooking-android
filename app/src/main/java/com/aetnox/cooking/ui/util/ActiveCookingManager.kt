package com.aetnox.cooking.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.aetnox.cooking.data.remote.BackendClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

@Serializable
data class ActiveTimerData(
    val stepNumber: Int,
    val totalSeconds: Int,
    val secondsRemaining: Int,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
    val label: String = "",
    // Epoch-мс последнего сохранённого тика; позволяет учесть реальное время,
    // прошедшее пока приложение было убито системой
    val lastTickEpochMs: Long = 0L
)

@Serializable
data class ActiveCookingSession(
    val recipeId: Long,
    val recipeTitle: String,
    val activeStepIndex: Int,
    val totalSteps: Int,
    val checkedIngredients: Set<Long> = emptySet(),
    val stepCheckedTokens: Set<String> = emptySet(),
    val currentServings: Double = 4.0,
    val lastUpdatedTimestamp: Long = 0L,
    val activeTimers: Map<Int, ActiveTimerData> = emptyMap()
)

object ActiveCookingManager {
    private const val PREFS_NAME = "active_cooking_prefs"
    private const val KEY_ACTIVE_SESSIONS_MAP = "active_sessions_map_json"
    private const val KEY_ACTIVE_SESSION_LEGACY = "active_session_json"

    val activeSessionsMap = mutableStateOf<Map<Long, ActiveCookingSession>>(emptyMap())

    val activeSession get() = activeSessionsMap.value.values.maxByOrNull { it.lastUpdatedTimestamp }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val timerJobs = mutableMapOf<String, Job>() // key = "${recipeId}_${stepNumber}"
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        TimerNotificationManager.createNotificationChannel(context.applicationContext)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val mapJson = prefs.getString(KEY_ACTIVE_SESSIONS_MAP, null)

        val loadedMap = if (!mapJson.isNullOrBlank()) {
            runCatching {
                val mapSerializer = MapSerializer(Long.serializer(), ActiveCookingSession.serializer())
                BackendClient.json.decodeFromString(mapSerializer, mapJson)
            }.getOrDefault(emptyMap())
        } else {
            val legacyJson = prefs.getString(KEY_ACTIVE_SESSION_LEGACY, null)
            val legacySess = legacyJson?.let {
                runCatching { BackendClient.json.decodeFromString(ActiveCookingSession.serializer(), it) }.getOrNull()
            }
            if (legacySess != null) mapOf(legacySess.recipeId to legacySess) else emptyMap()
        }

        val nowMs = System.currentTimeMillis()
        val expiredWhileAway = mutableListOf<Triple<Long, Int, String>>() // recipeId, step, label
        val correctedMap = loadedMap.mapValues { (_, session) ->
            val correctedTimers = session.activeTimers.mapValues { (_, timer) ->
                if (timer.isRunning && (timer.lastTickEpochMs > 0)) {
                    val elapsedSec = ((nowMs - timer.lastTickEpochMs) / 1000L).toInt()
                    if (elapsedSec > 0) {
                        val rem = timer.secondsRemaining - elapsedSec
                        if (rem <= 0) {
                            expiredWhileAway.add(Triple(session.recipeId, timer.stepNumber, timer.label))
                            timer.copy(secondsRemaining = 0, isRunning = false, isFinished = true, lastTickEpochMs = 0L)
                        } else {
                            timer.copy(secondsRemaining = rem, lastTickEpochMs = nowMs)
                        }
                    } else timer
                } else timer
            }
            session.copy(activeTimers = correctedTimers)
        }

        // Уведомляем о таймерах, которые истекли, пока приложение не работало
        expiredWhileAway.forEach { (expRecipeId, expStep, expLabel) ->
            val title = correctedMap[expRecipeId]?.recipeTitle ?: AppLocaleManager.tr("Рецепт", "Recipe")
            appContext?.let { ctx ->
                TimerNotificationManager.sendTimerCompletionNotification(
                    ctx, expRecipeId, title, expStep, expLabel
                )
            }
        }

        // При перезапуске приложения сессии без запущенных таймеров не сохраняются,
        // чтобы плашка «Продолжить готовку» автоматически снималась при холодном старте
        val activeSessionsWithRunningTimers = correctedMap.filterValues { session ->
            session.activeTimers.values.any { t -> t.isRunning && t.secondsRemaining > 0 }
        }

        activeSessionsMap.value = activeSessionsWithRunningTimers
        appContext?.let { persistSessionsMap(it, activeSessionsWithRunningTimers) }

        // Resume active running timers across sessions
        activeSessionsWithRunningTimers.values.forEach { session ->
            session.activeTimers.forEach { (stepNum, timer) ->
                if (timer.isRunning && timer.secondsRemaining > 0) {
                    launchTimerJob(session.recipeId, stepNum)
                }
            }
        }
    }

    fun getSessionForRecipe(recipeId: Long): ActiveCookingSession? {
        return activeSessionsMap.value[recipeId]
    }

    fun saveProgress(
        context: Context,
        recipeId: Long,
        recipeTitle: String,
        activeStepIndex: Int,
        totalSteps: Int,
        checkedIngredients: Set<Long>,
        stepCheckedTokens: Set<String>,
        currentServings: Double
    ) {
        appContext = context.applicationContext
        val currentSessions = activeSessionsMap.value.toMutableMap()
        val existingSess = currentSessions[recipeId]
        val currentTimers = existingSess?.activeTimers ?: emptyMap()

        val updatedSession = ActiveCookingSession(
            recipeId = recipeId,
            recipeTitle = recipeTitle,
            activeStepIndex = activeStepIndex,
            totalSteps = totalSteps,
            checkedIngredients = checkedIngredients,
            stepCheckedTokens = stepCheckedTokens,
            currentServings = currentServings,
            lastUpdatedTimestamp = System.currentTimeMillis(),
            activeTimers = currentTimers
        )

        currentSessions[recipeId] = updatedSession
        persistSessionsMap(context, currentSessions)
        AppLogger.d("COOK", "Saved cooking session progress for recipe '$recipeTitle' (id=$recipeId, step=$activeStepIndex)")
    }

    fun startOrPauseTimer(context: Context, recipeId: Long, stepNumber: Int, durationSeconds: Int, label: String = "") {
        appContext = context.applicationContext
        val currentSessions = activeSessionsMap.value.toMutableMap()
        val currentSess = currentSessions[recipeId] ?: return
        val currentTimers = currentSess.activeTimers.toMutableMap()

        val timer = currentTimers[stepNumber] ?: ActiveTimerData(
            stepNumber = stepNumber,
            totalSeconds = durationSeconds,
            secondsRemaining = durationSeconds,
            label = label
        )

        val jobKey = "${recipeId}_$stepNumber"

        if (timer.isRunning) {
            timerJobs[jobKey]?.cancel()
            currentTimers[stepNumber] = timer.copy(isRunning = false)
            currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
            persistSessionsMap(context, currentSessions)
            TimerNotificationManager.cancelNotification(context, recipeId, stepNumber)
            AppLogger.d("TIMER", "Timer paused for recipeId=$recipeId, step=$stepNumber")
        } else {
            val nextSecs = if (timer.isFinished || timer.secondsRemaining <= 0) timer.totalSeconds else timer.secondsRemaining
            currentTimers[stepNumber] = timer.copy(
                totalSeconds = durationSeconds,
                secondsRemaining = nextSecs,
                isRunning = true,
                isFinished = false,
                label = label,
                lastTickEpochMs = System.currentTimeMillis()
            )
            currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
            persistSessionsMap(context, currentSessions)
            launchTimerJob(recipeId, stepNumber)
            AppLogger.d("TIMER", "Timer started for recipeId=$recipeId, step=$stepNumber, duration=${durationSeconds}s")
        }
    }

    /** Добавляет время к существующему таймеру, не меняя его состояние (запущен/пауза). */
    fun addSecondsToTimer(context: Context, recipeId: Long, stepNumber: Int, secondsToAdd: Int) {
        if (secondsToAdd <= 0) return
        appContext = context.applicationContext
        val currentSessions = activeSessionsMap.value.toMutableMap()
        val currentSess = currentSessions[recipeId] ?: return
        val currentTimers = currentSess.activeTimers.toMutableMap()
        val timer = currentTimers[stepNumber] ?: return

        val wasFinished = timer.isFinished
        currentTimers[stepNumber] = timer.copy(
            totalSeconds = timer.totalSeconds + secondsToAdd,
            secondsRemaining = timer.secondsRemaining + secondsToAdd,
            isFinished = false,
            lastTickEpochMs = if (timer.isRunning) System.currentTimeMillis() else timer.lastTickEpochMs
        )
        currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
        persistSessionsMap(context, currentSessions)

        // Обновляем постоянное уведомление, если таймер идёт
        val updated = currentTimers[stepNumber]
        if (updated != null && updated.isRunning) {
            TimerNotificationManager.showOngoingTimerNotification(
                context,
                recipeId,
                currentSess.recipeTitle,
                stepNumber,
                updated.secondsRemaining,
                updated.label
            )
        }
        AppLogger.d("TIMER", "Added ${secondsToAdd}s to timer recipeId=$recipeId, step=$stepNumber (wasFinished=$wasFinished)")
    }

    fun resetTimer(context: Context, recipeId: Long, stepNumber: Int) {
        appContext = context.applicationContext
        val jobKey = "${recipeId}_${stepNumber}"
        timerJobs[jobKey]?.cancel()

        TimerNotificationManager.cancelNotification(context, recipeId, stepNumber)

        val currentSessions = activeSessionsMap.value.toMutableMap()
        val currentSess = currentSessions[recipeId] ?: return
        val currentTimers = currentSess.activeTimers.toMutableMap()
        val timer = currentTimers[stepNumber] ?: return

        currentTimers[stepNumber] = timer.copy(
            secondsRemaining = timer.totalSeconds,
            isRunning = false,
            isFinished = false,
            lastTickEpochMs = 0L
        )
        currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
        persistSessionsMap(context, currentSessions)
        AppLogger.d("TIMER", "Timer reset for recipeId=$recipeId, step=$stepNumber")
    }

    private fun launchTimerJob(recipeId: Long, stepNumber: Int) {
        val jobKey = "${recipeId}_${stepNumber}"
        timerJobs[jobKey]?.cancel()
        timerJobs[jobKey] = scope.launch {
            var ticksSincePersist = 0
            while (true) {
                delay(1000L)
                val currentSessions = activeSessionsMap.value.toMutableMap()
                val currentSess = currentSessions[recipeId] ?: break
                val currentTimers = currentSess.activeTimers.toMutableMap()
                val timer = currentTimers[stepNumber] ?: break
                if (!timer.isRunning) break

                val rem = timer.secondsRemaining - 1
                if (rem <= 0) {
                    AppLogger.i("TIMER", "Timer reached 0s for recipeId=$recipeId, step=$stepNumber. Posting notification & audio alert!")
                    currentTimers[stepNumber] = timer.copy(secondsRemaining = 0, isRunning = false, isFinished = true, lastTickEpochMs = 0L)
                    currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
                    activeSessionsMap.value = currentSessions
                    appContext?.let { ctx ->
                        persistSessionsMap(ctx, currentSessions)
                        TimerNotificationManager.sendTimerCompletionNotification(
                            ctx,
                            recipeId,
                            currentSess.recipeTitle,
                            stepNumber,
                            timer.label
                        )
                    }
                    break
                } else {
                    currentTimers[stepNumber] = timer.copy(secondsRemaining = rem, lastTickEpochMs = System.currentTimeMillis())
                    currentSessions[recipeId] = currentSess.copy(activeTimers = currentTimers)
                    activeSessionsMap.value = currentSessions
                    appContext?.let { ctx ->
                        TimerNotificationManager.showOngoingTimerNotification(
                            ctx,
                            recipeId,
                            currentSess.recipeTitle,
                            stepNumber,
                            rem,
                            timer.label
                        )
                        // Не дёргаем диск каждую секунду: персистим раз в 10 секунд
                        ticksSincePersist++
                        if (ticksSincePersist >= 10) {
                            ticksSincePersist = 0
                            persistSessionsMap(ctx, currentSessions)
                        }
                    }
                }
            }
        }
    }

    private fun persistSessionsMap(context: Context, map: Map<Long, ActiveCookingSession>) {
        val runningSessions = map.filterValues { session ->
            session.activeTimers.values.any { t -> t.isRunning && t.secondsRemaining > 0 }
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (runningSessions.isNotEmpty()) {
            val mapSerializer = MapSerializer(Long.serializer(), ActiveCookingSession.serializer())
            val jsonStr = BackendClient.json.encodeToString(mapSerializer, runningSessions)
            prefs.edit()
                .putString(KEY_ACTIVE_SESSIONS_MAP, jsonStr)
                .remove(KEY_ACTIVE_SESSION_LEGACY)
                .apply()
        } else {
            prefs.edit()
                .remove(KEY_ACTIVE_SESSIONS_MAP)
                .remove(KEY_ACTIVE_SESSION_LEGACY)
                .apply()
        }
        activeSessionsMap.value = map
    }

    fun clearSession(context: Context, recipeId: Long? = null) {
        val currentSessions = activeSessionsMap.value.toMutableMap()
        if (recipeId == null) {
            timerJobs.values.forEach { it.cancel() }
            timerJobs.clear()
            currentSessions.clear()
        } else {
            timerJobs.keys.filter { it.startsWith("${recipeId}_") }.forEach { key ->
                timerJobs[key]?.cancel()
                timerJobs.remove(key)
            }
            currentSessions.remove(recipeId)
        }
        persistSessionsMap(context, currentSessions)
        AppLogger.d("COOK", "Cleared active session for recipeId=$recipeId")
    }
}
