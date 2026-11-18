package com.aetnox.cooking.data.remote.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

// --- Системные ---

@Serializable
data class AiCostsDto(
    @SerialName("assistant") val assistant: Int = 1,
    @SerialName("parse") val parse: Int = 1,
    @SerialName("parse_photo") val parsePhoto: Int = 2,
    @SerialName("search_single_per_60") val searchSinglePer60: Int = 1,
    @SerialName("search_finish") val searchFinish: Int = 1,
    @SerialName("search_plan") val searchPlan: Int = 0
)

@Serializable
data class ConfigFeaturesDto(
    @SerialName("parse_enabled") val parseEnabled: Boolean = true,
    @SerialName("assistant_enabled") val assistantEnabled: Boolean = true,
    @SerialName("ai_search_enabled") val aiSearchEnabled: Boolean = true,
    @SerialName("feedback_enabled") val feedbackEnabled: Boolean = true
)

@Serializable
data class AppConfigResponse(
    @SerialName("min_version_code") val minVersionCode: Int,
    @SerialName("latest_version_code") val latestVersionCode: Int,
    @SerialName("update_url") val updateUrl: String,
    @SerialName("maintenance_active") val maintenanceActive: Boolean,
    @SerialName("features") val features: ConfigFeaturesDto? = null,
    @SerialName("ai_costs") val aiCosts: AiCostsDto? = null
)

// --- Авторизация ---

@Serializable
data class AuthInitRequest(
    @SerialName("device_id") val deviceId: String,
    @SerialName("device_info") val deviceInfo: String
)

@Serializable
data class AuthInitResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("session_token") val sessionToken: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("plan_level") val planLevel: Int,
    @SerialName("subscription_until") val subscriptionUntil: Long
)

@Serializable
data class TransferCreateResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("transfer_code") val transferCode: String,
    @SerialName("expires_in_seconds") val expiresInSeconds: Int
)

@Serializable
data class TransferClaimRequest(
    @SerialName("code") val code: String
)

@Serializable
data class TransferClaimResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("plan_level") val planLevel: Int,
    @SerialName("subscription_until") val subscriptionUntil: Long
)

// --- Статус и квоты ---

@Serializable
data class UserStatusResponse(
    @SerialName("account_id") val accountId: String,
    @SerialName("plan_level") val planLevel: Int,
    @SerialName("plan_name") val planName: String,
    @SerialName("subscription_until") val subscriptionUntil: Long,
    @SerialName("subscription_active") val subscriptionActive: Boolean = false,
    @SerialName("is_quarantined") val isQuarantined: Boolean,
    @SerialName("daily_resets_at") val dailyResetsAt: Long = 0L,
    @SerialName("monthly_resets_at") val monthlyResetsAt: Long = 0L,
    @SerialName("quotas") val quotas: UserQuotasDto,
    @SerialName("features") val features: UserFeaturesDto,
    @SerialName("ai_costs") val aiCosts: AiCostsDto? = null
)

@Serializable
data class UserQuotasDto(
    @SerialName("ai_requests") val aiRequests: MonthlyQuotaDto? = null,
    @SerialName("recipes") val recipes: QuotaDetailDto? = null,
    @SerialName("assistant_iterations") val assistantIterations: QuotaDetailDto? = null,
    @SerialName("photos") val photos: PhotoQuotaDto? = null,
    @SerialName("cloud") val cloud: CloudQuotaDto? = null
)

@Serializable
data class MonthlyQuotaDto(
    @SerialName("used_this_month") val usedThisMonth: Int = 0,
    @SerialName("monthly_limit") val monthlyLimit: Int = 0,
    @SerialName("remaining") val remaining: Int = 0
)

@Serializable
data class CloudQuotaDto(
    @SerialName("used_total") val usedTotal: Int = 0,
    @SerialName("limit") val limit: Int = 0,
    @SerialName("remaining") val remaining: Int = 0
)

