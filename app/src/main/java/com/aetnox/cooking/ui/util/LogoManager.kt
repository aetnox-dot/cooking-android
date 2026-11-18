package com.aetnox.cooking.ui.util

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

enum class LogoStyle(
    private val ruLabel: String,
    private val enLabel: String
) {
    ORIGINAL("🎨 Полноцветный фирменный", "🎨 Full Color Brand"),
    MONOCHROME("✨ Монохромный", "✨ Monochrome"),
    SURFACE_BADGE("🛡️ В круглом бейдже", "🛡️ Circular Badge");

    val label: String
        get() = AppLocaleManager.tr(ruLabel, enLabel)
}

enum class LogoColorOption(
    private val ruLabel: String,
    private val enLabel: String,
    val color: Color?
) {
    THEME_PRIMARY("Цвет темы", "Theme Color", null),
    GOLD("Янтарь / Золото", "Amber / Gold", Color(0xFFFFB800)),
    CORAL("Закатный коралл", "Sunset Coral", Color(0xFFFF5252)),
    MINT("Мятный изумруд", "Mint Emerald", Color(0xFF00E676)),
    VIOLET("Неоновый фиолетовый", "Neon Violet", Color(0xFFB388FF)),
    ORANGE("Сочный апельсин", "Juicy Orange", Color(0xFFFF9100));

    val label: String
        get() = AppLocaleManager.tr(ruLabel, enLabel)
}

object LogoManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_STYLE = "logo_style"
    private const val KEY_COLOR = "logo_color"

    val currentStyle = mutableStateOf(LogoStyle.ORIGINAL)
    val currentColorOption = mutableStateOf(LogoColorOption.THEME_PRIMARY)

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val styleName = prefs.getString(KEY_STYLE, LogoStyle.ORIGINAL.name)
        currentStyle.value = runCatching { LogoStyle.valueOf(styleName!!) }.getOrDefault(LogoStyle.ORIGINAL)

        val colName = prefs.getString(KEY_COLOR, LogoColorOption.THEME_PRIMARY.name)
        currentColorOption.value = runCatching { LogoColorOption.valueOf(colName!!) }.getOrDefault(LogoColorOption.THEME_PRIMARY)
    }

    fun setStyle(context: Context, style: LogoStyle) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_STYLE, style.name).apply()
        currentStyle.value = style
    }

    fun setColorOption(context: Context, option: LogoColorOption) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_COLOR, option.name).apply()
        currentColorOption.value = option
    }
}
