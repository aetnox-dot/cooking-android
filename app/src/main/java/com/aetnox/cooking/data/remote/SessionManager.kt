package com.aetnox.cooking.data.remote

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aetnox.cooking.data.remote.model.UserStatusResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore(name = "backend_session")

data class UserSessionData(
    val sessionToken: String? = null,
    val accountId: String = "",
    val planLevel: Int = 1,
    val planName: String = "Free",
    val subscriptionUntil: Long = 0L,
    val subscriptionActive: Boolean = false,
    val isQuarantined: Boolean = false,
    val monthlyResetsAt: Long = 0L,
    val aiRequestsUsedThisMonth: Int = 0,
    val aiRequestsMonthlyLimit: Int = 0,
    val aiRequestsRemaining: Int = 0,
    val recipesUsedToday: Int = 0,
    val recipesDailyLimit: Int = 5,
    val recipesRemaining: Int = 5,
    val assistantUsedToday: Int = 0,
    val assistantDailyLimit: Int = 20,
    val assistantRemaining: Int = 20,
    val photosUsedThisMonth: Int = 0,
    val photosMonthlyLimit: Int = 0,
    val photosRemaining: Int = 0,
    val cloudUsedTotal: Int = 0,
    val cloudLimit: Int = 0,
    val cloudRemaining: Int = 0,
    val allowPhotos: Boolean = false,
    val maxAgentDepth: Int = 3,
    val aiCosts: com.aetnox.cooking.data.remote.model.AiCostsDto = com.aetnox.cooking.data.remote.model.AiCostsDto()
)

/**
 * Хранилище сессии бэкенда (session_token, account_id, тариф, квоты) в DataStore.
 * Синхронные геттеры нужны OkHttp-интерсепторам; suspend/flow версии — репозиторию и UI.
 */
object SessionManager {

    private val KEY_SESSION_TOKEN = stringPreferencesKey("session_token")
    private val KEY_ACCOUNT_ID = stringPreferencesKey("account_id")
    private val KEY_PLAN_LEVEL = intPreferencesKey("plan_level")
    private val KEY_PLAN_NAME = stringPreferencesKey("plan_name")
    private val KEY_SUBSCRIPTION_UNTIL = longPreferencesKey("subscription_until")
    private val KEY_SUBSCRIPTION_ACTIVE = booleanPreferencesKey("subscription_active")
    private val KEY_IS_QUARANTINED = booleanPreferencesKey("is_quarantined")
    private val KEY_MONTHLY_RESETS_AT = longPreferencesKey("monthly_resets_at")

    private val KEY_AI_USED_MONTH = intPreferencesKey("ai_used_this_month")
    private val KEY_AI_MONTHLY_LIMIT = intPreferencesKey("ai_monthly_limit")
    private val KEY_AI_REMAINING = intPreferencesKey("ai_remaining")

    private val KEY_RECIPES_USED = intPreferencesKey("recipes_used_today")
    private val KEY_RECIPES_LIMIT = intPreferencesKey("recipes_daily_limit")
    private val KEY_RECIPES_REMAINING = intPreferencesKey("recipes_remaining")

    private val KEY_ASSISTANT_USED = intPreferencesKey("assistant_used_today")
    private val KEY_ASSISTANT_LIMIT = intPreferencesKey("assistant_daily_limit")
    private val KEY_ASSISTANT_REMAINING = intPreferencesKey("assistant_remaining")

    private val KEY_PHOTOS_USED_MONTH = intPreferencesKey("photos_used_this_month")
    private val KEY_PHOTOS_MONTHLY_LIMIT = intPreferencesKey("photos_monthly_limit")
    private val KEY_PHOTOS_REMAINING = intPreferencesKey("photos_remaining")

    private val KEY_CLOUD_USED = intPreferencesKey("cloud_used_total")
    private val KEY_CLOUD_LIMIT = intPreferencesKey("cloud_limit")
    private val KEY_CLOUD_REMAINING = intPreferencesKey("cloud_remaining")

    private val KEY_ALLOW_PHOTOS = booleanPreferencesKey("allow_photos")
    private val KEY_MAX_AGENT_DEPTH = intPreferencesKey("max_agent_depth")

