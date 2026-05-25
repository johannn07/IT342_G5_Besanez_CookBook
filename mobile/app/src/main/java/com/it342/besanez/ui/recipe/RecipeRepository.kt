package com.it342.besanez.ui.recipe

import com.it342.besanez.model.RecipeResponse
import com.it342.besanez.model.PageResponse
import com.it342.besanez.network.ApiClient

class RecipeRepository {

    private val api = ApiClient.apiService

    suspend fun getRecipes(
        search: String? = null,
        page: Int = 0,
        size: Int = 10
    ): Result<PageResponse<RecipeResponse>> {
        return try {
            val response = api.getRecipes(
                search = if (search.isNullOrBlank()) null else search,
                page = page,
                size = size,
                sort = "createdAt,asc"
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load recipes"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Check your connection."))
        }
    }

    suspend fun deleteRecipe(id: Long): Result<Unit> {
        return try {
            val response = api.deleteRecipe(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete recipe"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error. Check your connection."))
        }
    }
}