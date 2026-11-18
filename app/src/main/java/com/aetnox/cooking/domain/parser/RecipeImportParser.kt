package com.aetnox.cooking.domain.parser

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class JsonImportModel(
    val title: String? = null,
    val name: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val prepTime: Int? = null,
    val cookTime: Int? = null,
    val servings: Double? = null,
    val yieldUnit: String? = null,
    val chefHeatingType: String? = null,
    val chefTemperature: String? = null,
    val chefTiming: String? = null,
    val chefReadinessSign: String? = null,
    val ingredients: List<JsonIngredientModel>? = null,
    val steps: List<String>? = null,
    val instructions: List<String>? = null,
    val tags: List<String>? = null,
    val notes: String? = null,
    val sourceUrl: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
)

@Serializable
private data class JsonIngredientModel(
    val name: String? = null,
    val amount: kotlinx.serialization.json.JsonElement? = null,
    val quantity: kotlinx.serialization.json.JsonElement? = null,
    val unit: String? = null,
    val note: String? = null,
    val groupName: String? = null,
    @kotlinx.serialization.SerialName("group_name") val groupNameSnakeCase: String? = null,
    val group: String? = null,
    val section: String? = null,
    val category: String? = null
)

data class ParsedRecipe(
    val recipe: RecipeEntity,
    val ingredients: List<IngredientEntity>,
    val steps: List<StepEntity>,
    val tags: List<String>
)

object RecipeImportParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private fun extractJsonString(element: kotlinx.serialization.json.JsonElement?): String {
        if (element == null) return ""
        return if (element is kotlinx.serialization.json.JsonPrimitive) {
            element.content
        } else {
            element.toString()
        }
    }

    fun parse(rawText: String): ParsedRecipe {
        val trimmed = rawText.trim()
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val jsonModel = json.decodeFromString<JsonImportModel>(trimmed)
                return convertJsonModel(jsonModel)
            } catch (_: Exception) {
                // Fallback to text parsing
            }
        }

        return parseTextFormat(trimmed)
    }

    private fun convertJsonModel(model: JsonImportModel): ParsedRecipe {
        val title = model.title ?: model.name ?: "Импортированный рецепт"
        val prepTime = model.prepTimeMinutes ?: model.prepTime ?: 0
        val cookTime = model.cookTimeMinutes ?: model.cookTime ?: 0
        val baseServings = model.servings ?: 4.0
        val yieldUnit = model.yieldUnit ?: "порций"

        val recipe = RecipeEntity(
            title = title,
            prepTimeMinutes = prepTime,
            cookTimeMinutes = cookTime,
            baseServings = baseServings,
            yieldUnit = yieldUnit,
            chefHeatingType = model.chefHeatingType ?: "",
            chefTemperature = model.chefTemperature ?: "",
            chefTiming = model.chefTiming ?: "",
            chefReadinessSign = model.chefReadinessSign ?: "",
            notes = model.notes ?: "",
            sourceUrl = model.sourceUrl ?: "",
            calories = model.calories,
            protein = model.protein,
            fat = model.fat,
            carbs = model.carbs,
            status = RecipeEntity.STATUS_PLANNED
        )

        val ingredients = (model.ingredients ?: emptyList()).mapIndexed { index, ing ->
            val rawAmountStr = extractJsonString(ing.amount).ifBlank { extractJsonString(ing.quantity) }
            val (parsedNum, noteStr) = parseSafeAmount(rawAmountStr)
            val finalNote = listOfNotNull(
                ing.note?.ifBlank { null },
                noteStr?.ifBlank { null }
            ).joinToString("; ")
            val group = ing.groupName ?: ing.groupNameSnakeCase ?: ing.group ?: ing.section ?: ing.category ?: ""

            IngredientEntity(
                recipeId = 0,
                name = ing.name ?: "Ингредиент",
                amount = parsedNum,
                unit = ing.unit ?: "г",
                selectedUnit = ing.unit ?: "г",
                note = finalNote,
                groupName = group,
                orderIndex = index
            )
        }

        val rawSteps = model.steps ?: model.instructions ?: emptyList()
        val steps = rawSteps.mapIndexed { index, stepText ->
            StepEntity(
                recipeId = 0,
                stepNumber = index + 1,
                instructionText = stepText
            )
        }

        val tags = model.tags ?: emptyList()

        return ParsedRecipe(recipe, ingredients, steps, tags)
    }

    private fun parseTextFormat(text: String): ParsedRecipe {
        val lines = text.lines()
        var title = "Новый рецепт"
        val ingredients = mutableListOf<IngredientEntity>()
        val steps = mutableListOf<StepEntity>()
        val tags = mutableListOf<String>()
        var notes = ""

        var currentSection = "TITLE" // TITLE, INGREDIENTS, STEPS, NOTES
        var currentGroupName = ""

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            val lower = trimmedLine.lowercase()
            when {
                lower.startsWith("ингредиенты") || lower.startsWith("состав") ||
                lower.startsWith("компоненты") || lower.startsWith("продукты") ||
                lower.startsWith("понадобится") || lower.startsWith("нужно:") || lower.startsWith("надо:") -> {
                    currentSection = "INGREDIENTS"
                    currentGroupName = ""
                    continue
                }
                lower.startsWith("шаги") || lower.startsWith("приготовление") ||
                lower.startsWith("инструкция") || lower.startsWith("пошагово") ||
                lower.startsWith("как готовить") || lower.startsWith("процесс") ||
                lower.startsWith("способ приготовления") || lower.startsWith("порядок действий") -> {
                    currentSection = "STEPS"
                    continue
                }
                lower.startsWith("теги") || lower.startsWith("метки") || lower.startsWith("категории") -> {
                    val tagPart = trimmedLine.substringAfter(":").trim()
                    tags.addAll(tagPart.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() })
                    continue
                }
                lower.startsWith("заметки") || lower.startsWith("примечания") || lower.startsWith("советы") -> {
                    currentSection = "NOTES"
                    continue
                }
            }

            when (currentSection) {
                "TITLE" -> {
                    if (trimmedLine.startsWith("# ")) {
                        title = trimmedLine.substring(2).trim()
                    } else if (trimmedLine.contains(":")) {
                        val key = trimmedLine.substringBefore(":").lowercase()
                        if (key.contains("название") || key.contains("рецепт")) {
                            title = trimmedLine.substringAfter(":").trim()
                        } else {
                            title = trimmedLine
                        }
                    } else {
                        title = trimmedLine
                    }
                    currentSection = "INGREDIENTS"
                }
                "INGREDIENTS" -> {
                    // Check if line defines an ingredient sub-group section (e.g., "Для теста:", "📌 Для соуса")
                    if (trimmedLine.endsWith(":") || trimmedLine.startsWith("📌") ||
                        trimmedLine.lowercase().startsWith("для ") || trimmedLine.lowercase().startsWith("соус")
                    ) {
                        val potentialGroupName = trimmedLine
                            .removePrefix("📌")
                            .removePrefix("#")
                            .removeSuffix(":")
                            .trim()
                        if (potentialGroupName.length < 35 && !potentialGroupName.contains("—") && !potentialGroupName.contains("-")) {
                            currentGroupName = potentialGroupName
                            continue
                        }
                    }

                    val ing = parseIngredientLine(trimmedLine, ingredients.size, currentGroupName)
                    if (ing != null) {
                        ingredients.add(ing)
                    }
                }
                "STEPS" -> {
                    val cleanStep = trimmedLine.replaceFirst(Regex("""^(\d+[.)]|-|\*|📌)\s*"""), "")
                    if (cleanStep.isNotEmpty()) {
                        steps.add(
                            StepEntity(
                                recipeId = 0,
                                stepNumber = steps.size + 1,
                                instructionText = cleanStep
                            )
                        )
                    }
                }
                "NOTES" -> {
                    notes = if (notes.isEmpty()) trimmedLine else "$notes\n$trimmedLine"
                }
            }
        }

        val recipe = RecipeEntity(
            title = title.ifBlank { "Новый рецепт" },
            notes = notes,
            status = RecipeEntity.STATUS_PLANNED
        )

        return ParsedRecipe(recipe, ingredients, steps, tags)
    }

    private fun parseIngredientLine(line: String, index: Int, groupName: String): IngredientEntity? {
        val clean = line.replaceFirst(Regex("""^(-\s*|\*\s*|\d+[\.\)]\s*|📌\s*)"""), "").trim()
        if (clean.isEmpty()) return null

        // Try regex patterns like: "Мука — 200 г" or "Мука 200g (просеянная)" or "200g мука"
        val regex = Regex("""^(.+?)\s*[-—:]\s*([\d/\.,\s–-]+)\s*([a-zA-Zа-яА-Я\.\s]+)?(?:\((.*)\))?$""")
        val match = regex.find(clean)

        if (match != null) {
            val name = match.groupValues[1].trim()
            val amountStr = match.groupValues[2].trim()
            val unitStr = match.groupValues[3].trim().ifBlank { "г" }
            val noteInParen = match.groupValues[4].trim()

            val (amount, extraNote) = parseSafeAmount(amountStr)
            val fullNote = listOfNotNull(noteInParen.ifBlank { null }, extraNote?.ifBlank { null }).joinToString("; ")

            return IngredientEntity(
                recipeId = 0,
                name = name,
                amount = amount,
                unit = unitStr,
                selectedUnit = unitStr,
                note = fullNote,
                groupName = groupName,
                orderIndex = index
            )
        }

        // Fallback: parse numbers from line
        val numMatch = Regex("""([\d/\.,]+)""").find(clean)
        if (numMatch != null) {
            val amountStr = numMatch.value
            val (amount, extraNote) = parseSafeAmount(amountStr)
            val name = clean.replace(amountStr, "").trim()
            return IngredientEntity(
                recipeId = 0,
                name = name.ifBlank { clean },
                amount = amount,
                unit = "г",
                selectedUnit = "г",
                note = extraNote ?: "",
                groupName = groupName,
                orderIndex = index
            )
        }

        // Non-numeric amount: store entire quantity string in note
        return IngredientEntity(
            recipeId = 0,
            name = clean,
            amount = 0.0,
            unit = "г",
            selectedUnit = "г",
            note = "по вкусу",
            groupName = groupName,
            orderIndex = index
        )
    }

    data class ParsedAmountUnit(
        val amount: Double,
        val unit: String?,
        val extraNote: String?
    )

    fun parseAmountAndUnit(rawStr: String, baseAmount: Double = 0.0): ParsedAmountUnit {
        val clean = rawStr.trim().replace(',', '.')
        if (clean.isEmpty()) return ParsedAmountUnit(0.0, null, null)

        val lower = clean.lowercase()
        val isDecrease = lower.contains("меньше") || lower.contains("минус") || lower.contains("уменьш")
        val isIncrease = lower.contains("больше") || lower.contains("плюс") || lower.contains("увелич")

        val numMatchInRelative = Regex("""(\d+(?:\.\d+)?)""").find(clean)
        val extractedVal = numMatchInRelative?.groupValues?.get(1)?.toDoubleOrNull()

        if ((isDecrease || isIncrease) && extractedVal != null && baseAmount > 0.0) {
            val delta = if (isDecrease) -extractedVal else extractedVal
            val calculatedAmount = maxOf(0.0, baseAmount + delta)
            val unitPart = clean.replace(Regex("""(?i)(на|меньше|больше|минус|плюс|уменьшить|увеличить|\d+(?:\.\d+)?)"""), "").trim().ifBlank { null }
            return ParsedAmountUnit(calculatedAmount, unitPart, null)
        }

        val match = Regex("""^(\d+(?:\.\d+)?|\d+\s+\d+/\d+|\d+/\d+)\s*(.*)$""").find(clean)
        if (match != null) {
            val numPart = match.groupValues[1]
            val unitPart = match.groupValues[2].trim().ifBlank { null }
            val (num, note) = parseSafeAmount(numPart)
            if (num > 0.0 || numPart == "0") {
                return ParsedAmountUnit(num, unitPart, note)
            }
        }

        val (num, note) = parseSafeAmount(clean)
        return ParsedAmountUnit(num, null, note)
    }

    /**
     * Safely converts amount string to Double (supporting mixed fractions like "1 1/2",
     * simple fractions like "1/2", ranges like "2-3", and trailing unit strings like "210 мл").
     * Non-numeric strings are returned as note remark.
     */
    fun parseSafeAmount(rawStr: String): Pair<Double, String?> {
        val clean = rawStr.trim().replace(',', '.')
        if (clean.isEmpty()) return Pair(0.0, null)

        // Range amounts like "2-3" or "2–3"
        val rangeMatch = Regex("""^(\d+(?:\.\d+)?)\s*[\-–—]\s*(\d+(?:\.\d+)?)(?:\s*(.*))?$""").find(clean)
        if (rangeMatch != null) {
            val first = rangeMatch.groupValues[1].toDoubleOrNull()
            val second = rangeMatch.groupValues[2].toDoubleOrNull()
            if (first != null && second != null) {
                val avg = (first + second) / 2.0
                return Pair(avg, rawStr)
            }
        }

        // Mixed fraction like "1 1/2"
        val mixedMatch = Regex("""^(\d+)\s+(\d+)/(\d+)(?:\s*(.*))?$""").find(clean)
        if (mixedMatch != null) {
            val whole = mixedMatch.groupValues[1].toDoubleOrNull() ?: 0.0
            val num = mixedMatch.groupValues[2].toDoubleOrNull() ?: 0.0
            val den = mixedMatch.groupValues[3].toDoubleOrNull() ?: 1.0
            if (den != 0.0) {
                return Pair(whole + (num / den), null)
            }
        }

        // Simple fraction like "1/2", "3/4"
        val fracMatch = Regex("""^(\d+)/(\d+)(?:\s*(.*))?$""").find(clean)
        if (fracMatch != null) {
            val num = fracMatch.groupValues[1].toDoubleOrNull()
            val den = fracMatch.groupValues[2].toDoubleOrNull()
            if (num != null && den != null && den != 0.0) {
                return Pair(num / den, null)
            }
        }

        // Leading number with optional trailing unit string, e.g. "210 мл", "210г", "1.5 л"
        val numMatch = Regex("""^(\d+(?:\.\d+)?)(?:\s*(.*))?$""").find(clean)
        if (numMatch != null) {
            val parsed = numMatch.groupValues[1].toDoubleOrNull()
            if (parsed != null) {
                return Pair(parsed, null)
            }
        }

        val parsed = clean.toDoubleOrNull()
        return if (parsed != null) {
            Pair(parsed, null)
        } else {
            Pair(0.0, clean)
        }
    }
}
