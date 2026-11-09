package com.aetnox.cooking.domain.parser

sealed class StepSegment {
    data class TextSegment(val text: String) : StepSegment()
    data class IngredientTokenSegment(
        val ingredientName: String,
        val rawToken: String
    ) : StepSegment()
}

object StepTokenParser {

    private val TOKEN_REGEX = Regex("""@\[([^\]]+)\]|@\{([^\}]+)\}""")

    /**
     * Inserts an ingredient token `@[IngredientName]` into the text at the given cursor position.
     */
    fun insertToken(text: String, selectionStart: Int, ingredientName: String): Pair<String, Int> {
        val safeIndex = selectionStart.coerceIn(0, text.length)
        val tokenStr = "@[$ingredientName] "
        val newText = StringBuilder(text).insert(safeIndex, tokenStr).toString()
        val newSelection = safeIndex + tokenStr.length
        return Pair(newText, newSelection)
    }

    /**
     * Extracts all ingredient names referenced in tokens in the step text.
     */
    fun extractIngredientNames(text: String): List<String> {
        return TOKEN_REGEX.findAll(text).map { match ->
            match.groupValues[1].ifEmpty { match.groupValues[2] }
        }.toList()
    }

    /**
     * Parses step text into a list of segments (normal text vs ingredient tokens).
     */
    fun parseSegments(text: String): List<StepSegment> {
        if (text.isEmpty()) return emptyList()

        val segments = mutableListOf<StepSegment>()
        var lastIndex = 0

        for (match in TOKEN_REGEX.findAll(text)) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                segments.add(StepSegment.TextSegment(text.substring(lastIndex, start)))
            }

            val ingName = match.groupValues[1].ifEmpty { match.groupValues[2] }
            segments.add(StepSegment.IngredientTokenSegment(ingredientName = ingName, rawToken = match.value))

            lastIndex = end
        }

        if (lastIndex < text.length) {
            segments.add(StepSegment.TextSegment(text.substring(lastIndex)))
        }

        return segments
    }
}
