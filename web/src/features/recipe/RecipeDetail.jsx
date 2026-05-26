import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
    ArrowLeft, FolderPlus, Pencil, Printer, Trash2,
    Timer, Flame, Clock, Globe, Lock, ChefHat, AlertCircle,
} from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import SharePanel from './components/SharePanel';
import AddToCollectionModal from '../collection/components/AddToCollectionModal';
import CookbookFacade from '../../shared/patterns/CookbookFacade';
import { withErrorBoundary } from '../../shared/patterns/ComponentDecorators';
import styles from './RecipeDetail.module.css';
import LoadingScreen from '../../shared/components/LoadingScreen';
import ConfirmDialog from '../../shared/components/ConfirmDialog';

const RecipeDetail = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { id } = useParams();

    const [recipe, setRecipe] = useState(null);
    const [ingredients, setIngredients] = useState([]);
    const [instructions, setInstructions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showAddToCollection, setShowAddToCollection] = useState(false);
    const [showDialog, setShowDialog] = useState(false);

    useEffect(() => {
        const fetchAll = async () => {
            setLoading(true);
            setError('');
            try {
                const detail = await CookbookFacade.getRecipeDetail(id);
                setRecipe(detail.recipe);
                setIngredients(detail.ingredients);
                setInstructions(detail.instructions);
            } catch {
                setError('Failed to load recipe. It may not exist or you may not have access.');
            } finally {
                setLoading(false);
            }
        };
        fetchAll();
    }, [id]);

    const handleDelete = () => setShowDialog(true);

    const confirmDelete = async () => {
        try {
            await CookbookFacade.deleteRecipe(id);
            setShowDialog(false);
            navigate('/recipes');
        } catch {
            alert('Failed to delete recipe.');
        }
    };

    const formatTime = (minutes) => {
        if (!minutes) return null;
        if (minutes < 60) return `${minutes} min`;
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        return m ? `${h}h ${m}m` : `${h}h`;
    };

    const formatDate = (dateStr) => {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', {
            month: 'short', day: 'numeric', year: 'numeric',
        });
    };

    if (loading) {
        return (
            <>
                <DefaultHeader user={user} />
                <LoadingScreen
                    icon={<ChefHat size={52} strokeWidth={1.3} />}
                    message="Loading recipe details..."
                    fullPage={false}
                />
            </>
        );
    }

    if (error || !recipe) {
        return (
            <>
                <DefaultHeader user={user} />
                <div className={styles.errorState}>
                    <div className={styles.emptyEmoji}>
                        <AlertCircle size={56} strokeWidth={1.2} style={{ color: 'var(--text-light, #B09080)' }} />
                    </div>
                    <h3>{error || 'Recipe not found.'}</h3>
                    <button className={styles.btnGhost} onClick={() => navigate('/recipes')}>
                        <ArrowLeft size={15} strokeWidth={2} style={{ marginRight: 4, verticalAlign: 'middle' }} />
                        Back to Recipes
                    </button>
                </div>
            </>
        );
    }

    const heroStyle = recipe.imageUrl
        ? {
            backgroundImage: `linear-gradient(to bottom, rgba(92,61,46,0.55) 0%, rgba(58,42,30,0.72) 100%), url(${recipe.imageUrl})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
        }
        : {};

    const hasImage = Boolean(recipe.imageUrl);

    return (
        <>
            <DefaultHeader user={user} />
            <div className={styles.page}>
                <div
                    className={`${styles.heroBar} ${hasImage ? styles.heroBarWithImage : ''}`}
                    style={heroStyle}
                >
                    <button
                        className={hasImage ? styles.backBtnOnImage : styles.backBtn}
                        onClick={() => navigate('/recipes')}
                    >
                        <ArrowLeft size={15} strokeWidth={2} style={{ marginRight: 4, verticalAlign: 'middle' }} />
                        My Recipes
                    </button>
                    <div className={styles.titleRow}>
                        <h1 className={`${styles.recipeTitle} ${hasImage ? styles.recipeTitleOnImage : ''}`}>
                            {recipe.name}
                        </h1>
                        <div className={styles.recipeActions}>
                            <SharePanel recipeId={id} initialToken={recipe.shareToken} />
                            <button
                                className={hasImage ? styles.btnGhostOnImage : styles.btnGhost}
                                onClick={() => setShowAddToCollection(true)}
                                title="Add to collection"
                            >
                                <FolderPlus size={14} strokeWidth={2} style={{ marginRight: 5, verticalAlign: 'middle' }} />
                                Add to Collection
                            </button>
                            <button
                                className={hasImage ? styles.btnGhostOnImage : styles.btnGhost}
                                onClick={() => navigate(`/recipe/${id}/edit`)}
                            >
                                <Pencil size={14} strokeWidth={2} style={{ marginRight: 5, verticalAlign: 'middle' }} />
                                Edit
                            </button>
                            <button
                                className={hasImage ? styles.btnGhostOnImage : styles.btnGhost}
                                onClick={() => window.print()}
                            >
                                <Printer size={14} strokeWidth={2} style={{ marginRight: 5, verticalAlign: 'middle' }} />
                                Print
                            </button>
                            <button className={styles.btnDanger} onClick={handleDelete}>
                                <Trash2 size={14} strokeWidth={2} style={{ marginRight: 5, verticalAlign: 'middle' }} />
                                Delete
                            </button>
                        </div>
                    </div>
                    <div className={styles.timeBadges}>
                        {recipe.prepTimeMinutes && (
                            <div className={`${styles.timeBadge} ${hasImage ? styles.timeBadgeOnImage : ''}`}>
                                <Timer size={13} strokeWidth={2} />
                                Prep <span>{formatTime(recipe.prepTimeMinutes)}</span>
                            </div>
                        )}
                        {recipe.cookTimeMinutes && (
                            <div className={`${styles.timeBadge} ${hasImage ? styles.timeBadgeOnImage : ''}`}>
                                <Flame size={13} strokeWidth={2} />
                                Cook <span>{formatTime(recipe.cookTimeMinutes)}</span>
                            </div>
                        )}
                        {recipe.totalTimeMinutes && (
                            <div className={`${styles.timeBadge} ${hasImage ? styles.timeBadgeOnImage : ''}`}>
                                <Clock size={13} strokeWidth={2} />
                                Total <span>{formatTime(recipe.totalTimeMinutes)}</span>
                            </div>
                        )}
                        <div className={`${styles.timeBadge} ${hasImage ? styles.timeBadgeOnImage : ''}`}>
                            {recipe.isPublic
                                ? <><Globe size={13} strokeWidth={2} /> <span>Public</span></>
                                : <><Lock size={12} strokeWidth={2} /> <span>Private</span></>
                            }
                        </div>
                    </div>
                </div>

                <div className={styles.body}>
                    <div className={styles.left}>
                        {ingredients.length > 0 && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>
                                    Ingredients
                                    <span className={styles.countBadge}>{ingredients.length}</span>
                                </h4>
                                <ul className={styles.ingredientList}>
                                    {ingredients.map((ing) => (
                                        <li key={ing.id} className={styles.ingredientItem}>
                                            <strong>
                                                {ing.quantity}{ing.unit ? ` ${ing.unit.toLowerCase()}` : ''}
                                            </strong>{' '}
                                            {ing.name}
                                            {ing.notes && (
                                                <span className={styles.ingNotes}> — {ing.notes}</span>
                                            )}
                                        </li>
                                    ))}
                                </ul>
                            </div>
                        )}

                        {recipe.notes && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>Notes</h4>
                                <p className={styles.notesText}>{recipe.notes}</p>
                            </div>
                        )}

                        <div className={styles.detailCard}>
                            <h4 className={styles.cardTitle}>Details</h4>
                            <div className={styles.detailMeta}>
                                {recipe.createdAt && (
                                    <span>Created: <strong>{formatDate(recipe.createdAt)}</strong></span>
                                )}
                                {recipe.updatedAt && (
                                    <span>Updated: <strong>{formatDate(recipe.updatedAt)}</strong></span>
                                )}
                            </div>
                            <div className={styles.tagRow}>
                                <span className={recipe.isPublic ? styles.tagPublic : styles.tagPrivate}>
                                    {recipe.isPublic
                                        ? <><Globe size={12} strokeWidth={2} style={{ marginRight: 4, verticalAlign: 'middle' }} /> Public</>
                                        : <><Lock size={11} strokeWidth={2} style={{ marginRight: 4, verticalAlign: 'middle' }} /> Private</>
                                    }
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className={styles.right}>
                        {recipe.description && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>About</h4>
                                <p className={styles.notesText}>{recipe.description}</p>
                            </div>
                        )}

                        {instructions.length > 0 && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>Instructions</h4>
                                <ol className={styles.stepsList}>
                                    {instructions.map((step) => (
                                        <li key={step.id} className={styles.stepItem}>
                                            <div className={styles.stepNum}>{step.stepNumber}</div>
                                            <p className={styles.stepText}>{step.description}</p>
                                        </li>
                                    ))}
                                </ol>
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {showAddToCollection && (
                <AddToCollectionModal
                    recipe={recipe}
                    onClose={() => setShowAddToCollection(false)}
                    onSaved={() => setShowAddToCollection(false)}
                />
            )}

            <ConfirmDialog
                isOpen={showDialog}
                onClose={() => setShowDialog(false)}
                onConfirm={confirmDelete}
                title="Delete Recipe?"
                message="Are you sure you want to delete this recipe? This action cannot be undone!"
            />
        </>
    );
};

export default withErrorBoundary(RecipeDetail);