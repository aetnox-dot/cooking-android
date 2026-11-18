package com.aetnox.cooking.data.database

import com.aetnox.cooking.data.entity.IngredientEntity
import com.aetnox.cooking.data.entity.RecipeEntity
import com.aetnox.cooking.data.entity.StepEntity
import com.aetnox.cooking.ui.util.AppLocaleManager

object SampleData {

    fun createSampleRecipeData(isEnglish: Boolean = AppLocaleManager.isEnglish): SampleRecipePayload {
        if (isEnglish) {
            val recipe = RecipeEntity(
                id = 0,
                title = "🍫 Chocolate Cherry Brownie (Sample)",
                prepTimeMinutes = 15,
                cookTimeMinutes = 25,
                servingsEnabled = true,
                baseServings = 4.0,
                yieldUnit = "servings",
                status = RecipeEntity.STATUS_VERIFIED,
                rating = 5,
                postCookNote = "Best served warm with a scoop of vanilla ice cream!",
                chefHeatingType = "Oven",
                chefTemperature = "180 °C",
                chefTiming = "25 minutes",
                chefReadinessSign = "Clean toothpick & fudgy center",
                notes = "💡 Chef Tip: Do not overbake brownies — keep the interior moist and fudgy!",
                rawNotes = "",
                sourceUrl = "",
                imageUrisJson = "[]",
                calories = 380.0,
                protein = 6.5,
                fat = 22.0,
                carbs = 38.0,
                updatedAt = System.currentTimeMillis()
            )

            val ingredients = listOf(
                IngredientEntity(id = 0, recipeId = 0, name = "Dark Chocolate (70%)", amount = 200.0, unit = "г", selectedUnit = "г", note = "broken into pieces", groupName = "For chocolate batter", orderIndex = 0),
                IngredientEntity(id = 0, recipeId = 0, name = "Butter", amount = 150.0, unit = "г", selectedUnit = "г", note = "room temperature", groupName = "For chocolate batter", orderIndex = 1),
                IngredientEntity(id = 0, recipeId = 0, name = "Sugar", amount = 150.0, unit = "г", selectedUnit = "г", note = "preferably cane", groupName = "For chocolate batter", orderIndex = 2),
                IngredientEntity(id = 0, recipeId = 0, name = "Eggs", amount = 3.0, unit = "шт", selectedUnit = "шт", note = "large", groupName = "For chocolate batter", orderIndex = 3),
                IngredientEntity(id = 0, recipeId = 0, name = "Flour", amount = 100.0, unit = "г", selectedUnit = "г", note = "sifted", groupName = "For chocolate batter", orderIndex = 4),
                IngredientEntity(id = 0, recipeId = 0, name = "Cocoa powder", amount = 2.0, unit = "ст. л.", selectedUnit = "ст. л.", note = "unsweetened", groupName = "For chocolate batter", orderIndex = 5),
                IngredientEntity(id = 0, recipeId = 0, name = "Pitted cherries", amount = 150.0, unit = "г", selectedUnit = "г", note = "fresh or thawed", groupName = "For filling", orderIndex = 6),
                IngredientEntity(id = 0, recipeId = 0, name = "Walnuts", amount = 50.0, unit = "г", selectedUnit = "г", note = "roughly chopped", groupName = "For filling", orderIndex = 7)
            )

            val steps = listOf(
                StepEntity(id = 0, recipeId = 0, stepNumber = 1, instructionText = "Melt @[Dark Chocolate (70%) (For chocolate batter)] and @[Butter (For chocolate batter)] in a double boiler or microwave until smooth.", imageUrisJson = "[]"),
                StepEntity(id = 0, recipeId = 0, stepNumber = 2, instructionText = "In a separate bowl, beat @[Eggs (For chocolate batter)] with @[Sugar (For chocolate batter)] for 3 minutes until fluffy.", imageUrisJson = "[]"),
                StepEntity(id = 0, recipeId = 0, stepNumber = 3, instructionText = "Pour melted chocolate mixture into eggs. Fold in @[Flour (For chocolate batter)] and @[Cocoa powder (For chocolate batter)] gently.", imageUrisJson = "[]"),
                StepEntity(id = 0, recipeId = 0, stepNumber = 4, instructionText = "Gently mix in @[Pitted cherries (For filling)] and chopped @[Walnuts (For filling)].", imageUrisJson = "[]"),
                StepEntity(id = 0, recipeId = 0, stepNumber = 5, instructionText = "Pour batter into a parchment-lined baking pan and bake in preheated oven at 180°C for 25 minutes.", imageUrisJson = "[]")
            )

            val tags = listOf("Tutorial", "Sample", "Baking", "Desserts", "Chocolate")
            return SampleRecipePayload(recipe, ingredients, steps, tags)
        }

        val recipe = RecipeEntity(
            id = 0,
            title = "🍫 Шоколадный Брауни с вишней (Образец)",
            prepTimeMinutes = 15,
            cookTimeMinutes = 25,
            servingsEnabled = true,
            baseServings = 4.0,
            yieldUnit = "порций",
            status = RecipeEntity.STATUS_VERIFIED,
            rating = 5,
            postCookNote = "Идеально подавать тёплым с шариком ванильного мороженого!",
            chefHeatingType = "Духовка",
            chefTemperature = "180 °C",
            chefTiming = "25 минут",
            chefReadinessSign = "Сухая шпажка и влажная хрустящая корочка",
            notes = "💡 Кулинарная хитрость: Не передерживайте брауни в духовке — внутри он должен оставаться слегка влажным и тягучим!",
            rawNotes = "",
            sourceUrl = "",
            imageUrisJson = "[]",
            calories = 380.0,
            protein = 6.5,
            fat = 22.0,
            carbs = 38.0,
            updatedAt = System.currentTimeMillis()
        )

        val ingredients = listOf(
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Шоколад тёмный (70%)",
                amount = 200.0,
                unit = "г",
                selectedUnit = "г",
                note = "поломать на дольки",
                groupName = "Для шоколадного теста",
                orderIndex = 0
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Масло сливочное",
                amount = 150.0,
                unit = "г",
                selectedUnit = "г",
                note = "комнатной температуры",
                groupName = "Для шоколадного теста",
                orderIndex = 1
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Сахар",
                amount = 150.0,
                unit = "г",
                selectedUnit = "г",
                note = "лучше тростниковый",
                groupName = "Для шоколадного теста",
                orderIndex = 2
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Яйца",
                amount = 3.0,
                unit = "шт",
                selectedUnit = "шт",
                note = "крупные",
                groupName = "Для шоколадного теста",
                orderIndex = 3
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Мука",
                amount = 100.0,
                unit = "г",
                selectedUnit = "г",
                note = "просеять",
                groupName = "Для шоколадного теста",
                orderIndex = 4
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Какао-порошок",
                amount = 2.0,
                unit = "ст. л.",
                selectedUnit = "ст. л.",
                note = "без сахара",
                groupName = "Для шоколадного теста",
                orderIndex = 5
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Вишня (без косточек)",
                amount = 150.0,
                unit = "г",
                selectedUnit = "г",
                note = "свежая или размороженная",
                groupName = "Для начинки",
                orderIndex = 6
            ),
            IngredientEntity(
                id = 0,
                recipeId = 0,
                name = "Грецкие орехи",
                amount = 50.0,
                unit = "г",
                selectedUnit = "г",
                note = "крупно порубить",
                groupName = "Для начинки",
                orderIndex = 7
            )
        )

