package com.aetnox.cooking.domain.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StepTokenParserTest {

    @Test
    fun testTokenInsertion() {
        val initialText = "Смешайте в миске."
        val (newText, newCursor) = StepTokenParser.insertToken(
            text = initialText,
            selectionStart = 9,
            ingredientName = "Мука"
        )

        assertEquals("Смешайте @[Мука] в миске.", newText)
        assertEquals(17, newCursor)
    }

    @Test
    fun testParseSegments() {
        val stepText = "Добавьте @[Мука] и замесите тесто."
        val segments = StepTokenParser.parseSegments(stepText)

        assertEquals(3, segments.size)
        assertTrue(segments[0] is StepSegment.TextSegment)
        assertEquals("Добавьте ", (segments[0] as StepSegment.TextSegment).text)

        assertTrue(segments[1] is StepSegment.IngredientTokenSegment)
        assertEquals("Мука", (segments[1] as StepSegment.IngredientTokenSegment).ingredientName)

        assertTrue(segments[2] is StepSegment.TextSegment)
        assertEquals(" и замесите тесто.", (segments[2] as StepSegment.TextSegment).text)
    }

    @Test
    fun testSampleDataTokensParsing() {
        val samplePayload = com.aetnox.cooking.data.database.SampleData.createSampleRecipeData()
        val allStepTokens = samplePayload.steps.flatMap { step ->
            StepTokenParser.extractIngredientNames(step.instructionText)
        }

        assertTrue("Sample recipe must contain ingredient tokens", allStepTokens.isNotEmpty())
        assertEquals(8, allStepTokens.size)

        // Check each token matches an ingredient in sample payload
        val ingredientDisplayNames = samplePayload.ingredients.map { ing ->
            if (ing.groupName.isNotBlank()) "${ing.name} (${ing.groupName})" else ing.name
        }

        for (token in allStepTokens) {
            assertTrue(
                "Token '$token' should match an ingredient display name",
                ingredientDisplayNames.contains(token)
            )
        }
    }
}
