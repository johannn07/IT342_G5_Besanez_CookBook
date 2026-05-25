package com.it342.besanez.ui.collection

import androidx.lifecycle.*
import com.it342.besanez.model.CollectionResponse
import com.it342.besanez.model.RecipeResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CollectionViewModel : ViewModel() {

    private val repo = CollectionRepository()

    private val _collections = MutableLiveData<List<CollectionResponse>>()
    val collections: LiveData<List<CollectionResponse>> = _collections

    private val _recipes = MutableLiveData<List<RecipeResponse>>()
    val recipes: LiveData<List<RecipeResponse>> = _recipes

    private val _selected = MutableLiveData<CollectionResponse?>()
    val selected: LiveData<CollectionResponse?> = _selected

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> = _event

    sealed class Event {
        object Created : Event()
        object Updated : Event()
        object Deleted : Event()
        object RecipeAdded : Event()
        object RecipeRemoved : Event()
    }

    private var searchJob: Job? = null

    // ── List ────────────────────────────────────────────────────────────────

    fun load(search: String? = null) {
        _loading.value = true
        viewModelScope.launch {
            repo.getCollections(search = search)
                .onSuccess { _collections.value = it.content; _error.value = null }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    fun searchDebounced(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            load(search = query.ifBlank { null })
        }
    }

    // ── Detail ──────────────────────────────────────────────────────────────

    fun loadById(id: Long) {
        _loading.value = true
        viewModelScope.launch {
            repo.getCollectionById(id)
                .onSuccess { _selected.value = it; _error.value = null }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }

        viewModelScope.launch {
            repo.getRecipesByCollection(id)
                .onSuccess { _recipes.value = it; _error.value = null }
                .onFailure { _error.value = it.message }
        }
    }

    // ── Create ──────────────────────────────────────────────────────────────

    fun create(name: String, description: String?) {
        if (name.isBlank()) { _error.value = "Name required"; return }
        _loading.value = true
        viewModelScope.launch {
            repo.createCollection(name.trim(), description?.trim()?.ifBlank { null })
                .onSuccess { col ->
                    _collections.value = (_collections.value ?: emptyList()) + listOf(col)
                    _event.value = Event.Created
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    // ── Update ──────────────────────────────────────────────────────────────

    fun update(id: Long, name: String, description: String?) {
        if (name.isBlank()) { _error.value = "Name required"; return }
        _loading.value = true
        viewModelScope.launch {
            repo.updateCollection(id, name.trim(), description?.trim()?.ifBlank { null })
                .onSuccess { updated ->
                    _collections.value = _collections.value?.map {
                        if (it.id == id) updated else it
                    }
                    if (_selected.value?.id == id) _selected.value = updated
                    _event.value = Event.Updated
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    // ── Delete ──────────────────────────────────────────────────────────────

    fun delete(id: Long) {
        _loading.value = true
        viewModelScope.launch {
            repo.deleteCollection(id)
                .onSuccess {
                    _collections.value = _collections.value?.filter { it.id != id }
                    _event.value = Event.Deleted
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
            _loading.value = false
        }
    }

    // ── Recipe membership ───────────────────────────────────────────────────

    fun removeRecipe(collectionId: Long, recipeId: Long) {
        viewModelScope.launch {
            repo.removeRecipe(collectionId, recipeId)
                .onSuccess { updated ->
                    _selected.value = updated
                    _event.value = Event.RecipeRemoved
                    _error.value = null
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun clearEvent() { _event.value = null }
    fun clearError() { _error.value = null }
}