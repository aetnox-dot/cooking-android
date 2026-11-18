package com.aetnox.cooking.domain.converter

import com.aetnox.cooking.ui.util.AppLocaleManager
import java.util.Locale
import kotlin.math.roundToInt

data class DensityInfo(
    val gPerTbsp: Double,  // ст. л.
    val gPerTsp: Double,   // ч. л.
    val gPerCup: Double    // стакан
)

object UnitConverter {

    val SUPPORTED_UNITS = listOf("г", "кг", "мл", "л", "ст. л.", "ч. л.", "стакан", "шт")

    fun getSupportedUnits(isEnglish: Boolean = AppLocaleManager.isEnglish): List<String> {
        return if (isEnglish) {
            listOf("g", "kg", "ml", "l", "tbsp", "tsp", "cup", "pc")
        } else {
            SUPPORTED_UNITS
        }
    }

    fun formatUnitForDisplay(unit: String, isEnglish: Boolean = AppLocaleManager.isEnglish): String {
        val norm = normalizeUnit(unit)
        if (!isEnglish) return norm
        return when (norm) {
            "г" -> "g"
            "кг" -> "kg"
            "мл" -> "ml"
            "л" -> "l"
            "ст. л." -> "tbsp"
            "ч. л." -> "tsp"
            "стакан" -> "cup"
            "шт" -> "pc"
            else -> unit.trim()
        }
    }

