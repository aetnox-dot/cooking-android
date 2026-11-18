package com.aetnox.cooking.ui.util

object FormatUtils {
    /**
     * Плюрализация и форматирование понятия «витамин ИИ» / «AI vitamins» по правилам языка:
     * - Russian (`isEnglish = false`):
     *   - Full: 1 -> «витамин ИИ», 2-4 -> «витамина ИИ», 5-20/11-14 -> «витаминов ИИ», 21 -> «витамин ИИ»
     *   - Compact: «вит. ИИ»
     * - English (`isEnglish = true`):
     *   - Full: 1 -> «AI vitamin», else -> «AI vitamins»
     *   - Compact: «AI vit.»
     */
    fun formatVitaminAi(
        count: Int,
        short: Boolean = false,
        includeNumber: Boolean = true,
        isEnglish: Boolean = AppLocaleManager.isEnglish
    ): String {
        val absCount = kotlin.math.abs(count)
        if (isEnglish) {
            if (short) {
                val word = "AI vit."
                return if (includeNumber) "$count $word" else word
            }
            val word = if (absCount == 1) "AI vitamin" else "AI vitamins"
            return if (includeNumber) "$count $word" else word
        }

        if (short) {
            val word = "вит. ИИ"
            return if (includeNumber) "$count $word" else word
        }

        val rem100 = absCount % 100
        val rem10 = absCount % 10
        val word = when {
            rem100 in 11..14 -> "витаминов ИИ"
            rem10 == 1 -> "витамин ИИ"
            rem10 in 2..4 -> "витамина ИИ"
            else -> "витаминов ИИ"
        }
        return if (includeNumber) "$count $word" else word
    }
}
