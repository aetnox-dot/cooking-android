package com.aetnox.cooking.data.remote

import android.content.Context
import com.aetnox.cooking.data.remote.model.*
import retrofit2.Response

/**
 * Типизированная ошибка бэкенда (документация, раздел 4).
 * [errorCode] — строка ERROR_CODE из ответа сервера, [httpCode] — HTTP-статус.
 */
class ApiException(
    val httpCode: Int,
    val errorCode: String?,
    message: String
) : Exception(message)

/**
 * Высокоуровневая обёртка над [CookingApiService]: единая обработка ошибок,
 * управление сессией и жизненный цикл запуска (документация, раздел 2).
 */
class BackendRepository(private val appContext: Context) {

    private val api: CookingApiService
        get() = BackendClient.api(appContext)

    // --- Жизненный цикл запуска ---

    /** Первый вызов при старте: конфигурация приложения (min версия, техработы). */
    suspend fun getAppConfig(): Result<AppConfigResponse> = runCall {
        api.getAppConfig()
    }.onSuccess { config ->
        SessionManager.saveAppConfig(appContext, config)
    }

    /**
     * Гарантирует наличие валидной сессии: если токена нет — вызывает /auth/init
     * с device_id и сохраняет результат в DataStore.
     */
    suspend fun ensureSession(): Result<AuthInitResponse> {
        if (SessionManager.getToken(appContext) != null) {
            return Result.failure(IllegalStateException("Сессия уже существует"))
        }
        return initAuth()
    }

    /** Принудительная (пере)авторизация устройства. */
    suspend fun initAuth(): Result<AuthInitResponse> {
        val request = AuthInitRequest(
            deviceId = DeviceUtils.getHardwareDeviceId(appContext),
            deviceInfo = DeviceUtils.getDeviceInfo()
        )
        return runCall { api.initAuth(request) }.onSuccess { auth ->
            SessionManager.saveSession(
                appContext,
                auth.sessionToken,
                auth.accountId,
                auth.planLevel,
                auth.subscriptionUntil
            )
        }
    }

    // --- Пользователь ---

    // --- Пользователь ---

    companion object {
        @Volatile
        private var lastStatusFetchTime: Long = 0L

        @Volatile
        private var cachedUserStatus: UserStatusResponse? = null
    }

    suspend fun getUserStatus(forceRefresh: Boolean = false): Result<UserStatusResponse> {
        val now = System.currentTimeMillis()
        if (!forceRefresh && (cachedUserStatus != null) && (now - lastStatusFetchTime < 5000L)) {
            return Result.success(cachedUserStatus!!)
        }
        return runCall {
            api.getUserStatus()
        }.onSuccess { status ->
            cachedUserStatus = status
            lastStatusFetchTime = System.currentTimeMillis()
            SessionManager.saveUserStatus(appContext, status)
        }
    }

    suspend fun activateKey(key: String): Result<ActivateKeyResponse> = runCall {
        api.activateKey(ActivateKeyRequest(key))
    }

    // --- Перенос подписки ---

    suspend fun createTransferCode(): Result<TransferCreateResponse> = runCall {
        api.createTransferCode()
    }

    suspend fun claimTransferCode(code: String): Result<TransferClaimResponse> =
        runCall { api.claimTransferCode(TransferClaimRequest(code)) }.onSuccess {
            SessionManager.updatePlan(appContext, it.planLevel, it.subscriptionUntil)
        }

    // --- ИИ-функции ---

    /** Оцифровка рецепта из сырого текста (максимум 4000 символов по документации). */
    suspend fun parseRecipeFromText(rawText: String): Result<RecipeParseResponse> {
        if (rawText.length > 4000) {
            return Result.failure(ApiException(400, null, "Текст рецепта длиннее 4000 символов"))
        }
        val result = runCall { api.parseRecipe(RecipeParseRequest(rawText = rawText)) }
        result.getOrNull()?.remainingMonth?.let { rem ->
            SessionManager.updateAiRequestsRemaining(appContext, rem)
        }
        return result
    }

    /** Оцифровка рецепта из фотографии (требует allow_photos == true в тарифе). */
    suspend fun parseRecipeFromImage(imageBase64: String, mimeType: String): Result<RecipeParseResponse> {
        val result = runCall { api.parseRecipe(RecipeParseRequest(imageBase64 = imageBase64, mimeType = mimeType)) }
        result.getOrNull()?.remainingMonth?.let { rem ->
            SessionManager.updateAiRequestsRemaining(appContext, rem)
            runCatching { getUserStatus(forceRefresh = true) }
        }
        return result
    }

    /** Итерация кухонного ассистента. Ответ tool_call парсить как "TOOL: <command> | <arg>". */
    suspend fun assistantStep(
        recipeContext: String,
        messages: List<ChatMessageDto>,
        allowRecipeUpdates: Boolean = true
    ): Result<AssistantStepResponse> {
        val result = runCall { api.assistantStep(AssistantStepRequest(recipeContext, messages, allowRecipeUpdates)) }
        result.getOrNull()?.remainingMonth?.let { rem ->
            SessionManager.updateAiRequestsRemaining(appContext, rem)
        }
        return result
    }

    // --- Облачные бэкапы (Раздел 3) ---

    suspend fun uploadCloudBackup(recipes: List<BackupRecipeDto>): Result<CloudBackupUploadResponse> = runCall {
        api.uploadCloudBackup(CloudBackupUploadRequest(recipes))
    }

    suspend fun getCloudBackup(): Result<CloudBackupGetResponse> = runCall {
        api.getCloudBackup()
    }

    // --- Облачные рецепты ---