    // Density table for common ingredients (grams per unit)
    private val DENSITY_TABLE = mapOf(
        "мука" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 130.0),
        "мука пшеничная" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 130.0),
        "сахар" to DensityInfo(gPerTbsp = 20.0, gPerTsp = 7.0, gPerCup = 200.0),
        "сахарный песок" to DensityInfo(gPerTbsp = 20.0, gPerTsp = 7.0, gPerCup = 200.0),
        "соль" to DensityInfo(gPerTbsp = 25.0, gPerTsp = 8.0, gPerCup = 280.0),
        "крахмал" to DensityInfo(gPerTbsp = 12.0, gPerTsp = 4.0, gPerCup = 150.0),
        "крахмал кукурузный" to DensityInfo(gPerTbsp = 12.0, gPerTsp = 4.0, gPerCup = 150.0),
        "кукурузный крахмал" to DensityInfo(gPerTbsp = 12.0, gPerTsp = 4.0, gPerCup = 150.0),
        "крахмал картофельный" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 160.0),
        "картофельный крахмал" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 160.0),
        "масло сливочное" to DensityInfo(gPerTbsp = 16.0, gPerTsp = 5.0, gPerCup = 225.0),
        "сливочное масло" to DensityInfo(gPerTbsp = 16.0, gPerTsp = 5.0, gPerCup = 225.0),
        "масло растительное" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 200.0),
        "растительное масло" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 200.0),
        "подсолнечное масло" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 200.0),
        "вода" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 250.0),
        "молоко" to DensityInfo(gPerTbsp = 15.0, gPerTsp = 5.0, gPerCup = 250.0)
    )

    fun findDensity(ingredientName: String): DensityInfo? {
        val nameLower = ingredientName.trim().lowercase(Locale.ROOT)
        for ((key, density) in DENSITY_TABLE) {
            if (nameLower.contains(key)) {
                return density
            }
        }
        return null
    }

    /**
     * Checks whether converting from [fromUnit] to [toUnit] is valid for [ingredientName]
     * with 100% confidence.
     */
    fun canConvert(
        fromUnit: String,
        toUnit: String,
        ingredientName: String = "",
        customGramsPerUnit: Double? = null
    ): Boolean {
        val from = normalizeUnit(fromUnit)
        val to = normalizeUnit(toUnit)
        if (from == to) return true

        val massUnits = setOf("г", "кг")
        val volumeUnits = setOf("мл", "л", "ст. л.", "ч. л.", "стакан")

        if ((from in massUnits) && (to in massUnits)) return true
        if ((from in volumeUnits) && (to in volumeUnits)) return true

        // Cross-category mass <-> volume requires density info or custom factor
        if ((from in massUnits && to in volumeUnits) || (from in volumeUnits && to in massUnits)) {
            return customGramsPerUnit != null || findDensity(ingredientName) != null
        }

        // Piece items ("шт") to mass/volume requires custom factor
        if (from == "шт" || to == "шт") {
            return customGramsPerUnit != null && customGramsPerUnit > 0
        }

        return false
    }

    /**
     * Converts a base amount from [fromUnit] to [toUnit] for a given ingredient.
     * Takes into account density table when available, or standard volume/mass conversions.
     */
    fun convertAmount(
        amount: Double,
        fromUnit: String,
        toUnit: String,
        ingredientName: String = "",
        customGramsPerUnit: Double? = null
    ): Double {
        if (amount <= 0.0 || fromUnit == toUnit) return amount

        val from = normalizeUnit(fromUnit)
        val to = normalizeUnit(toUnit)
        if (from == to) return amount

        // Convert from source unit to base grams (or ml)
        val gramsOrMl = toGramsOrMl(amount, from, ingredientName, customGramsPerUnit)
            ?: return amount // Fallback if non-convertible (e.g. piece item to grams without custom factor)

        // Convert base grams/ml to target unit
        return fromGramsOrMl(gramsOrMl, to, ingredientName, customGramsPerUnit) ?: amount
    }

    private fun toGramsOrMl(
        amount: Double,
        unit: String,
        ingredientName: String,
        customGramsPerUnit: Double?
    ): Double? {
        val density = findDensity(ingredientName)
        return when (unit) {
            "г", "мл" -> amount
            "кг", "л" -> amount * 1000.0
            "ст. л." -> {
                if (customGramsPerUnit != null) amount * customGramsPerUnit
                else if (density != null) amount * density.gPerTbsp
                else amount * 15.0
            }
            "ч. л." -> {
                if (customGramsPerUnit != null) amount * (customGramsPerUnit / 3.0)
                else if (density != null) amount * density.gPerTsp
                else amount * 5.0
            }
            "стакан" -> {
                if (customGramsPerUnit != null) amount * (customGramsPerUnit * 16.0 / 15.0)
                else if (density != null) amount * density.gPerCup
                else amount * 250.0
            }
            "шт" -> {
                if (customGramsPerUnit != null) amount * customGramsPerUnit else null
            }
            else -> null
        }
    }

    private fun fromGramsOrMl(
        gramsOrMl: Double,
        targetUnit: String,
        ingredientName: String,
        customGramsPerUnit: Double?
    ): Double? {
        val density = findDensity(ingredientName)
        return when (targetUnit) {
            "г", "мл" -> gramsOrMl
            "кг", "л" -> gramsOrMl / 1000.0
            "ст. л." -> {
                val factor = customGramsPerUnit ?: density?.gPerTbsp ?: 15.0
                if (factor > 0) gramsOrMl / factor else null
            }
            "ч. л." -> {
                val factor = if (customGramsPerUnit != null) customGramsPerUnit / 3.0 else density?.gPerTsp ?: 5.0
                if (factor > 0) gramsOrMl / factor else null
            }
            "стакан" -> {
                val factor = density?.gPerCup ?: 250.0
                if (factor > 0) gramsOrMl / factor else null
            }
            "шт" -> {
                if (customGramsPerUnit != null && customGramsPerUnit > 0) gramsOrMl / customGramsPerUnit else null
            }
            else -> null
        }
    }

    /**
     * Scale ingredient quantity based on target portions vs base portions,
     * convert to target display unit, and apply rounding rule.
     */
    fun scaleAndFormatAmount(
        amount: Double,
        fromUnit: String,
        toUnit: String,
        baseServings: Double,
        currentServings: Double,
        ingredientName: String = "",
        customGramsPerUnit: Double? = null
    ): String {
        if (amount <= 0.0) return ""
        val multiplier = if (baseServings > 0) currentServings / baseServings else 1.0
        val scaled = amount * multiplier

        val normFrom = normalizeUnit(fromUnit)
        val normTo = normalizeUnit(toUnit)

        // If target unit cannot be converted with 100% confidence, fall back to original unit amount
        if (normFrom != normTo && !canConvert(fromUnit, toUnit, ingredientName, customGramsPerUnit)) {
            return formatAmount(scaled, fromUnit)
        }

        val converted = convertAmount(scaled, fromUnit, toUnit, ingredientName, customGramsPerUnit)
        return formatAmount(converted, toUnit)
    }

    /**
     * Format rules according to spec:
     * Whole numbers for grams (г) and milliliters (мл)
     * 1 decimal place for piece items (шт) and spoons (ст. л., ч. л.), etc.
     */
    fun formatAmount(value: Double, unit: String): String {
        if (value <= 0.0) return ""
        val normUnit = normalizeUnit(unit)
        return when (normUnit) {
            "г", "мл" -> {
                val rounded = value.roundToInt()
                rounded.toString()
            }
            else -> {
                // Round to 1 decimal place
                val rounded = (value * 10.0).roundToInt() / 10.0
                if (rounded % 1.0 == 0.0) {
                    rounded.toInt().toString()
                } else {
                    String.format(Locale.ROOT, "%.1f", rounded)
                }
            }
        }
    }

    fun normalizeUnit(unit: String): String {
        val u = unit.trim().lowercase(Locale.ROOT)
        return when {
            u in listOf("g", "г", "грамм", "грамма", "граммов") -> "г"
            u in listOf("kg", "кг", "килограмм", "килограмма") -> "кг"
            u in listOf("ml", "мл", "миллилитр", "миллилитра") -> "мл"
            u in listOf("l", "л", "литр", "литра") -> "л"
            u in listOf("ст.л.", "ст. л.", "ст л", "столовая ложка", "столовые ложки", "tbsp") -> "ст. л."
            u in listOf("ч.л.", "ч. л.", "ч л", "чайная ложка", "чайные ложки", "tsp") -> "ч. л."
            u in listOf("стакан", "стакана", "стаканов", "ст.", "cup", "cups") -> "стакан"
            u in listOf("шт", "шт.", "штука", "штуки", "штук", "pc", "pcs") -> "шт"
            else -> unit.trim()
        }
    }
}
