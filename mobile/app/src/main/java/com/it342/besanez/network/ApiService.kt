package com.it342.besanez.network

import com.it342.besanez.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<BaseResponse>

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<BaseResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResponse>

    @POST("api/auth/google/mobile")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<AuthResponse>

    // ─── User ─────────────────────────────────────────────────────────────────

    @GET("api/user/me")
    suspend fun getCurrentUser(): Response<UserResponse>

    @PUT("api/user/{id}")
    suspend fun updateUser(
        @Path("id") id: Long,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @Multipart
    @POST("api/user/me/profile-image/upload")
    suspend fun uploadProfileImage(
        @Part file: MultipartBody.Part
    ): Response<UserResponse>

    @DELETE("api/user/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<Unit>

    @Multipart
    @POST("api/image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Query("folder") folder: String
    ): Response<ImageUploadResponse>

    // ─── Recipe ───────────────────────────────────────────────────────────────

    @GET("api/recipe")
    suspend fun getRecipes(
        @Query("search") search: String? = null,
        @Query("collection") collectionId: Long? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String? = null
    ): Response<PageResponse<RecipeResponse>>

    @GET("api/recipe/public")
    suspend fun getPublicRecipes(
        @Query("search") search: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<RecipeResponse>>

    @GET("api/recipe/{id}")
    suspend fun getRecipeById(@Path("id") id: Long): Response<RecipeResponse>

    @POST("api/recipe")
    suspend fun createRecipe(@Body request: RecipeRequest): Response<RecipeResponse>

    @PUT("api/recipe/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Long,
        @Body request: RecipeRequest
    ): Response<RecipeResponse>

    @DELETE("api/recipe/{id}")
    suspend fun deleteRecipe(@Path("id") id: Long): Response<Unit>

    // ─── Ingredient ───────────────────────────────────────────────────────────

    @GET("api/recipe/{recipeId}/ingredient")
    suspend fun getIngredients(@Path("recipeId") recipeId: Long): Response<List<IngredientResponse>>

    @POST("api/recipe/{recipeId}/ingredient")
    suspend fun addIngredient(
        @Path("recipeId") recipeId: Long,
        @Body request: IngredientRequest
    ): Response<IngredientResponse>

    @PUT("api/recipe/{recipeId}/ingredient/{id}")
    suspend fun updateIngredient(
        @Path("recipeId") recipeId: Long,
        @Path("id") id: Long,
        @Body request: IngredientRequest
    ): Response<IngredientResponse>

    @DELETE("api/recipe/{recipeId}/ingredient/{id}")
    suspend fun deleteIngredient(
        @Path("recipeId") recipeId: Long,
        @Path("id") id: Long
    ): Response<Unit>

    // ─── Instruction ──────────────────────────────────────────────────────────

    @GET("api/recipe/{recipeId}/instruction")
    suspend fun getInstructions(@Path("recipeId") recipeId: Long): Response<List<InstructionResponse>>

    @POST("api/recipe/{recipeId}/instruction")
    suspend fun addInstruction(
        @Path("recipeId") recipeId: Long,
        @Body request: InstructionRequest
    ): Response<InstructionResponse>

    @PUT("api/recipe/{recipeId}/instruction/{id}")
    suspend fun updateInstruction(
        @Path("recipeId") recipeId: Long,
        @Path("id") id: Long,
        @Body request: InstructionRequest
    ): Response<InstructionResponse>

    @DELETE("api/recipe/{recipeId}/instruction/{id}")
    suspend fun deleteInstruction(
        @Path("recipeId") recipeId: Long,
        @Path("id") id: Long
    ): Response<Unit>

    // ─── Collection ───────────────────────────────────────────────────────────

    @GET("api/collection")
    suspend fun getCollections(
        @Query("search") search: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String? = null
    ): Response<PageResponse<CollectionResponse>>

    @GET("api/collection/{id}")
    suspend fun getCollectionById(@Path("id") id: Long): Response<CollectionResponse>

    @POST("api/collection")
    suspend fun createCollection(@Body request: CollectionRequest): Response<CollectionResponse>

    @PUT("api/collection/{id}")
    suspend fun updateCollection(
        @Path("id") id: Long,
        @Body request: CollectionRequest
    ): Response<CollectionResponse>

    @DELETE("api/collection/{id}")
    suspend fun deleteCollection(@Path("id") id: Long): Response<Unit>

    @POST("api/collection/{id}/recipe/{recipeId}")
    suspend fun addRecipeToCollection(
        @Path("id") id: Long,
        @Path("recipeId") recipeId: Long
    ): Response<CollectionResponse>

    @DELETE("api/collection/{id}/recipe/{recipeId}")
    suspend fun removeRecipeFromCollection(
        @Path("id") id: Long,
        @Path("recipeId") recipeId: Long
    ): Response<CollectionResponse>
}