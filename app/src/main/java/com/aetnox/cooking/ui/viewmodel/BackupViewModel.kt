package com.aetnox.cooking.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.remote.ApiException
import com.aetnox.cooking.data.remote.BackendRepository
import com.aetnox.cooking.data.remote.SessionManager
import com.aetnox.cooking.data.remote.UserSessionData
import com.aetnox.cooking.data.repository.RecipeRepository
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.domain.ai.GeminiApiService
import com.aetnox.cooking.domain.ai.GeminiSettingsManager
import com.aetnox.cooking.domain.net.DnsTester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val isTestingGemini: Boolean = false,
    val isFetchingModels: Boolean = false,
    val isTestingDns: Boolean = false,
    val dnsTestMessage: String? = null,
    val fetchedModels: List<GeminiSettingsManager.ModelOption>? = null,
    val fetchedModelsMessage: String? = null,
    val geminiTestMessage: String? = null,
    val message: String? = null,
    val error: String? = null,

    // Account & Subscription Status
    val userSession: UserSessionData = UserSessionData(),
    val isLoadingUserStatus: Boolean = false,
    val isActivatingKey: Boolean = false,
    val keyActivationMessage: String? = null,

    // Transfer Subscription
    val isCreatingTransfer: Boolean = false,
    val transferCode: String? = null,
    val transferExpiresIn: Int = 0,
    val isClaimingTransfer: Boolean = false,
    val transferMessage: String? = null,

    // Cloud Recipe Download & Lists
    val isDownloadingCloud: Boolean = false,
    val cloudDownloadMessage: String? = null,
    val myCloudRecipes: List<com.aetnox.cooking.data.remote.model.RecipeMeta> = emptyList(),
    val isLoadingMyCloudRecipes: Boolean = false,
    val myCloudRecipesMessage: String? = null,
    val publicCloudRecipes: List<com.aetnox.cooking.data.remote.model.RecipeMeta> = emptyList(),
    val isLoadingPublicCloudRecipes: Boolean = false,
    val isSearchingCloud: Boolean = false,
    val cloudSearchResults: List<com.aetnox.cooking.data.remote.model.RecipeMeta> = emptyList(),
    val cloudSearchMessage: String? = null,

    // Feedback & Account Operations
    val isSendingFeedback: Boolean = false,
    val feedbackStatusMessage: String? = null,
    val isLoggingOut: Boolean = false,

    // Cloud Backup (v2)
    val isUploadingCloudBackup: Boolean = false,
    val isRestoringCloudBackup: Boolean = false,
    val isLoadingCloudBackupStatus: Boolean = false,
    val cloudBackupStatusMessage: String? = null,
    val cloudBackupInfo: com.aetnox.cooking.data.remote.model.CloudBackupGetResponse? = null
)

class BackupViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun initSessionObserver(context: Context) {
        viewModelScope.launch {
            SessionManager.getSessionFlow(context).collect { session ->
                _uiState.value = _uiState.value.copy(userSession = session)
            }
        }
        refreshUserStatus(context)
        refreshCloudBackupStatus(context)
    }

    fun refreshUserStatus(context: Context, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingUserStatus = true)
            val repo = BackendRepository(context.applicationContext)
            repo.getUserStatus(forceRefresh = forceRefresh)
            _uiState.value = _uiState.value.copy(isLoadingUserStatus = false)
        }
    }

    fun activateKey(context: Context, key: String) {
        if (key.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActivatingKey = true, keyActivationMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.activateKey(key.trim())
            result.onSuccess { res ->
                repo.getUserStatus(forceRefresh = true)
                _uiState.value = _uiState.value.copy(
                    isActivatingKey = false,
                    keyActivationMessage = "✅ ${AppLocaleManager.tr("Ключ подписки успешно активирован!", "Subscription key successfully activated!")}"
                )
            }.onFailure { err ->
                val msg = if (err is ApiException) {
                    when (err.httpCode) {
                        400 -> "❌ ${AppLocaleManager.tr("Неверный формат ключа подписки", "Invalid license key format")}"
                        409 -> "❌ ${AppLocaleManager.tr("Данный ключ подписки уже был активирован ранее", "License key has already been activated")}"
                        else -> "❌ ${AppLocaleManager.tr("Ошибка активации:", "Activation error:")} ${err.message}"
                    }
                } else "❌ ${AppLocaleManager.tr("Ошибка сети:", "Network error:")} ${err.localizedMessage}"

                _uiState.value = _uiState.value.copy(
                    isActivatingKey = false,
                    keyActivationMessage = msg
                )
            }
        }
    }

    fun createTransferCode(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingTransfer = true, transferMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.createTransferCode()
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isCreatingTransfer = false,
                    transferCode = res.transferCode,
                    transferExpiresIn = res.expiresInSeconds,
                    transferMessage = "✅ ${AppLocaleManager.tr("Код переноса сгенерирован!", "Transfer code generated!")}"
                )
            }.onFailure { err ->
                val msg = if (err is ApiException) {
                    when (err.httpCode) {
                        403 -> "❌ ${AppLocaleManager.tr("Перенос недоступен на бесплатном тарифе (нужен Pro/Ultra) или исчерпан лимит переносов (макс 2).", "Transfer unavailable on Free plan (Pro/Ultra required) or limit reached (max 2).")}"
                        429 -> "❌ ${AppLocaleManager.tr("Заблокировано кулдауном. Попробуйте на следующей неделе.", "Cooldown active. Try next week.")}"
                        else -> "❌ ${AppLocaleManager.tr("Ошибка:", "Error:")} ${err.message}"
                    }
                } else "❌ ${AppLocaleManager.tr("Ошибка сети:", "Network error:")} ${err.localizedMessage}"

                _uiState.value = _uiState.value.copy(
                    isCreatingTransfer = false,
                    transferMessage = msg
                )
            }
        }
    }

    fun claimTransferCode(context: Context, code: String) {
        if (code.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isClaimingTransfer = true, transferMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.claimTransferCode(code.trim())
            result.onSuccess { res ->
                repo.getUserStatus(forceRefresh = true)
                _uiState.value = _uiState.value.copy(
                    isClaimingTransfer = false,
                    transferMessage = "🎉 ${AppLocaleManager.tr("Подписка успешно перенесена на это устройство!", "Subscription successfully transferred to this device!")}"
                )
            }.onFailure { err ->
                val msg = if (err is ApiException) {
                    when (err.httpCode) {
                        400 -> "❌ ${AppLocaleManager.tr("Неверный или просроченный код переноса", "Invalid or expired transfer code")}"
                        403 -> "❌ ${AppLocaleManager.tr("Исчерпан лимит переносов подписки (максимум 2 раза)", "Subscription transfer limit reached (max 2 times)")}"
                        429 -> "❌ ${AppLocaleManager.tr("Перенос заблокирован кулдауном (1 неделя)", "Transfer blocked by cooldown (1 week)")}"
                        else -> "❌ ${AppLocaleManager.tr("Ошибка:", "Error:")} ${err.message}"
                    }
                } else "❌ ${AppLocaleManager.tr("Ошибка сети:", "Network error:")} ${err.localizedMessage}"

                _uiState.value = _uiState.value.copy(
                    isClaimingTransfer = false,
                    transferMessage = msg
                )
            }
        }
    }

    fun downloadCloudRecipe(context: Context, recipeId: String, onSuccess: (Long) -> Unit) {
        if (recipeId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloadingCloud = true, cloudDownloadMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.getCloudRecipe(recipeId.trim())
            result.onSuccess { cloudResp ->
                try {
                    val localId = repository.importSingleRecipeJson(cloudResp.payloadJson)
                    _uiState.value = _uiState.value.copy(
                        isDownloadingCloud = false,
                        cloudDownloadMessage = "✅ ${AppLocaleManager.tr("Рецепт «${cloudResp.title}» успешно скачан из облака!", "Recipe \"${cloudResp.title}\" successfully downloaded from cloud!")}"
                    )
                    onSuccess(localId)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingCloud = false,
                        cloudDownloadMessage = "❌ ${AppLocaleManager.tr("Ошибка сохранения рецепта:", "Error saving recipe:")} ${e.localizedMessage}"
                    )
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isDownloadingCloud = false,
                    cloudDownloadMessage = "❌ ${AppLocaleManager.tr("Ошибка скачивания из облака:", "Cloud download error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun downloadRecipeByAccessCode(context: Context, accessCode: String, onSuccess: (Long) -> Unit) {
        if (accessCode.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDownloadingCloud = true, cloudDownloadMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.getRecipeByAccessCode(accessCode.trim())
            result.onSuccess { cloudResp ->
                try {
                    val localId = repository.importSingleRecipeJson(cloudResp.payloadJson)
                    _uiState.value = _uiState.value.copy(
                        isDownloadingCloud = false,
                        cloudDownloadMessage = "✅ ${AppLocaleManager.tr("Рецепт «${cloudResp.title}» успешно скачан по коду!", "Recipe \"${cloudResp.title}\" downloaded by code!")}"
                    )
                    onSuccess(localId)
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isDownloadingCloud = false,
                        cloudDownloadMessage = "❌ ${AppLocaleManager.tr("Ошибка сохранения рецепта:", "Error saving recipe:")} ${e.localizedMessage}"
                    )
                }
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isDownloadingCloud = false,
                    cloudDownloadMessage = "❌ ${err.localizedMessage}"
                )
            }
        }
    }

    fun searchCloudRecipes(context: Context, query: String) {
        if (query.trim().length < 2) {
            _uiState.value = _uiState.value.copy(cloudSearchMessage = AppLocaleManager.tr("Введите минимум 2 символа для поиска", "Enter at least 2 characters to search"))
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingCloud = true, cloudSearchMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.searchCloudRecipes(query.trim())
            result.onSuccess { res ->
                val msg = if (res.results.isEmpty()) AppLocaleManager.tr("Ничего не найдено по запросу «${query.trim()}»", "Nothing found for \"${query.trim()}\"") else null
                _uiState.value = _uiState.value.copy(
                    isSearchingCloud = false,
                    cloudSearchResults = res.results,
                    cloudSearchMessage = msg
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSearchingCloud = false,
                    cloudSearchMessage = "❌ ${AppLocaleManager.tr("Ошибка поиска:", "Search error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun loadMyCloudRecipes(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingMyCloudRecipes = true, myCloudRecipesMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.listMyRecipes()
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMyCloudRecipes = false,
                    myCloudRecipes = res.recipes
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoadingMyCloudRecipes = false,
                    myCloudRecipesMessage = "❌ ${AppLocaleManager.tr("Ошибка загрузки списка:", "List loading error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun deleteCloudRecipe(context: Context, recipeId: String) {
        viewModelScope.launch {
            val repo = BackendRepository(context.applicationContext)
            val result = repo.deleteCloudRecipe(recipeId)
            result.onSuccess {
                loadMyCloudRecipes(context)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    myCloudRecipesMessage = "❌ ${AppLocaleManager.tr("Не удалось удалить рецепт:", "Failed to delete recipe:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun loadPublicCloudRecipes(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPublicCloudRecipes = true)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.listPublicRecipes()
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isLoadingPublicCloudRecipes = false,
                    publicCloudRecipes = res.recipes
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoadingPublicCloudRecipes = false)
            }
        }
    }

    fun uploadCloudBackup(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingCloudBackup = true, cloudBackupStatusMessage = null)
            val result = repository.uploadCloudBackup(context)
            result.onSuccess { res ->
                _uiState.value = _uiState.value.copy(
                    isUploadingCloudBackup = false,
                    cloudBackupStatusMessage = "✅ ${AppLocaleManager.tr("Облачный бэкап выгружен! Сохранено рецептов:", "Cloud backup uploaded! Saved recipes:")} ${res.stored}"
                )
                refreshCloudBackupStatus(context)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isUploadingCloudBackup = false,
                    cloudBackupStatusMessage = "❌ ${AppLocaleManager.tr("Ошибка выгрузки:", "Upload error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun restoreCloudBackup(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoringCloudBackup = true, cloudBackupStatusMessage = null)
            val result = repository.restoreCloudBackup(context)
            result.onSuccess { count ->
                _uiState.value = _uiState.value.copy(
                    isRestoringCloudBackup = false,
                    cloudBackupStatusMessage = "🎉 ${AppLocaleManager.tr("База данных восстановлена из облака! Рецептов:", "Database restored from cloud! Recipes:")} $count"
                )
                onSuccess()
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isRestoringCloudBackup = false,
                    cloudBackupStatusMessage = "❌ ${AppLocaleManager.tr("Ошибка восстановления:", "Restore error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun refreshCloudBackupStatus(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingCloudBackupStatus = true)
            val result = repository.getCloudBackupStatus(context)
            result.onSuccess { info ->
                _uiState.value = _uiState.value.copy(
                    isLoadingCloudBackupStatus = false,
                    cloudBackupInfo = info
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoadingCloudBackupStatus = false)
            }
        }
    }

    fun sendFeedback(context: Context, messageText: String) {
        if (messageText.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingFeedback = true, feedbackStatusMessage = null)
            val repo = BackendRepository(context.applicationContext)
            val result = repo.sendFeedback(messageText.trim())
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSendingFeedback = false,
                    feedbackStatusMessage = "✅ ${AppLocaleManager.tr("Сообщение успешно отправлено! Спасибо за ваш отзыв.", "Message successfully sent! Thank you for your feedback.")}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isSendingFeedback = false,
                    feedbackStatusMessage = "❌ ${AppLocaleManager.tr("Не удалось отправить сообщение:", "Failed to send message:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun logoutSession(context: Context, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoggingOut = true)
            val repo = BackendRepository(context.applicationContext)
            repo.logout()
            repo.initAuth()
            _uiState.value = _uiState.value.copy(isLoggingOut = false)
            onLoggedOut()
        }
    }

    fun uploadRecipeToCloud(context: Context, recipeId: Long, isPublic: Boolean, onResult: (Result<String>) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonStr = repository.exportSingleRecipeJson(recipeId)
                val recipeWithDetails = repository.getRecipeByIdSync(recipeId)
                    ?: throw IllegalStateException(AppLocaleManager.tr("Рецепт не найден", "Recipe not found"))

                val cloudId = "cloud-${recipeId}-${System.currentTimeMillis()}"
                val repo = BackendRepository(context.applicationContext)

                val result = repo.uploadRecipe(
                    recipeId = cloudId,
                    payloadJson = jsonStr,
                    isPublic = isPublic
                )

                result.onSuccess { resp ->
                    repository.updateRecipeCloudInfo(recipeId, cloudId, resp.accessCode)
                    onResult(Result.success(cloudId))
                }.onFailure { err ->
                    onResult(Result.failure(err))
                }
            } catch (e: Exception) {
                onResult(Result.failure(e))
            }
        }
    }

    fun exportFullBackup(onJsonReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null, error = null)
            try {
                val jsonStr = repository.exportFullBackupJson()
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = AppLocaleManager.tr("Бэкап успешно создан!", "Backup successfully created!")
                )
                onJsonReady(jsonStr)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "${AppLocaleManager.tr("Ошибка при создании бэкапа:", "Error creating backup:")} ${e.localizedMessage}"
                )
            }
        }
    }

    fun importFullBackup(jsonStr: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null, error = null)
            try {
                repository.importFullBackupJson(jsonStr)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = AppLocaleManager.tr("База данных успешно восстановлена!", "Database successfully restored!")
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "${AppLocaleManager.tr("Ошибка при импорте базы данных:", "Error importing database:")} ${e.localizedMessage}"
                )
            }
        }
    }

    fun importSingleRecipe(jsonStr: String, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, message = null, error = null)
            try {
                val recipeId = repository.importSingleRecipeJson(jsonStr)
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    message = AppLocaleManager.tr("Рецепт успешно импортирован!", "Recipe successfully imported!")
                )
                onSuccess(recipeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "${AppLocaleManager.tr("Ошибка при импорте рецепта:", "Error importing recipe:")} ${e.localizedMessage}"
                )
            }
        }
    }

    fun addSampleDemoRecipe() {
        viewModelScope.launch {
            try {
                repository.addSampleDemoRecipe()
                _uiState.value = _uiState.value.copy(
                    message = AppLocaleManager.tr("Рецепт-образец «Шоколадный Брауни с вишней» добавлен!", "Sample recipe 'Chocolate Cherry Brownie' added!")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "${AppLocaleManager.tr("Не удалось добавить образец:", "Failed to add sample:")} ${e.localizedMessage}"
                )
            }
        }
    }

    fun testGeminiConnection(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingGemini = true, geminiTestMessage = null)
            val result = GeminiApiService.testConnection(context)
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    isTestingGemini = false,
                    geminiTestMessage = msg
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingGemini = false,
                    geminiTestMessage = "❌ ${AppLocaleManager.tr("Ошибка подключения:", "Connection error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun fetchModels(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingModels = true, fetchedModelsMessage = null)
            val result = GeminiApiService.fetchAvailableModels(context)
            result.onSuccess { models ->
                _uiState.value = _uiState.value.copy(
                    isFetchingModels = false,
                    fetchedModels = models,
                    fetchedModelsMessage = "✅ ${AppLocaleManager.tr("Успешно загружено моделей с сервера:", "Successfully loaded models from server:")} ${models.size}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isFetchingModels = false,
                    fetchedModelsMessage = "❌ ${AppLocaleManager.tr("Не удалось загрузить модели:", "Failed to load models:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun testDnsResolution(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTestingDns = true, dnsTestMessage = null)
            val result = DnsTester.testResolution(context)
            result.onSuccess { msg ->
                _uiState.value = _uiState.value.copy(
                    isTestingDns = false,
                    dnsTestMessage = msg
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isTestingDns = false,
                    dnsTestMessage = "❌ ${AppLocaleManager.tr("Ошибка DNS:", "DNS error:")} ${err.localizedMessage}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(message = null, error = null, geminiTestMessage = null)
    }

    class Factory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupViewModel(repository) as T
        }
    }
}
