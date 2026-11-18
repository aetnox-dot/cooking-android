package com.aetnox.cooking.domain.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.aetnox.cooking.domain.net.AppOkHttp
import com.aetnox.cooking.domain.parser.AiPromptTemplates
import com.aetnox.cooking.domain.parser.ParsedRecipe
import com.aetnox.cooking.domain.parser.RecipeImportParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
private data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
private data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
private data class GeminiPart(
    val text: String? = null,
    @SerialName("inline_data") val inlineData: InlineData? = null,
    val thought: Boolean? = null
)

@Serializable
private data class InlineData(
    @SerialName("mime_type") val mimeType: String,
    val data: String
)

@Serializable
private data class GenerationConfig(
    val temperature: Double? = 0.2,
    val responseMimeType: String? = "application/json",
    val maxOutputTokens: Int? = null,
    val thinkingConfig: ThinkingConfig? = null
)

@Serializable
private data class ThinkingConfig(
    val thinkingBudget: Int
)

@Serializable
private data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@Serializable
private data class Candidate(
    val content: GeminiContent? = null
)

@Serializable
private data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

object GeminiApiService {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        // Не сериализуем null-поля: строгие прокси коверкают запросы вида "responseMimeType": null
        explicitNulls = false
    }

    private fun createOkHttpClient(context: Context): okhttp3.OkHttpClient {
        // Единая фабрика: применяет экспериментальные сетевые настройки (DNS)
        return AppOkHttp.client(context)
    }

    suspend fun generateRecipe(
        context: Context,
        promptInput: String,
        imageUriStr: String? = null,
        onConnected: (() -> Unit)? = null
    ): ParsedRecipe = withContext(Dispatchers.IO) {
        if (com.aetnox.cooking.domain.net.ExperimentalSettingsManager.isAiDisabled(context)) {
            throw IllegalStateException("ИИ полностью отключен в экспериментальных настройках.")
        }
        val settings = GeminiSettingsManager.getSettings(context)
        val apiKey = settings.apiKey.ifBlank {
            throw IllegalArgumentException("API-ключ Gemini не указан! Введите его в Настройках.")
        }

        val parts = mutableListOf<GeminiPart>()

        // Add System/Instructions Prompt + User Request
        val fullSystemPrompt = "${AiPromptTemplates.AI_RECIPE_JSON_PROMPT}\n\nЗапрос пользователя:\n$promptInput"
        parts.add(GeminiPart(text = fullSystemPrompt))

        // Process image if provided
        if (!imageUriStr.isNullOrBlank()) {
            val inlineData = readImageAsInlineData(context, imageUriStr)
            if (inlineData != null) {
                parts.add(GeminiPart(inlineData = inlineData))
            }
        }

        val requestPayload = GeminiRequest(
            contents = listOf(GeminiContent(parts = parts)),
            generationConfig = GenerationConfig(temperature = 0.2, responseMimeType = "application/json")
        )

        val requestJsonStr = json.encodeToString(GeminiRequest.serializer(), requestPayload)
        val baseUrlRaw = settings.customBaseUrl.removeSuffix("/")
        val baseUrl = baseUrlRaw.toHttpUrlOrNull()
            ?: throw IllegalStateException("Некорректный URL провайдера: '$baseUrlRaw'. Исправьте его в Настройках.")
        val model = settings.model.ifBlank { GeminiSettingsManager.DEFAULT_MODEL }
        val endpointUrl = baseUrl.newBuilder()
            .addPathSegments("v1beta/models/$model:generateContent")
            .build()

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJsonStr.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val client = createOkHttpClient(context)

        // Результат самого запроса и подтвердит соединение — прогресс освобождаем сразу
        withContext(Dispatchers.Main) {
            onConnected?.invoke()
        }

        val responseText = client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errResp = json.decodeFromString<GeminiResponse>(bodyStr)
                    errResp.error?.message ?: "Ошибка HTTP ${response.code}: ${response.message}"
                } catch (_: Exception) {
                    "Ошибка HTTP ${response.code}: $bodyStr"
                }
                throw IllegalStateException("Ошибка Gemini API: $errorMsg")
            }
            bodyStr
        }

        val geminiResp = json.decodeFromString<GeminiResponse>(responseText)
        val candidateText = geminiResp.candidates?.firstOrNull()?.content?.parts
            ?.filter { (it.thought != true) && !it.text.isNullOrBlank() }
            ?.mapNotNull { it.text }
            ?.joinToString("")
            ?: ""
        if (candidateText.isBlank()) {
            throw IllegalStateException("Gemini вернул пустой ответ")
        }

        val cleanJson = cleanMarkdownJson(candidateText)
        RecipeImportParser.parse(cleanJson)
    }

    suspend fun testConnection(context: Context): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (com.aetnox.cooking.domain.net.ExperimentalSettingsManager.isAiDisabled(context)) {
                return@withContext Result.failure(IllegalStateException("ИИ полностью отключен в экспериментальных настройках."))
            }
            val settings = GeminiSettingsManager.getSettings(context)
            val apiKey = settings.apiKey.trim()

            if (apiKey.isBlank() || apiKey == GeminiSettingsManager.DEFAULT_API_KEY) {
                return@withContext Result.failure(IllegalArgumentException("Не указан ваш API-ключ! Введите ваш ключ от Google AI Studio (начинается с AIzaSy...)."))
            }

            val baseUrlRaw = settings.customBaseUrl.removeSuffix("/")
            val baseUrl = baseUrlRaw.toHttpUrlOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Некорректный URL провайдера: '$baseUrlRaw'. Исправьте его в Настройках."))
            val model = settings.model.ifBlank { GeminiSettingsManager.DEFAULT_MODEL }
            val endpointUrl = baseUrl.newBuilder()
                .addPathSegments("v1beta/models/$model")
                .build()

            val request = Request.Builder()
                .url(endpointUrl)
                .addHeader("x-goog-api-key", apiKey)
                .get()
                .build()

            val client = createOkHttpClient(context)

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()

                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errResp = json.decodeFromString<GeminiResponse>(body)
                        errResp.error?.message ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }

                    if (response.code == 400 && (errorMsg.contains("API key", ignoreCase = true) || errorMsg.contains("invalid", ignoreCase = true))) {
                        return@withContext Result.failure(IllegalStateException("Неверный API-ключ ($errorMsg). Получите ваш бесплатный ключ на aistudio.google.com"))
                    }
                    if (response.code == 403) {
                        return@withContext Result.failure(IllegalStateException("Доступ заблокирован Google (HTTP 403)."))
                    }

                    return@withContext Result.failure(IllegalStateException("❌ Ошибка: $errorMsg"))
                }

                val successMsg = "Google подтвердил доступность модели $model!"
                Result.success(successMsg)
            }
        } catch (e: Exception) {
            val errMsg = "❌ ${e.localizedMessage ?: e.javaClass.simpleName}"
            Result.failure(IllegalStateException(errMsg, e))
        }
    }

    suspend fun fetchAvailableModels(context: Context): Result<List<GeminiSettingsManager.ModelOption>> = withContext(Dispatchers.IO) {
        try {
            if (com.aetnox.cooking.domain.net.ExperimentalSettingsManager.isAiDisabled(context)) {
                return@withContext Result.failure(IllegalStateException("ИИ полностью отключен в экспериментальных настройках."))
            }
            val settings = GeminiSettingsManager.getSettings(context)
            val apiKey = settings.apiKey.ifBlank {
                return@withContext Result.failure(IllegalArgumentException("Ключ API не заполнен!"))
            }

            val baseUrlRaw = settings.customBaseUrl.removeSuffix("/")
            val baseUrl = baseUrlRaw.toHttpUrlOrNull()
                ?: return@withContext Result.failure(IllegalStateException("Некорректный URL провайдера: '$baseUrlRaw'. Исправьте его в Настройках."))
            val endpointUrl = baseUrl.newBuilder()
                .addPathSegments("v1beta/models")
                .build()

            val request = Request.Builder()
                .url(endpointUrl)
                .addHeader("x-goog-api-key", apiKey)
                .get()
                .build()

            val client = createOkHttpClient(context)

            val fetchedList = mutableListOf<GeminiSettingsManager.ModelOption>()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    val errorMsg = try {
                        val errResp = json.decodeFromString<GeminiResponse>(body)
                        errResp.error?.message ?: "HTTP ${response.code}"
                    } catch (e: Exception) {
                        "HTTP ${response.code}"
                    }
                    return@withContext Result.failure(IllegalStateException(errorMsg))
                }

                try {
                    val jsonElement = json.parseToJsonElement(body)
                    val modelsArray = jsonElement.jsonObject["models"]?.jsonArray
                    if (modelsArray != null) {
                        for (item in modelsArray) {
                            val rawName = item.jsonObject["name"]?.jsonPrimitive?.content ?: continue
                            val modelId = rawName.removePrefix("models/")
                            val displayName = item.jsonObject["displayName"]?.jsonPrimitive?.content ?: modelId
                            val description = item.jsonObject["description"]?.jsonPrimitive?.content ?: "Модель провайдера"

                            if (modelId.contains("flash") || modelId.contains("pro") || modelId.contains("gemini")) {
                                fetchedList.add(
                                    GeminiSettingsManager.ModelOption(
                                        id = modelId,
                                        displayName = "🤖 $displayName",
                                        description = description
                                    )
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore parse inner exception
                }
            }

            if (fetchedList.isEmpty()) {
                Result.failure(IllegalStateException("Провайдер не вернул список моделей"))
            } else {
                Result.success(fetchedList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Универсальный текстовый запрос без JSON-схемы ответа.
     * Используется ИИ-ассистентом в режиме готовки.
     */
    suspend fun generateAssistantReply(context: Context, prompt: String): String = withContext(Dispatchers.IO) {
        if (com.aetnox.cooking.domain.net.ExperimentalSettingsManager.isAiDisabled(context)) {
            throw IllegalStateException("ИИ полностью отключен в экспериментальных настройках.")
        }
        val settings = GeminiSettingsManager.getSettings(context)
        val apiKey = settings.apiKey.ifBlank {
            throw IllegalArgumentException("API-ключ Gemini не указан! Введите его в Настройках.")
        }

        val thinkingMode = com.aetnox.cooking.domain.net.ExperimentalSettingsManager
            .getSettings(context).assistantThinkingMode
        val thinkingBudget = thinkingMode.budget

        val payload = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GenerationConfig(
                temperature = 0.3,
                responseMimeType = null,
                // Бюджет ответа = бюджет размышлений + место под сам текст ответа
                maxOutputTokens = thinkingBudget + 1024,
                thinkingConfig = if (thinkingBudget > 0) ThinkingConfig(thinkingBudget) else null
            )
        )

        val requestJsonStr = json.encodeToString(GeminiRequest.serializer(), payload)
        val baseUrlRaw = settings.customBaseUrl.removeSuffix("/")
        val baseUrl = baseUrlRaw.toHttpUrlOrNull()
            ?: throw IllegalStateException("Некорректный URL провайдера: '$baseUrlRaw'. Исправьте его в Настройках.")
        val model = settings.model.ifBlank { GeminiSettingsManager.DEFAULT_MODEL }
        val endpointUrl = baseUrl.newBuilder()
            .addPathSegments("v1beta/models/$model:generateContent")
            .build()

        val request = Request.Builder()
            .url(endpointUrl)
            .addHeader("x-goog-api-key", apiKey)
            .post(requestJsonStr.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val client = createOkHttpClient(context)

        val responseText = client.newCall(request).execute().use { response ->
            val bodyStr = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errResp = json.decodeFromString<GeminiResponse>(bodyStr)
                    errResp.error?.message ?: "Ошибка HTTP ${response.code}: ${response.message}"
                } catch (e: Exception) {
                    "Ошибка HTTP ${response.code}: $bodyStr"
                }
                throw IllegalStateException("Ошибка ИИ: $errorMsg")
            }
            bodyStr
        }

        val geminiResp = json.decodeFromString<GeminiResponse>(responseText)
        // Thinking-модели отдают рассуждение отдельными частями (thought=true):
        // берём ответные части целиком, склеивая их, а не первую попавшуюся
        val allParts = geminiResp.candidates?.firstOrNull()?.content?.parts.orEmpty()
        val answerParts = allParts.filter { (it.thought != true) && !it.text.isNullOrBlank() }
        val textParts = answerParts.ifEmpty { allParts.filter { !it.text.isNullOrBlank() } }
            .mapNotNull { it.text }
            .joinToString("\n")
            .trim()
        if (textParts.isBlank()) {
            throw IllegalStateException("ИИ вернул пустой ответ")
        }
        textParts
    }

    private fun cleanMarkdownJson(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```json")) {
            clean = clean.removePrefix("```json")
        } else if (clean.startsWith("```")) {
            clean = clean.removePrefix("```")
        }
        if (clean.endsWith("```")) {
            clean = clean.removeSuffix("```")
        }
        return clean.trim()
    }

    fun readImageBase64(context: Context, uriStr: String): Pair<String, String>? {
        val inline = readImageAsInlineData(context, uriStr) ?: return null
        return Pair(inline.data, inline.mimeType)
    }

    private fun readImageAsInlineData(context: Context, uriStr: String): InlineData? {
        return try {
            val uri = Uri.parse(uriStr)
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            // Сначала читаем только размеры, чтобы не грузить многофотопиксельный
            // снимок в память целиком (риск OOM)
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val maxDimension = 1600
            var sampleSize = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sampleSize * 2) >= maxDimension) {
                sampleSize *= 2
            }

            val bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
            } ?: return null

            val outputMime = if (mimeType == "image/png") "image/png" else "image/jpeg"
            val output = java.io.ByteArrayOutputStream()
            bitmap.compress(
                if (outputMime == "image/png") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                85,
                output
            )
            bitmap.recycle()

            InlineData(
                mimeType = outputMime,
                data = Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
            )
        } catch (_: Exception) {
            null
        }
    }
}
