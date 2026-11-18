package com.aetnox.cooking.ui.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun testVitaminAiFullPluralization() {
        assertEquals("0 витаминов ИИ", FormatUtils.formatVitaminAi(0, isEnglish = false))
        assertEquals("1 витамин ИИ", FormatUtils.formatVitaminAi(1, isEnglish = false))
        assertEquals("2 витамина ИИ", FormatUtils.formatVitaminAi(2, isEnglish = false))
        assertEquals("3 витамина ИИ", FormatUtils.formatVitaminAi(3, isEnglish = false))
        assertEquals("4 витамина ИИ", FormatUtils.formatVitaminAi(4, isEnglish = false))
        assertEquals("5 витаминов ИИ", FormatUtils.formatVitaminAi(5, isEnglish = false))
        assertEquals("11 витаминов ИИ", FormatUtils.formatVitaminAi(11, isEnglish = false))
        assertEquals("12 витаминов ИИ", FormatUtils.formatVitaminAi(12, isEnglish = false))
        assertEquals("14 витаминов ИИ", FormatUtils.formatVitaminAi(14, isEnglish = false))
        assertEquals("21 витамин ИИ", FormatUtils.formatVitaminAi(21, isEnglish = false))
        assertEquals("22 витамина ИИ", FormatUtils.formatVitaminAi(22, isEnglish = false))
        assertEquals("25 витаминов ИИ", FormatUtils.formatVitaminAi(25, isEnglish = false))
    }

    @Test
    fun testVitaminAiCompactFormat() {
        assertEquals("1 вит. ИИ", FormatUtils.formatVitaminAi(1, short = true, isEnglish = false))
        assertEquals("2 вит. ИИ", FormatUtils.formatVitaminAi(2, short = true, isEnglish = false))
        assertEquals("5 вит. ИИ", FormatUtils.formatVitaminAi(5, short = true, isEnglish = false))
        assertEquals("21 вит. ИИ", FormatUtils.formatVitaminAi(21, short = true, isEnglish = false))
    }

    @Test
    fun testVitaminAiEnglishPluralization() {
        assertEquals("0 AI vitamins", FormatUtils.formatVitaminAi(0, isEnglish = true))
        assertEquals("1 AI vitamin", FormatUtils.formatVitaminAi(1, isEnglish = true))
        assertEquals("2 AI vitamins", FormatUtils.formatVitaminAi(2, isEnglish = true))
        assertEquals("5 AI vitamins", FormatUtils.formatVitaminAi(5, isEnglish = true))
        assertEquals("1 AI vit.", FormatUtils.formatVitaminAi(1, short = true, isEnglish = true))
        assertEquals("5 AI vit.", FormatUtils.formatVitaminAi(5, short = true, isEnglish = true))
    }
}
