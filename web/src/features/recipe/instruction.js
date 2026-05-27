import api from '../../shared/api/api';

// ─── InstructionController  /api/recipe/:recipeId/instruction ────────────────

// POST /api/recipe/:recipeId/instruction
export const addInstruction = (recipeId, instructionData) =>
    api.post(`/api/recipe/${recipeId}/instruction`, instructionData);

// GET /api/recipe/:recipeId/instruction
export const getInstructions = (recipeId) =>
    api.get(`/api/recipe/${recipeId}/instruction`);

// GET /api/recipe/:recipeId/instruction/:id
export const getInstructionById = (recipeId, id) =>
    api.get(`/api/recipe/${recipeId}/instruction/${id}`);

// PUT /api/recipe/:recipeId/instruction/:id
export const updateInstruction = (recipeId, id, instructionData) =>
    api.put(`/api/recipe/${recipeId}/instruction/${id}`, instructionData);

// DELETE /api/recipe/:recipeId/instruction/:id
export const deleteInstruction = (recipeId, id) =>
    api.delete(`/api/recipe/${recipeId}/instruction/${id}`);

const instructionAPI = {
    addInstruction,
    getInstructions,
    getInstructionById,
    updateInstruction,
    deleteInstruction,
};

export default instructionAPI;