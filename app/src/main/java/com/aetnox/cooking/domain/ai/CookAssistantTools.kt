package com.aetnox.cooking.domain.ai

import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.ui.viewmodel.AssistantMessage

/**
 * Агентный слой ассистента при готовке.
 *
 * Нейронке НЕ передаётся весь рецепт. Вместо этого она получает краткий индекс
 * и список инструментов. Если данных не хватает — модель отвечает строкой
 * «TOOL: имя | аргумент», приложение локально выполняет инструмент и подкладывает
 * результат обратно в промпт. Так токены тратятся только на реально нужные данные.
 */
data class ToolCall(val name: String, val arg: String)

data class ToolResult(
    val text: String,
    val newStepIndex: Int? = null,
    val timerSuggestionSeconds: Int? = null,
    val timerSuggestionLabel: String? = null
)

object CookAssistantTools {

    const val MAX_TOOL_ITERATIONS = 3

    fun protocolDescription(): String = """
        Ты — кулинарный ассистент с инструментами доступа к рецепту.
        Правила:
        - Если для ответа не хватает данных — вызови инструмент строкой: TOOL: имя | аргумент
        - Если аргумент не нужен, оставь после «|» пусто: TOOL: get_all_steps |
        - За один диалог — максимум 3 вызова подряд, потом обязателен финальный ответ.
        - Финальный ответ — кратко (до 100 слов), по делу, на русском, без markdown-заголовков.

        Инструменты:
        get_step | N — текст шага N
        get_all_steps | — все шаги пронумерованным списком
        get_ingredients | — ингредиенты на текущее количество порций
        get_notes | — заметки к рецепту
        get_raw_notes | — черновая заметка пользователя
        get_chef_sheet | — шпаргалка шефа (нагрев, температура, тайминг, признак готовности)
        get_nutrition | — калории и КБЖУ
        go_to_step | N — перейти к шагу N на экране (только когда повар просит перейти/открыть шаг)
        suggest_timer | время, описание — предложить пользователю запустить таймер (пример: suggest_timer | 10 минут | Варить пасту)
    """.trimIndent()

    /** Ищет строку «TOOL: ...» в ответе модели. Остальной текст в этой итерации игнорируем. */
    fun parseToolCall(reply: String): ToolCall? {
        if (reply.isBlank()) return null

        val line = reply.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.contains("TOOL:", ignoreCase = true) }
            ?: return null

        val body = line.substringAfter("TOOL:", "").ifBlank { line.substringAfter("tool:", "") }.trim()
        if (body.isBlank()) return null

        // Separate action name and argument string cleanly
        val firstSepIdx = body.indexOfAny(charArrayOf(':', '|'))
        var rawName = (if (firstSepIdx != -1) body.substring(0, firstSepIdx) else body.substringBefore(' ')).trim().lowercase()
        var rawArg = (if (firstSepIdx != -1) body.substring(firstSepIdx + 1) else if (body.contains(' ')) body.substringAfter(' ') else "").trim().removePrefix("|").removePrefix(":").trim()

        // Extract trailing digits if embedded in name, e.g. "go_to_step_3" -> name="go_to_step", arg="3"
        val trailingDigitMatch = Regex("""^([a-z_]+?)_?(\d+)$""").find(rawName)
        if ((trailingDigitMatch != null) && rawArg.isBlank()) {
            rawName = trailingDigitMatch.groupValues[1].trimEnd('_')
            rawArg = trailingDigitMatch.groupValues[2]
        }

        // Normalize tool name aliases
        val normalizedName = when (rawName) {
            "get_steps", "get_all_step", "get_step_all", "all_steps", "steps" -> "get_all_steps"
            "get_ingredient", "ingredients", "ingredient", "get_all_ingredients" -> "get_ingredients"
            "get_note", "notes", "note" -> "get_notes"
            "raw_notes", "raw_note" -> "get_raw_notes"
            "chef_sheet", "get_chef", "chef", "chef_info" -> "get_chef_sheet"
            "nutrition", "get_kbzhu", "kbzhu", "calories", "get_calories" -> "get_nutrition"
            "next_step", "step_next", "go_next" -> {
                if (rawArg.isBlank()) rawArg = "next"
                "go_to_step"
            }
            "prev_step", "previous_step", "step_prev", "go_prev" -> {
                if (rawArg.isBlank()) rawArg = "prev"
                "go_to_step"
            }
            "set_timer", "add_timer", "timer", "create_timer" -> "suggest_timer"
            else -> rawName
        }

