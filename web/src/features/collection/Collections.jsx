import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import collectionAPI from './collection';
import CollectionModal from './components/CollectionModal';
import CollectionImageSlideshow from './components/CollectionImageSlideshow';
import { FolderOpen, Pencil, Trash2, Plus, ChefHat } from 'lucide-react';
import styles from './Collections.module.css';
import LoadingScreen from '../../shared/components/LoadingScreen';

const COLOR_CLASSES = ['rust', 'sage', 'amber', 'rose', 'sky', 'plum'];

const Collections = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [collections, setCollections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [search, setSearch] = useState('');
    const [sortBy, setSortBy] = useState('createdAt,desc');

    const [modal, setModal] = useState(null);

    const fetchCollections = useCallback(async () => {
        setLoading(true);
        setError('');
        try {
            const params = {
                size: 50,
                sort: sortBy,
                ...(search.trim() && { search: search.trim() }),
            };
            const res = await collectionAPI.getCollections(params);
            setCollections(res.data.content || []);
        } catch {
            setError('Failed to load collections.');
        } finally {
            setLoading(false);
        }
    }, [search, sortBy]);

    useEffect(() => {
        const debounce = setTimeout(fetchCollections, 300);
        return () => clearTimeout(debounce);
    }, [fetchCollections]);

    const handleSaved = (saved) => {
        if (modal?.mode === 'edit') {
            setCollections(prev => prev.map(c => c.id === saved.id ? saved : c));
        } else {
            setCollections(prev => [saved, ...prev]);
        }
        setModal(null);
    };

    const handleDelete = async (e, col) => {
        e.stopPropagation();
        if (!window.confirm(`Delete "${col.name}"? This cannot be undone.`)) return;
        try {
            await collectionAPI.deleteCollection(col.id);
            setCollections(prev => prev.filter(c => c.id !== col.id));
        } catch {
            alert('Failed to delete collection.');
        }
    };

    const totalRecipes = collections.reduce((a, c) => a + (c.recipeCount || 0), 0);

    return (
        <>
            <DefaultHeader user={user} />
            <div className={styles.page}>
                <div className={styles.pageHeader}>
                    <div>
                        <h2 className={styles.pageTitle}>My Collections</h2>
                        <p className={styles.pageSubtitle}>
                            {collections.length} collections · {totalRecipes} recipes total
                        </p>
                    </div>
                    <button className={styles.btnPrimary} onClick={() => setModal({ mode: 'create' })}>
                        <Plus size={15} strokeWidth={2.5} style={{ marginRight: 5 }} />
                        New Collection
                    </button>
                </div>

                <div className={styles.filterBar}>
                    <input
                        className={styles.searchInput}
                        placeholder="Search collections…"
                        value={search}
                        onChange={e => setSearch(e.target.value)}
                    />
                    <select
                        className={styles.sortSelect}
                        value={sortBy}
                        onChange={e => setSortBy(e.target.value)}
                    >
                        <option value="name,asc">Sort: Name A–Z</option>
                        <option value="createdAt,desc">Sort: Newest</option>
                        <option value="createdAt,asc">Sort: Oldest</option>
                    </select>
                </div>

                {error && <div className={styles.errorBanner}>{error}</div>}

                {loading ? (
                    <LoadingScreen
                        icon={<ChefHat size={52} strokeWidth={1.3} />}
                        message="Loading collections..."
                        fullPage={false}
                    />
                ) : (
                    <div className={styles.collGrid}>
                        {collections.map((col, index) => {
                            const hasImages = (col.recipeImages?.length ?? 0) > 0;
                            return (
                                <div
                                    key={col.id}
                                    className={styles.collCard}
                                    onClick={() => navigate(`/collections/${col.id}`)}
                                >
                                    {hasImages ? (
                                        <div className={styles.collCardImgArea}>
                                            <CollectionImageSlideshow
                                                images={col.recipeImages}
                                                overlay={false}
                                            />
                                        </div>
                                    ) : (
                                        <div className={`${styles.colorBar} ${styles[COLOR_CLASSES[index % COLOR_CLASSES.length]]}`} />
                                    )}
                                    <div className={styles.collCardBody}>
                                        <div className={styles.collCardName}>{col.name}</div>
                                        {col.description && (
                                            <div className={styles.collCardDesc}>{col.description}</div>
                                        )}
                                        <div className={styles.collCardFooter}>
                                            <span className={styles.collCount}>{col.recipeCount || 0} recipes</span>
                                        </div>
                                        <div className={styles.cardActions} onClick={e => e.stopPropagation()}>
                                            <button
                                                className={styles.iconBtn}
                                                title="Edit"
                                                onClick={e => { e.stopPropagation(); setModal({ mode: 'edit', collection: col }); }}
                                            >
                                                <Pencil size={13} strokeWidth={2} />
                                            </button>
                                            <button
                                                className={styles.iconBtn}
                                                title="Delete"
                                                onClick={e => handleDelete(e, col)}
                                            >
                                                <Trash2 size={13} strokeWidth={2} />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}

                        <div className={styles.newCollCard} onClick={() => setModal({ mode: 'create' })}>
                            <div className={styles.newCollInner}>
                                <div className={styles.newCollIcon}>
                                    <Plus size={28} strokeWidth={1.8} color="var(--text-light, #B09080)" />
                                </div>
                                <div className={styles.newCollLabel}>New Collection</div>
                            </div>
                        </div>
                    </div>
                )}

                {!loading && collections.length === 0 && (
                    <div className={styles.emptyState}>
                        <FolderOpen size={56} strokeWidth={1.3} color="var(--text-light, #B09080)" />
                        <h3 className={styles.emptyTitle}>
                            {search ? 'No collections found' : 'No collections yet'}
                        </h3>
                        <p className={styles.emptyText}>
                            {search
                                ? 'Try a different search term.'
                                : 'Create your first collection to organise your recipes.'}
                        </p>
                    </div>
                )}
            </div>

            {modal && (
                <CollectionModal
                    mode={modal.mode}
                    collection={modal.collection ?? null}
                    onClose={() => setModal(null)}
                    onSaved={handleSaved}
                />
            )}
        </>
    );
};

export default Collections;