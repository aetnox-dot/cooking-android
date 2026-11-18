package com.aetnox.cooking.data.remote

import com.aetnox.cooking.data.remote.model.ApiErrorCodes
import com.aetnox.cooking.data.remote.model.ApiErrorResponse
import com.aetnox.cooking.data.remote.model.AppConfigResponse
import com.aetnox.cooking.data.remote.model.AssistantStepResponse
import com.aetnox.cooking.data.remote.model.CloudRecipeByCodeResponse
import com.aetnox.cooking.data.remote.model.CloudRecipeResponse
import com.aetnox.cooking.data.remote.model.RecipeJsonDto
import com.aetnox.cooking.data.remote.model.RecipeMeta
import com.aetnox.cooking.data.remote.model.RecipeSearchResponse
import com.aetnox.cooking.data.remote.model.RecipeUploadResponse
import com.aetnox.cooking.data.remote.model.RecipeVisibilityRequest
import com.aetnox.cooking.data.remote.model.RecipesListResponse
import com.aetnox.cooking.data.remote.model.SuccessResponse
import com.aetnox.cooking.data.remote.model.UserStatusResponse
import com.aetnox.cooking.data.remote.model.toParsedRecipe
import com.aetnox.cooking.domain.ai.CookAssistantTools
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendRepositoryTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testErrorResponseParsing() {
        val jsonInput = """
            {
              "error": "Исчерпан суточный лимит оцифровки рецептов",
              "error_code": "QUOTA_EXCEEDED"
            }
        """.trimIndent()

        val parsed = json.decodeFromString(ApiErrorResponse.serializer(), jsonInput)

        assertEquals("Исчерпан суточный лимит оцифровки рецептов", parsed.error)
        assertEquals(ApiErrorCodes.QUOTA_EXCEEDED, parsed.errorCode)
    }

    @Test
    fun testAppConfigResponseParsing() {
        val jsonInput = """
            {
              "min_version_code": 1,
              "latest_version_code": 5,
              "update_url": "https://github.com/aetnox/cooking/releases",
              "maintenance_active": false
            }
        """.trimIndent()

        val parsed = json.decodeFromString(AppConfigResponse.serializer(), jsonInput)

        assertEquals(1, parsed.minVersionCode)
        assertEquals(5, parsed.latestVersionCode)
        assertEquals("https://github.com/aetnox/cooking/releases", parsed.updateUrl)
        assertFalse(parsed.maintenanceActive)
    }

    @Test
    fun testUserStatusResponseParsing() {
        val jsonInput = """
            {
              "account_id": "acc_3f4a9b218c",
              "plan_level": 2,
              "plan_name": "Pro",
              "subscription_until": 1741549800,
              "is_quarantined": false,
              "quotas": {
                "recipes": {
                  "used_today": 3,
                  "daily_limit": 35,
                  "remaining": 32
                },
                "assistant_iterations": {
                  "used_today": 12,
                  "daily_limit": 80,
                  "remaining": 68
                },
                "photos": {
                  "used_this_month": 4,
                  "monthly_limit": 30,
                  "remaining": 26
                }
              },
              "features": {
                "allow_photos": true,
                "max_agent_depth": 3
              }
            }
        """.trimIndent()

        val parsed = json.decodeFromString(UserStatusResponse.serializer(), jsonInput)

        assertEquals("acc_3f4a9b218c", parsed.accountId)
        assertEquals(2, parsed.planLevel)
        assertEquals("Pro", parsed.planName)
        assertEquals(32, parsed.quotas.recipes?.remaining)
        assertEquals(68, parsed.quotas.assistantIterations?.remaining)
        assertNotNull(parsed.quotas.photos)
        assertEquals(4, parsed.quotas.photos?.usedThisMonth)
        assertEquals(30, parsed.quotas.photos?.monthlyLimit)
        assertEquals(26, parsed.quotas.photos?.remaining)
        assertTrue(parsed.features.allowPhotos)
    }

    @Test
    fun testRecipeJsonDtoToParsedRecipeMapping() {
        val jsonInput = """
            {
              "title": "Борщ Украинский",
              "prepTimeMinutes": 30,
              "cookTimeMinutes": 90,
              "servings": 6,
              "yieldUnit": "порций",
              "chefHeatingType": "Плита медленный огонь",
              "chefTemperature": "95 °C",
              "chefTiming": "90 мин",
              "chefReadinessSign": "Овощи мягкие, бульон насыщенный",
              "ingredients": [
                {
                  "name": "Говядина на кости",
                  "amount": "500",
                  "unit": "г",
                  "groupName": "Бульон",
                  "note": "сварить до готовности"
                },
                {
                  "name": "Свекла",
                  "amount": "2",
                  "unit": "шт",
                  "groupName": "Зажарка",
                  "note": "натереть на терке"
                }
              ],
              "steps": [
                "Сварить бульон из говядины.",
                "Пассеровать свеклу с томатной пастой."
              ],
              "tags": ["Супы", "Традиционная кухня"],
              "notes": "Подавать со сметаной и чесночными пампушками.",
              "calories": 320,
              "protein": 18,
              "fat": 14,
              "carbs": 28
            }
        """.trimIndent()

        val recipeDto = json.decodeFromString(RecipeJsonDto.serializer(), jsonInput)
        val parsed = recipeDto.toParsedRecipe()

        assertEquals("Борщ Украинский", parsed.recipe.title)
        assertEquals(30, parsed.recipe.prepTimeMinutes)
        assertEquals(90, parsed.recipe.cookTimeMinutes)
        assertEquals(6.0, parsed.recipe.baseServings, 0.01)
        assertEquals(2, parsed.ingredients.size)
        assertEquals("Говядина на кости", parsed.ingredients[0].name)
        assertEquals("Бульон", parsed.ingredients[0].groupName)
        assertEquals(2, parsed.steps.size)
        assertEquals(2, parsed.tags.size)
    }

    @Test
    fun testAssistantToolCallParsing() {
        val toolCallJson = """
            {
              "reply_type": "tool_call",
              "content": "TOOL: go_to_step | 3",
              "remaining_iterations": 66
            }
        """.trimIndent()

        val response = json.decodeFromString(AssistantStepResponse.serializer(), toolCallJson)
        assertEquals("tool_call", response.replyType)

        val toolCall = CookAssistantTools.parseToolCall(response.content)
        assertNotNull(toolCall)
        assertEquals("go_to_step", toolCall?.name)
        assertEquals("3", toolCall?.arg)
    }

    @Test
    fun testRecipesListResponseParsing() {
        val jsonInput = """
            {
              "success": true,
              "recipes": [
                {
                  "recipe_id": "cloud-123",
                  "title": "Шарлотка с яблоками",
                  "is_public": 1,
                  "created_at": 1741549800,
                  "updated_at": 1741550000
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.RecipesListResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals(1, parsed.recipes.size)
        assertEquals("cloud-123", parsed.recipes[0].recipeId)
        assertEquals("Шарлотка с яблоками", parsed.recipes[0].title)
    }

    @Test
    fun testSuccessResponseParsing() {
        val jsonInput = """
            {
              "success": true,
              "message": "Отзыв успешно записан"
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.SuccessResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals("Отзыв успешно записан", parsed.message)
    }

    @Test
    fun testUserStatusWithCloudQuotaParsing() {
        val jsonInput = """
            {
              "account_id": "acc_12345",
              "plan_level": 2,
              "plan_name": "Pro",
              "subscription_until": 1741549800,
              "is_quarantined": false,
              "quotas": {
                "recipes": { "used_today": 1, "daily_limit": 10, "remaining": 9 },
                "assistant_iterations": { "used_today": 0, "daily_limit": 50, "remaining": 50 },
                "cloud": { "used_total": 7, "limit": 50, "remaining": 43 }
              },
              "features": { "allow_photos": true, "max_agent_depth": 3 }
            }
        """.trimIndent()

        val parsed = json.decodeFromString(UserStatusResponse.serializer(), jsonInput)

        assertNotNull(parsed.quotas.cloud)
        assertEquals(7, parsed.quotas.cloud?.usedTotal)
        assertEquals(50, parsed.quotas.cloud?.limit)
        assertEquals(43, parsed.quotas.cloud?.remaining)
    }

    @Test
    fun testRecipeUploadResponseWithAccessCode() {
        val jsonInput = """
            {
              "success": true,
              "recipe_id": "cloud-99",
              "access_code": "Xx9Ab3a7K2"
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.RecipeUploadResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals("cloud-99", parsed.recipeId)
        assertEquals("Xx9Ab3a7K2", parsed.accessCode)
    }

    @Test
    fun testRecipesListResponseWithJsonStringTagsParsing() {
        val jsonInput = """
            {
              "success": true,
              "recipes": [
                {
                  "recipe_id": "cloud-4-7908",
                  "title": "Эклеры / профитроли с кремом Пломбир",
                  "is_public": 1,
                  "tags": "[\"Выпечка\",\"Десерты\",\"Французская кухня\",\"Сладкое\"]",
                  "access_code": null,
                  "created_at": 1794342808,
                  "updated_at": 1794342808
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.RecipesListResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals(1, parsed.recipes.size)
        assertEquals("cloud-4-7908", parsed.recipes[0].recipeId)
        assertEquals(4, parsed.recipes[0].tags?.size)
        assertEquals("Выпечка", parsed.recipes[0].tags?.get(0))
        assertEquals("Сладкое", parsed.recipes[0].tags?.get(3))
    }

    @Test
    fun testRecipeSearchResponseParsing() {
        val jsonInput = """
            {
              "success": true,
              "results": [
                {
                  "recipe_id": "cloud-404",
                  "title": "Борщ с пампушками",
                  "tags": ["суп", "обед"],
                  "access_code": null,
                  "is_public": 1,
                  "updated_at": 1741550000
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.RecipeSearchResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals(1, parsed.results.size)
        assertEquals("Борщ с пампушками", parsed.results[0].title)
        assertEquals(2, parsed.results[0].tags?.size)
    }

    @Test
    fun testCloudRecipeByCodeResponseParsing() {
        val jsonInput = """
            {
              "recipe_id": "cloud-777",
              "title": "Секретный Пирог",
              "payload_json": "{\"title\":\"Секретный Пирог\",\"ingredients\":[],\"steps\":[]}"
            }
        """.trimIndent()

        val parsed = json.decodeFromString(com.aetnox.cooking.data.remote.model.CloudRecipeByCodeResponse.serializer(), jsonInput)

        assertEquals("cloud-777", parsed.recipeId)
        assertEquals("Секретный Пирог", parsed.title)
        assertTrue(parsed.payloadJson.contains("Секретный Пирог"))
    }

    @Test
    fun testCloudQuotaExceededErrorParsing() {
        val jsonInput = """
            {
              "error": "Cloud recipe quota exceeded for your plan",
              "error_code": "CLOUD_QUOTA_EXCEEDED"
            }
        """.trimIndent()

        val parsed = json.decodeFromString(ApiErrorResponse.serializer(), jsonInput)

        assertEquals(ApiErrorCodes.CLOUD_QUOTA_EXCEEDED, parsed.errorCode)
    }

    @Test
    fun testSmartCloudIdOrAccessCodeDetection() {
        val cloudIdSample = "cloud-123-456"
        val accessCodeSample = "8392018471"
        val alphaAccessCodeSample = "Xx9Ab3a7K2"

        val isCloudId1 = cloudIdSample.startsWith("cloud-", ignoreCase = true)
        val isCloudId2 = accessCodeSample.startsWith("cloud-", ignoreCase = true)
        val isAccessCode1 = !accessCodeSample.startsWith("cloud-", ignoreCase = true) && accessCodeSample.length in 6..16
        val isAccessCode2 = !alphaAccessCodeSample.startsWith("cloud-", ignoreCase = true) && alphaAccessCodeSample.length in 6..16

        assertTrue(isCloudId1)
        assertFalse(isCloudId2)
        assertTrue(isAccessCode1)
        assertTrue(isAccessCode2)
    }

    @Test
    fun testAppConfigResponseWithFeaturesParsing() {
        val jsonInput = """
            {
              "min_version_code": 1,
              "latest_version_code": 5,
              "update_url": "https://github.com/aetnox/cooking/releases",
              "maintenance_active": false,
              "features": {
                "parse_enabled": true,
                "assistant_enabled": false,
                "feedback_enabled": true
              }
            }
        """.trimIndent()

        val parsed = json.decodeFromString(AppConfigResponse.serializer(), jsonInput)

        assertNotNull(parsed.features)
        assertTrue(parsed.features?.parseEnabled == true)
        assertFalse(parsed.features?.assistantEnabled == true)
        assertTrue(parsed.features?.feedbackEnabled == true)
    }

    @Test
    fun testUserStatusWithResetsAndSubscriptionActiveParsing() {
        val jsonInput = """
            {
              "account_id": "acc_3f4a9b218c",
              "plan_level": 2,
              "plan_name": "Pro",
              "subscription_until": 1741549800,
              "subscription_active": true,
              "is_quarantined": false,
              "daily_resets_at": 1762905600,
              "monthly_resets_at": 1764547200,
              "quotas": {
                "recipes": { "used_today": 3, "daily_limit": 35, "remaining": 32 },
                "assistant_iterations": { "used_today": 12, "daily_limit": 80, "remaining": 68 }
              },
              "features": { "allow_photos": true, "max_agent_depth": 3 }
            }
        """.trimIndent()

        val parsed = json.decodeFromString(UserStatusResponse.serializer(), jsonInput)

        assertTrue(parsed.subscriptionActive)
        assertEquals(1762905600L, parsed.dailyResetsAt)
        assertEquals(1764547200L, parsed.monthlyResetsAt)
    }

    @Test
    fun testRecipeVisibilityRequestSerialization() {
        val req = RecipeVisibilityRequest(recipeId = "cloud-123", isPublic = true)
        val jsonStr = json.encodeToString(RecipeVisibilityRequest.serializer(), req)

        assertTrue(jsonStr.contains("\"recipe_id\":\"cloud-123\""))
        assertTrue(jsonStr.contains("\"is_public\":true"))
    }

    @Test
    fun testRecipeSearchResponseWithRelaxedAndPagination() {
        val jsonInput = """
            {
              "success": true,
              "page": 1,
              "relaxed": true,
              "has_more": true,
              "results": [
                {
                  "recipe_id": "cloud-505",
                  "title": "Похожий рецепт",
                  "tags": ["суп"],
                  "access_code": "CODE123456",
                  "is_public": 1,
                  "updated_at": 1741550000
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString(RecipeSearchResponse.serializer(), jsonInput)

        assertTrue(parsed.success)
        assertEquals(1, parsed.page)
        assertTrue(parsed.relaxed)
        assertTrue(parsed.hasMore)
        assertEquals(1, parsed.results.size)
    }

    @Test
    fun testAppConfigResponseWithAiSearchEnabledFeatureParsing() {
        val jsonInput = """
            {
              "min_version_code": 1,
              "latest_version_code": 5,
              "update_url": "https://github.com/aetnox/cooking/releases",
              "maintenance_active": false,
              "features": {
                "parse_enabled": true,
                "assistant_enabled": false,
                "ai_search_enabled": false,
                "feedback_enabled": true
              }
            }
        """.trimIndent()

        val parsed = json.decodeFromString(AppConfigResponse.serializer(), jsonInput)

        assertNotNull(parsed.features)
        assertTrue(parsed.features?.parseEnabled == true)
        assertFalse(parsed.features?.assistantEnabled == true)
        assertFalse(parsed.features?.aiSearchEnabled == true)
        assertTrue(parsed.features?.feedbackEnabled == true)
    }
}