@Serializable
data class PhotoQuotaDto(
    @SerialName("used_this_month") val usedThisMonth: Int = 0,
    @SerialName("monthly_limit") val monthlyLimit: Int = 0,
    @SerialName("remaining") val remaining: Int = 0
)

@Serializable
data class QuotaDetailDto(
    @SerialName("used_today") val usedToday: Int,
    @SerialName("daily_limit") val dailyLimit: Int,
    @SerialName("remaining") val remaining: Int
)

@Serializable
data class UserFeaturesDto(
    @SerialName("allow_photos") val allowPhotos: Boolean,
    @SerialName("max_agent_depth") val maxAgentDepth: Int
)

// --- Активация ключей ---

@Serializable
data class ActivateKeyRequest(
    @SerialName("key") val key: String
)

@Serializable
data class ActivateKeyResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("plan_level") val planLevel: Int,
    @SerialName("subscription_until") val subscriptionUntil: Long
)

// --- Оцифровка рецепта ---

@Serializable
data class RecipeParseRequest(
    @SerialName("raw_text") val rawText: String? = null,
    @SerialName("image_base64") val imageBase64: String? = null,
    @SerialName("mime_type") val mimeType: String? = null
)

@Serializable
data class RecipeParseResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("recipe") val recipe: RecipeJsonDto,
    @SerialName("remaining_today") val remainingToday: Int = 0,
    @SerialName("remaining_month") val remainingMonth: Int? = null,
    @SerialName("cost") val cost: Int? = null
)

@Serializable
data class RecipeJsonDto(
    @SerialName("title") val title: String,
    @SerialName("prepTimeMinutes") val prepTimeMinutes: Int = 0,
    @SerialName("cookTimeMinutes") val cookTimeMinutes: Int = 0,
    @SerialName("servings") val servings: Int = 1,
    @SerialName("yieldUnit") val yieldUnit: String = "порций",
    @SerialName("chefHeatingType") val chefHeatingType: String = "",
    @SerialName("chefTemperature") val chefTemperature: String = "",
    @SerialName("chefTiming") val chefTiming: String = "",
    @SerialName("chefReadinessSign") val chefReadinessSign: String = "",
    @SerialName("ingredients") val ingredients: List<IngredientDto>,
    @SerialName("steps") val steps: List<String>,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("notes") val notes: String = "",
    @SerialName("calories") val calories: Int = 0,
    @SerialName("protein") val protein: Int = 0,
    @SerialName("fat") val fat: Int = 0,
    @SerialName("carbs") val carbs: Int = 0
)

@Serializable
data class IngredientDto(
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: String,
    @SerialName("unit") val unit: String,
    @SerialName("groupName") val groupName: String = "",
    @SerialName("note") val note: String = ""
)

// --- Ассистент ---

@Serializable
data class AssistantStepRequest(
    @SerialName("recipe_context") val recipeContext: String,
    @SerialName("messages") val messages: List<ChatMessageDto>,
    @SerialName("allow_recipe_updates") val allowRecipeUpdates: Boolean? = true
)

@Serializable
data class ChatMessageDto(
    @SerialName("role") val role: String, // "user" или "assistant"
    @SerialName("content") val content: String
)

@Serializable
data class AssistantStepResponse(
    @SerialName("reply_type") val replyType: String, // "text" или "tool_call"
    @SerialName("content") val content: String,
    @SerialName("remaining_iterations") val remainingIterations: Int = 0,
    @SerialName("remaining_month") val remainingMonth: Int? = null,
    @SerialName("cost") val cost: Int? = null,
    @SerialName("proposed_update") val proposedUpdate: ProposedUpdateDto? = null
)

@Serializable
data class ProposedUpdateDto(
    @SerialName("summary") val summary: String? = null,
    @SerialName("patch") val patch: ProposedUpdatePatchDto? = null
)

