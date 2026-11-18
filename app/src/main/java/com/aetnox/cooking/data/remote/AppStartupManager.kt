package com.aetnox.cooking.data.remote

import android.content.Context
import com.aetnox.cooking.BuildConfig
import com.aetnox.cooking.data.remote.model.ApiErrorCodes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StartupState(
    val isChecking: Boolean = true,
    val isOnline: Boolean = true,
    val isMaintenance: Boolean = false,
    val maintenanceMessage: String = "На сервере идут плановые технические работы. Пожалуйста, попробуйте позже.",
    val isUpdateRequired: Boolean = false,
    val updateUrl: String = "",
    val minVersionCode: Int = 1,
    val isSessionReady: Boolean = false,
    val parseEnabled: Boolean = true,
    val assistantEnabled: Boolean = true,
    val aiSearchEnabled: Boolean = true,
    val feedbackEnabled: Boolean = true,
    val error: String? = null
)

object AppStartupManager {

    private val _startupState = MutableStateFlow(StartupState())
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    fun updateOnlineStatus(isOnline: Boolean) {
        _startupState.value = _startupState.value.copy(isOnline = isOnline)
    }

    /** Проверка перед вызовом ИИ: блокирует запрос до отправки на сервер при техработах, отключенной функции или отсутствии сети. */
    fun canUseNetworkAi(
        context: Context,
        isAssistant: Boolean = false,
        isSearch: Boolean = false
    ): Pair<Boolean, String> {
        val isOnlineNow = NetworkObserver.isOnline(context)
        updateOnlineStatus(isOnlineNow)

        if (!isOnlineNow) {
            return Pair(false, "📡 Нет подключения к сети. Проверьте Wi-Fi или мобильный интернет.")
        }
        if (_startupState.value.isMaintenance) {
            return Pair(false, "🛠 На сервере идут плановые технические работы. Все ИИ-функции временно заблокированы.")
        }
        if (isSearch && !_startupState.value.aiSearchEnabled) {
            return Pair(false, "🛠 ИИ-поиск временно отключен на сервере.")
        }
        if (isAssistant && !_startupState.value.assistantEnabled) {
            return Pair(false, "🛠 Кухонный ассистент временно отключен на сервере.")
        }
        if (!isAssistant && !isSearch && !_startupState.value.parseEnabled) {
            return Pair(false, "🛠 Оцифровка рецептов ИИ временно отключена на сервере.")
        }
        return Pair(true, "")
    }

    suspend fun checkAppLaunch(context: Context) {
        val isOnlineNow = NetworkObserver.isOnline(context)
        _startupState.value = _startupState.value.copy(isChecking = true, isOnline = isOnlineNow, error = null)

        if (!isOnlineNow) {
            _startupState.value = _startupState.value.copy(isChecking = false)
            return
        }

        val repo = BackendRepository(context.applicationContext)

        // 1. GET /app/config
        val configResult = repo.getAppConfig()
        configResult.onSuccess { config ->
            val parseEnabled = config.features?.parseEnabled ?: true
            val assistantEnabled = config.features?.assistantEnabled ?: true
            val aiSearchEnabled = config.features?.aiSearchEnabled ?: true
            val feedbackEnabled = config.features?.feedbackEnabled ?: true

            if (config.maintenanceActive) {
                _startupState.value = _startupState.value.copy(
                    isChecking = false,
                    isMaintenance = true,
                    parseEnabled = parseEnabled,
                    assistantEnabled = assistantEnabled,
                    aiSearchEnabled = aiSearchEnabled,
                    feedbackEnabled = feedbackEnabled
                )
                return
            }

            if (BuildConfig.VERSION_CODE < config.minVersionCode) {
                _startupState.value = _startupState.value.copy(
                    isChecking = false,
                    isUpdateRequired = true,
                    updateUrl = config.updateUrl,
                    minVersionCode = config.minVersionCode,
                    parseEnabled = parseEnabled,
                    assistantEnabled = assistantEnabled,
                    aiSearchEnabled = aiSearchEnabled,
                    feedbackEnabled = feedbackEnabled
                )
                return
            }

            _startupState.value = _startupState.value.copy(
                parseEnabled = parseEnabled,
                assistantEnabled = assistantEnabled,
                aiSearchEnabled = aiSearchEnabled,
                feedbackEnabled = feedbackEnabled
            )
        }.onFailure { err ->
            val formattedError = NetworkDiagnostics.formatErrorMessage(context, err)
            if (err is ApiException) {
                if (err.httpCode == 503 || err.errorCode == ApiErrorCodes.MAINTENANCE) {
                    _startupState.value = _startupState.value.copy(
                        isChecking = false,
                        isMaintenance = true,
                        maintenanceMessage = err.message ?: "Технические работы"
                    )
                    return
                }
                if (err.httpCode == 426 || err.errorCode == ApiErrorCodes.UPGRADE_REQUIRED) {
                    _startupState.value = _startupState.value.copy(
                        isChecking = false,
                        isUpdateRequired = true
                    )
                    return
                }
            }
            _startupState.value = _startupState.value.copy(
                error = formattedError
            )
        }

        // 2. Auth & Session Check
        val token = SessionManager.getToken(context)
        if (token == null) {
            repo.initAuth().onSuccess {
                repo.getUserStatus()
            }
        } else {
            repo.getUserStatus()
        }

        _startupState.value = _startupState.value.copy(
            isChecking = false,
            isSessionReady = true
        )
    }
}
