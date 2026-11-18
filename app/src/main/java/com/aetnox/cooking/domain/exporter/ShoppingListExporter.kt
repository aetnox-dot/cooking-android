package com.aetnox.cooking.domain.exporter

import com.aetnox.cooking.data.relation.RecipeWithDetails
import com.aetnox.cooking.domain.converter.UnitConverter
import com.aetnox.cooking.ui.util.AppLocaleManager
import com.aetnox.cooking.ui.util.CustomCartItem

data class ConsolidatedIngredient(
    val key: String,
    val name: String,
    val totalAmount: Double,
    val unit: String,
    val groupName: String,
    val notes: List<String>
)

object ShoppingListExporter {

    fun consolidateIngredients(
        recipes: List<RecipeWithDetails>,
        recipeServingsMap: Map<Long, Double> = emptyMap(),
        isEnglish: Boolean = AppLocaleManager.isEnglish
    ): List<ConsolidatedIngredient> {
        val ingredientMap = mutableMapOf<String, ConsolidatedIngredient>()

        for (recipeWithDetails in recipes) {
            val baseServings = recipeWithDetails.recipe.baseServings
            val targetServings = recipeServingsMap[recipeWithDetails.recipe.id] ?: baseServings
            val multiplier = if (baseServings > 0 && targetServings > 0) targetServings / baseServings else 1.0

            for (ing in recipeWithDetails.ingredients) {
                if (ing.name.isBlank()) continue

                val normName = ing.name.trim()
                val normUnit = ing.selectedUnit.ifBlank { ing.unit }
                val key = "${normName.lowercase()}_$normUnit"
                val scaledAmount = ing.amount * multiplier

                val current = ingredientMap[key]
                if (current != null) {
                    val updatedNotes = if (ing.note.isNotBlank() && !current.notes.contains(ing.note)) {
                        current.notes + ing.note
                    } else current.notes

                    ingredientMap[key] = current.copy(
                        totalAmount = current.totalAmount + scaledAmount,
                        notes = updatedNotes
                    )
                } else {
                    val smartGroup = com.aetnox.cooking.domain.converter.IngredientCategoryMapper.getCategoryForIngredient(normName, ing.groupName)
                    val notesList = if (ing.note.isNotBlank()) listOf(ing.note) else emptyList()
                    ingredientMap[key] = ConsolidatedIngredient(
                        key = key,
                        name = normName,
                        totalAmount = scaledAmount,
                        unit = normUnit,
                        groupName = smartGroup,
                        notes = notesList
                    )
                }
            }
        }

        return ingredientMap.values.toList()
    }

    fun generateShoppingListText(
        recipes: List<RecipeWithDetails>,
        recipeServingsMap: Map<Long, Double> = emptyMap(),
        checkedKeys: Set<String> = emptySet(),
        customItems: List<CustomCartItem> = emptyList(),
        isEnglish: Boolean = AppLocaleManager.isEnglish
    ): String {
        if (recipes.isEmpty() && customItems.isEmpty()) {
            return if (isEnglish) "Shopping list is empty." else "Список покупок пуст."
        }

        val sb = StringBuilder()
        if (isEnglish) {
            sb.append("🛒 SHOPPING LIST\n")
            if (recipes.isNotEmpty()) {
                sb.append("Recipes (").append(recipes.size).append("): ")
                    .append(recipes.joinToString(", ") { "\"${it.recipe.title}\"" })
                    .append("\n\n")
            }
        } else {
            sb.append("🛒 СПИСОК ПОКУПОК\n")
            if (recipes.isNotEmpty()) {
                sb.append("Рецепты (").append(recipes.size).append("): ")
                    .append(recipes.joinToString(", ") { "«${it.recipe.title}»" })
                    .append("\n\n")
            }
        }

        val consolidated = consolidateIngredients(recipes, recipeServingsMap, isEnglish)
        val grouped = consolidated.groupBy { it.groupName }

        grouped.forEach { (groupName, list) ->
            sb.append("📌 ").append(groupName).append(":\n")
            for (item in list.sortedBy { it.name }) {
                val isChecked = checkedKeys.contains(item.key)
                val checkMarker = if (isChecked) "[v] " else "[ ] "
                val formattedAmt = UnitConverter.formatAmount(item.totalAmount, item.unit)
                val displayUnit = UnitConverter.formatUnitForDisplay(item.unit, isEnglish)

                sb.append(checkMarker).append(item.name)
                if (formattedAmt.isNotEmpty() && item.totalAmount > 0) {
                    sb.append(" — ").append(formattedAmt).append(" ").append(displayUnit)
                }
                if (item.notes.isNotEmpty()) {
                    sb.append(" (").append(item.notes.joinToString(", ")).append(")")
                }
                sb.append("\n")
            }
            sb.append("\n")
        }

        if (customItems.isNotEmpty()) {
            val customTitle = if (isEnglish) "📌 Custom Purchases:" else "📌 Свои покупки:"
            sb.append(customTitle).append("\n")
            for (item in customItems) {
                val checkMarker = if (item.isChecked) "[v] " else "[ ] "
                sb.append(checkMarker).append(item.name).append("\n")
            }
            sb.append("\n")
        }

        return sb.toString().trim()
    }
}
