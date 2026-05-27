import api from '../../shared/api/api';

// ─── IngredientController  /api/recipe/:recipeId/ingredient ──────────────────

// POST /api/recipe/:recipeId/ingredient
export const addIngredient = (recipeId, ingredientData) =>
    api.post(`/api/recipe/${recipeId}/ingredient`, ingredientData);

// GET /api/recipe/:recipeId/ingredient
export const getIngredients = (recipeId) =>
    api.get(`/api/recipe/${recipeId}/ingredient`);

// GET /api/recipe/:recipeId/ingredient/:id
export const getIngredientById = (recipeId, id) =>
    api.get(`/api/recipe/${recipeId}/ingredient/${id}`);

// PUT /api/recipe/:recipeId/ingredient/:id
export const updateIngredient = (recipeId, id, ingredientData) =>
    api.put(`/api/recipe/${recipeId}/ingredient/${id}`, ingredientData);

// DELETE /api/recipe/:recipeId/ingredient/:id
export const deleteIngredient = (recipeId, id) =>
    api.delete(`/api/recipe/${recipeId}/ingredient/${id}`);

const ingredientAPI = {
    addIngredient,
    getIngredients,
    getIngredientById,
    updateIngredient,
    deleteIngredient,
};

export default ingredientAPI;