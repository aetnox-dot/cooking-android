package com.aetnox.cooking.data.remote

import com.aetnox.cooking.data.remote.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit-контракт Cooking Backend API (см. «Документация для Android-клиента.md», раздел 6).
 * Base URL: https://cooking.aetnox.workers.dev/api/v1/
 */
interface CookingApiService {

    @GET("app/config")
    suspend fun getAppConfig(): Response<AppConfigResponse>

    @POST("auth/init")
    suspend fun initAuth(
        @Body request: AuthInitRequest
    ): Response<AuthInitResponse>

    @POST("auth/transfer/create")
    suspend fun createTransferCode(
        @Body body: Map<String, String> = emptyMap()
    ): Response<TransferCreateResponse>

    @POST("auth/transfer/claim")
    suspend fun claimTransferCode(
        @Body body: TransferClaimRequest
    ): Response<TransferClaimResponse>

    @GET("user/status")
    suspend fun getUserStatus(): Response<UserStatusResponse>

    @POST("user/activate-key")
    suspend fun activateKey(
        @Body body: ActivateKeyRequest
    ): Response<ActivateKeyResponse>

    @POST("recipe/parse")
    suspend fun parseRecipe(
        @Body request: RecipeParseRequest
    ): Response<RecipeParseResponse>

    @POST("assistant/step")
    suspend fun assistantStep(
        @Body request: AssistantStepRequest
    ): Response<AssistantStepResponse>

    @POST("recipes/upload")
    suspend fun uploadRecipe(
        @Body request: RecipeUploadRequest
    ): Response<RecipeUploadResponse>

    @POST("recipes/visibility")
    suspend fun changeRecipeVisibility(
        @Body request: RecipeVisibilityRequest
    ): Response<SuccessResponse>

    @GET("recipes/{id}")
    suspend fun getCloudRecipe(
        @Path("id") recipeId: String
    ): Response<CloudRecipeResponse>

    @GET("recipes")
    suspend fun listMyRecipes(): Response<RecipesListResponse>

    @retrofit2.http.DELETE("recipes/{recipeId}")
    suspend fun deleteCloudRecipe(
        @Path("recipeId") recipeId: String
    ): Response<SuccessResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<SuccessResponse>

    @GET("recipes/public")
    suspend fun listPublicRecipes(
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("limit") limit: Int = 100
    ): Response<RecipesListResponse>

    @GET("recipes/search")
    suspend fun searchCloudRecipes(
        @retrofit2.http.Query("q") query: String,
        @retrofit2.http.Query("page") page: Int = 0,
        @retrofit2.http.Query("relaxed") relaxed: Int? = null
    ): Response<RecipeSearchResponse>

    @POST("recipes/ai-search")
    suspend fun aiSearch(
        @Body request: AiSearchRequest
    ): Response<AiSearchResponse>

    @POST("recipes/ai-search/plan")
    suspend fun aiSearchPlan(
        @Body request: AiSearchPlanRequest
    ): Response<AiSearchPlanResponse>

    @POST("recipes/ai-search/finish")
    suspend fun aiSearchFinish(
        @Body request: AiSearchFinishRequest
    ): Response<AiSearchResponse>

    @POST("recipes/backup")
    suspend fun uploadCloudBackup(
        @Body request: CloudBackupUploadRequest
    ): Response<CloudBackupUploadResponse>

    @GET("recipes/backup")
    suspend fun getCloudBackup(): Response<CloudBackupGetResponse>

    @GET("recipes/access/{code}")
    suspend fun getRecipeByAccessCode(
        @Path("code") code: String
    ): Response<CloudRecipeByCodeResponse>

    @POST("feedback")
    suspend fun sendFeedback(
        @Body body: FeedbackRequest
    ): Response<SuccessResponse>
}
