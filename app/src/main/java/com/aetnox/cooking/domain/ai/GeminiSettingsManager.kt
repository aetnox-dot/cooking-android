package com.aetnox.cooking.domain.ai

import android.content.Context
import android.content.SharedPreferences

data class GeminiSettings(
    val apiKey: String,
    val model: String,
    val customBaseUrl: String
)

object GeminiSettingsManager {
    private const val PREFS_NAME = "cooking_prefs"

    private const val KEY_API_KEY = "gemini_api_key"
    private const val KEY_MODEL = "gemini_model"
    private const val KEY_CUSTOM_BASE_URL = "gemini_custom_base_url"

    const val DEFAULT_API_KEY = "AQ.Ab8RN6Ld9e655Prlb6VLjSA4bTuuFnT6UNMwRsWbZc1ZuRziVg"
    const val DEFAULT_MODEL = "gemini-3.8-flash"
    const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com"

    val AVAILABLE_MODELS = listOf(
        ModelOption(
            id = "gemini-3.8-flash",
            displayName = "🌟 Шеф-Флеш 3.8 (Рекомендуемый)",
            description = "Быстрый, супер-точный, с идеальной группировкой продуктов по секциям"
        ),
        ModelOption(
            id = "gemini-3.7-flash",
            displayName = "⚡ Шеф-Флеш 3.7",
            description = "Модель с глубокой детальной проработкой сложных кулинарных рецептов"
        ),
        ModelOption(
            id = "gemini-3.5-flash",
            displayName = "🍃 Шеф-Флеш 3.5",
            description = "Базовый сбалансированный ассистент"
        ),
        ModelOption(
            id = "gemini-3.1-flash",
            displayName = "🚀 Шеф-Флеш 3.1",
            description = "Компактная быстрая модель"
        ),
        ModelOption(
            id = "gemini-3.3-flash",
            displayName = "🛡 Шеф-Флеш 3.3",
            description = "Стабильная версия модели"
        )
    )

    data class ModelOption(
        val id: String,
        val displayName: String,
        val description: String
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSettings(context: Context): GeminiSettings {
        val prefs = getPrefs(context)
        val rawModel = prefs.getString(KEY_MODEL, null)?.ifBlank { null } ?: DEFAULT_MODEL

        // Auto-migrate old deprecated model names to gemini-3.8-flash
        val isDeprecated = rawModel.contains("2.0") ||
                rawModel.contains("1.5") ||
                rawModel == "gemini-2.0-flash" ||
                rawModel == "gemini-1.5-flash" ||
                rawModel == "gemini-2.0-flash-lite"

        val finalModel = if (isDeprecated) {
            DEFAULT_MODEL
        } else {
            rawModel
        }

        if (finalModel != rawModel) {
            prefs.edit().putString(KEY_MODEL, finalModel).apply()
        }

        return GeminiSettings(
            apiKey = prefs.getString(KEY_API_KEY, null)?.ifBlank { null } ?: DEFAULT_API_KEY,
            model = finalModel,
            customBaseUrl = prefs.getString(KEY_CUSTOM_BASE_URL, null)?.ifBlank { null } ?: DEFAULT_BASE_URL
        )
    }

    fun saveSettings(
        context: Context,
        apiKey: String,
        model: String,
        customBaseUrl: String = DEFAULT_BASE_URL
    ) {
        val trimmedUrl = customBaseUrl.trim()
        val uri = android.net.Uri.parse(trimmedUrl)
        val validUrl = if (trimmedUrl.isNotBlank() &&
            (uri.scheme == "https" || uri.scheme == "http") &&
            !uri.host.isNullOrBlank()
        ) {
            trimmedUrl
        } else {
            DEFAULT_BASE_URL
        }

        getPrefs(context).edit()
            .putString(KEY_API_KEY, apiKey.trim())
            .putString(KEY_MODEL, model.trim())
            .putString(KEY_CUSTOM_BASE_URL, validUrl)
            .apply()
    }
}
