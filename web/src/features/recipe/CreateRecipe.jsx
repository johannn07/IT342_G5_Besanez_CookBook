import { useState, useEffect, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import recipeAPI from './recipe';
import ingredientAPI from './ingredient';
import instructionAPI from './instruction';
import collectionAPI from '../collection/collection';
import { ImageUploadContext, IMAGE_STRATEGIES } from './ImageUploadStrategy';
import { withErrorBoundary } from '../../shared/patterns/ComponentDecorators';
import { Save, Loader, ArrowLeft } from 'lucide-react';
import styles from './CreateRecipe.module.css';

import RecipeBasicInfo from './components/RecipeBasicInfo';
import IngredientList from './components/IngredientList';
import InstructionList from './components/InstructionList';
import RecipeNotes from './components/RecipeNotes';
import RecipeOrganization from './components/RecipeOrganization';

const emptyIngredient = () => ({
    _key: crypto.randomUUID(),
    name: '', quantity: '', unit: '', notes: '',
    id: null, _deleted: false,
});

const emptyStep = () => ({
    _key: crypto.randomUUID(),
    description: '',
    id: null, stepNumber: null, _deleted: false,
});

const CreateRecipe = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { id } = useParams();
    const isEditing = Boolean(id);

    const [form, setForm] = useState({
        name: '', description: '',
        prepTimeMinutes: '', cookTimeMinutes: '', totalTimeMinutes: '',
        imageUrl: '', isPublic: false, notes: '',
    });
    const [ingredients, setIngredients] = useState([emptyIngredient()]);
    const [steps, setSteps] = useState([emptyStep()]);
    const [collections, setCollections] = useState([]);
    const [selectedCollections, setSelectedCollections] = useState([]);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');

    const [imageMode, setImageMode] = useState('cloudinary');
    const [imageFile, setImageFile] = useState(null);
    const [imagePreview, setImagePreview] = useState(null);
    const [urlInput, setUrlInput] = useState('');
    const [uploading, setUploading] = useState(false);

    // Single instance, update strategy when mode changes
    const imageContext = useRef(new ImageUploadContext(IMAGE_STRATEGIES.cloudinary)).current;
    useEffect(() => {
        imageContext.setStrategy(IMAGE_STRATEGIES[imageMode]);
    }, [imageMode, imageContext]);

    // Auto-compute total time whenever prep or cook time changes
    useEffect(() => {
        const prep = Number(form.prepTimeMinutes) || 0;
        const cook = Number(form.cookTimeMinutes) || 0;
        setForm(f => ({ ...f, totalTimeMinutes: (prep + cook) || '' }));
    }, [form.prepTimeMinutes, form.cookTimeMinutes]);

    // Revoke blob URLs on unmount to prevent memory leaks
    useEffect(() => {
        return () => {
            if (imagePreview?.startsWith('blob:')) URL.revokeObjectURL(imagePreview);
        };
    }, [imagePreview]);

    // ─── Load existing recipe (edit mode) ───────────────────────────────────
    useEffect(() => {
        if (!isEditing) return;
        let alive = true;
        const load = async () => {
            try {
                const [recipeRes, ingRes, instRes] = await Promise.all([
                    recipeAPI.getRecipeById(id),
                    ingredientAPI.getIngredients(id),
                    instructionAPI.getInstructions(id),
                ]);
                if (!alive) return;
                const r = recipeRes.data;
                setForm({
                    name: r.name || '', description: r.description || '',
                    prepTimeMinutes: r.prepTimeMinutes ?? '',
                    cookTimeMinutes: r.cookTimeMinutes ?? '',
                    totalTimeMinutes: r.totalTimeMinutes ?? '',
                    imageUrl: r.imageUrl || '', isPublic: r.isPublic || false,
                    notes: r.notes || '',
                });
                if (r.imageUrl) {
                    setImagePreview(r.imageUrl);
                    setUrlInput(r.imageUrl);
                    setImageMode('url');
                }
                setIngredients(
                    ingRes.data.length
                        ? ingRes.data.map(i => ({ ...i, _key: crypto.randomUUID(), _deleted: false }))
                        : [emptyIngredient()]
                );
                setSteps(
                    instRes.data.length
                        ? instRes.data.map(s => ({ ...s, _key: crypto.randomUUID(), _deleted: false }))
                        : [emptyStep()]
                );
            } catch {
                setError('Failed to load recipe for editing.');
            }
        };
        load();
        return () => { alive = false; };
    }, [id, isEditing]);

    // ─── Load collections (create mode only) ────────────────────────────────
    useEffect(() => {
        if (isEditing) return;
        collectionAPI.getCollections({ size: 100 })
            .then(res => setCollections(res.data.content || []))
            .catch(() => { });
    }, [isEditing]);

    // ─── Image helpers ───────────────────────────────────────────────────────
    const resetImageState = () => {
        setImagePreview(prev => {
            if (prev?.startsWith('blob:')) URL.revokeObjectURL(prev);
            return null;
        });
        setImageFile(null);
        setUrlInput('');
        setForm(f => ({ ...f, imageUrl: '' }));
    };

    // Action-based handler passed to ImageUploader — single responsibility per action
    const handleImageChange = ({ action, file, url, mode }) => {
        switch (action) {
            case 'SWITCH_MODE':
                resetImageState();
                setImageMode(mode);
                break;

            case 'SELECT_FILE': {
                const err = imageContext.validate(file);
                if (err) { setError(err); return; }
                setError('');
                setImageFile(file);
                setImagePreview(URL.createObjectURL(file));
                break;
            }

            case 'APPLY_URL': {
                const err = imageContext.validate(url);
                if (err) { setError(err); return; }
                setError('');
                setImagePreview(url);
                setUrlInput(url);
                setImageFile(null);
                setForm(f => ({ ...f, imageUrl: url }));
                break;
            }

            case 'REMOVE':
                resetImageState();
                break;

            default:
                break;
        }
    };

    // ─── Ingredient helpers ──────────────────────────────────────────────────
    const addIngredient = () => setIngredients(p => [...p, emptyIngredient()]);

    const updateIngredient = (key, field, val) =>
        setIngredients(p => p.map(i => i._key === key ? { ...i, [field]: val } : i));

    const removeIngredient = (key) =>
        setIngredients(p => p.map(i => i._key === key ? { ...i, _deleted: true } : i));

    // ─── Step helpers ────────────────────────────────────────────────────────
    const addStep = () => setSteps(p => [...p, emptyStep()]);

    const updateStep = (key, val) =>
        setSteps(p => p.map(s => s._key === key ? { ...s, description: val } : s));

    const removeStep = (key) =>
        setSteps(p => p.map(s => s._key === key ? { ...s, _deleted: true } : s));

    const toggleCollection = (colId) =>
        setSelectedCollections(p =>
            p.includes(colId) ? p.filter(c => c !== colId) : [...p, colId]
        );

    // ─── Submit ──────────────────────────────────────────────────────────────
    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSaving(true);

        try {
            let resolvedImageUrl = form.imageUrl;

            if (imageFile) {
                setUploading(true);
                resolvedImageUrl = await imageContext.resolve({ file: imageFile, userId: user?.userId });
                setUploading(false);
            } else if (imageMode === 'url' && urlInput.trim()) {
                resolvedImageUrl = await imageContext.resolve({ url: urlInput.trim() });
            }

            const recipePayload = {
                ...form,
                imageUrl: resolvedImageUrl || null,
                prepTimeMinutes: form.prepTimeMinutes ? Number(form.prepTimeMinutes) : null,
                cookTimeMinutes: form.cookTimeMinutes ? Number(form.cookTimeMinutes) : null,
                totalTimeMinutes: form.totalTimeMinutes ? Number(form.totalTimeMinutes) : null,
            };

            let recipeId;
            if (isEditing) {
                await recipeAPI.updateRecipe(id, recipePayload);
                recipeId = id;
            } else {
                const res = await recipeAPI.createRecipe(recipePayload);
                recipeId = res.data.id;
            }

            const validIngredients = ingredients.filter(i => !i._deleted && i.name.trim());
            const validSteps = steps
                .filter(s => !s._deleted && s.description.trim())
                .map((s, idx) => ({ ...s, stepNumber: idx + 1 }));

            if (!isEditing) {
                await Promise.all([
                    ...validIngredients.map(ing =>
                        ingredientAPI.addIngredient(recipeId, {
                            name: ing.name.trim(),
                            quantity: ing.quantity ? Number(ing.quantity) : 0,
                            unit: ing.unit || null, notes: ing.notes || null,
                        })
                    ),
                    ...validSteps.map(step =>
                        instructionAPI.addInstruction(recipeId, {
                            stepNumber: step.stepNumber,
                            description: step.description.trim(),
                        })
                    ),
                ]);
                if (selectedCollections.length > 0) {
                    await Promise.all(
                        selectedCollections.map(colId =>
                            collectionAPI.addRecipeToCollection(colId, recipeId)
                        )
                    );
                }
            } else {
                // Sync ingredients
                const existingIngredients = ingredients.filter(i => i.id && !i._deleted);
                const newIngredients = validIngredients.filter(i => !i.id);
                const deletedIngredients = ingredients.filter(i => i.id && i._deleted);

                await Promise.all([
                    ...deletedIngredients.map(i => ingredientAPI.deleteIngredient(recipeId, i.id)),
                    ...existingIngredients.map(ing =>
                        ingredientAPI.updateIngredient(recipeId, ing.id, {
                            name: ing.name.trim(),
                            quantity: ing.quantity ? Number(ing.quantity) : 0,
                            unit: ing.unit || null, notes: ing.notes || null,
                        })
                    ),
                    ...newIngredients.map(ing =>
                        ingredientAPI.addIngredient(recipeId, {
                            name: ing.name.trim(),
                            quantity: ing.quantity ? Number(ing.quantity) : 0,
                            unit: ing.unit || null, notes: ing.notes || null,
                        })
                    ),
                ]);

                // Sync steps
                const existingSteps = steps.filter(s => s.id && !s._deleted);
                const newSteps = validSteps.filter(s => !s.id);
                const deletedSteps = steps.filter(s => s.id && s._deleted);

                await Promise.all([
                    ...deletedSteps.map(s => instructionAPI.deleteInstruction(recipeId, s.id)),
                    ...existingSteps.map(step =>
                        instructionAPI.updateInstruction(recipeId, step.id, {
                            stepNumber: step.stepNumber,
                            description: step.description.trim(),
                        })
                    ),
                    ...newSteps.map(step =>
                        instructionAPI.addInstruction(recipeId, {
                            stepNumber: step.stepNumber,
                            description: step.description.trim(),
                        })
                    ),
                ]);
            }

            navigate(`/recipe/${recipeId}`);
        } catch (err) {
            setUploading(false);
            setError(err.response?.data?.message || err.message || 'Failed to save recipe. Please try again.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <>
            <DefaultHeader user={user} />
            <div className={styles.page}>
                <div className={styles.pageHeader}>
                    <div className={styles.pageHeaderLeft}>
                        <button
                            type="button"
                            className={styles.btnOutline}
                            onClick={() => navigate('/recipes')}
                            disabled={saving || uploading}
                        >
                            <ArrowLeft size={15} strokeWidth={2} style={{ marginRight: 6 }} />
                            Back to Recipes
                        </button>
                        <h2 className={styles.pageTitle}>
                            {isEditing ? 'Edit Recipe' : 'Create New Recipe'}
                        </h2>
                    </div>
                </div>

                {error && <div className={styles.errorBanner}>{error}</div>}

                <form onSubmit={handleSubmit} className={styles.formBody}>
                    <RecipeBasicInfo
                        form={form}
                        onChange={updates => setForm(f => ({ ...f, ...updates }))}
                        imageMode={imageMode}
                        imagePreview={imagePreview}
                        imageFile={imageFile}
                        urlInput={urlInput}
                        uploading={uploading}
                        onImageChange={handleImageChange}
                    />

                    <IngredientList
                        ingredients={ingredients.filter(i => !i._deleted)}
                        onAdd={addIngredient}
                        onUpdate={updateIngredient}
                        onRemove={removeIngredient}
                    />

                    <InstructionList
                        steps={steps.filter(s => !s._deleted)}
                        onAdd={addStep}
                        onUpdate={updateStep}
                        onRemove={removeStep}
                    />

                    <RecipeNotes
                        value={form.notes}
                        onChange={val => setForm(f => ({ ...f, notes: val }))}
                    />

                    <RecipeOrganization
                        isPublic={form.isPublic}
                        onPublicChange={val => setForm(f => ({ ...f, isPublic: val }))}
                        collections={collections}
                        selectedCollections={selectedCollections}
                        onToggleCollection={toggleCollection}
                        showCollections={!isEditing && collections.length > 0}
                    />

                    <div className={styles.formActions}>
                        <button type="submit" className={styles.btnPrimary} disabled={saving || uploading}>
                            {uploading ? (
                                <><Loader size={15} strokeWidth={2} className={styles.spin} style={{ marginRight: 6 }} />Uploading image…</>
                            ) : saving ? 'Saving…' : (
                                <><Save size={15} strokeWidth={2} style={{ marginRight: 6 }} />{isEditing ? 'Update Recipe' : 'Save Recipe'}</>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </>
    );
};

export default withErrorBoundary(CreateRecipe);