    suspend fun uploadRecipe(
        recipeId: String,
        payloadJson: String,
        isPublic: Boolean
    ): Result<RecipeUploadResponse> = runCall {
        api.uploadRecipe(RecipeUploadRequest(recipeId = recipeId, payloadJson = payloadJson, isPublic = isPublic))
    }

    suspend fun changeRecipeVisibility(
        recipeId: String,
        isPublic: Boolean
    ): Result<SuccessResponse> = runCall {
        api.changeRecipeVisibility(RecipeVisibilityRequest(recipeId = recipeId, isPublic = isPublic))
    }

    suspend fun getCloudRecipe(recipeId: String): Result<CloudRecipeResponse> = runCall {
        api.getCloudRecipe(recipeId)
    }

    suspend fun searchCloudRecipes(
        query: String,
        page: Int = 0,
        relaxed: Boolean = false
    ): Result<RecipeSearchResponse> {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return Result.failure(ApiException(400, null, "Поисковый запрос должен содержать минимум 2 символа"))
        }
        val relaxedParam = if (relaxed) 1 else null
        return runCall { api.searchCloudRecipes(query = trimmed, page = page, relaxed = relaxedParam) }
    }

    /** Диалоговый ИИ-поиск рецептов (одиночный запрос). */
    suspend fun aiSearch(
        scope: String = "all",
        limit: Int = 10,
        messages: List<ChatMessageDto>,
        localRecipes: List<AiSearchLocalRecipeDto>? = null
    ): Result<AiSearchResponse> {
        val request = AiSearchRequest(
            scope = scope,
            limit = limit,
            messages = messages,
            localRecipes = localRecipes
        )
        val result = runCall { api.aiSearch(request) }
        result.getOrNull()?.remainingMonth?.let { rem ->
            SessionManager.updateAiRequestsRemaining(appContext, rem)
        }
        return result
    }

    /** Двухфазный ИИ-поиск v2 — Шаг 1 (генерация плана поиска, квоту не списывает). */
    suspend fun aiSearchPlan(
        scope: String = "all",
        messages: List<ChatMessageDto>,
        cloudIds: List<String>? = null
    ): Result<AiSearchPlanResponse> = runCall {
        api.aiSearchPlan(AiSearchPlanRequest(scope = scope, messages = messages, cloudIds = cloudIds))
    }

    /** Двухфазный ИИ-поиск v2 — Шаг 2 (финиш с локальными совпадениями, списывает квоту). */
    suspend fun aiSearchFinish(
        searchToken: String,
        messages: List<ChatMessageDto>,
        limit: Int = 10,
        localMatches: List<AiSearchLocalMatchDto> = emptyList()
    ): Result<AiSearchResponse> {
        val request = AiSearchFinishRequest(
            searchToken = searchToken,
            messages = messages,
            limit = limit,
            localMatches = localMatches
        )
        val result = runCall { api.aiSearchFinish(request) }
        result.getOrNull()?.remainingMonth?.let { rem ->
            SessionManager.updateAiRequestsRemaining(appContext, rem)
        }
        return result
    }

    suspend fun getRecipeByAccessCode(code: String): Result<CloudRecipeByCodeResponse> {
        val trimmed = code.trim()
        if (trimmed.isBlank()) {
            return Result.failure(ApiException(400, null, "Код доступа не может быть пустым"))
        }
        return runCall { api.getRecipeByAccessCode(trimmed) }
    }

    suspend fun listMyRecipes(): Result<RecipesListResponse> = runCall {
        api.listMyRecipes()
    }

    suspend fun listPublicRecipes(page: Int = 0, limit: Int = 100): Result<RecipesListResponse> = runCall {
        api.listPublicRecipes(page = page, limit = limit)
    }

    suspend fun deleteCloudRecipe(recipeId: String): Result<SuccessResponse> = runCall {
        api.deleteCloudRecipe(recipeId)
    }

    // --- Выход, аккаунт и фидбек ---

    suspend fun logout(): Result<SuccessResponse> {
        val result = runCall { api.logout() }
        SessionManager.clear(appContext)
        return result
    }

    suspend fun sendFeedback(message: String): Result<SuccessResponse> {
        if (message.isBlank()) {
            return Result.failure(ApiException(400, null, "Сообщение не может быть пустым"))
        }
        return runCall { api.sendFeedback(FeedbackRequest(message)) }
    }

    // --- Общая обработка ошибок ---

    private suspend fun <T> runCall(block: suspend () -> Response<T>): Result<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(ApiException(response.code(), null, "Пустой ответ сервера"))
                }
            } else {
                Result.failure(parseError(response))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(ApiException(-1, null, formatNetworkException(e)))
        }
    }

    private fun formatNetworkException(e: Exception): String {
        return NetworkDiagnostics.formatErrorMessage(appContext, e)
    }

    private fun parseError(response: Response<*>): ApiException {
        val rawBody = runCatching { response.errorBody()?.string() }.getOrNull()
        val parsed = rawBody?.let {
            runCatching {
                BackendClient.json.decodeFromString(ApiErrorResponse.serializer(), it)
            }.getOrNull()
        }
        val errText = parsed?.error
        val errCode = parsed?.errorCode
        val isEnglish = com.aetnox.cooking.ui.util.AppLocaleManager.isEnglish
        val endpoint = response.raw().request.url.encodedPath

        val friendlyMessage = ErrorLocalizationManager.getLocalizedErrorMessage(
            httpCode = response.code(),
            errorCode = errCode,
            serverErrorText = errText,
            endpoint = endpoint,
            isEnglish = isEnglish
        )
        return ApiException(
            httpCode = response.code(),
            errorCode = errCode,
            message = friendlyMessage
        )
    }
}
