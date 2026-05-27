import { useState, useEffect } from 'react';
import { Globe, Lock, CookingPot, Trash2, Loader2, ChefHat } from 'lucide-react';
import adminAPI from '../admin';
import Pagination from './Pagination';
import { formatRelativeTime } from '../../../shared/utils/formatRelativeTime';
import styles from '../AdminDashboard.module.css';
import LoadingScreen from '../../../shared/components/LoadingScreen';
import ConfirmDialog from '../../../shared/components/ConfirmDialog';

const RecipesManager = () => {
    const [recipes, setRecipes] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [actionLoading, setActionLoading] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState(null);

    const load = async () => {
        setLoading(true);
        try {
            const res = await adminAPI.getAdminRecipes({
                page,
                size: 10,
                sort: 'createdAt,desc',
                ...(search.trim() && { search: search.trim() }),
            });
            setRecipes(res.data.content || []);
            setTotalPages(res.data.page.totalPages || 0);
            setTotalElements(res.data.page.totalElements || 0);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        const t = setTimeout(load, 300);
        return () => clearTimeout(t);
    }, [page, search]);
    useEffect(() => {
        setPage(0);
    }, [search]);

    const openDeleteDialog = (id, name) => {
        setDeleteTarget({ id, name });
        setDeleteDialogOpen(true);
    };

    const confirmDelete = async () => {
        if (!deleteTarget) return;
        setActionLoading(deleteTarget.id);
        try {
            await adminAPI.deleteAdminRecipe(deleteTarget.id);
            setRecipes((prev) => prev.filter((r) => r.id !== deleteTarget.id));
            setDeleteDialogOpen(false);
        } catch {
            alert('Failed to delete recipe.');
        } finally {
            setActionLoading(null);
        }
    };

    return (
        <div className={styles.managerWrap}>
            <div className={styles.managerHeader}>
                <span className={styles.managerCount}>{totalElements} recipes total</span>
                <input
                    className={styles.searchBar}
                    placeholder="Search by recipe name…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>
            {loading ? (
                <LoadingScreen
                    icon={<ChefHat size={52} strokeWidth={1.3} />}
                    message="Loading recipes..."
                    fullPage={false}
                />
            ) : (
                <>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                {['Recipe', 'Owner ID', 'Visibility', 'Created', 'Actions'].map((h) => (
                                    <th key={h} className={styles.th}>
                                        {h}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {recipes.map((r) => (
                                <tr key={r.id} className={styles.tr}>
                                    <td className={styles.td}>
                                        <div className={styles.recipeCell}>
                                            <div className={styles.recipeMiniThumb}>
                                                {r.imageUrl ? (
                                                    <img src={r.imageUrl} alt="" />
                                                ) : (
                                                    <CookingPot size={20} />
                                                )}
                                            </div>
                                            <span>{r.name}</span>
                                        </div>
                                    </td>
                                    <td className={styles.td}>
                                        <span className={styles.idChip}>#{r.userId}</span>
                                    </td>
                                    <td className={styles.td}>
                                        {r.isPublic ? (
                                            <span className={styles.publicBadge}>
                                                <Globe size={14} /> Public
                                            </span>
                                        ) : (
                                            <span className={styles.privateBadge}>
                                                <Lock size={14} /> Private
                                            </span>
                                        )}
                                    </td>
                                    <td className={styles.td}>{formatRelativeTime(r.createdAt)}</td>
                                    <td className={styles.td}>
                                        <button
                                            className={styles.btnDelete}
                                            onClick={() => openDeleteDialog(r.id, r.name)}
                                            disabled={actionLoading === r.id}
                                        >
                                            {actionLoading === r.id ? (
                                                <Loader2 size={16} />
                                            ) : (
                                                <Trash2 size={16} />
                                            )}
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                </>
            )}

            <ConfirmDialog
                isOpen={deleteDialogOpen}
                onClose={() => setDeleteDialogOpen(false)}
                onConfirm={confirmDelete}
                title="Delete Recipe?"
                message={`Are you sure you want to delete the recipe "${deleteTarget?.name}"? This action cannot be undone.`}
            />
        </div>
    );
};

export default RecipesManager;