package com.aetnox.cooking

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.data.entity.TagEntity
import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.data.remote.model.AiCostsDto
import com.aetnox.cooking.data.remote.model.AiSearchPlanDto
import com.aetnox.cooking.data.remote.model.AiSearchResponse
import com.aetnox.cooking.data.remote.model.ApiErrorCodes
import com.aetnox.cooking.data.remote.model.AssistantStepResponse
import com.aetnox.cooking.data.remote.model.ProposedUpdateDto
import com.aetnox.cooking.data.remote.model.ProposedUpdatePatchDto
import com.aetnox.cooking.data.remote.model.RecipeParseResponse
import com.aetnox.cooking.domain.exporter.RecipeExporter
import com.aetnox.cooking.ui.viewmodel.RecipeListViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudBackupAndAiSearchV2Test {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testBackupDtoRoundtrip() {
        val recipe = RecipeEntity(
            id = 42,
            title = "Малиновый пирог",
            prepTimeMinutes = 15,
            cookTimeMinutes = 35,
            status = RecipeEntity.STATUS_NEEDS_WORK,
            isFavorite = true,
            rating = 4,
            postCookNote = "В следующий раз дать остыть",
            chefHeatingType = "Духовка верх-низ",
            chefTemperature = "180°C",
            notes = "Очень вкусно со свежей малиной",
            cloudId = "cloud-42-12345",
            cloudAccessCode = "1234567890"
        )
        val ingredients = listOf(
            IngredientEntity(id = 1, recipeId = 42, name = "Малина", amount = 300.0, unit = "г"),
            IngredientEntity(id = 2, recipeId = 42, name = "Мука", amount = 200.0, unit = "г")
        )
        val steps = listOf(
            StepEntity(id = 1, recipeId = 42, stepNumber = 1, instructionText = "Замесить тесто и выпекать.")
        )
        val tags = listOf(TagEntity(id = 1, name = "Десерты"))

        val details = RecipeWithDetails(recipe, ingredients, steps, tags)

        val backupDto = RecipeExporter.recipeToBackupDto(details)

        assertEquals("Малиновый пирог", backupDto.title)
        assertEquals(15, backupDto.prepTimeMinutes)
        assertEquals(35, backupDto.cookTimeMinutes)
        assertEquals(RecipeEntity.STATUS_NEEDS_WORK, backupDto.status)
        assertTrue(backupDto.isFavorite)
        assertEquals("cloud-42-12345", backupDto.cloudId)
        assertEquals(2, backupDto.ingredients.size)

        val restoredDetails = RecipeExporter.backupDtoToRecipeWithDetails(backupDto)
        assertEquals("Малиновый пирог", restoredDetails.recipe.title)
        assertEquals(RecipeEntity.STATUS_NEEDS_WORK, restoredDetails.recipe.status)
        assertEquals("cloud-42-12345", restoredDetails.recipe.cloudId)
        assertEquals(2, restoredDetails.ingredients.size)
        assertEquals("Малина", restoredDetails.ingredients[0].name)
    }

    @Test
    fun testBackupDtoLimitsAndClamping() {
        val longTitle = "A".repeat(300)
        val longNote = "B".repeat(3000)
        val recipe = RecipeEntity(
            id = 1,
            title = longTitle,
            rating = 10,
            postCookNote = longNote,
            notes = longNote
        )
        val details = RecipeWithDetails(recipe, emptyList(), emptyList(), emptyList())
        val backupDto = RecipeExporter.recipeToBackupDto(details)

        assertEquals(200, backupDto.title.length)
        assertEquals(5, backupDto.rating)
        assertEquals(2000, backupDto.postCookNote.length)
        assertEquals(2000, backupDto.notes.length)
    }

    @Test
    fun testFilterLocalRecipesForPlan() {
        val recipe1 = RecipeEntity(
            id = 1,
            title = "Малиновый десерт",
            prepTimeMinutes = 10,
            cookTimeMinutes = 20,
            status = RecipeEntity.STATUS_NEEDS_WORK
        )
        val details1 = RecipeWithDetails(
            recipe = recipe1,
            ingredients = listOf(IngredientEntity(id = 1, recipeId = 1, name = "Малина", amount = 200.0, unit = "г")),
            steps = emptyList(),
            tags = listOf(TagEntity(id = 1, name = "Десерт"))
        )

        val recipe2 = RecipeEntity(
            id = 2,
            title = "Борщ украинский",
            prepTimeMinutes = 30,
            cookTimeMinutes = 90,
            status = RecipeEntity.STATUS_VERIFIED
        )
        val details2 = RecipeWithDetails(
            recipe = recipe2,
            ingredients = listOf(IngredientEntity(id = 2, recipeId = 2, name = "Свекла", amount = 300.0, unit = "г")),
            steps = emptyList(),
            tags = listOf(TagEntity(id = 2, name = "Суп"))
        )

        val plan = AiSearchPlanDto(
            keywords = listOf("малина"),
            ingredients = emptyList(),
            minMinutes = null,
            maxMinutes = 40
        )

        val matches = RecipeListViewModel.filterLocalRecipesForPlan(listOf(details1, details2), plan)

        assertEquals(1, matches.size)
        assertEquals("1", matches[0].id)
        assertEquals("Малиновый десерт", matches[0].title)
        assertEquals(RecipeEntity.STATUS_NEEDS_WORK, matches[0].status)
    }

    @Test
    fun testAiCostsAndResponseCostSerialization() {
        val aiCostsJson = """{"assistant":1,"parse":1,"parse_photo":2,"search_single_per_60":1,"search_finish":1,"search_plan":0}"""
        val costs = json.decodeFromString<AiCostsDto>(aiCostsJson)
        assertEquals(1, costs.assistant)
        assertEquals(2, costs.parsePhoto)
        assertEquals(0, costs.searchPlan)

        val assistantRespJson = """{"reply_type":"text","content":"Ответ","cost":1,"remaining_month":95}"""
        val assistantResp = json.decodeFromString<AssistantStepResponse>(assistantRespJson)
        assertEquals(1, assistantResp.cost)
        assertEquals(95, assistantResp.remainingMonth)

        val parseRespJson = """{"success":true,"recipe":{"title":"Суп","ingredients":[],"steps":[]},"cost":2,"remaining_month":90}"""
        val parseResp = json.decodeFromString<RecipeParseResponse>(parseRespJson)
        assertEquals(2, parseResp.cost)

        val searchRespJson = """{"success":true,"scope":"all","reply":"Найдено","results":[],"cost":1,"remaining_month":89}"""
        val searchResp = json.decodeFromString<AiSearchResponse>(searchRespJson)
        assertEquals(1, searchResp.cost)
    }

    @Test
    fun testProposedPatchDtoSerialization() {
        val patchJson = """
            {
              "reply_type": "text",
              "content": "Снижаем температуру",
              "proposed_update": {
                "summary": "Снижаем температуру и время",
                "patch": {
                  "postCookNote": "Выпекать при 170°C",
                  "chefTemperature": "170°C",
                  "suggested_status": "NEEDS_WORK",
                  "ingredient_amounts": [{"name": "творог", "new_amount": "180 г"}]
                }
              },
              "cost": 1
            }
        """.trimIndent()

        val resp = json.decodeFromString<AssistantStepResponse>(patchJson)
        assertNotNull(resp.proposedUpdate)
        val update = resp.proposedUpdate!!
        assertEquals("Снижаем температуру и время", update.summary)
        val patch = update.patch
        assertNotNull(patch)
        assertEquals("170°C", patch?.chefTemperature)
        assertEquals("NEEDS_WORK", patch?.suggestedStatus)
        assertEquals(1, patch?.ingredientAmounts?.size)
        assertEquals("творог", patch?.ingredientAmounts?.get(0)?.name)
    }

    @Test
    fun testEmptyContentFallbackWithSummary() {
        val emptyContentPatchJson = """
            {
              "reply_type": "text",
              "content": "",
              "proposed_update": {
                "summary": "Предлагаю уменьшить сахар до 50 г",
                "patch": {
                  "ingredient_amounts": [{"name": "сахар", "new_amount": "50 г"}]
                }
              },
              "cost": 1
            }
        """.trimIndent()

        val resp = json.decodeFromString<AssistantStepResponse>(emptyContentPatchJson)
        val textToUse = resp.content.ifBlank { resp.proposedUpdate?.summary ?: "" }
        assertEquals("Предлагаю уменьшить сахар до 50 г", textToUse)
    }

    @Test
    fun testErrorCodesConstants() {
        assertEquals("INVALID_BACKUP", ApiErrorCodes.INVALID_BACKUP)
        assertEquals("SESSION_NOT_FOUND", ApiErrorCodes.SESSION_NOT_FOUND)
        assertEquals("SESSION_EXPIRED", ApiErrorCodes.SESSION_EXPIRED)
        assertEquals("FEATURE_DISABLED", ApiErrorCodes.FEATURE_DISABLED)
    }

    @Test
    fun testCostForecastCalculation() {
        val costs = AiCostsDto(
            assistant = 1,
            parse = 1,
            parsePhoto = 2,
            searchSinglePer60 = 1,
            searchFinish = 1,
            searchPlan = 0
        )
        val userSession = com.aetnox.cooking.data.remote.UserSessionData(aiCosts = costs)

        // 45 recipes -> max(1, ceil(45/60)) * 1 = 1
        val state45 = com.aetnox.cooking.ui.viewmodel.RecipeListUiState(
            recipes = List(45) { RecipeWithDetails(RecipeEntity(id = it.toLong(), title = "Recipe $it"), emptyList(), emptyList(), emptyList()) },
            userSession = userSession
        )
        assertEquals(1, state45.getSingleSearchCostForecast())
        assertEquals(1, state45.getTwoPhaseSearchCostForecast())

        // 130 recipes -> max(1, ceil(130/60)) * 1 = 3
        val state130 = com.aetnox.cooking.ui.viewmodel.RecipeListUiState(
            recipes = List(130) { RecipeWithDetails(RecipeEntity(id = it.toLong(), title = "Recipe $it"), emptyList(), emptyList(), emptyList()) },
            userSession = userSession
        )
        assertEquals(3, state130.getSingleSearchCostForecast())
        assertEquals(1, state130.getTwoPhaseSearchCostForecast())
    }

    @Test
    fun testMessageContentTruncation() {
        val longMessage = "X".repeat(1000)
        val truncated = longMessage.take(600)
        assertEquals(600, truncated.length)
    }

    @Test
    fun testAssistantFifoSlidingWindowHistory() {
        val messages = (1..15).map { index ->
            com.aetnox.cooking.ui.viewmodel.AssistantMessage(
                isUser = index % 2 != 0,
                text = "Message $index"
            )
        }
        val trimmed = com.aetnox.cooking.ui.viewmodel.CookModeViewModel.trimAssistantMessages(messages)
        assertEquals(12, trimmed.size)
        assertEquals("Message 4", trimmed.first().text)
        assertEquals("Message 15", trimmed.last().text)
    }

    @Test
    fun testParseAmountAndUnitWithUnits() {
        val parsedMilk = com.aetnox.cooking.domain.parser.RecipeImportParser.parseAmountAndUnit("210 мл")
        assertEquals(210.0, parsedMilk.amount, 0.01)
        assertEquals("мл", parsedMilk.unit)

        val parsedFlour = com.aetnox.cooking.domain.parser.RecipeImportParser.parseAmountAndUnit("210г")
        assertEquals(210.0, parsedFlour.amount, 0.01)
        assertEquals("г", parsedFlour.unit)

        val parsedSugar = com.aetnox.cooking.domain.parser.RecipeImportParser.parseAmountAndUnit("1.5 стакана")
        assertEquals(1.5, parsedSugar.amount, 0.01)
        assertEquals("стакана", parsedSugar.unit)

        val (numMilk, noteMilk) = com.aetnox.cooking.domain.parser.RecipeImportParser.parseSafeAmount("210 мл")
        assertEquals(210.0, numMilk, 0.01)
        assertEquals(null, noteMilk)

        val parsedRelative = com.aetnox.cooking.domain.parser.RecipeImportParser.parseAmountAndUnit("на 10 меньше", baseAmount = 210.0)
        assertEquals(200.0, parsedRelative.amount, 0.01)
    }

    @Test
    fun testToolCallParsingAndAliasNormalization() {
        val tool1 = com.aetnox.cooking.domain.ai.CookAssistantTools.parseToolCall("TOOL: go_to_step_3")
        assertNotNull(tool1)
        assertEquals("go_to_step", tool1?.name)
        assertEquals("3", tool1?.arg)

        val tool2 = com.aetnox.cooking.domain.ai.CookAssistantTools.parseToolCall("TOOL: get_steps")
        assertNotNull(tool2)
        assertEquals("get_all_steps", tool2?.name)

        val tool3 = com.aetnox.cooking.domain.ai.CookAssistantTools.parseToolCall("TOOL: next_step")
        assertNotNull(tool3)
        assertEquals("go_to_step", tool3?.name)
        assertEquals("next", tool3?.arg)

        val tool4 = com.aetnox.cooking.domain.ai.CookAssistantTools.parseToolCall("TOOL: suggest_timer: 10 минут | Варить пасту")
        assertNotNull(tool4)
        assertEquals("suggest_timer", tool4?.name)
        assertTrue(tool4?.arg?.contains("10 минут") == true)

        val toolWithLeadingText = com.aetnox.cooking.domain.ai.CookAssistantTools.parseToolCall("Сверяюсь с рецептом...\nTOOL: get_ingredients |")
        assertNotNull(toolWithLeadingText)
        assertEquals("get_ingredients", toolWithLeadingText?.name)
    }
}