    private val KEY_COST_ASSISTANT = intPreferencesKey("cost_assistant")
    private val KEY_COST_PARSE = intPreferencesKey("cost_parse")
    private val KEY_COST_PARSE_PHOTO = intPreferencesKey("cost_parse_photo")
    private val KEY_COST_SEARCH_SINGLE_60 = intPreferencesKey("cost_search_single_per_60")
    private val KEY_COST_SEARCH_FINISH = intPreferencesKey("cost_search_finish")
    private val KEY_COST_SEARCH_PLAN = intPreferencesKey("cost_search_plan")

    @Volatile
    private var cachedToken: String? = null

    /** Быстрый доступ из OkHttp-интерсептора (кэш в памяти). */
    fun getTokenBlocking(): String? = cachedToken

    suspend fun getToken(context: Context): String? {
        cachedToken?.let { return it }
        val token = context.sessionDataStore.data.first()[KEY_SESSION_TOKEN]
        cachedToken = token
        return token
    }

    fun getSessionFlow(context: Context): Flow<UserSessionData> {
        return context.sessionDataStore.data.map { prefs ->
            UserSessionData(
                sessionToken = prefs[KEY_SESSION_TOKEN],
                accountId = prefs[KEY_ACCOUNT_ID] ?: "",
                planLevel = prefs[KEY_PLAN_LEVEL] ?: 1,
                planName = prefs[KEY_PLAN_NAME] ?: "Free",
                subscriptionUntil = prefs[KEY_SUBSCRIPTION_UNTIL] ?: 0L,
                subscriptionActive = prefs[KEY_SUBSCRIPTION_ACTIVE] ?: false,
                isQuarantined = prefs[KEY_IS_QUARANTINED] ?: false,
                monthlyResetsAt = prefs[KEY_MONTHLY_RESETS_AT] ?: 0L,
                aiRequestsUsedThisMonth = prefs[KEY_AI_USED_MONTH] ?: 0,
                aiRequestsMonthlyLimit = prefs[KEY_AI_MONTHLY_LIMIT] ?: 0,
                aiRequestsRemaining = prefs[KEY_AI_REMAINING] ?: 0,
                recipesUsedToday = prefs[KEY_RECIPES_USED] ?: 0,
                recipesDailyLimit = prefs[KEY_RECIPES_LIMIT] ?: 5,
                recipesRemaining = prefs[KEY_RECIPES_REMAINING] ?: 5,
                assistantUsedToday = prefs[KEY_ASSISTANT_USED] ?: 0,
                assistantDailyLimit = prefs[KEY_ASSISTANT_LIMIT] ?: 20,
                assistantRemaining = prefs[KEY_ASSISTANT_REMAINING] ?: 20,
                photosUsedThisMonth = prefs[KEY_PHOTOS_USED_MONTH] ?: 0,
                photosMonthlyLimit = prefs[KEY_PHOTOS_MONTHLY_LIMIT] ?: 0,
                photosRemaining = prefs[KEY_PHOTOS_REMAINING] ?: 0,
                cloudUsedTotal = prefs[KEY_CLOUD_USED] ?: 0,
                cloudLimit = prefs[KEY_CLOUD_LIMIT] ?: 0,
                cloudRemaining = prefs[KEY_CLOUD_REMAINING] ?: 0,
                allowPhotos = prefs[KEY_ALLOW_PHOTOS] ?: false,
                maxAgentDepth = prefs[KEY_MAX_AGENT_DEPTH] ?: 3,
                aiCosts = com.aetnox.cooking.data.remote.model.AiCostsDto(
                    assistant = prefs[KEY_COST_ASSISTANT] ?: 1,
                    parse = prefs[KEY_COST_PARSE] ?: 1,
                    parsePhoto = prefs[KEY_COST_PARSE_PHOTO] ?: 2,
                    searchSinglePer60 = prefs[KEY_COST_SEARCH_SINGLE_60] ?: 1,
                    searchFinish = prefs[KEY_COST_SEARCH_FINISH] ?: 1,
                    searchPlan = prefs[KEY_COST_SEARCH_PLAN] ?: 0
                )
            )
        }
    }

