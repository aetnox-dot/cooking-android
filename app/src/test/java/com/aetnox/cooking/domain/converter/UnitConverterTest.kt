package com.aetnox.cooking.domain.converter

import org.junit.Assert.assertEquals
import org.junit.Test

class UnitConverterTest {

    @Test
    fun testFlourDensityConversion() {
        // 1 tbsp flour = 15g
        val grams = UnitConverter.convertAmount(
            amount = 2.0,
            fromUnit = "ст. л.",
            toUnit = "г",
            ingredientName = "Мука пшеничная"
        )
        assertEquals(30.0, grams, 0.01)

        // 1 cup flour = 130g
        val cupGrams = UnitConverter.convertAmount(
            amount = 1.0,
            fromUnit = "стакан",
            toUnit = "г",
            ingredientName = "мука"
        )
        assertEquals(130.0, cupGrams, 0.01)
    }

    @Test
    fun testSugarDensityConversion() {
        // 1 tbsp sugar = 20g
        val grams = UnitConverter.convertAmount(
            amount = 3.0,
            fromUnit = "ст. л.",
            toUnit = "г",
            ingredientName = "Сахар"
        )
        assertEquals(60.0, grams, 0.01)
    }

    @Test
    fun testPortionScalingAndRoundingRules() {
        // Grams: base 4 servings -> 250g. Target 6 servings -> 375g. Whole integer rounding.
        val scaledGrams = UnitConverter.scaleAndFormatAmount(
            amount = 250.0,
            fromUnit = "г",
            toUnit = "г",
            baseServings = 4.0,
            currentServings = 6.0,
            ingredientName = "Сахар"
        )
        assertEquals("375", scaledGrams)

        // Spoons: base 4 servings -> 1 ст. л. Target 6 servings -> 1.5 ст. л. 1 decimal place.
        val scaledSpoons = UnitConverter.scaleAndFormatAmount(
            amount = 1.0,
            fromUnit = "ст. л.",
            toUnit = "ст. л.",
            baseServings = 4.0,
            currentServings = 6.0,
            ingredientName = "Соль"
        )
        assertEquals("1.5", scaledSpoons)
    }

    @Test
    fun testCornstarchDensityConversion() {
        // 1 tbsp cornstarch = 12g
        val grams = UnitConverter.convertAmount(
            amount = 2.0,
            fromUnit = "ст. л.",
            toUnit = "г",
            ingredientName = "кукурузный крахмал"
        )
        assertEquals(24.0, grams, 0.01)
    }

    @Test
    fun testCanConvertSafeChecks() {
        // Same category is convertible
        assert(UnitConverter.canConvert("г", "кг"))
        assert(UnitConverter.canConvert("мл", "л"))

        // Pieces to grams without custom factor is NOT convertible
        assert(!UnitConverter.canConvert("шт", "г", ingredientName = "яблоки"))

        // Piece item WITH custom factor IS convertible
        assert(UnitConverter.canConvert("шт", "г", ingredientName = "яблоки", customGramsPerUnit = 150.0))

        // Flour from tbsp to grams IS convertible via density
        assert(UnitConverter.canConvert("ст. л.", "г", ingredientName = "мука"))
    }

    @Test
    fun testUnitDisplayTranslation() {
        assertEquals("g", UnitConverter.formatUnitForDisplay("г", isEnglish = true))
        assertEquals("kg", UnitConverter.formatUnitForDisplay("кг", isEnglish = true))
        assertEquals("ml", UnitConverter.formatUnitForDisplay("мл", isEnglish = true))
        assertEquals("l", UnitConverter.formatUnitForDisplay("л", isEnglish = true))
        assertEquals("tbsp", UnitConverter.formatUnitForDisplay("ст. л.", isEnglish = true))
        assertEquals("tsp", UnitConverter.formatUnitForDisplay("ч. л.", isEnglish = true))
        assertEquals("cup", UnitConverter.formatUnitForDisplay("стакан", isEnglish = true))
        assertEquals("pc", UnitConverter.formatUnitForDisplay("шт", isEnglish = true))

        assertEquals("г", UnitConverter.formatUnitForDisplay("г", isEnglish = false))
        assertEquals("ст. л.", UnitConverter.formatUnitForDisplay("ст. л.", isEnglish = false))
    }
}