@Serializable
data class ProposedUpdatePatchDto(
    @SerialName("postCookNote") val postCookNote: String? = null,
    @SerialName("notes") val notes: String? = null,
    @SerialName("chefHeatingType") val chefHeatingType: String? = null,
    @SerialName("chefTemperature") val chefTemperature: String? = null,
    @SerialName("chefTiming") val chefTiming: String? = null,
    @SerialName("suggested_status") val suggestedStatus: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("ingredient_amounts") val ingredientAmounts: List<ProposedIngredientAmountDto>? = null,
    @SerialName("change_summary") val changeSummary: String? = null
)

@Serializable
data class ProposedIngredientAmountDto(
    @SerialName("name") val name: String,
    @SerialName("new_amount") val newAmount: String
)

// --- Облачные бэкапы (Раздел 3 документации) ---

@Serializable
data class BackupIngredientDto(
    @SerialName("name") val name: String,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("unit") val unit: String = "г",
    @SerialName("selectedUnit") val selectedUnit: String = unit,
    @SerialName("gramsPerUnit") val gramsPerUnit: Double? = null,
    @SerialName("note") val note: String = "",
    @SerialName("groupName") val groupName: String = ""
)

@Serializable
data class BackupRecipeDto(
    @SerialName("id") val id: Long? = null,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String? = null,
    @SerialName("prepTimeMinutes") val prepTimeMinutes: Int = 0,
    @SerialName("cookTimeMinutes") val cookTimeMinutes: Int = 0,
    @SerialName("servingsEnabled") val servingsEnabled: Boolean = true,
    @SerialName("baseServings") val baseServings: Double = 4.0,
    @SerialName("yieldUnit") val yieldUnit: String = "порций",
    @SerialName("status") val status: String = "PLANNED",
    @SerialName("isFavorite") val isFavorite: Boolean = false,
    @SerialName("rating") val rating: Int = 0,
    @SerialName("postCookNote") val postCookNote: String = "",
    @SerialName("chefHeatingType") val chefHeatingType: String = "",
    @SerialName("chefTemperature") val chefTemperature: String = "",
    @SerialName("notes") val notes: String = "",
    @SerialName("calories") val calories: Double? = null,
    @SerialName("protein") val protein: Double? = null,
    @SerialName("fat") val fat: Double? = null,
    @SerialName("carbs") val carbs: Double? = null,
    @SerialName("cloudId") val cloudId: String? = null,
    @SerialName("cloudAccessCode") val cloudAccessCode: String? = null,
    @SerialName("createdAt") val createdAt: Long = 0L,
    @SerialName("updatedAt") val updatedAt: Long = 0L,
    @SerialName("ingredients") val ingredients: List<BackupIngredientDto> = emptyList(),
    @SerialName("steps") val steps: List<String> = emptyList(),
    @SerialName("tags") val tags: List<String> = emptyList()
)

@Serializable
data class CloudBackupUploadRequest(
    @SerialName("recipes") val recipes: List<BackupRecipeDto>
)

@Serializable
data class CloudBackupUploadResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("stored") val stored: Int = 0,
    @SerialName("size_bytes") val sizeBytes: Long = 0L,
    @SerialName("created_at") val createdAt: Long = 0L
)

@Serializable
data class CloudBackupGetResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("exists") val exists: Boolean = false,
    @SerialName("created_at") val createdAt: Long? = null,
    @SerialName("recipes_count") val recipesCount: Int = 0,
    @SerialName("recipes") val recipes: List<BackupRecipeDto> = emptyList()
)

// --- Облачные рецепты ---

@Serializable
data class RecipeUploadRequest(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("payload_json") val payloadJson: String,
    @SerialName("is_public") val isPublic: Boolean
)

@Serializable
data class RecipeVisibilityRequest(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("is_public") val isPublic: Boolean
)

@Serializable
data class RecipeUploadResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("access_code") val accessCode: String? = null
)

@Serializable
data class CloudRecipeResponse(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("title") val title: String,
    @SerialName("payload_json") val payloadJson: String
)

