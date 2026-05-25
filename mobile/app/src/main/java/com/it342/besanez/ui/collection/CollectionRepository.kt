package com.it342.besanez.ui.collection

import com.it342.besanez.model.CollectionRequest
import com.it342.besanez.model.CollectionResponse
import com.it342.besanez.model.PageResponse
import com.it342.besanez.model.RecipeResponse
import com.it342.besanez.network.ApiClient

class CollectionRepository {

    private val api = ApiClient.apiService

    suspend fun getCollections(
        search: String? = null,
        page: Int = 0,
        size: Int = 50
    ): Result<PageResponse<CollectionResponse>> = runCatching {
        val res = api.getCollections(
            search = search?.ifBlank { null },
            page = page,
            size = size,
            sort = "createdAt,asc"
        )
        if (res.isSuccessful) res.body()!!
        else error("Failed to load collections")
    }

    suspend fun getCollectionById(id: Long): Result<CollectionResponse> = runCatching {
        val res = api.getCollectionById(id)
        if (res.isSuccessful) res.body()!!
        else error("Collection not found")
    }

    suspend fun createCollection(name: String, description: String?): Result<CollectionResponse> =
        runCatching {
            val res = api.createCollection(CollectionRequest(name, description))
            if (res.isSuccessful) res.body()!!
            else error("Failed to create collection")
        }

    suspend fun updateCollection(
        id: Long,
        name: String,
        description: String?
    ): Result<CollectionResponse> = runCatching {
        val res = api.updateCollection(id, CollectionRequest(name, description))
        if (res.isSuccessful) res.body()!!
        else error("Failed to update collection")
    }

    suspend fun deleteCollection(id: Long): Result<Unit> = runCatching {
        val res = api.deleteCollection(id)
        if (!res.isSuccessful) error("Failed to delete collection")
    }

    suspend fun getRecipesByCollection(collectionId: Long): Result<List<RecipeResponse>> = runCatching {
        val res = api.getRecipes(collectionId = collectionId, size = 200, sort = "createdAt,asc")
        if (res.isSuccessful) res.body()!!.content
        else error("Failed to load recipes")
    }

    suspend fun addRecipe(collectionId: Long, recipeId: Long): Result<CollectionResponse> =
        runCatching {
            val res = api.addRecipeToCollection(collectionId, recipeId)
            if (res.isSuccessful) res.body()!!
            else error("Failed to add recipe")
        }

    suspend fun removeRecipe(collectionId: Long, recipeId: Long): Result<CollectionResponse> =
        runCatching {
            val res = api.removeRecipeFromCollection(collectionId, recipeId)
            if (res.isSuccessful) res.body()!!
            else error("Failed to remove recipe")
        }
}