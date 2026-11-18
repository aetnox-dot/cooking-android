package com.aetnox.cooking.data.remote

import com.aetnox.cooking.data.remote.model.ApiErrorCodes
import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorLocalizationManagerTest {

    @Test
    fun testTableAErrorCodesRussian() {
        val unauthorized = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 401,
            errorCode = ApiErrorCodes.UNAUTHORIZED,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Требуется авторизация / Сессия не найдена или истекла", unauthorized)

        val quotaExceeded = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 403,
            errorCode = ApiErrorCodes.QUOTA_EXCEEDED,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Недостаточно лимита витаминов ИИ.", quotaExceeded)

        val photoQuota = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 403,
            errorCode = ApiErrorCodes.PHOTO_QUOTA_EXCEEDED,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Месячный лимит фото-оцифровок исчерпан", photoQuota)
    }

    @Test
    fun testTableAErrorCodesEnglish() {
        val unauthorized = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 401,
            errorCode = ApiErrorCodes.UNAUTHORIZED,
            serverErrorText = null,
            isEnglish = true
        )
        assertEquals("Authorization required / Session not found or expired", unauthorized)

        val photoQuota = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 403,
            errorCode = ApiErrorCodes.PHOTO_QUOTA_EXCEEDED,
            serverErrorText = null,
            isEnglish = true
        )
        assertEquals("Monthly photo digitization limit reached", photoQuota)
    }

    @Test
    fun testSessionExpiredEndpointDistinction() {
        val authExpiredRu = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 401,
            errorCode = ApiErrorCodes.SESSION_EXPIRED,
            serverErrorText = null,
            endpoint = "/v1/recipes/backup",
            isEnglish = false
        )
        assertEquals("Сессия авторизации истекла. Войдите снова", authExpiredRu)

        val searchExpiredRu = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 410,
            errorCode = ApiErrorCodes.SESSION_EXPIRED,
            serverErrorText = null,
            endpoint = "/v1/recipes/ai-search/finish",
            isEnglish = false
        )
        assertEquals("Сессия поиска истекла. Начните поиск заново", searchExpiredRu)
    }

    @Test
    fun testTableBMapping() {
        val server502 = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 502,
            errorCode = null,
            serverErrorText = "The AI service is temporarily unavailable.",
            isEnglish = true
        )
        assertEquals("The AI service is temporarily unavailable. Please try again later.", server502)

        val imageTooLarge = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 413,
            errorCode = ApiErrorCodes.IMAGE_TOO_LARGE,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Файл изображения слишком большой (максимум ~5 МБ)", imageTooLarge)
    }

    @Test
    fun testSearchSessionCodes() {
        val notFound = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 404,
            errorCode = ApiErrorCodes.SEARCH_SESSION_NOT_FOUND,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Сессия поиска не найдена. Начните поиск заново", notFound)

        val expired = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 410,
            errorCode = ApiErrorCodes.SEARCH_SESSION_EXPIRED,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Сессия поиска истекла. Начните поиск заново", expired)
    }

    @Test
    fun testAiCascadeCodes() {
        val unavailable = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 502,
            errorCode = ApiErrorCodes.AI_UNAVAILABLE,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Сервис ИИ временно недоступен. Попробуйте снова через 1-2 минуты.", unavailable)

        val overloaded = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 502,
            errorCode = ApiErrorCodes.AI_OVERLOADED,
            serverErrorText = null,
            isEnglish = true
        )
        assertEquals("All available AI models are overloaded. Please try again in 1-2 minutes.", overloaded)
    }

    @Test
    fun testValidationAndTransferCodes() {
        val emptyRequest = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 400,
            errorCode = ApiErrorCodes.EMPTY_REQUEST,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Укажите текст или прикрепите фото", emptyRequest)

        val invalidKey = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 400,
            errorCode = ApiErrorCodes.INVALID_KEY,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Неверный или уже использованный ключ", invalidKey)

        val cooldown = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 429,
            errorCode = ApiErrorCodes.TRANSFER_COOLDOWN,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Перенос временно заблокирован (кулдаун 7 дней)", cooldown)

        val notFound = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = 404,
            errorCode = ApiErrorCodes.CLOUD_RECIPE_NOT_FOUND,
            serverErrorText = null,
            isEnglish = false
        )
        assertEquals("Рецепт не найден в облаке", notFound)
    }
}
