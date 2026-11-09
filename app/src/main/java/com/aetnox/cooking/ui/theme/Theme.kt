package com.aetnox.cooking.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

/**
 * Доступные темы оформления. Системная — дефолт и ведёт себя как раньше
 * (Material You / запасная статичная палитра на Android < 12).
 */
enum class AppTheme(
    val label: String,
    val description: String,
    val alwaysDark: Boolean = false
) {
    SYSTEM("Системная (Material You)", "Подстраивается под цвета вашего телефона"),
    HERBAL("Травяная кухня", "Изумруд, янтарь и терракота — фирменная палитра"),
    BAKERY("Светлая пекарня", "Сливки, карамель и корица"),
    GARDEN("Утренний сад", "Свежая зелень и прохладная роса"),
    OCEAN("Морская глубина", "Бирюза, глубокая вода и закатный коралл"),
    NIGHT_CHEF("Ночной шеф", "Уголь, тлеющий янтарь и сталь", alwaysDark = true),
    CHOCOLATE("Шоколад и карамель", "Тёмный какао с молочной пеной", alwaysDark = true),
    NEON("Неоновая кухня", "Кибер-кухня: мятный неон и маджента", alwaysDark = true);

    fun lightPalette(): ThemePalette = when (this) {
        SYSTEM, HERBAL -> HerbalLightPalette
        BAKERY -> BakeryLightPalette
        GARDEN -> GardenLightPalette
        OCEAN -> OceanLightPalette
        NIGHT_CHEF -> NightChefPalette
        CHOCOLATE -> ChocolatePalette
        NEON -> NeonPalette
    }

    fun darkPalette(): ThemePalette = when (this) {
        SYSTEM, HERBAL -> HerbalDarkPalette
        BAKERY -> BakeryDarkPalette
        GARDEN -> GardenDarkPalette
        OCEAN -> OceanDarkPalette
        NIGHT_CHEF -> NightChefPalette
        CHOCOLATE -> ChocolatePalette
        NEON -> NeonPalette
    }
}

object ThemeManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_THEME = "app_theme"

    /** Snapshot-state: читается в CookingTheme, поэтому смена темы применяется мгновенно */
    val currentTheme = mutableStateOf(AppTheme.SYSTEM)

    fun init(context: Context) {
        currentTheme.value = read(context)
    }

    fun read(context: Context): AppTheme {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, null)
        return name?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: AppTheme.SYSTEM
    }

    fun setTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
        currentTheme.value = theme
    }
}

/** Три цвета-образца для превью в настройках */
fun themePreviewSwatches(theme: AppTheme): List<Color> = when (theme) {
    AppTheme.SYSTEM -> listOf(
        Color(0xFF1B6B42),
        Color(0xFF725C00),
        Color(0xFFFBFBF6)
    )
    else -> listOf(
        theme.lightPalette().primary,
        theme.lightPalette().secondary,
        theme.lightPalette().surface
    )
}

// Тёплые «дневные» темы дышат шире — чуть разреженный набор
private val TypographyPlayful = Typography.copy(
    displayLarge = Typography.displayLarge.copy(letterSpacing = 0.5.sp),
    displayMedium = Typography.displayMedium.copy(letterSpacing = 0.4.sp),
    headlineMedium = Typography.headlineMedium.copy(letterSpacing = 0.3.sp),
    titleMedium = Typography.titleMedium.copy(letterSpacing = 0.4.sp),
    bodyLarge = Typography.bodyLarge.copy(letterSpacing = 0.6.sp),
    bodyMedium = Typography.bodyMedium.copy(letterSpacing = 0.4.sp)
)

// Тёмные «вечерние» темы — плотнее и технoчнее: разреженные подписи, плотные заголовки
private val TypographyNight = Typography.copy(
    labelLarge = Typography.labelLarge.copy(letterSpacing = 1.0.sp),
    labelMedium = Typography.labelMedium.copy(letterSpacing = 0.8.sp),
    labelSmall = Typography.labelSmall.copy(letterSpacing = 0.7.sp),
    titleMedium = Typography.titleMedium.copy(letterSpacing = 0.5.sp),
    displayMedium = Typography.displayMedium.copy(letterSpacing = (-0.2).sp),
    headlineMedium = Typography.headlineMedium.copy(letterSpacing = (-0.1).sp)
)

@Composable
fun CookingTheme(content: @Composable () -> Unit) {
    val theme = ThemeManager.currentTheme.value
    val systemDark = isSystemInDarkTheme()
    val darkTheme = theme.alwaysDark || systemDark

    val context = LocalContext.current
    val colorScheme = when (theme) {
        AppTheme.SYSTEM -> when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            darkTheme -> HerbalDarkPalette.toDarkScheme()
            else -> HerbalLightPalette.toLightScheme()
        }
        else ->
            if (darkTheme) theme.darkPalette().toDarkScheme()
            else theme.lightPalette().toLightScheme()
    }

    val typography = when (theme) {
        AppTheme.BAKERY, AppTheme.GARDEN -> TypographyPlayful
        AppTheme.NIGHT_CHEF, AppTheme.CHOCOLATE, AppTheme.NEON -> TypographyNight
        else -> Typography
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
