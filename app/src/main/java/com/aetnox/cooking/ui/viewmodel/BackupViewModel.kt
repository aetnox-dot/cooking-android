package com.aetnox.cooking.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aetnox.cooking.data.repository.RecipeRepository
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
    val error: String? = null
)

class BackupViewModel(private val repository: RecipeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportFullBackup(onJsonReady: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, message = null, error = null)
            try {
                val jsonStr = repository.exportFullBackupJson()
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    message = "Бэкап успешно создан!"
                )
                onJsonReady(jsonStr)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Ошибка при создании бэкапа: ${e.localizedMessage}"
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
                    message = "База данных успешно восстановлена!"
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Ошибка при импорте базы данных: ${e.localizedMessage}"
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
                    message = "Рецепт успешно импортирован!"
                )
                onSuccess(recipeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Ошибка при импорте рецепта: ${e.localizedMessage}"
                )
            }
        }
    }

    fun addSampleDemoRecipe() {
        viewModelScope.launch {
            try {
                repository.addSampleDemoRecipe()
                _uiState.value = _uiState.value.copy(
                    message = "Рецепт-образец «Шоколадный Брауни с вишней» добавлен!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Не удалось добавить образец: ${e.localizedMessage}"
                )
            }
        }
    }

    fun testGeminiConnection(context: android.content.Context) {
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
                    geminiTestMessage = "❌ Ошибка подключения: ${err.localizedMessage}"
                )
            }
        }
    }

    fun fetchModels(context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFetchingModels = true, fetchedModelsMessage = null)
            val result = GeminiApiService.fetchAvailableModels(context)
            result.onSuccess { models ->
                _uiState.value = _uiState.value.copy(
                    isFetchingModels = false,
                    fetchedModels = models,
                    fetchedModelsMessage = "✅ Успешно загружено моделей с сервера: ${models.size}"
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isFetchingModels = false,
                    fetchedModelsMessage = "❌ Не удалось загрузить модели: ${err.localizedMessage}"
                )
            }
        }
    }

    fun testDnsResolution(context: android.content.Context) {
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
                    dnsTestMessage = "❌ Ошибка DNS: ${err.localizedMessage}"
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
