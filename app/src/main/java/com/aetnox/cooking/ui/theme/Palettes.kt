package com.aetnox.cooking.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Полная палитра темы. Пары «светлая/тёмная» собираются в ColorScheme ниже.
 */
data class ThemePalette(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val surfaceContainerLow: Color,
    val surfaceContainerHigh: Color
) {
    fun toLightScheme() = lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerHigh = surfaceContainerHigh
    )

    fun toDarkScheme() = darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant,
        surfaceContainerLow = surfaceContainerLow,
        surfaceContainerHigh = surfaceContainerHigh
    )
}

// --- Травяная кухня (прежняя статичная палитра приложения) ---
val HerbalLightPalette = ThemePalette(
    primary = Color(0xFF1B6B42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD3F3DC),
    onPrimaryContainer = Color(0xFF002110),
    secondary = Color(0xFF725C00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFE07D),
    onSecondaryContainer = Color(0xFF231B00),
    tertiary = Color(0xFF8B4A2B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF380D00),
    surface = Color(0xFFFBFBF6),
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = Color(0xFFDFE4DD),
    onSurfaceVariant = Color(0xFF424943),
    outline = Color(0xFF737973),
    outlineVariant = Color(0xFFC2C8C2),
    surfaceContainerLow = Color(0xFFF5F5EF),
    surfaceContainerHigh = Color(0xFFEAEAE4)
)

val HerbalDarkPalette = ThemePalette(
    primary = Color(0xFF8AD7A2),
    onPrimary = Color(0xFF00391E),
    primaryContainer = Color(0xFF00522E),
    onPrimaryContainer = Color(0xFFA5F4BD),
    secondary = Color(0xFFE8C349),
    onSecondary = Color(0xFF3C2F00),
    secondaryContainer = Color(0xFF564500),
    onSecondaryContainer = Color(0xFFFFE07D),
    tertiary = Color(0xFFFFB59B),
    onTertiary = Color(0xFF531E03),
    tertiaryContainer = Color(0xFF703416),
    onTertiaryContainer = Color(0xFFFFDBCF),
    surface = Color(0xFF121413),
    onSurface = Color(0xFFE2E3DE),
    surfaceVariant = Color(0xFF424943),
    onSurfaceVariant = Color(0xFFC2C8C2),
    outline = Color(0xFF8C938D),
    outlineVariant = Color(0xFF424943),
    surfaceContainerLow = Color(0xFF1A1C1B),
    surfaceContainerHigh = Color(0xFF282B29)
)

// --- Светлая пекарня: сливки, карамель, корица ---
val BakeryLightPalette = ThemePalette(
    primary = Color(0xFF8B5000),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDCC2),
    onPrimaryContainer = Color(0xFF2E1500),
    secondary = Color(0xFF7A5900),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDE9E),
    onSecondaryContainer = Color(0xFF271800),
    tertiary = Color(0xFF8C4A2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDBCF),
    onTertiaryContainer = Color(0xFF380D00),
    surface = Color(0xFFFFF8F3),
    onSurface = Color(0xFF221A11),
    surfaceVariant = Color(0xFFF3DED3),
    onSurfaceVariant = Color(0xFF52443B),
    outline = Color(0xFF85746A),
    outlineVariant = Color(0xFFD6C3B8),
    surfaceContainerLow = Color(0xFFFFF1E8),
    surfaceContainerHigh = Color(0xFFEADFD6)
)

val BakeryDarkPalette = ThemePalette(
    primary = Color(0xFFFFB77C),
    onPrimary = Color(0xFF4A2800),
    primaryContainer = Color(0xFF6B3C00),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFFF2C14E),
    onSecondary = Color(0xFF3C2F00),
    secondaryContainer = Color(0xFF5C4300),
    onSecondaryContainer = Color(0xFFFFDE9E),
    tertiary = Color(0xFFFFB59B),
    onTertiary = Color(0xFF531E03),
    tertiaryContainer = Color(0xFF723524),
    onTertiaryContainer = Color(0xFFFFDBCF),
    surface = Color(0xFF191210),
    onSurface = Color(0xFFEDE0D8),
    surfaceVariant = Color(0xFF52443B),
    onSurfaceVariant = Color(0xFFD6C3B8),
    outline = Color(0xFF9E8D82),
    outlineVariant = Color(0xFF52443B),
    surfaceContainerLow = Color(0xFF211916),
    surfaceContainerHigh = Color(0xFF2E2320)
)

// --- Утренний сад: листья, роса, оливки ---
val GardenLightPalette = ThemePalette(
    primary = Color(0xFF3E6A35),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBFF0A8),
    onPrimaryContainer = Color(0xFF0A2900),
    secondary = Color(0xFF56624B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD9E7CA),
    onSecondaryContainer = Color(0xFF141E0D),
    tertiary = Color(0xFF386666),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBBEBEB),
    onTertiaryContainer = Color(0xFF002020),
    surface = Color(0xFFF8FBF1),
    onSurface = Color(0xFF191D17),
    surfaceVariant = Color(0xFFDDE5D3),
    onSurfaceVariant = Color(0xFF41493F),
    outline = Color(0xFF71796D),
    outlineVariant = Color(0xFFC1C9B8),
    surfaceContainerLow = Color(0xFFEFF3E4),
    surfaceContainerHigh = Color(0xFFE3E8D8)
)