        val steps = listOf(
            StepEntity(
                id = 0,
                recipeId = 0,
                stepNumber = 1,
                instructionText = "Растопите @[Шоколад тёмный (70%) (Для шоколадного теста)] и @[Масло сливочное (Для шоколадного теста)] на водяной бане или в микроволновке импульсами по 20 секунд до гладкой массы.",
                imageUrisJson = "[]"
            ),
            StepEntity(
                id = 0,
                recipeId = 0,
                stepNumber = 2,
                instructionText = "В отдельной миске взбейте миксером @[Яйца (Для шоколадного теста)] с @[Сахар (Для шоколадного теста)] в пышную светлую пену в течение 3 минут.",
                imageUrisJson = "[]"
            ),
            StepEntity(
                id = 0,
                recipeId = 0,
                stepNumber = 3,
                instructionText = "Влейте растопленную шоколадно-масляную смесь к яйцам. Добавьте @[Мука (Для шоколадного теста)] и @[Какао-порошок (Для шоколадного теста)], затем аккуратно перемешайте лопаткой движениями снизу вверх.",
                imageUrisJson = "[]"
            ),
            StepEntity(
                id = 0,
                recipeId = 0,
                stepNumber = 4,
                instructionText = "Аккуратно вмешайте в тесто @[Вишня (без косточек) (Для начинки)] и порубленные @[Грецкие орехи (Для начинки)].",
                imageUrisJson = "[]"
            ),
            StepEntity(
                id = 0,
                recipeId = 0,
                stepNumber = 5,
                instructionText = "Переложите тесто в форму, выстланную пергаментом, и выпекайте в разогретой духовке при 180°C ровно 25 минут.",
                imageUrisJson = "[]"
            )
        )

        val tags = listOf("Обучение", "Образец", "Выпечка", "Десерты", "Шоколад")

        return SampleRecipePayload(recipe, ingredients, steps, tags)
    }
}

data class SampleRecipePayload(
    val recipe: RecipeEntity,
    val ingredients: List<IngredientEntity>,
    val steps: List<StepEntity>,
    val tags: List<String>
)