@Serializable
data class CloudRecipeByCodeResponse(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("title") val title: String,
    @SerialName("payload_json") val payloadJson: String
)

// --- DTO для ответов списков и поиска ---

@Serializable
data class SuccessResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("message") val message: String? = null
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object RecipeMetaTagsSerializer : KSerializer<List<String>?> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("RecipeMetaTags", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<String>?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeString(Json.encodeToString(value))
        }
    }

    override fun deserialize(decoder: Decoder): List<String>? {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return runCatching { decoder.decodeString().split(",").map { it.trim() } }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        return when (element) {
            is JsonArray -> {
                element.mapNotNull { (it as? JsonPrimitive)?.content }
            }
            is JsonPrimitive -> {
                val str = element.content
                if (str.isBlank()) return emptyList()
                runCatching {
                    Json.decodeFromString<List<String>>(str)
                }.getOrElse {
                    str.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
                }
            }
            else -> null
        }
    }
}

@Serializable
data class RecipeMeta(
    @SerialName("recipe_id") val recipeId: String,
    @SerialName("title") val title: String,
    @Serializable(with = RecipeMetaTagsSerializer::class)
    @SerialName("tags") val tags: List<String>? = null,
    @SerialName("access_code") val accessCode: String? = null,
    @SerialName("is_public") val isPublic: Int = 1,
    @SerialName("created_at") val createdAt: Long = 0L,
    @SerialName("updated_at") val updatedAt: Long = 0L
)

@Serializable
data class RecipesListResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("page") val page: Int = 0,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("recipes") val recipes: List<RecipeMeta> = emptyList()
)

@Serializable
data class RecipeSearchResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("page") val page: Int = 0,
    @SerialName("relaxed") val relaxed: Boolean = false,
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("results") val results: List<RecipeMeta> = emptyList()
)

// --- ИИ Диалоговый поиск рецептов ---

@Serializable
data class AiSearchLocalRecipeDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("minutes") val minutes: Int = 0,
    @SerialName("ingredients") val ingredients: String = "",
    @SerialName("status") val status: String? = null,
    @SerialName("cloudId") val cloudId: String? = null
)

@Serializable
data class AiSearchRequest(
    @SerialName("scope") val scope: String = "all",
    @SerialName("limit") val limit: Int = 10,
    @SerialName("messages") val messages: List<ChatMessageDto>,
    @SerialName("local_recipes") val localRecipes: List<AiSearchLocalRecipeDto>? = null
)

// Двухфазный протокол ИИ-поиска (v2)

@Serializable
data class AiSearchPlanRequest(
    @SerialName("scope") val scope: String = "all",
    @SerialName("messages") val messages: List<ChatMessageDto>,
    @SerialName("cloud_ids") val cloudIds: List<String>? = null
)

@Serializable
data class AiSearchPlanDto(
    @SerialName("keywords") val keywords: List<String> = emptyList(),
    @SerialName("ingredients") val ingredients: List<String> = emptyList(),
    @SerialName("min_minutes") val minMinutes: Int? = null,
    @SerialName("max_minutes") val maxMinutes: Int? = null
)

@Serializable
data class AiSearchPlanResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("search_token") val searchToken: String = "",
    @SerialName("plan") val plan: AiSearchPlanDto = AiSearchPlanDto(),
    @SerialName("cloud_found") val cloudFound: Int = 0,
    @SerialName("expires_in") val expiresIn: Int = 600
)

@Serializable
data class AiSearchLocalMatchDto(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("status") val status: String? = null,
    @SerialName("minutes") val minutes: Int = 0,
    @SerialName("ingredients") val ingredients: String = ""
)

@Serializable
data class AiSearchFinishRequest(
    @SerialName("search_token") val searchToken: String,
    @SerialName("messages") val messages: List<ChatMessageDto>,
    @SerialName("limit") val limit: Int = 10,
    @SerialName("local_matches") val localMatches: List<AiSearchLocalMatchDto> = emptyList()
)

