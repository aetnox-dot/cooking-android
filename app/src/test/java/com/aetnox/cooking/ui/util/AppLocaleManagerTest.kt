package com.aetnox.cooking.ui.util

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AppLocaleManagerTest {

    @Before
    fun setUp() {
        AppLocaleManager.currentLanguage.value = AppLanguage.RUSSIAN
    }

    @Test
    fun testTrRussianAndEnglish() {
        AppLocaleManager.currentLanguage.value = AppLanguage.RUSSIAN
        assertEquals("Привет", AppLocaleManager.tr("Привет", "Hello"))

        AppLocaleManager.currentLanguage.value = AppLanguage.ENGLISH
        assertEquals("Hello", AppLocaleManager.tr("Привет", "Hello"))
    }

    @Test
    fun testTrPluralRussian() {
        AppLocaleManager.currentLanguage.value = AppLanguage.RUSSIAN

        // 1 рецепт
        assertEquals("рецепт", AppLocaleManager.trPlural(1, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецепт", AppLocaleManager.trPlural(21, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))

        // 2..4 рецепта
        assertEquals("рецепта", AppLocaleManager.trPlural(2, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецепта", AppLocaleManager.trPlural(4, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецепта", AppLocaleManager.trPlural(23, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))

        // 5..20, 0 рецептов
        assertEquals("рецептов", AppLocaleManager.trPlural(0, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецептов", AppLocaleManager.trPlural(5, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецептов", AppLocaleManager.trPlural(11, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("рецептов", AppLocaleManager.trPlural(19, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
    }

    @Test
    fun testTrPluralEnglish() {
        AppLocaleManager.currentLanguage.value = AppLanguage.ENGLISH

        assertEquals("recipe", AppLocaleManager.trPlural(1, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("recipes", AppLocaleManager.trPlural(0, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("recipes", AppLocaleManager.trPlural(2, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
        assertEquals("recipes", AppLocaleManager.trPlural(5, "рецепт", "рецепта", "рецептов", "recipe", "recipes"))
    }

    @Test
    fun testTrFormat() {
        AppLocaleManager.currentLanguage.value = AppLanguage.RUSSIAN
        assertEquals("Шаг 3 из 10", AppLocaleManager.trFormat("Шаг %d из %d", "Step %d of %d", 3, 10))

        AppLocaleManager.currentLanguage.value = AppLanguage.ENGLISH
        assertEquals("Step 3 of 10", AppLocaleManager.trFormat("Шаг %d из %d", "Step %d of %d", 3, 10))
    }
}
