package com.aetnox.cooking.domain.converter

import com.aetnox.cooking.ui.util.AppLocaleManager

object IngredientCategoryMapper {

    private val produceKeywords = listOf(
        "картофель", "картошка", "лук", "чеснок", "морковь", "помидор", "томат", "огурец",
        "перец", "зелень", "укроп", "петрушка", "базилик", "салат", "кинза", "мята", "шпинат",
        "яблоко", "яблоки", "лимон", "лайм", "авокадо", "банан", "бананы", "апельсин", "ягоды",
        "клубника", "малина", "вишня", "черешня", "капуста", "грибы", "шампиньоны", "кабачок",
        "баклажан", "тыква", "брокколи", "цветная капуста", "груша", "персик", "слива", "сельдерей",
        "potato", "onion", "garlic", "carrot", "tomato", "cucumber", "pepper", "herbs", "dill",
        "parsley", "basil", "salad", "spinach", "apple", "lemon", "lime", "avocado", "banana",
        "berry", "berries", "cabbage", "mushroom", "mushrooms", "zucchini", "eggplant", "pumpkin"
    )

    private val dairyKeywords = listOf(
        "молоко", "сливки", "масло сливочное", "масло сливочного", "сыр", "моцарелла",
        "пармезан", "сулугуни", "фета", "рикотта", "маскарпоне", "творог", "сметана",
        "йогурт", "кефир", "ряженка", "яйцо", "яйца", "желток", "белок",
        "milk", "cream", "butter", "cheese", "mozzarella", "parmesan", "feta", "ricotta",
        "cottage cheese", "sour cream", "yogurt", "egg", "eggs", "yolk"
    )

    private val meatKeywords = listOf(
        "курица", "куриное", "куриный", "филе", "фарш", "говядина", "свинина", "бекон",
        "колбаса", "сосиски", "ветчина", "индейка", "утка", "баранина", "мясо", "сало", "рёбрышки",
        "chicken", "beef", "pork", "bacon", "sausage", "turkey", "lamb", "meat", "minced"
    )

    private val seafoodKeywords = listOf(
        "рыба", "лосось", "семга", "горбуша", "форель", "креветки", "тунец", "треска",
        "мидии", "кальмар", "осьминог", "икра", "краб", "крабовые",
        "fish", "salmon", "trout", "shrimp", "shrimps", "prawns", "tuna", "cod", "squid", "crab"
    )

    private val bakeryKeywords = listOf(
        "хлеб", "батон", "булочка", "лаваш", "пита", "багет", "круассан", "панировочные",
        "bread", "bun", "pita", "baguette", "croissant", "breadcrumbs"
    )

    private val pantryKeywords = listOf(
        "мука", "рис", "гречка", "макароны", "спагетти", "паста", "овсянка", "геркулес",
        "перловка", "пшено", "сахар", "сахарная пудра", "крахмал", "сода", "дрожжи", "разрыхлитель",
        "желатин", "какао-порошок",
        "flour", "rice", "spaghetti", "pasta", "noodle", "noodles", "oats", "oatmeal", "sugar",
        "starch", "soda", "yeast", "baking powder", "gelatin", "cocoa"
    )

    private val spicesKeywords = listOf(
        "соль", "перец черный", "перец красный", "паприка", "корица", "ванилин", "ванили",
        "соевый соус", "масло растительное", "масло оливковое", "масло подсолнечное", "уксус",
        "майонез", "кетчуп", "томатная паста", "горчица", "специи", "приправа", "орегано",
        "прованские", "куркума", "карри", "зира", "лавр", "лавровый",
        "salt", "pepper", "paprika", "cinnamon", "vanilla", "soy sauce", "oil", "olive oil",
        "sunflower oil", "vinegar", "mayonnaise", "ketchup", "mustard", "spices", "spice",
        "seasoning", "oregano", "turmeric", "curry", "bay leaf"
    )

    private val sweetsKeywords = listOf(
        "шоколад", "какао", "орехи", "миндаль", "арахис", "грецкий орех", "фундук", "кешью",
        "изюм", "курага", "чернослив", "мед", "мёд", "джем", "варенье", "чай", "кофе", "сок",
        "вода", "газировка", "чипсы", "печенье",
        "chocolate", "cocoa", "nuts", "nut", "almond", "peanuts", "walnut", "hazelnut", "cashew",
        "raisins", "honey", "jam", "tea", "coffee", "juice", "water", "cookies"
    )

    fun getCategoryForIngredient(ingredientName: String, originalGroupName: String = ""): String {
        val normGroup = originalGroupName.trim()

        // If user already assigned a custom meaningful group name (other than default), respect it
        if (normGroup.isNotBlank() &&
            !normGroup.equals("Общие продукты", ignoreCase = true) &&
            !normGroup.equals("General items", ignoreCase = true)
        ) {
            return normGroup
        }

        val name = ingredientName.lowercase()

        return when {
            produceKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🥬 Овощи, фрукты и зелень", "🥬 Produce & Vegetables")
            dairyKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🥛 Молочные продукты и яйца", "🥛 Dairy & Eggs")
            meatKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🥩 Мясо и птица", "🥩 Meat & Poultry")
            seafoodKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🐟 Рыба и морепродукты", "🐟 Seafood & Fish")
            bakeryKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🍞 Хлеб и выпечка", "🍞 Bakery & Bread")
            pantryKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🌾 Бакалея и крупы", "🌾 Grains & Pantry")
            spicesKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🧂 Специи, соусы и масла", "🧂 Spices, Sauces & Oils")
            sweetsKeywords.any { name.contains(it) } -> AppLocaleManager.tr("🍫 Сладости и напитки", "🍫 Snacks & Drinks")
            else -> AppLocaleManager.tr("🛍️ Общие покупки", "🛍️ General Items")
        }
    }
}
