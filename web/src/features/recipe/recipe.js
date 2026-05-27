import api from '../../shared/api/api';

// ─── RecipeController  /api/recipe ────────────────────────────────────────────

// POST /api/recipe
export const createRecipe = (recipeData) =>
    api.post('/api/recipe', recipeData);

// GET /api/recipe
export const getRecipes = (params = {}) =>
    api.get('/api/recipe', { params });

// GET /api/recipe/public
export const getPublicRecipes = (params = {}) =>
    api.get('/api/recipe/public', { params });

// GET /api/recipe/:id
export const getRecipeById = (id) =>
    api.get(`/api/recipe/${id}`);

// PUT /api/recipe/:id
export const updateRecipe = (id, recipeData) =>
    api.put(`/api/recipe/${id}`, recipeData);

// DELETE /api/recipe/:id
export const deleteRecipe = (id) =>
    api.delete(`/api/recipe/${id}`);

// ─── Convenience helpers ──────────────────────────────────────────────────────

export const searchRecipes = (name, params = {}) =>
    getRecipes({ search: name, ...params });

export const getRecipesByCollection = (collectionId, params = {}) =>
    getRecipes({ collection: collectionId, ...params });

const recipeAPI = {
    createRecipe,
    getRecipes,
    getPublicRecipes,
    getRecipeById,
    updateRecipe,
    deleteRecipe,
    searchRecipes,
    getRecipesByCollection,
};

export default recipeAPI;