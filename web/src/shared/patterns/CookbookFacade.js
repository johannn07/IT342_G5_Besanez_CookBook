import recipeAPI from '../../features/recipe/recipe';
import ingredientAPI from '../../features/recipe/ingredient';
import instructionAPI from '../../features/recipe/instruction';
import collectionAPI from '../../features/collection/collection';
import { publicClient } from '../api/APIClientFactory';

class CookbookFacade {

    static async getRecipeDetail(recipeId) {
        const [recipeRes, ingRes, instRes] = await Promise.all([
            recipeAPI.getRecipeById(recipeId),
            ingredientAPI.getIngredients(recipeId),
            instructionAPI.getInstructions(recipeId),
        ]);
        return {
            recipe: recipeRes.data,
            ingredients: ingRes.data,
            instructions: instRes.data,
        };
    }

    static async getSharedRecipeDetail(token) {
        const recipeRes = await publicClient.get(`/api/share/${token}`);
        const recipe = recipeRes.data;
        const [ingRes, instRes] = await Promise.all([
            publicClient.get(`/api/recipe/${recipe.id}/ingredient`).catch(() => ({ data: [] })),
            publicClient.get(`/api/recipe/${recipe.id}/instruction`).catch(() => ({ data: [] })),
        ]);

        return {
            recipe,
            ingredients: ingRes.data || [],
            instructions: instRes.data || [],
        };
    }

    static async getDashboardData() {
        const [recentRes, allRes, collectionsRes] = await Promise.all([
            recipeAPI.getRecipes({ size: 3, sort: 'createdAt,desc', page: 0 }),
            recipeAPI.getRecipes({ size: 1, page: 0 }),
            collectionAPI.getCollections({ size: 50, sort: 'createdAt,desc', page: 0 }),
        ]);

        const totalRecipes = allRes.data.page?.totalElements
            ?? allRes.data.totalElements
            ?? 0;

        const totalCollections = collectionsRes.data.page?.totalElements
            ?? collectionsRes.data.totalElements
            ?? 0;

        return {
            recentRecipes: recentRes.data.content || [],
            totalRecipes,
            collections: collectionsRes.data.content || [],
            totalCollections,
        };
    }

    static async createRecipeWithDetails(recipePayload, ingredients, steps, collectionIds = []) {
        const recipeRes = await recipeAPI.createRecipe(recipePayload);
        const recipeId = recipeRes.data.id;

        await Promise.all([
            ...ingredients.map((ing) =>
                ingredientAPI.addIngredient(recipeId, ing)
            ),
            ...steps.map((step, idx) =>
                instructionAPI.addInstruction(recipeId, {
                    stepNumber: idx + 1,
                    description: step.description.trim(),
                })
            ),
        ]);

        if (collectionIds.length > 0) {
            await Promise.all(
                collectionIds.map((colId) =>
                    collectionAPI.addRecipeToCollection(colId, recipeId)
                )
            );
        }

        return { recipeId, recipe: recipeRes.data };
    }

    static async updateRecipeWithDetails(recipeId, recipePayload, ingredients, steps) {
        await recipeAPI.updateRecipe(recipeId, recipePayload);

        await Promise.all([
            ...ingredients
                .filter((i) => i.id)
                .map((ing) =>
                    ingredientAPI.updateIngredient(recipeId, ing.id, {
                        name: ing.name.trim(),
                        quantity: ing.quantity ? Number(ing.quantity) : 0,
                        unit: ing.unit || null,
                        notes: ing.notes || null,
                    })
                ),
            ...steps
                .filter((s) => s.id)
                .map((step, idx) =>
                    instructionAPI.updateInstruction(recipeId, step.id, {
                        stepNumber: step.stepNumber || idx + 1,
                        description: step.description.trim(),
                    })
                ),
        ]);
    }

    /** Deletes a recipe by id. */
    static async deleteRecipe(recipeId) {
        await recipeAPI.deleteRecipe(recipeId);
    }
}

export default CookbookFacade;