    suspend fun saveSession(
        context: Context,
        sessionToken: String,
        accountId: String,
        planLevel: Int,
        subscriptionUntil: Long
    ) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_SESSION_TOKEN] = sessionToken
            prefs[KEY_ACCOUNT_ID] = accountId
            prefs[KEY_PLAN_LEVEL] = planLevel
            prefs[KEY_SUBSCRIPTION_UNTIL] = subscriptionUntil
        }
        cachedToken = sessionToken
    }

    suspend fun saveUserStatus(context: Context, status: UserStatusResponse) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_ID] = status.accountId
            prefs[KEY_PLAN_LEVEL] = status.planLevel
            prefs[KEY_PLAN_NAME] = status.planName
            prefs[KEY_SUBSCRIPTION_UNTIL] = status.subscriptionUntil
            prefs[KEY_SUBSCRIPTION_ACTIVE] = status.subscriptionActive
            prefs[KEY_IS_QUARANTINED] = status.isQuarantined
            prefs[KEY_MONTHLY_RESETS_AT] = status.monthlyResetsAt

            status.quotas.aiRequests?.let { ai ->
                prefs[KEY_AI_USED_MONTH] = ai.usedThisMonth
                prefs[KEY_AI_MONTHLY_LIMIT] = ai.monthlyLimit
                prefs[KEY_AI_REMAINING] = ai.remaining
            }

            status.quotas.recipes?.let { recipes ->
                prefs[KEY_RECIPES_USED] = recipes.usedToday
                prefs[KEY_RECIPES_LIMIT] = recipes.dailyLimit
                prefs[KEY_RECIPES_REMAINING] = recipes.remaining
            }

            status.quotas.assistantIterations?.let { assistant ->
                prefs[KEY_ASSISTANT_USED] = assistant.usedToday
                prefs[KEY_ASSISTANT_LIMIT] = assistant.dailyLimit
                prefs[KEY_ASSISTANT_REMAINING] = assistant.remaining
            }

            status.quotas.photos?.let { photos ->
                prefs[KEY_PHOTOS_USED_MONTH] = photos.usedThisMonth
                prefs[KEY_PHOTOS_MONTHLY_LIMIT] = photos.monthlyLimit
                prefs[KEY_PHOTOS_REMAINING] = photos.remaining
            }

            status.quotas.cloud?.let { cloud ->
                prefs[KEY_CLOUD_USED] = cloud.usedTotal
                prefs[KEY_CLOUD_LIMIT] = cloud.limit
                prefs[KEY_CLOUD_REMAINING] = cloud.remaining
            }

            prefs[KEY_ALLOW_PHOTOS] = status.features.allowPhotos
            prefs[KEY_MAX_AGENT_DEPTH] = status.features.maxAgentDepth

            status.aiCosts?.let { costs ->
                prefs[KEY_COST_ASSISTANT] = costs.assistant
                prefs[KEY_COST_PARSE] = costs.parse
                prefs[KEY_COST_PARSE_PHOTO] = costs.parsePhoto
                prefs[KEY_COST_SEARCH_SINGLE_60] = costs.searchSinglePer60
                prefs[KEY_COST_SEARCH_FINISH] = costs.searchFinish
                prefs[KEY_COST_SEARCH_PLAN] = costs.searchPlan
            }
        }
    }

    suspend fun saveAppConfig(context: Context, config: com.aetnox.cooking.data.remote.model.AppConfigResponse) {
        config.aiCosts?.let { costs ->
            context.sessionDataStore.edit { prefs ->
                prefs[KEY_COST_ASSISTANT] = costs.assistant
                prefs[KEY_COST_PARSE] = costs.parse
                prefs[KEY_COST_PARSE_PHOTO] = costs.parsePhoto
                prefs[KEY_COST_SEARCH_SINGLE_60] = costs.searchSinglePer60
                prefs[KEY_COST_SEARCH_FINISH] = costs.searchFinish
                prefs[KEY_COST_SEARCH_PLAN] = costs.searchPlan
            }
        }
    }

    suspend fun updateAiRequestsRemaining(context: Context, remainingMonth: Int) {
        context.sessionDataStore.edit { prefs ->
            val limit = prefs[KEY_AI_MONTHLY_LIMIT] ?: 0
            prefs[KEY_AI_REMAINING] = remainingMonth
            if (limit > 0) {
                prefs[KEY_AI_USED_MONTH] = (limit - remainingMonth).coerceAtLeast(0)
            }
        }
    }

    suspend fun updatePlan(context: Context, planLevel: Int, subscriptionUntil: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_PLAN_LEVEL] = planLevel
            prefs[KEY_SUBSCRIPTION_UNTIL] = subscriptionUntil
        }
    }

    suspend fun clear(context: Context) {
        context.sessionDataStore.edit { it.clear() }
        cachedToken = null
    }
}