        return ToolCall(normalizedName, rawArg)
    }

    /** Локальное выполнение инструмента — без сети и без токенов. */
    fun execute(
        call: ToolCall,
        details: RecipeWithDetails,
        currentServings: Double,
        activeStepIndex: Int = 1
    ): ToolResult {
        val recipe = details.recipe
        val steps = details.steps.sortedBy { it.stepNumber }

        return when (call.name) {
            "get_step" -> {
                val n = call.arg.toIntOrNull()
                val step = n?.let { steps.getOrNull(it - 1) }
                if (step == null) {
                    ToolResult("Ошибка: шага '${call.arg}' нет. Всего шагов: ${steps.size}.")
                } else {
                    ToolResult("Шаг ${step.stepNumber}: ${cleanStepText(step.instructionText)}")
                }
            }

            "get_all_steps" -> {
                if (steps.isEmpty()) {
                    ToolResult("Шагов нет.")
                } else {
                    ToolResult(steps.joinToString("\n") { "${it.stepNumber}. ${cleanStepText(it.instructionText)}" })
                }
            }

            "get_ingredients" -> {
                if (details.ingredients.isEmpty()) {
                    ToolResult("Ингредиенты не указаны.")
                } else {
                    val lines = details.ingredients.map { ing ->
                        val amt = UnitConverter.scaleAndFormatAmount(
                            amount = ing.amount,
                            fromUnit = ing.unit,
                            toUnit = ing.selectedUnit,
                            baseServings = recipe.baseServings,
                            currentServings = currentServings,
                            ingredientName = ing.name,
                            customGramsPerUnit = ing.gramsPerUnit
                        )
                        buildString {
                            append("- ").append(ing.name)
                            if (amt.isNotEmpty()) append(" — ").append(amt).append(' ').append(ing.selectedUnit)
                            if (ing.note.isNotBlank()) append(" (").append(ing.note).append(')')
                        }
                    }
                    ToolResult(lines.joinToString("\n"))
                }
            }

            "get_notes" -> {
                val notes = listOfNotNull(
                    recipe.notes.ifBlank { null },
                    recipe.postCookNote.ifBlank { null }?.let { "Заметка после готовки: $it" }
                )
                if (notes.isEmpty()) ToolResult("Заметок нет.")
                else ToolResult(notes.joinToString("\n"))
            }

            "get_raw_notes" -> {
                if (recipe.rawNotes.isBlank()) ToolResult("Черновая заметка пуста.")
                else ToolResult(recipe.rawNotes)
            }

            "get_chef_sheet" -> {
                val bits = listOfNotNull(
                    recipe.chefHeatingType.ifBlank { null }?.let { "Нагрев: $it" },
                    recipe.chefTemperature.ifBlank { null }?.let { "Температура: $it" },
                    recipe.chefTiming.ifBlank { null }?.let { "Тайминг: $it" },
                    recipe.chefReadinessSign.ifBlank { null }?.let { "Признак готовности: $it" }
                )
                if (bits.isEmpty()) ToolResult("Шпаргалка не заполнена.")
                else ToolResult(bits.joinToString("\n"))
            }

            "get_nutrition" -> {
                if (recipe.calories == null && recipe.protein == null && recipe.fat == null && recipe.carbs == null) {
                    ToolResult("КБЖУ не указано.")
                } else {
                    val bits = listOfNotNull(
                        recipe.calories?.let { "Калории: ${it.toInt()} ккал" },
                        recipe.protein?.let { "Белки: ${it.toInt()} г" },
                        recipe.fat?.let { "Жиры: ${it.toInt()} г" },
                        recipe.carbs?.let { "Углеводы: ${it.toInt()} г" }
                    )
                    ToolResult(bits.joinToString("\n"))
                }
            }

            "go_to_step" -> {
                val argLower = call.arg.lowercase().trim()
                val targetStep = when {
                    argLower == "next" || argLower.contains("следующ") || argLower.contains("вперед") ->
                        (activeStepIndex + 1).coerceAtMost(steps.size)
                    argLower == "prev" || argLower.contains("предыдущ") || argLower.contains("назад") ->
                        (activeStepIndex - 1).coerceAtLeast(1)
                    else -> call.arg.toIntOrNull()
                }

                if (targetStep != null && targetStep in 1..steps.size) {
                    ToolResult("Экран перелистан к шагу $targetStep.", newStepIndex = targetStep)
                } else {
                    ToolResult("Ошибка: шага '${call.arg}' нет. Всего шагов: ${steps.size}.")
                }
            }

            "suggest_timer", "set_timer" -> {
                val parts = call.arg.split("|", limit = 2)
                val timeStr = parts.getOrNull(0)?.trim().orEmpty()
                val label = parts.getOrNull(1)?.trim()?.ifEmpty { "Таймер" } ?: "Таймер"

                val parsedSec = com.aetnox.cooking.ui.viewmodel.StepTimerParser.parseDurationInSeconds(timeStr)
                    ?: timeStr.toIntOrNull()?.let { if (it < 60) it * 60 else it }

                if (parsedSec == null || parsedSec <= 0) {
                    ToolResult("Ошибка: не удалось распознать время '$timeStr'. Укажите время, например: 5 минут, 30 сек.")
                } else {
                    val formatted = com.aetnox.cooking.ui.viewmodel.StepTimerParser.formatTime(parsedSec)
                    ToolResult(
                        text = "Пользователю показана кнопка для запуска таймера «$label» на $formatted. Ожидание решения пользователя.",
                        timerSuggestionSeconds = parsedSec,
                        timerSuggestionLabel = label
                    )
                }
            }

            else -> ToolResult("Ошибка: неизвестный инструмент '${call.name}'.")
        }
    }

    /**
     * Стартовый промпт: только индекс рецепта, а не его полный текст.
     * Модель сама дозапросит недостающее через инструменты.
     */
    fun buildInitialPrompt(
        details: RecipeWithDetails,
        currentServings: Double,
        activeStepIndex: Int,
        history: List<AssistantMessage>,
        question: String
    ): String {
        val recipe = details.recipe
        val totalSteps = details.steps.size

        val sb = StringBuilder()
        sb.append(protocolDescription()).append("\n\n")

        sb.append("Краткий контекст: рецепт «").append(recipe.title).append("»")
        if (recipe.servingsEnabled) {
            sb.append(", порций ")
                .append(UnitConverter.formatAmount(currentServings, "шт"))
                .append(' ').append(recipe.yieldUnit)
        }
        sb.append(", всего шагов: ").append(totalSteps)
        if (activeStepIndex in 1..totalSteps) {
            sb.append(", сейчас открыт шаг ").append(activeStepIndex)
        } else {
            sb.append(", сейчас этап подготовки ингредиентов")
        }
        sb.append('\n')

        if (history.isNotEmpty()) {
            sb.append("\nНедавний диалог:\n")
            history.forEach { msg ->
                sb.append(if (msg.isUser) "Повар: " else "Ассистент: ").append(msg.text).append('\n')
            }
        }

        sb.append("\nВопрос повара: ").append(question)
        return sb.toString()
    }

    /** Продолжение диалога после выполнения инструмента. */
    fun appendToolResult(prompt: String, call: ToolCall, result: ToolResult): String {
        return prompt +
            "\n\nРезультат инструмента «${call.name}»:\n" + result.text +
            "\n\nТеперь дай финальный ответ повару (если данных всё ещё не хватает — вызови ещё один инструмент тем же форматом TOOL: имя | аргумент)."
    }

    private fun cleanStepText(text: String): String {
        return text.replace(Regex("""@\[([^]]+)\]"""), "$1")
    }
}
