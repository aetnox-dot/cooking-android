package com.aetnox.cooking.ui.util

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

enum class AppLanguage(val code: String, val label: String) {
    RUSSIAN("ru", "🇷🇺 Русский"),
    ENGLISH("en", "🇬🇧 English")
}

object AppLocaleManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_LANGUAGE = "app_language"

    val currentLanguage = mutableStateOf(AppLanguage.RUSSIAN)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.RUSSIAN.code) ?: AppLanguage.RUSSIAN.code
        val lang = AppLanguage.entries.find { it.code == code } ?: AppLanguage.RUSSIAN
        currentLanguage.value = lang
        applyLocale(context, lang.code)
    }

    fun setLanguage(context: Context, language: AppLanguage) {
        if (currentLanguage.value == language) return

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()

        currentLanguage.value = language
        applyLocale(context, language.code)

        (context as? Activity)?.recreate()
    }

    fun applyLocale(context: Context, languageCode: String) {
        runCatching {
            val locale = Locale.forLanguageTag(languageCode)
            Locale.setDefault(locale)
            val config = context.resources.configuration
            config.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }

    val isEnglish: Boolean
        get() = currentLanguage.value == AppLanguage.ENGLISH

    fun tr(ru: String, en: String): String {
        return if (isEnglish) en else ru
    }

    /**
     * Плюрализация для русского (ru1, ru2, ru5) и английского (en1, enPlural).
     * Например: trPlural(count, "рецепт", "рецепта", "рецептов", "recipe", "recipes")
     */
    fun trPlural(
        count: Int,
        ru1: String,
        ru2: String,
        ru5: String,
        en1: String,
        enPlural: String
    ): String {
        val absCount = kotlin.math.abs(count)
        if (isEnglish) {
            return if (absCount == 1) en1 else enPlural
        }
        val mod100 = absCount % 100
        val mod10 = absCount % 10
        val ruText = when {
            mod100 in 11..19 -> ru5
            mod10 == 1 -> ru1
            mod10 in 2..4 -> ru2
            else -> ru5
        }
        return ruText
    }

    /**
     * Локализованное форматирование строки с подстановкой аргументов.
     */
    fun trFormat(ruFormat: String, enFormat: String, vararg args: Any?): String {
        val format = tr(ruFormat, enFormat)
        val locale = if (isEnglish) Locale.US else Locale.getDefault()
        return String.format(locale, format, *args)
    }
}
