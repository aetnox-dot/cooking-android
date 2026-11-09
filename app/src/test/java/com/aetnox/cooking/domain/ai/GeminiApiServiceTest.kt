package com.aetnox.cooking.domain.ai

import com.aetnox.cooking.domain.parser.RecipeImportParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiApiServiceTest {

    @Test
    fun testDefaultSettings() {
        assertEquals("AQ.Ab8RN6Ld9e655Prlb6VLjSA4bTuuFnT6UNMwRsWbZc1ZuRziVg", GeminiSettingsManager.DEFAULT_API_KEY)
        assertEquals("gemini-3.8-flash", GeminiSettingsManager.DEFAULT_MODEL)
        assertEquals("https://generativelanguage.googleapis.com", GeminiSettingsManager.DEFAULT_BASE_URL)

        assertTrue(GeminiSettingsManager.AVAILABLE_MODELS.any { it.id == "gemini-3.8-flash" })
        assertTrue(GeminiSettingsManager.AVAILABLE_MODELS.any { it.id == "gemini-3.5-flash" })
        assertTrue(GeminiSettingsManager.AVAILABLE_MODELS.any { it.id == "gemini-3.7-flash" })
        assertEquals("gemini-3.8-flash", GeminiSettingsManager.AVAILABLE_MODELS.first().id)
    }

    @Test
    fun testGeminiResponseParsing() {
        val simulatedGeminiJsonOutput = """
            ```json
            {
              "title": "ИИ Паста Карбонара",
              "prepTimeMinutes": 10,
              "cookTimeMinutes": 15,
              "servings": 2,
              "yieldUnit": "порции",
              "chefHeatingType": "Плита средний огонь",
              "chefTemperature": "100 °C",
              "chefTiming": "10-12 мин",
              "chefReadinessSign": "Паста al dente, соус шелковистый",
              "ingredients": [
                {
                  "name": "Спагетти",
                  "amount": "200",
                  "unit": "г",
                  "groupName": "Основное",
                  "note": "из твердых сортов"
                },
                {
                  "name": "Гуанчале или бекон",
                  "amount": "100",
                  "unit": "г",
                  "groupName": "Основное",
                  "note": "нарезать брусочками"
                },
                {
                  "name": "Желтки яичные",
                  "amount": "3",
                  "unit": "шт",
                  "groupName": "Соус",
                  "note": ""
                },
                {
                  "name": "Сыр Пекорино Романо",
                  "amount": "50",
                  "unit": "г",
                  "groupName": "Соус",
                  "note": "мелко натереть"
                }
              ],
              "steps": [
                "Отварить спагетти в кипящей подсоленной воде до состояния al dente.",
                "Обжарить бекон на сухой сковороде до хрустящей корочки.",
                "Смешать желтки с натертым сыром и свежемолотым черным перцем.",
                "Соединить спагетти с беконом и влить сырно-яичную смесь, быстро перемешивая."
              ],
              "tags": ["Итальянская кухня", "Паста", "Быстро"],
              "notes": "Не нагревайте сковороду при добавлении яиц, иначе они свернутся!"
            }
            ```
        """.trimIndent()

        val cleanJson = simulatedGeminiJsonOutput
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val parsed = RecipeImportParser.parse(cleanJson)

        assertEquals("ИИ Паста Карбонара", parsed.recipe.title)
        assertEquals(10, parsed.recipe.prepTimeMinutes)
        assertEquals(15, parsed.recipe.cookTimeMinutes)
        assertEquals(2.0, parsed.recipe.baseServings, 0.01)
        assertEquals(4, parsed.ingredients.size)
        assertEquals(4, parsed.steps.size)
        assertEquals(3, parsed.tags.size)

        assertEquals("Основное", parsed.ingredients[0].groupName)
        assertEquals("Соус", parsed.ingredients[2].groupName)
        assertEquals("Спагетти", parsed.ingredients[0].name)
        assertEquals(200.0, parsed.ingredients[0].amount, 0.01)
    }

    @Test
    fun testSuggestTimerToolCallParsing() {
        val reply = "TOOL: suggest_timer | 10 минут | Варить макароны"
        val call = CookAssistantTools.parseToolCall(reply)
        assertNotNull(call)
        assertEquals("suggest_timer", call?.name)
        assertEquals("10 минут | Варить макароны", call?.arg)

        val durationSec = com.aetnox.cooking.ui.viewmodel.StepTimerParser.parseDurationInSeconds("10 минут")
        assertEquals(600, durationSec)
    }
}