@Serializable
data class AiSearchResultItemDto(
    @SerialName("source") val source: String,
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("tags") val tags: List<String> = emptyList(),
    @SerialName("minutes") val minutes: Int = 0,
    @SerialName("reason") val reason: String = ""
)

@Serializable
data class AiSearchResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("scope") val scope: String = "all",
    @SerialName("reply") val reply: String = "",
    @SerialName("results") val results: List<AiSearchResultItemDto> = emptyList(),
    @SerialName("remaining_month") val remainingMonth: Int? = null,
    @SerialName("cost") val cost: Int? = null
)

@Serializable
data class FeedbackRequest(
    @SerialName("message") val message: String
)

// --- Ошибки бэкенда ---

@Serializable
data class ApiErrorResponse(
    @SerialName("error") val error: String,
    @SerialName("error_code") val errorCode: String? = null
)

/** Известные коды ошибок бэкенда (см. раздел 4 документации). */
object ApiErrorCodes {
    const val UNAUTHORIZED = "UNAUTHORIZED"
    const val FREE_TIER_QUARANTINE = "FREE_TIER_QUARANTINE"
    const val QUOTA_EXCEEDED = "QUOTA_EXCEEDED"
    const val CLOUD_QUOTA_EXCEEDED = "CLOUD_QUOTA_EXCEEDED"
    const val PHOTO_RESTRICTED = "PHOTO_RESTRICTED"
    const val PHOTO_QUOTA_EXCEEDED = "PHOTO_QUOTA_EXCEEDED"
    const val ASSISTANT_LOCKED = "ASSISTANT_LOCKED"
    const val ACCOUNT_BANNED = "ACCOUNT_BANNED"
    const val UPGRADE_REQUIRED = "UPGRADE_REQUIRED"
    const val MAINTENANCE = "MAINTENANCE"
    const val PLAN_DOWNGRADE_BLOCKED = "PLAN_DOWNGRADE_BLOCKED"
    const val KEY_PLAN_INVALID = "KEY_PLAN_INVALID"
    const val RATE_LIMITED = "RATE_LIMITED"
    const val INVALID_BACKUP = "INVALID_BACKUP"
    const val SESSION_NOT_FOUND = "SESSION_NOT_FOUND"
    const val SESSION_EXPIRED = "SESSION_EXPIRED"

    // С 17.11.2026 сервер возвращает явный error_code на любой ошибке.
    // Сессии поиска выделены в отдельные коды (старые SESSION_* — фолбэк для старых серверов).
    const val SEARCH_SESSION_NOT_FOUND = "SEARCH_SESSION_NOT_FOUND"
    const val SEARCH_SESSION_EXPIRED = "SEARCH_SESSION_EXPIRED"

    // ИИ-каскад (502/500)
    const val AI_UNAVAILABLE = "AI_UNAVAILABLE"
    const val AI_OVERLOADED = "AI_OVERLOADED"
    const val AI_PARSE_FAILED = "AI_PARSE_FAILED"
    const val INVALID_RECIPE = "INVALID_RECIPE"

    // Глобальные ошибки воркера
    const val INVALID_JSON = "INVALID_JSON"
    const val ENDPOINT_NOT_FOUND = "ENDPOINT_NOT_FOUND"
    const val INTERNAL_ERROR = "INTERNAL_ERROR"
    const val VALIDATION_ERROR = "VALIDATION_ERROR"

    // Валидация полей
    const val EMPTY_REQUEST = "EMPTY_REQUEST"
    const val TEXT_TOO_LONG = "TEXT_TOO_LONG"
    const val EMPTY_FEEDBACK = "EMPTY_FEEDBACK"
    const val QUERY_TOO_SHORT = "QUERY_TOO_SHORT"

