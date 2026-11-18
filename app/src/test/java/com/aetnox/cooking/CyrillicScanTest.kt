package com.aetnox.cooking

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CyrillicScanTest {

    data class LiteralToken(
        val content: String,
        val rawLiteral: String,
        val lineNum: Int,
        val lineText: String
    )

    @Test
    fun scanForUntranslatedCyrillicStringsInUi() {
        val candidate1 = File("app/src/main/java/com/aetnox/cooking/ui")
        val candidate2 = File("src/main/java/com/aetnox/cooking/ui")
        val rootDir = when {
            candidate1.exists() -> candidate1
            candidate2.exists() -> candidate2
            else -> null
        }

        assertTrue(
            "UI directory not found at ${candidate1.absolutePath} or ${candidate2.absolutePath}",
            rootDir != null && rootDir.exists()
        )

        val cyrillicRegex = Regex("""[\u0400-\u04FF]""")
        val emptyTrRegex = Regex("""AppLocaleManager\.tr\(\s*"[^"]*"\s*,\s*""\s*\)""")
        val unhandled = mutableListOf<String>()

        rootDir!!.walkTopDown().filter { it.isFile && it.extension == "kt" }.forEach { file ->
            val relativePath = file.path.replace("\\", "/")
            val fileContent = file.readText()
            val lines = fileContent.lines()
            val tokens = extractStringLiterals(fileContent)

            tokens.forEach { token ->
                if (cyrillicRegex.containsMatchIn(token.content)) {
                    val lineIdx = token.lineNum - 1
                    val line = lines.getOrElse(lineIdx) { "" }
                    val trimmed = line.trim()

                    val contextStart = (lineIdx - 6).coerceAtLeast(0)
                    val contextEnd = (lineIdx + 1).coerceAtMost(lines.size)
                    val context = lines.subList(contextStart, contextEnd).joinToString("\n")

                    val isInsideTr = file.name in listOf("Theme.kt", "LogoManager.kt", "AppIconManager.kt", "FormatUtils.kt") ||
                            line.contains("AppLocaleManager.tr") ||
                            line.contains("trPlural") ||
                            line.contains("trFormat") ||
                            context.contains("AppLocaleManager.tr") ||
                            line.contains("enum class") || context.contains("enum class") ||
                            line.contains("ruLabel") || context.contains("ruLabel") ||
                            line.contains("enLabel") || context.contains("enLabel") ||
                            line.contains("ruName") || context.contains("ruName") ||
                            line.contains("ruTitle") || context.contains("ruTitle") ||
                            line.contains("ruHeadline") || context.contains("ruHeadline") ||
                            line.contains("ruStepTitle") || context.contains("ruStepTitle") ||
                            line.contains("ruDesc") || context.contains("ruDesc") ||
                            line.contains("ruCaption") || context.contains("ruCaption") ||
                            line.contains("ruQuestTitle") || context.contains("ruQuestTitle") ||
                            line.contains("ruActionPrompt") || context.contains("ruActionPrompt") ||
                            line.contains("ruDetail") || context.contains("ruDetail") ||
                            line.contains("isEnglish") || context.contains("isEnglish") ||
                            line.contains("Regex") || context.contains("Regex") ||
                            line.contains("RUSSIAN") || context.contains("RUSSIAN") ||
                            line.contains("contains(") || context.contains("contains(")

                    val hasEmptyTrEnglish = emptyTrRegex.containsMatchIn(line)

                    if (!isInsideTr || hasEmptyTrEnglish) {
                        unhandled.add("$relativePath:${token.lineNum}: \"${token.content}\" in: $trimmed")
                    }
                }
            }
        }

        if (unhandled.isNotEmpty()) {
            val failureMsg = "Found ${unhandled.size} unhandled Cyrillic string literals in UI:\n" +
                    unhandled.joinToString("\n")
            assertTrue(failureMsg, false)
        }
    }

    private fun extractStringLiterals(text: String): List<LiteralToken> {
        val tokens = mutableListOf<LiteralToken>()
        var i = 0
        val len = text.length
        var currentLine = 1

        fun getLineText(pos: Int): String {
            val lineEnd = text.indexOf('\n', pos).let { if (it == -1) len else it }
            val lineStart = text.lastIndexOf('\n', pos).let { if (it == -1) 0 else it + 1 }
            return text.substring(lineStart, lineEnd).trim()
        }

        while (i < len) {
            val ch = text[i]

            if (ch == '\n') {
                currentLine++
                i++
                continue
            }

            // Single line comment
            if (ch == '/' && i + 1 < len && text[i + 1] == '/') {
                i += 2
                while (i < len && text[i] != '\n') {
                    i++
                }
                continue
            }

            // Multi line comment
            if (ch == '/' && i + 1 < len && text[i + 1] == '*') {
                i += 2
                while (i + 1 < len && !(text[i] == '*' && text[i + 1] == '/')) {
                    if (text[i] == '\n') currentLine++
                    i++
                }
                if (i + 1 < len) i += 2
                continue
            }

            // Raw triple-quoted string """..."""
            if (ch == '"' && i + 2 < len && text[i + 1] == '"' && text[i + 2] == '"') {
                val startLine = currentLine
                val startPos = i
                i += 3
                val contentStart = i
                while (i + 2 < len && !(text[i] == '"' && text[i + 1] == '"' && text[i + 2] == '"')) {
                    if (text[i] == '\n') currentLine++
                    i++
                }
                val contentEnd = i
                if (i + 2 < len) i += 3
                val content = text.substring(contentStart, contentEnd)
                val rawLiteral = text.substring(startPos, (i).coerceAtMost(len))
                tokens.add(LiteralToken(content, rawLiteral, startLine, getLineText(startPos)))
                continue
            }

            // Standard string "..."
            if (ch == '"') {
                val startLine = currentLine
                val startPos = i
                i++
                val contentStart = i
                while (i < len && text[i] != '"' && text[i] != '\n') {
                    if (text[i] == '\\' && i + 1 < len) {
                        i += 2
                    } else {
                        i++
                    }
                }
                val contentEnd = i
                if (i < len && text[i] == '"') i++
                val content = text.substring(contentStart, contentEnd)
                val rawLiteral = text.substring(startPos, (i).coerceAtMost(len))
                tokens.add(LiteralToken(content, rawLiteral, startLine, getLineText(startPos)))
                continue
            }

            i++
        }

        return tokens
    }
}
