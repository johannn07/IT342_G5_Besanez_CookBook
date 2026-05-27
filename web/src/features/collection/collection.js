import api from '../../shared/api/api';

// ─── CollectionController  /api/collection ────────────────────────────────────

// POST /api/collection
export const createCollection = (collectionData) =>
    api.post('/api/collection', collectionData);

// GET /api/collection
export const getCollections = (params = {}) =>
    api.get('/api/collection', { params });

// GET /api/collection/:id
export const getCollectionById = (id) =>
    api.get(`/api/collection/${id}`);

// PUT /api/collection/:id
export const updateCollection = (id, collectionData) =>
    api.put(`/api/collection/${id}`, collectionData);

// DELETE /api/collection/:id
export const deleteCollection = (id) =>
    api.delete(`/api/collection/${id}`);

// ─── Recipe membership ────────────────────────────────────────────────────────

// POST /api/collection/:id/recipe/:recipeId
export const addRecipeToCollection = (collectionId, recipeId) =>
    api.post(`/api/collection/${collectionId}/recipe/${recipeId}`);

// DELETE /api/collection/:id/recipe/:recipeId
export const removeRecipeFromCollection = (collectionId, recipeId) =>
    api.delete(`/api/collection/${collectionId}/recipe/${recipeId}`);

// ─── Convenience helpers ──────────────────────────────────────────────────────

export const searchCollections = (name, params = {}) =>
    getCollections({ search: name, ...params });

const collectionAPI = {
    createCollection,
    getCollections,
    getCollectionById,
    updateCollection,
    deleteCollection,
    addRecipeToCollection,
    removeRecipeFromCollection,
    searchCollections,
};

export default collectionAPI;