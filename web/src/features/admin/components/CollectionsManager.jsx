import { useState, useEffect } from 'react';
import { FolderOpen, Trash2, Loader2, ChefHat } from 'lucide-react';
import adminAPI from '../admin';
import Pagination from './Pagination';
import { formatRelativeTime } from '../../../shared/utils/formatRelativeTime';
import styles from '../AdminDashboard.module.css';
import LoadingScreen from '../../../shared/components/LoadingScreen';
import ConfirmDialog from '../../../shared/components/ConfirmDialog';

const CollectionsManager = () => {
    const [collections, setCollections] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [actionLoading, setActionLoading] = useState(null);
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState(null);

    const load = async () => {
        setLoading(true);
        try {
            const res = await adminAPI.getAdminCollections({
                page,
                size: 10,
                sort: 'createdAt,desc',
            });
            setCollections(res.data.content || []);
            setTotalPages(res.data.page.totalPages || 0);
            setTotalElements(res.data.page.totalElements || 0);
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, [page]);

    const openDeleteDialog = (id, name) => {
        setDeleteTarget({ id, name });
        setDeleteDialogOpen(true);
    };

    const confirmDelete = async () => {
        if (!deleteTarget) return;
        setActionLoading(deleteTarget.id);
        try {
            await adminAPI.deleteAdminCollection(deleteTarget.id);
            setCollections((prev) => prev.filter((c) => c.id !== deleteTarget.id));
            setDeleteDialogOpen(false);
        } catch {
            alert('Failed to delete collection.');
        } finally {
            setActionLoading(null);
        }
    };

    return (
        <div className={styles.managerWrap}>
            <div className={styles.managerHeader}>
                <span className={styles.managerCount}>{totalElements} collections total</span>
            </div>
            {loading ? (
                <LoadingScreen
                    icon={<ChefHat size={52} strokeWidth={1.3} />}
                    message="Loading collections..."
                    fullPage={false}
                />
            ) : (
                <>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                {['Collection', 'Owner ID', 'Recipes', 'Created', 'Actions'].map((h) => (
                                    <th key={h} className={styles.th}>
                                        {h}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {collections.map((c) => (
                                <tr key={c.id} className={styles.tr}>
                                    <td className={styles.td}>
                                        <div className={styles.collectionCell}>
                                            <div className={styles.collMiniThumb}>
                                                {c.coverImage ? (
                                                    <img src={c.coverImage} alt="" />
                                                ) : (
                                                    <FolderOpen size={20} />
                                                )}
                                            </div>
                                            <div>
                                                <div className={styles.collName}>{c.name}</div>
                                                {c.description && (
                                                    <div className={styles.collDesc}>
                                                        {c.description.slice(0, 50)}
                                                        {c.description.length > 50 ? '…' : ''}
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </td>
                                    <td className={styles.td}>
                                        <span className={styles.idChip}>#{c.userId}</span>
                                    </td>
                                    <td className={styles.td}>
                                        <span className={styles.countChip}>{c.recipeCount}</span>
                                    </td>
                                    <td className={styles.td}>{formatRelativeTime(c.createdAt)}</td>
                                    <td className={styles.td}>
                                        <button
                                            className={styles.btnDelete}
                                            onClick={() => openDeleteDialog(c.id, c.name)}
                                            disabled={actionLoading === c.id}
                                        >
                                            {actionLoading === c.id ? (
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
                title="Delete Collection?"
                message={`Are you sure you want to delete the collection "${deleteTarget?.name}"? This action cannot be undone.`}
            />
        </div>
    );
};

export default CollectionsManager;