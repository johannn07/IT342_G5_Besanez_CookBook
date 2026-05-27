export const emptyIngredient = () => ({
    _key: Date.now() + Math.random(),
    name: '',
    quantity: '',
    unit: '',
    notes: '',
});

export const emptyStep = () => ({
    _key: Date.now() + Math.random(),
    description: '',
});

class RecipeBuilder {
    constructor() {
        this._form = {
            name: '',
            description: '',
            prepTimeMinutes: '',
            cookTimeMinutes: '',
            totalTimeMinutes: '',
            imageUrl: '',
            isPublic: false,
            notes: '',
        };
        this._ingredients = [emptyIngredient()];
        this._steps = [emptyStep()];
    }

    withName(name) {
        this._form.name = name || '';
        return this;
    }

    withDescription(description) {
        this._form.description = description || '';
        return this;
    }

    withPrepTime(minutes) {
        this._form.prepTimeMinutes = minutes ?? '';
        return this;
    }

    withCookTime(minutes) {
        this._form.cookTimeMinutes = minutes ?? '';
        return this;
    }

    withTotalTime(minutes) {
        this._form.totalTimeMinutes = minutes ?? '';
        return this;
    }

    withImageUrl(url) {
        this._form.imageUrl = url || '';
        return this;
    }

    withPublic(isPublic) {
        this._form.isPublic = Boolean(isPublic);
        return this;
    }

    withNotes(notes) {
        this._form.notes = notes || '';
        return this;
    }

    withIngredients(ingredientsArray) {
        if (!Array.isArray(ingredientsArray) || ingredientsArray.length === 0) {
            this._ingredients = [emptyIngredient()];
        } else {
            this._ingredients = ingredientsArray.map((i) => ({
                ...i,
                _key: i.id ?? Date.now() + Math.random(),
                quantity: i.quantity ?? '',
                unit: i.unit ?? '',
                notes: i.notes ?? '',
            }));
        }
        return this;
    }

    withSteps(stepsArray) {
        if (!Array.isArray(stepsArray) || stepsArray.length === 0) {
            this._steps = [emptyStep()];
        } else {
            this._steps = stepsArray.map((s) => ({
                ...s,
                _key: s.id ?? Date.now() + Math.random(),
            }));
        }
        return this;
    }

    build() {
        return {
            form: { ...this._form },
            ingredients: [...this._ingredients],
            steps: [...this._steps],
        };
    }

    static fromAPIResponse(recipeDTO, ingredients = [], steps = []) {
        return new RecipeBuilder()
            .withName(recipeDTO.name)
            .withDescription(recipeDTO.description)
            .withPrepTime(recipeDTO.prepTimeMinutes)
            .withCookTime(recipeDTO.cookTimeMinutes)
            .withTotalTime(recipeDTO.totalTimeMinutes)
            .withImageUrl(recipeDTO.imageUrl)
            .withPublic(recipeDTO.isPublic)
            .withNotes(recipeDTO.notes)
            .withIngredients(ingredients)
            .withSteps(steps);
    }

    static blank() {
        return new RecipeBuilder();
    }

    static toRequestPayload(formState, imageUrl = null) {
        return {
            ...formState,
            imageUrl: imageUrl || null,
            prepTimeMinutes: formState.prepTimeMinutes ? Number(formState.prepTimeMinutes) : null,
            cookTimeMinutes: formState.cookTimeMinutes ? Number(formState.cookTimeMinutes) : null,
            totalTimeMinutes: formState.totalTimeMinutes ? Number(formState.totalTimeMinutes) : null,
        };
    }
}

export default RecipeBuilder;