val GardenDarkPalette = ThemePalette(
    primary = Color(0xFFA4D58C),
    onPrimary = Color(0xFF0F3D00),
    primaryContainer = Color(0xFF27501F),
    onPrimaryContainer = Color(0xFFBFF0A8),
    secondary = Color(0xFFBDCBB0),
    onSecondary = Color(0xFF293423),
    secondaryContainer = Color(0xFF3E4A37),
    onSecondaryContainer = Color(0xFFD9E7CA),
    tertiary = Color(0xFFA0CFCF),
    onTertiary = Color(0xFF003737),
    tertiaryContainer = Color(0xFF1E4E4E),
    onTertiaryContainer = Color(0xFFBBEBEB),
    surface = Color(0xFF111410),
    onSurface = Color(0xFFE0E4DB),
    surfaceVariant = Color(0xFF41493F),
    onSurfaceVariant = Color(0xFFC1C9B8),
    outline = Color(0xFF8B9387),
    outlineVariant = Color(0xFF41493F),
    surfaceContainerLow = Color(0xFF191D17),
    surfaceContainerHigh = Color(0xFF262B23)
)

// --- Морская глубина: бирюза, вода, закатный коралл ---
val OceanLightPalette = ThemePalette(
    primary = Color(0xFF00687A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFABEDFF),
    onPrimaryContainer = Color(0xFF001F27),
    secondary = Color(0xFF4B6269),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCEE7EF),
    onSecondaryContainer = Color(0xFF061E25),
    tertiary = Color(0xFF815365),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF2F1122),
    surface = Color(0xFFF5FAFC),
    onSurface = Color(0xFF171C1E),
    surfaceVariant = Color(0xFFDBE4E8),
    onSurfaceVariant = Color(0xFF3F484B),
    outline = Color(0xFF6F797C),
    outlineVariant = Color(0xFFBFC8CC),
    surfaceContainerLow = Color(0xFFEFF4F6),
    surfaceContainerHigh = Color(0xFFE3E9EB)
)

val OceanDarkPalette = ThemePalette(
    primary = Color(0xFF54D6F3),
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFFABEDFF),
    secondary = Color(0xFFB2CBD3),
    onSecondary = Color(0xFF1C343A),
    secondaryContainer = Color(0xFF334A51),
    onSecondaryContainer = Color(0xFFCEE7EF),
    tertiary = Color(0xFFEEB8CC),
    onTertiary = Color(0xFF492534),
    tertiaryContainer = Color(0xFF633B4D),
    onTertiaryContainer = Color(0xFFFFD8E4),
    surface = Color(0xFF0F1416),
    onSurface = Color(0xFFDEE3E6),
    surfaceVariant = Color(0xFF3F484B),
    onSurfaceVariant = Color(0xFFBFC8CC),
    outline = Color(0xFF899295),
    outlineVariant = Color(0xFF3F484B),
    surfaceContainerLow = Color(0xFF171C1E),
    surfaceContainerHigh = Color(0xFF242A2C)
)

// --- Ночной шеф: уголь, тлеющий янтарь, сталь ---
val NightChefPalette = ThemePalette(
    primary = Color(0xFFFFB871),
    onPrimary = Color(0xFF472A00),
    primaryContainer = Color(0xFF663D00),
    onPrimaryContainer = Color(0xFFFFDCC0),
    secondary = Color(0xFFBAC7C9),
    onSecondary = Color(0xFF263234),
    secondaryContainer = Color(0xFF3A484A),
    onSecondaryContainer = Color(0xFFD5E3E5),
    tertiary = Color(0xFFFFB4A9),
    onTertiary = Color(0xFF5C1712),
    tertiaryContainer = Color(0xFF73342D),
    onTertiaryContainer = Color(0xFFFFDAD5),
    surface = Color(0xFF141211),
    onSurface = Color(0xFFE8E1DD),
    surfaceVariant = Color(0xFF4A4644),
    onSurfaceVariant = Color(0xFFCBC5C2),
    outline = Color(0xFF948F8C),
    outlineVariant = Color(0xFF4A4644),
    surfaceContainerLow = Color(0xFF1C1A19),
    surfaceContainerHigh = Color(0xFF2A2725)
)

