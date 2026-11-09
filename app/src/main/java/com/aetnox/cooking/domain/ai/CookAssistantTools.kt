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
        val line = reply.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("TOOL:", ignoreCase = true) }
            ?: return null

        val body = line.substringAfter(':').trim()
        if (body.isBlank()) return null

        val name = body.substringBefore('|').trim().lowercase()
        val arg = if ('|' in body) body.substringAfter('|').trim() else ""
        if (name.isEmpty()) return null

        return ToolCall(name, arg)
    }

    /** Локальное выполнение инструмента — без сети и без токенов. */
    fun execute(call: ToolCall, details: RecipeWithDetails, currentServings: Double): ToolResult {
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
                val n = call.arg.toIntOrNull()
                if (n != null && n in 1..steps.size) {
                    ToolResult("Экран перелистан к шагу $n.", newStepIndex = n)
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
                    ToolResult("Ошибка: не удалось распознать время '${timeStr}'. Укажите время, например: 5 минут, 30 сек.")
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
        return text.replace(Regex("""@\[([^\]]+)\]"""), "$1")
    }
}
