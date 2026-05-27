import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import {
    ChefHat, Link2, Timer, Flame, Clock, Download, Lock, AlertCircle,
} from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import CookbookFacade from '../../shared/patterns/CookbookFacade';
import { withErrorBoundary } from '../../shared/patterns/ComponentDecorators';
import SaveRecipeModal from '../collection/components/SaveRecipeModal';
import styles from './SharedRecipePage.module.css';
import LoadingScreen from '../../shared/components/LoadingScreen';

const SharedRecipePage = () => {
    const { token } = useParams();
    const { user } = useAuth();
    const navigate = useNavigate();

    const [recipe, setRecipe] = useState(null);
    const [ingredients, setIngredients] = useState([]);
    const [instructions, setInstructions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [showSaveModal, setShowSaveModal] = useState(false);
    const [savedRecipe, setSavedRecipe] = useState(null);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError('');
            try {
                const detail = await CookbookFacade.getSharedRecipeDetail(token);
                setRecipe(detail.recipe);
                setIngredients(detail.ingredients);
                setInstructions(detail.instructions);
            } catch (err) {
                setError(
                    err.response?.data?.message ||
                    'This recipe link is invalid or has expired.'
                );
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [token]);

    const formatTime = (minutes) => {
        if (!minutes) return null;
        if (minutes < 60) return `${minutes} min`;
        const h = Math.floor(minutes / 60);
        const m = minutes % 60;
        return m ? `${h}h ${m}m` : `${h}h`;
    };

    const isOwnRecipe = user && recipe && user.userId === recipe.userId;

    if (loading) {
        return (
            <>
                <DefaultHeader user={user} />
                <LoadingScreen
                    icon={<ChefHat size={52} strokeWidth={1.3} />}
                    message="Loading shared recipe..."
                    fullPage={false}
                />
            </>
        );
    }

    if (error) {
        return (
            <>
                <DefaultHeader user={user} />
                <div className={styles.pageWrap}>
                    <div className={styles.errorState}>
                        <div className={styles.errorEmoji}>
                            <AlertCircle size={56} strokeWidth={1.2} style={{ color: 'var(--text-light, #B09080)' }} />
                        </div>
                        <h2 className={styles.errorTitle}>Link unavailable</h2>
                        <p className={styles.errorDesc}>{error}</p>
                        <Link to="/" className={styles.homeLink}>← Back to CookBook</Link>
                    </div>
                </div>
            </>
        );
    }

    const hasImage = Boolean(recipe.imageUrl);
    const heroStyle = hasImage
        ? {
            backgroundImage: `linear-gradient(to bottom, rgba(92,61,46,0.45) 0%, rgba(58,42,30,0.75) 100%), url(${recipe.imageUrl})`,
            backgroundSize: 'cover',
            backgroundPosition: 'center',
        }
        : {};

    return (
        <>
            <DefaultHeader user={user} />
            <div className={styles.pageWrap}>

                <div className={styles.sharedBanner}>
                    <span className={styles.sharedBannerIcon}>
                        <Link2 size={15} strokeWidth={2} />
                    </span>
                    <span>You're viewing a shared recipe</span>
                </div>

                <div className={`${styles.hero} ${hasImage ? styles.heroWithImage : ''}`} style={heroStyle}>
                    {/* Action buttons row */}
                    <div className={styles.heroActionRow}>
                        {savedRecipe && (
                            <span className={styles.savedBadge}>✓ Saved to your cookbook!</span>
                        )}

                        {user && !isOwnRecipe && !savedRecipe && (
                            <button className={styles.saveBtn} onClick={() => setShowSaveModal(true)}>
                                <Download size={14} strokeWidth={2} style={{ marginRight: 5, verticalAlign: 'middle' }} />
                                Save Recipe
                            </button>
                        )}

                        {isOwnRecipe && (
                            <button
                                className={`${styles.viewOwnBtn} ${hasImage ? styles.viewOwnBtnOnImage : ''}`}
                                onClick={() => navigate(`/recipe/${recipe.id}`)}
                            >
                                View in My Cookbook →
                            </button>
                        )}
                    </div>

                    <h1 className={`${styles.recipeTitle} ${hasImage ? styles.recipeTitleOnImage : ''}`}>
                        {recipe.name}
                    </h1>

                    {recipe.description && (
                        <p className={`${styles.recipeDesc} ${hasImage ? styles.recipeDescOnImage : ''}`}>
                            {recipe.description}
                        </p>
                    )}

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
                                <div className={!user ? styles.blurredContent : undefined}>
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
                                {!user && <GuestOverlay />}
                            </div>
                        )}

                        {recipe.notes && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>Notes</h4>
                                <div className={!user ? styles.blurredContent : undefined}>
                                    <p className={styles.notesText}>{recipe.notes}</p>
                                </div>
                                {!user && <GuestOverlay />}
                            </div>
                        )}
                    </div>

                    <div className={styles.right}>
                        {instructions.length > 0 && (
                            <div className={styles.detailCard}>
                                <h4 className={styles.cardTitle}>Instructions</h4>
                                <div className={!user ? styles.blurredContent : undefined}>
                                    <ol className={styles.stepsList}>
                                        {instructions.map((step) => (
                                            <li key={step.id} className={styles.stepItem}>
                                                <div className={styles.stepNum}>{step.stepNumber}</div>
                                                <p className={styles.stepText}>{step.description}</p>
                                            </li>
                                        ))}
                                    </ol>
                                </div>
                                {!user && <GuestOverlay />}
                            </div>
                        )}
                    </div>
                </div>

                {user && !isOwnRecipe && !savedRecipe && (
                    <div className={styles.floatingSave}>
                        <button
                            className={styles.floatingSaveBtn}
                            onClick={() => setShowSaveModal(true)}
                        >
                            <Download size={16} strokeWidth={2} style={{ marginRight: 6, verticalAlign: 'middle' }} />
                            Save Recipe to My Cookbook
                        </button>
                    </div>
                )}

                {showSaveModal && (
                    <SaveRecipeModal
                        token={token}
                        recipe={recipe}
                        onClose={() => setShowSaveModal(false)}
                        onSaved={(saved) => {
                            setSavedRecipe(saved);
                            setShowSaveModal(false);
                        }}
                    />
                )}
            </div>
        </>
    );
};

const GuestOverlay = () => (
    <div style={guestOverlayStyles.wrap}>
        <div style={guestOverlayStyles.card}>
            <div style={guestOverlayStyles.lock}>
                <Lock size={24} strokeWidth={1.8} style={{ color: 'var(--text-dark, #3A2A1E)' }} />
            </div>
            <p style={guestOverlayStyles.text}>
                <Link to="/" style={guestOverlayStyles.link}>Sign in</Link>
                {' '}to see the full recipe
            </p>
        </div>
    </div>
);

const guestOverlayStyles = {
    wrap: {
        position: 'absolute',
        inset: 0,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: '14px',
        background: 'rgba(253, 248, 242, 0.6)',
        backdropFilter: 'blur(2px)',
    },
    card: {
        background: 'white',
        borderRadius: '12px',
        padding: '16px 24px',
        textAlign: 'center',
        boxShadow: '0 4px 20px rgba(92, 61, 46, 0.12)',
        border: '1px solid #EDD8C4',
    },
    lock: { display: 'flex', justifyContent: 'center', marginBottom: '8px' },
    text: { fontSize: '0.875rem', color: '#7A5C46', margin: 0 },
    link: { color: '#C97D4E', fontWeight: 600, textDecoration: 'none' },
};

export default withErrorBoundary(SharedRecipePage);