// --- Шоколад и карамель: тёмный какао ---
val ChocolatePalette = ThemePalette(
    primary = Color(0xFFEFBD94),
    onPrimary = Color(0xFF48290B),
    primaryContainer = Color(0xFF623B1A),
    onPrimaryContainer = Color(0xFFFFDCC4),
    secondary = Color(0xFFD8C2AE),
    onSecondary = Color(0xFF3A2D20),
    secondaryContainer = Color(0xFF4A3826),
    onSecondaryContainer = Color(0xFFF5DEC9),
    tertiary = Color(0xFFC9CB9B),
    onTertiary = Color(0xFF30340A),
    tertiaryContainer = Color(0xFF44481A),
    onTertiaryContainer = Color(0xFFE5E8B3),
    surface = Color(0xFF171210),
    onSurface = Color(0xFFEBE1DB),
    surfaceVariant = Color(0xFF4D423B),
    onSurfaceVariant = Color(0xFFD0C5BD),
    outline = Color(0xFF998F87),
    outlineVariant = Color(0xFF4D423B),
    surfaceContainerLow = Color(0xFF1F1815),
    surfaceContainerHigh = Color(0xFF2D241F)
)

// --- Неоновая кухня: мятный неон, маджента, циан ---
val NeonPalette = ThemePalette(
    primary = Color(0xFF3DFFB4),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005235),
    onPrimaryContainer = Color(0xFF6CFFC7),
    secondary = Color(0xFFFF4FD8),
    onSecondary = Color(0xFF5B0050),
    secondaryContainer = Color(0xFF7A0069),
    onSecondaryContainer = Color(0xFFFFD6F2),
    tertiary = Color(0xFF5BE1FF),
    onTertiary = Color(0xFF003640),
    tertiaryContainer = Color(0xFF004E5C),
    onTertiaryContainer = Color(0xFFABEDFF),
    surface = Color(0xFF0C0C12),
    onSurface = Color(0xFFE3E1E9),
    surfaceVariant = Color(0xFF45464E),
    onSurfaceVariant = Color(0xFFC6C5CE),
    outline = Color(0xFF909098),
    outlineVariant = Color(0xFF45464E),
    surfaceContainerLow = Color(0xFF14141B),
    surfaceContainerHigh = Color(0xFF22222A)
)

// --- Nothing OS: Строгий монохром (#000000 / #141414) + Nothing Red (#D71921) ---
val NothingDarkPalette = ThemePalette(
    primary = Color(0xFFD71921),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF2B0A0C),
    onPrimaryContainer = Color(0xFFFFD5D7),
    secondary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF262629),
    onSecondaryContainer = Color(0xFFFFFFFF),
    tertiary = Color(0xFFD71921),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF2B0A0C),
    onTertiaryContainer = Color(0xFFFFD5D7),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFF8E8E93),
    outline = Color(0xFF262629),
    outlineVariant = Color(0xFF1F1F22),
    surfaceContainerLow = Color(0xFF000000),
    surfaceContainerHigh = Color(0xFF141414)
)

val NothingLightPalette = ThemePalette(
    primary = Color(0xFFD71921),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDADB),
    onPrimaryContainer = Color(0xFF410005),
    secondary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E5E5),
    onSecondaryContainer = Color(0xFF000000),
    tertiary = Color(0xFFD71921),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDADB),
    onTertiaryContainer = Color(0xFF410005),
    surface = Color(0xFFF2F2F2),
    onSurface = Color(0xFF000000),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF6E6E73),
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFEBEBEB),
    surfaceContainerLow = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color(0xFFFFFFFF)
)

fun buildCustomPalette(primary: Color, secondary: Color, isDark: Boolean): ThemePalette {
    return if (isDark) {
        ThemePalette(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primary.copy(alpha = 0.3f),
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = secondary.copy(alpha = 0.3f),
            onSecondaryContainer = Color.White,
            tertiary = secondary,
            onTertiary = Color.Black,
            tertiaryContainer = secondary.copy(alpha = 0.2f),
            onTertiaryContainer = Color.White,
            surface = Color(0xFF141416),
            onSurface = Color(0xFFE3E3E6),
            surfaceVariant = Color(0xFF28282E),
            onSurfaceVariant = Color(0xFFC6C6D0),
            outline = Color(0xFF888890),
            outlineVariant = Color(0xFF383840),
            surfaceContainerLow = Color(0xFF1A1A1D),
            surfaceContainerHigh = Color(0xFF242428)
        )
    } else {
        ThemePalette(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.15f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.15f),
            onSecondaryContainer = secondary,
            tertiary = secondary,
            onTertiary = Color.White,
            tertiaryContainer = secondary.copy(alpha = 0.1f),
            onTertiaryContainer = secondary,
            surface = Color(0xFFFBFBFD),
            onSurface = Color(0xFF1A1A1C),
            surfaceVariant = Color(0xFFE6E6EC),
            onSurfaceVariant = Color(0xFF44444A),
            outline = Color(0xFF767680),
            outlineVariant = Color(0xFFC6C6D0),
            surfaceContainerLow = Color(0xFFF4F4F8),
            surfaceContainerHigh = Color(0xFFEAEAEE)
        )
    }
}
