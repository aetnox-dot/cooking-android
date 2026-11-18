package com.aetnox.cooking.ui.theme

import com.aetnox.cooking.ui.util.AppIconVariant
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun testNothingThemePaletteColors() {
        val dark = NothingDarkPalette
        val light = NothingLightPalette

        // OLED Pitch Black Background #000000
        assertEquals(Color(0xFF000000), dark.surface)
        assertEquals(Color(0xFF000000), dark.surfaceContainerLow)

        // Surface Cards #141414
        assertEquals(Color(0xFF141414), dark.surfaceVariant)
        assertEquals(Color(0xFF141414), dark.surfaceContainerHigh)

        // Nothing Red Accent #D71921
        assertEquals(Color(0xFFD71921), dark.primary)

        // Light mode background #F2F2F2 and cards #FFFFFF
        assertEquals(Color(0xFFF2F2F2), light.surface)
        assertEquals(Color(0xFFFFFFFF), light.surfaceVariant)
    }

    @Test
    fun testAppThemeNothingProperties() {
        val theme = AppTheme.NOTHING
        assertTrue(theme.alwaysDark)
        assertEquals(NothingDarkPalette, theme.darkPalette())
        assertEquals(NothingLightPalette, theme.lightPalette())
    }

    @Test
    fun testAppIconVariantAliases() {
        assertEquals("com.aetnox.cooking.MainActivityAliasDefault", AppIconVariant.DEFAULT.aliasName)
        assertEquals("com.aetnox.cooking.MainActivityAliasNothing", AppIconVariant.NOTHING.aliasName)
    }

    @Test
    fun testDeviceUtilsIsNothingDeviceDefaultFalse() {
        val isNothing = com.aetnox.cooking.data.remote.DeviceUtils.isNothingDevice()
        // On standard JVM unit test environment Build.MANUFACTURER is null/empty
        assertEquals(false, isNothing)
    }
}
