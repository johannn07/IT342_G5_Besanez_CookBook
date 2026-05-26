import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Globe, Lock, FolderPlus, Pencil, Trash2, ChefHat } from 'lucide-react';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import AddToCollectionModal from '../collection/components/AddToCollectionModal';
import recipeAPI from './recipe';
import styles from './Recipes.module.css';
import LoadingScreen from '../../shared/components/LoadingScreen';
import ConfirmDialog from '../../shared/components/ConfirmDialog';

const BG_CLASSES = ['rc1', 'rc2', 'rc3', 'rc4', 'rc5', 'rc6'];
const EMOJI_MAP = ['🥘', '🥗', '🍋', '🍝', '🍜', '🥧', '🍲', '🥩', '🍰', '🥞'];

const Recipes = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [recipes, setRecipes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');
    const [sortBy, setSortBy] = useState('createdAt,desc');
    const [filterPublic, setFilterPublic] = useState('all');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [addToCollectionRecipe, setAddToCollectionRecipe] = useState(null);
    const [deleteTargetId, setDeleteTargetId] = useState(null);

    const fetchRecipes = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const params = {
                page,
                size: 9,
                sort: sortBy,
                ...(search.trim() && { search: search.trim() }),
            };
            const res = await recipeAPI.getRecipes(params);
            setRecipes(res.data.content || []);
            setTotalPages(res.data.totalPages || 0);
        } catch (err) {
            setError('Failed to load recipes. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [page, sortBy, search]);

    useEffect(() => {
        const debounce = setTimeout(fetchRecipes, 300);
        return () => clearTimeout(debounce);
    }, [fetchRecipes]);

    useEffect(() => {
        setPage(0);
    }, [search, sortBy, filterPublic]);

    const handleDelete = (e, id) => {
        e.stopPropagation();
        setDeleteTargetId(id);
    };

    const confirmDelete = async () => {
        if (!deleteTargetId) return;
        try {
            await recipeAPI.deleteRecipe(deleteTargetId);
            setRecipes(prev => prev.filter(r => r.id !== deleteTargetId));
        } catch {
            alert('Failed to delete recipe.');
        } finally {
            setDeleteTargetId(null);
        }
    };

    const handleAddToCollection = (e, recipe) => {
        e.stopPropagation();
        setAddToCollectionRecipe(recipe);
    };

    const filtered = filterPublic === 'all'
        ? recipes
        : recipes.filter(r => filterPublic === 'public' ? r.isPublic : !r.isPublic);

    const getBg = (index) => BG_CLASSES[index % BG_CLASSES.length];
    const getEmoji = (index) => EMOJI_MAP[index % EMOJI_MAP.length];

    return (
        <>
            <DefaultHeader user={user} />
            <div className={styles.page}>
                <div className={styles.pageHeader}>
                    <div className={styles.pageHeaderLeft}>
                        <h2 className={styles.pageTitle}>My Recipes</h2>
                        <span className={styles.recipeCount}>{filtered.length} recipes</span>
                    </div>
                    <button className={styles.btnPrimary} onClick={() => navigate('/create-recipe')}>
                        + New Recipe
                    </button>
                </div>

                <div className={styles.filterBar}>
                    <input
                        className={styles.searchInput}
                        placeholder="Search recipes…"
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                    <select
                        className={styles.sortSelect}
                        value={filterPublic}
                        onChange={e => setFilterPublic(e.target.value)}
                    >
                        <option value="all">All Recipes</option>
                        <option value="public">Public</option>
                        <option value="private">Private</option>
                    </select>
                    <select
                        className={styles.sortSelect}
                        value={sortBy}
                        onChange={e => setSortBy(e.target.value)}
                    >
                        <option value="createdAt,desc">Sort: Newest</option>
                        <option value="createdAt,asc">Sort: Oldest</option>
                        <option value="name,asc">Sort: Name A–Z</option>
                    </select>
                </div>

                {error && (
                    <div className={styles.errorBanner}>{error}</div>
                )}

                {loading ? (
                    <LoadingScreen
                        icon={<ChefHat size={52} strokeWidth={1.3} />}
                        message="Loading recipes..."
                        fullPage={false}
                    />
                ) : filtered.length === 0 ? (
                    <div className={styles.emptyState}>
                        <div className={styles.emptyEmoji}>
                            <ChefHat size={56} strokeWidth={1.2} style={{ color: 'var(--text-light, #B09080)' }} />
                        </div>
                        <h3 className={styles.emptyTitle}>
                            {search ? 'No recipes found' : 'No recipes yet'}
                        </h3>
                        <p className={styles.emptyText}>
                            {search
                                ? 'Try a different search term.'
                                : 'Start building your cookbook!'}
                        </p>
                        {!search && (
                            <button className={styles.btnPrimary} onClick={() => navigate('/create-recipe')}>
                                + Add New Recipe
                            </button>
                        )}
                    </div>
                ) : (
                    <>
                        <div className={styles.recipeGrid}>
                            {filtered.map((recipe, index) => (
                                <div
                                    key={recipe.id}
                                    className={styles.recipeCard}
                                    onClick={() => navigate(`/recipe/${recipe.id}`)}
                                >
                                    <div className={`${styles.recipeCardImg} ${styles[getBg(index)]}`}>
                                        {recipe.imageUrl
                                            ? <img src={recipe.imageUrl} alt={recipe.name} className={styles.recipeImg} />
                                            : getEmoji(index)
                                        }
                                    </div>
                                    <div className={styles.recipeCardBody}>
                                        <div className={styles.recipeCardTop}>
                                            <div className={styles.recipeCardTitle}>{recipe.name}</div>
                                            {recipe.isPublic
                                                ? <span className={styles.tagPublic}><Globe size={13} strokeWidth={2} /> Public</span>
                                                : <span className={styles.tagPrivate}><Lock size={12} strokeWidth={2} /> Private</span>
                                            }
                                        </div>
                                        <div className={styles.recipeCardMeta}>
                                            {recipe.totalTimeMinutes && (
                                                <span className={styles.metaPill}>
                                                    ⏱ {recipe.totalTimeMinutes >= 60
                                                        ? `${Math.floor(recipe.totalTimeMinutes / 60)}h ${recipe.totalTimeMinutes % 60 ? recipe.totalTimeMinutes % 60 + 'm' : ''}`
                                                        : `${recipe.totalTimeMinutes}m`}
                                                </span>
                                            )}
                                        </div>
                                        <div className={styles.recipeCardFooter}>
                                            <span className={styles.collectionTag}>
                                                {recipe.description
                                                    ? recipe.description.slice(0, 40) + (recipe.description.length > 40 ? '…' : '')
                                                    : ''}
                                            </span>
                                            <div className={styles.cardActions} onClick={e => e.stopPropagation()}>
                                                <button
                                                    className={styles.iconBtn}
                                                    onClick={(e) => handleAddToCollection(e, recipe)}
                                                    title="Add to collection"
                                                >
                                                    <FolderPlus size={13} strokeWidth={2} />
                                                </button>
                                                <button
                                                    className={styles.iconBtn}
                                                    onClick={() => navigate(`/recipe/${recipe.id}/edit`)}
                                                    title="Edit"
                                                >
                                                    <Pencil size={13} strokeWidth={2} />
                                                </button>
                                                <button
                                                    className={styles.iconBtn}
                                                    onClick={(e) => handleDelete(e, recipe.id)}
                                                    title="Delete"
                                                >
                                                    <Trash2 size={13} strokeWidth={2} />
                                                </button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>

                        {totalPages > 1 && (
                            <div className={styles.pagination}>
                                <button
                                    className={styles.pageBtn}
                                    onClick={() => setPage(p => p - 1)}
                                    disabled={page === 0}
                                >← Prev</button>
                                <span className={styles.pageInfo}>
                                    Page {page + 1} of {totalPages}
                                </span>
                                <button
                                    className={styles.pageBtn}
                                    onClick={() => setPage(p => p + 1)}
                                    disabled={page >= totalPages - 1}
                                >Next →</button>
                            </div>
                        )}
                    </>
                )}
            </div>

            {addToCollectionRecipe && (
                <AddToCollectionModal
                    recipe={addToCollectionRecipe}
                    onClose={() => setAddToCollectionRecipe(null)}
                    onSaved={() => setAddToCollectionRecipe(null)}
                />
            )}

            <ConfirmDialog
                isOpen={deleteTargetId !== null}
                onClose={() => setDeleteTargetId(null)}
                onConfirm={confirmDelete}
                title="Delete Recipe?"
                message="Are you sure you want to delete this recipe? This action cannot be undone!"
            />
        </>
    );
};

export default Recipes;