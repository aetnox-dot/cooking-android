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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp

/**
 * Доступные темы оформления.
 */
enum class AppTheme(
    private val ruLabel: String,
    private val enLabel: String,
    private val ruDesc: String,
    private val enDesc: String,
    val alwaysDark: Boolean = false
) {
    SYSTEM("Системная (Material You)", "System (Material You)", "Подстраивается под цвета вашего телефона", "Adapts to your device colors"),
    NOTHING("Nothing (Dot Style)", "Nothing (Dot Style)", "Черный монохром #000000, NDot шрифт и акцентный красный #D71921", "Monochrome black #000000, NDot font and #D71921 red accent", alwaysDark = true),
    HERBAL("Травяная кухня", "Herbal Kitchen", "Изумруд, янтарь и терракота — фирменная палитра", "Emerald, amber and terracotta signature palette"),
    BAKERY("Светлая пекарня", "Light Bakery", "Сливки, карамель и корица", "Cream, caramel and cinnamon"),
    GARDEN("Утренний сад", "Morning Garden", "Свежая зелень и прохладная роса", "Fresh greenery and cool dew"),
    OCEAN("Морская глубина", "Deep Ocean", "Бирюза, глубокая вода и закатный коралл", "Turquoise, deep water and sunset coral"),
    NIGHT_CHEF("Ночной шеф", "Night Chef", "Уголь, тлеющий янтарь и сталь", "Charcoal, glowing amber and steel", alwaysDark = true),
    CHOCOLATE("Шоколад и карамель", "Chocolate & Caramel", "Тёмный какао с молочной пеной", "Dark cocoa with milk foam", alwaysDark = true),
    NEON("Неоновая кухня", "Neon Kitchen", "Кибер-кухня: мятный неон и маджента", "Cyber-kitchen: mint neon and magenta", alwaysDark = true),
    CUSTOM("✨ Кастомная тема", "✨ Custom Theme", "Собственные акцентные цвета оформления", "Custom accent theme colors");

    val label: String
        get() = com.aetnox.cooking.ui.util.AppLocaleManager.tr(ruLabel, enLabel)

    val description: String
        get() = com.aetnox.cooking.ui.util.AppLocaleManager.tr(ruDesc, enDesc)

    fun lightPalette(): ThemePalette = when (this) {
        NOTHING -> NothingLightPalette
        SYSTEM, HERBAL -> HerbalLightPalette
        BAKERY -> BakeryLightPalette
        GARDEN -> GardenLightPalette
        OCEAN -> OceanLightPalette
        NIGHT_CHEF -> NightChefPalette
        CHOCOLATE -> ChocolatePalette
        NEON -> NeonPalette
        CUSTOM -> buildCustomPalette(CustomThemeManager.customPrimary.value, CustomThemeManager.customSecondary.value, false)
    }

    fun darkPalette(): ThemePalette = when (this) {
        NOTHING -> NothingDarkPalette
        SYSTEM, HERBAL -> HerbalDarkPalette
        BAKERY -> BakeryDarkPalette
        GARDEN -> GardenDarkPalette
        OCEAN -> OceanDarkPalette
        NIGHT_CHEF -> NightChefPalette
        CHOCOLATE -> ChocolatePalette
        NEON -> NeonPalette
        CUSTOM -> buildCustomPalette(CustomThemeManager.customPrimary.value, CustomThemeManager.customSecondary.value, true)
    }
}

object CustomThemeManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_CUSTOM_PRIMARY = "custom_primary_color"
    private const val KEY_CUSTOM_SECONDARY = "custom_secondary_color"

    val customPrimary = mutableStateOf(Color(0xFF1B6B42))
    val customSecondary = mutableStateOf(Color(0xFFFFB800))

    val presetColors = listOf(
        Color(0xFF1B6B42), // Изумрудный
        Color(0xFF673AB7), // Пурпурный
        Color(0xFFE65100), // Апельсин
        Color(0xFF00838F), // Бирюзовый
        Color(0xFFC62828), // Рубиновый
        Color(0xFF1565C0), // Синий
        Color(0xFFD4AF37), // Золотой
        Color(0xFFC2185B)  // Розовый
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val pInt = runCatching {
            prefs.getInt(KEY_CUSTOM_PRIMARY, 0xFF1B6B42.toInt())
        }.getOrElse {
            runCatching { prefs.getLong(KEY_CUSTOM_PRIMARY, 0xFF1B6B42).toInt() }.getOrDefault(0xFF1B6B42.toInt())
        }

        val sInt = runCatching {
            prefs.getInt(KEY_CUSTOM_SECONDARY, 0xFFFFB800.toInt())
        }.getOrElse {
            runCatching { prefs.getLong(KEY_CUSTOM_SECONDARY, 0xFFFFB800).toInt() }.getOrDefault(0xFFFFB800.toInt())
        }

        customPrimary.value = Color(pInt)
        customSecondary.value = Color(sInt)
    }

    fun setPrimaryColor(context: Context, color: Color) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CUSTOM_PRIMARY, color.toArgb())
            .apply()
        customPrimary.value = color
    }

    fun setSecondaryColor(context: Context, color: Color) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_CUSTOM_SECONDARY, color.toArgb())
            .apply()
        customSecondary.value = color
    }
}

object ThemeManager {
    private const val PREFS_NAME = "cooking_prefs"
    private const val KEY_THEME = "app_theme"

    val currentTheme = mutableStateOf(AppTheme.SYSTEM)

    fun init(context: Context) {
        currentTheme.value = read(context)
        CustomThemeManager.init(context)
    }

    fun read(context: Context): AppTheme {
        val name = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME, null)
        val defaultTheme = if (com.aetnox.cooking.data.remote.DeviceUtils.isNothingDevice()) AppTheme.NOTHING else AppTheme.SYSTEM
        return name?.let { runCatching { AppTheme.valueOf(it) }.getOrNull() } ?: defaultTheme
    }

    fun setTheme(context: Context, theme: AppTheme) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
        currentTheme.value = theme

        if (theme == AppTheme.NOTHING) {
            com.aetnox.cooking.ui.util.AppIconManager.setAppIcon(context, com.aetnox.cooking.ui.util.AppIconVariant.NOTHING)
        }
    }
}

/** Три цвета-образца для превью в настройках */
fun themePreviewSwatches(theme: AppTheme): List<Color> = when (theme) {
    AppTheme.SYSTEM -> listOf(
        Color(0xFF1B6B42),
        Color(0xFF725C00),
        Color(0xFFFBFBF6)
    )
    AppTheme.NOTHING -> listOf(
        Color(0xFF000000),
        Color(0xFFD71921),
        Color(0xFF141414)
    )
    AppTheme.CUSTOM -> listOf(
        CustomThemeManager.customPrimary.value,
        CustomThemeManager.customSecondary.value,
        Color(0xFFFBFBFD)
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
        AppTheme.CUSTOM ->
            if (darkTheme) theme.darkPalette().toDarkScheme()
            else theme.lightPalette().toLightScheme()
        else ->
            if (darkTheme) theme.darkPalette().toDarkScheme()
            else theme.lightPalette().toLightScheme()
    }

    val typography = when (theme) {
        AppTheme.NOTHING -> TypographyNothing
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
