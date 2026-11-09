package com.aetnox.cooking.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeImportParserTest {

    @Test
    fun testParseFractionAmount() {
        val (amount, note) = RecipeImportParser.parseSafeAmount("1/2")
        assertEquals(0.5, amount, 0.01)
        assertEquals(null, note)
    }

    @Test
    fun testParseNonNumericAmount() {
        val (amount, note) = RecipeImportParser.parseSafeAmount("по вкусу")
        assertEquals(0.0, amount, 0.01)
        assertEquals("по вкусу", note)
    }

    @Test
    fun testParseMixedFractionAndRangeAmount() {
        val (mixedAmt, mixedNote) = RecipeImportParser.parseSafeAmount("1 1/2")
        assertEquals(1.5, mixedAmt, 0.01)
        assertEquals(null, mixedNote)

        val (rangeAmt, rangeNote) = RecipeImportParser.parseSafeAmount("2-3")
        assertEquals(2.5, rangeAmt, 0.01)
        assertEquals("2-3", rangeNote)
    }

    @Test
    fun testParseTextWithSubGroupsAndHeaderSynonyms() {
        val text = """
            # Шарлотка с яблоками
            
            Состав:
            Для теста:
            Мука — 200 г
            Сахар — 150 г
            
            Для начинки:
            Яблоки — 3 шт
            
            Способ приготовления:
            1. Взбейте яйца с сахаром.
            2. Добавьте яблоки и запекайте.
            
            Теги: Выпечка, Десерты
        """.trimIndent()

        val parsed = RecipeImportParser.parse(text)
        assertEquals("Шарлотка с яблоками", parsed.recipe.title)
        assertEquals(3, parsed.ingredients.size)
        assertEquals("Для теста", parsed.ingredients[0].groupName)
        assertEquals("Мука", parsed.ingredients[0].name)
        assertEquals(200.0, parsed.ingredients[0].amount, 0.01)
        assertEquals("Для начинки", parsed.ingredients[2].groupName)
        assertEquals("Яблоки", parsed.ingredients[2].name)
        assertEquals(2, parsed.steps.size)
        assertEquals(2, parsed.tags.size)
    }

    @Test
    fun testParseJsonImport() {
        val jsonText = """
            {
              "title": "Блинчики классические",
              "prepTimeMinutes": 10,
              "cookTimeMinutes": 20,
              "servings": 4,
              "ingredients": [
                { "name": "Мука", "amount": "200", "unit": "г" },
                { "name": "Молоко", "amount": "500", "unit": "мл" }
              ],
              "steps": [
                "Смешайте муку и молоко.",
                "Выпекайте блинчики."
              ],
              "tags": ["Завтрак", "Выпечка"]
            }
        """.trimIndent()

        val parsed = RecipeImportParser.parse(jsonText)
        assertEquals("Блинчики классические", parsed.recipe.title)
        assertEquals(10, parsed.recipe.prepTimeMinutes)
        assertEquals(20, parsed.recipe.cookTimeMinutes)
        assertEquals(2, parsed.ingredients.size)
        assertEquals("Мука", parsed.ingredients[0].name)
        assertEquals(200.0, parsed.ingredients[0].amount, 0.01)
        assertEquals(2, parsed.steps.size)
        assertEquals(2, parsed.tags.size)
    }

    @Test
    fun testAiPromptTemplateIsNotEmpty() {
        val prompt = AiPromptTemplates.AI_RECIPE_JSON_PROMPT
        assert(prompt.contains("Cooking"))
        assert(prompt.contains("prepTimeMinutes"))
        assert(prompt.contains("groupName"))
        assert(prompt.contains("chefTemperature"))
    }

    @Test
    fun testParseUserSwedishCinnamonBunsJson() {
        val userJson = """
            {
            "title": "Шведские булочки с корицей",
            "prepTimeMinutes": 75,
            "cookTimeMinutes": 25,
            "servings": 8,
            "yieldUnit": "булочек",
            "chefHeatingType": "Духовка (режим верх-низ)",
            "chefTemperature": "180 °C",
            "chefTiming": "20-25 мин",
            "chefReadinessSign": "Аппетитная золотисто-румяная корочка",
            "ingredients": [
            {
            "name": "Молоко",
            "amount": "100",
            "unit": "мл",
            "groupName": "Для теста",
            "note": "теплое"
            },
            {
            "name": "Вода",
            "amount": "140",
            "unit": "мл",
            "groupName": "Для теста",
            "note": "теплая"
            },
            {
            "name": "Дрожжи сухие",
            "amount": "1.5",
            "unit": "ч. л.",
            "groupName": "Для теста",
            "note": ""
            },
            {
            "name": "Сахар",
            "amount": "4",
            "unit": "ч. л.",
            "groupName": "Для теста",
            "note": ""
            },
            {
            "name": "Соль",
            "amount": "0.5",
            "unit": "ч. л.",
            "groupName": "Для теста",
            "note": ""
            },
            {
            "name": "Масло подсолнечное",
            "amount": "10",
            "unit": "мл",
            "groupName": "Для теста",
            "note": ""
            },
            {
            "name": "Масло сливочное",
            "amount": "30",
            "unit": "г",
            "groupName": "Для теста",
            "note": "размягченное"
            },
            {
            "name": "Мука пшеничная",
            "amount": "400",
            "unit": "г",
            "groupName": "Для теста",
            "note": "просеять"
            },
            {
            "name": "Масло сливочное",
            "amount": "50",
            "unit": "г",
            "groupName": "Для начинки",
            "note": "комнатной температуры"
            },
            {
            "name": "Сахар",
            "amount": "60",
            "unit": "г",
            "groupName": "Для начинки",
            "note": ""
            },
            {
            "name": "Корица молотая",
            "amount": "2",
            "unit": "ч. л.",
            "groupName": "Для начинки",
            "note": ""
            }
            ],
            "steps": [
            "Смешать в емкости теплое @[Молоко (Для теста)], теплую @[Вода (Для теста)], @[Дрожжи сухие (Для теста)] и @[Сахар (Для теста)]. Добавить @[Соль (Для теста)], @[Масло подсолнечное (Для теста)], мягкое @[Масло сливочное (Для теста)] и @[Мука пшеничная (Для теста)]. Замесить эластичное тесто, накрыть и оставить в тепле на 50-60 минут подниматься вдвое.",
            "Раскатать подошедшее тесто в ровный прямоугольный пласт. Смазать одну половину пласта мягким @[Масло сливочное (Для начинки)] и равномерно посыпать смесью из @[Сахар (Для начинки)] и @[Корица молотая (Для начинки)].",
            "Сложить тесто пополам, накрыв начинку свободной половиной теста, и аккуратно прокатать скалкой.",
            "Нарезать пласт на продольные полоски шириной около 2 см, скрутить каждую полоску жгутом и свернуть в аккуратные узелки-булочки. Выложить их на противень с пергаментом.",
            "Выпекать в разогретой духовке 20-25 минут до золотистого румянца."
            ],
            "tags": ["Выпечка", "Булочки", "Шведская кухня", "К чаю"],
            "notes": "По традиционному скандинавскому рецепту в тесто можно добавить 0.5 ч. л. свежемолотого кардамона, а для начинки использовать коричневый сахар для глубокого карамельного аромата.",
            "calories": 310,
            "protein": 5,
            "fat": 10,
            "carbs": 50
            }
        """.trimIndent()

        val parsed = RecipeImportParser.parse(userJson)
        assertEquals("Шведские булочки с корицей", parsed.recipe.title)
        assertEquals(11, parsed.ingredients.size)
        assertEquals("Для теста", parsed.ingredients[0].groupName)
        assertEquals("Для начинки", parsed.ingredients[8].groupName)
    }

    @Test
    fun testParseJsonWithNumericAmountsAndAliasGroupNames() {
        val jsonText = """
            {
              "title": "Тестовый пирог",
              "ingredients": [
                { "name": "Мука", "amount": 250, "unit": "г", "group_name": "Для основы" },
                { "name": "Джем", "amount": 100, "unit": "г", "category": "Для начинки" }
              ],
              "steps": [ "Спечь пирог" ]
            }
        """.trimIndent()

        val parsed = RecipeImportParser.parse(jsonText)
        assertEquals("Тестовый пирог", parsed.recipe.title)
        assertEquals(2, parsed.ingredients.size)
        assertEquals("Для основы", parsed.ingredients[0].groupName)
        assertEquals(250.0, parsed.ingredients[0].amount, 0.01)
        assertEquals("Для начинки", parsed.ingredients[1].groupName)
        assertEquals(100.0, parsed.ingredients[1].amount, 0.01)
    }
}