    // Ключи подписки
    const val KEY_REQUIRED = "KEY_REQUIRED"
    const val INVALID_KEY = "INVALID_KEY"
    const val KEY_ALREADY_ACTIVATED = "KEY_ALREADY_ACTIVATED"

    // Перенос подписки между устройствами
    const val TRANSFER_NO_SUBSCRIPTION = "TRANSFER_NO_SUBSCRIPTION"
    const val INVALID_TRANSFER_CODE = "INVALID_TRANSFER_CODE"
    const val TRANSFER_SOURCE_NOT_FOUND = "TRANSFER_SOURCE_NOT_FOUND"
    const val TRANSFER_SAME_DEVICE = "TRANSFER_SAME_DEVICE"
    const val TRANSFER_SOURCE_EXPIRED = "TRANSFER_SOURCE_EXPIRED"
    const val TRANSFER_COOLDOWN = "TRANSFER_COOLDOWN"
    const val TRANSFER_LIMIT_REACHED = "TRANSFER_LIMIT_REACHED"

    // Облачные рецепты
    const val RECIPE_TOO_LARGE = "RECIPE_TOO_LARGE"
    const val CLOUD_RECIPE_NOT_FOUND = "CLOUD_RECIPE_NOT_FOUND"
    const val INVALID_ACCESS_CODE = "INVALID_ACCESS_CODE"
    const val ACCESS_RESTRICTED = "ACCESS_RESTRICTED"
    const val FORBIDDEN = "FORBIDDEN"

    const val AI_SEARCH_LOCKED = "AI_SEARCH_LOCKED"
    const val FEATURE_DISABLED = "FEATURE_DISABLED"
    const val UNSUPPORTED_IMAGE = "UNSUPPORTED_IMAGE"
    const val IMAGE_TOO_LARGE = "IMAGE_TOO_LARGE"
    const val PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE"

    // Бэкап
    const val BACKUP_TOO_LARGE = "BACKUP_TOO_LARGE"
}

fun RecipeJsonDto.toParsedRecipe(): com.aetnox.cooking.domain.parser.ParsedRecipe {
    val recipeEntity = com.aetnox.cooking.data.entity.RecipeEntity(
        title = title,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servingsEnabled = true,
        baseServings = servings.toDouble().coerceAtLeast(1.0),
        yieldUnit = yieldUnit.ifBlank { "порций" },
        chefHeatingType = chefHeatingType,
        chefTemperature = chefTemperature,
        chefTiming = chefTiming,
        chefReadinessSign = chefReadinessSign,
        notes = notes,
        calories = if (calories > 0) calories.toDouble() else null,
        protein = if (protein > 0) protein.toDouble() else null,
        fat = if (fat > 0) fat.toDouble() else null,
        carbs = if (carbs > 0) carbs.toDouble() else null
    )

    val ingredientEntities = ingredients.mapIndexed { index, ing ->
        val (numAmount, extraNote) = com.aetnox.cooking.domain.parser.RecipeImportParser.parseSafeAmount(ing.amount)
        val combinedNote = listOfNotNull(
            ing.note.ifBlank { null },
            extraNote?.ifBlank { null }
        ).joinToString("; ")

        com.aetnox.cooking.data.entity.IngredientEntity(
            recipeId = 0,
            name = ing.name.ifBlank { "Ингредиент ${index + 1}" },
            amount = numAmount,
            unit = ing.unit.ifBlank { "г" },
            selectedUnit = ing.unit.ifBlank { "г" },
            note = combinedNote,
            groupName = ing.groupName,
            orderIndex = index
        )
    }

    val stepEntities = steps.mapIndexed { index, stepText ->
        com.aetnox.cooking.data.entity.StepEntity(
            recipeId = 0,
            stepNumber = index + 1,
            instructionText = stepText
        )
    }

    return com.aetnox.cooking.domain.parser.ParsedRecipe(
        recipe = recipeEntity,
        ingredients = ingredientEntities,
        steps = stepEntities,
        tags = tags
    )
}
