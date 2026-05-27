import { useState, useEffect } from 'react';
import { Star, User, Trash2, Loader2, ChefHat } from 'lucide-react';
import adminAPI from '../admin';
import Pagination from './Pagination';
import { formatRelativeTime } from '../../../shared/utils/formatRelativeTime';
import styles from '../AdminDashboard.module.css';
import LoadingScreen from '../../../shared/components/LoadingScreen';
import ConfirmDialog from '../../../shared/components/ConfirmDialog';

const UsersManager = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
    const [actionLoading, setActionLoading] = useState(null);

    // Delete confirmation
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState(null);

    // Role change confirmation
    const [roleDialogOpen, setRoleDialogOpen] = useState(false);
    const [roleTarget, setRoleTarget] = useState(null);

    const load = async () => {
        setLoading(true);
        try {
            const res = await adminAPI.getAdminUsers({
                page,
                size: 10,
                sort: 'createdAt,desc',
                ...(search.trim() && { search: search.trim() }),
            });
            setUsers(res.data.content || []);
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

    // Delete dialog handlers
    const openDeleteDialog = (userId, name) => {
        setDeleteTarget({ userId, name });
        setDeleteDialogOpen(true);
    };

    const confirmDelete = async () => {
        if (!deleteTarget) return;
        setActionLoading(deleteTarget.userId);
        try {
            await adminAPI.deleteAdminUser(deleteTarget.userId);
            setUsers((prev) => prev.filter((u) => u.userId !== deleteTarget.userId));
            setDeleteDialogOpen(false);
        } catch {
            alert('Failed to delete user.');
        } finally {
            setActionLoading(null);
        }
    };

    // Role toggle dialog handlers
    const openRoleDialog = (userId, name, currentRole) => {
        setRoleTarget({ userId, name, currentRole });
        setRoleDialogOpen(true);
    };

    const confirmRoleToggle = async () => {
        if (!roleTarget) return;
        setActionLoading(roleTarget.userId);
        try {
            const res = await adminAPI.toggleUserRole(roleTarget.userId);
            setUsers((prev) =>
                prev.map((u) =>
                    u.userId === roleTarget.userId ? { ...u, ...res.data } : u
                )
            );
            setRoleDialogOpen(false);
        } catch {
            alert('Failed to toggle role.');
        } finally {
            setActionLoading(null);
        }
    };

    return (
        <div className={styles.managerWrap}>
            <div className={styles.managerHeader}>
                <div className={styles.managerInfo}>
                    <span className={styles.managerCount}>{totalElements} users total</span>
                </div>
                <input
                    className={styles.searchBar}
                    placeholder="Search by name or email…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                />
            </div>

            {loading ? (
                <LoadingScreen
                    icon={<ChefHat size={52} strokeWidth={1.3} />}
                    message="Loading users..."
                    fullPage={false}
                />
            ) : (
                <>
                    <table className={styles.table}>
                        <thead>
                            <tr>
                                {['User', 'Email', 'Role', 'Joined', 'Actions'].map((h) => (
                                    <th key={h} className={styles.th}>
                                        {h}
                                    </th>
                                ))}
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((u) => (
                                <tr key={u.userId} className={styles.tr}>
                                    <td className={styles.td}>
                                        <div className={styles.userCell}>
                                            <div className={styles.miniAvatar}>
                                                {u.profileImage ? (
                                                    <img src={u.profileImage} alt="" />
                                                ) : (
                                                    <span>
                                                        {(
                                                            (u.firstName?.[0] || '') + (u.lastName?.[0] || '')
                                                        ).toUpperCase()}
                                                    </span>
                                                )}
                                            </div>
                                            <span>
                                                {u.firstName} {u.lastName}
                                            </span>
                                        </div>
                                    </td>
                                    <td className={styles.td}>{u.email}</td>
                                    <td className={styles.td}>
                                        {u.role === 'ADMIN' ? (
                                            <span className={styles.adminRoleBadge}>
                                                <Star size={14} /> Admin
                                            </span>
                                        ) : (
                                            <span className={styles.userRoleBadge}>
                                                <User size={14} /> User
                                            </span>
                                        )}
                                    </td>
                                    <td className={styles.td}>
                                        {formatRelativeTime(u.createdAt || u.joinedAt)}
                                    </td>
                                    <td className={styles.td}>
                                        <div className={styles.actionBtns}>
                                            <button
                                                className={styles.btnToggleRole}
                                                onClick={() =>
                                                    openRoleDialog(
                                                        u.userId,
                                                        `${u.firstName} ${u.lastName}`,
                                                        u.role
                                                    )
                                                }
                                                disabled={actionLoading === u.userId}
                                                title={
                                                    u.role === 'ADMIN' ? 'Demote to User' : 'Promote to Admin'
                                                }
                                            >
                                                {actionLoading === u.userId ? (
                                                    <Loader2 size={16} />
                                                ) : u.role === 'ADMIN' ? (
                                                    <User size={16} />
                                                ) : (
                                                    <Star size={16} />
                                                )}
                                            </button>
                                            <button
                                                className={styles.btnDelete}
                                                onClick={() =>
                                                    openDeleteDialog(
                                                        u.userId,
                                                        `${u.firstName} ${u.lastName}`
                                                    )
                                                }
                                                disabled={actionLoading === u.userId}
                                                title="Delete user"
                                            >
                                                {actionLoading === u.userId ? (
                                                    <Loader2 size={16} />
                                                ) : (
                                                    <Trash2 size={16} />
                                                )}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                    <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
                </>
            )}

            {/* Delete confirmation */}
            <ConfirmDialog
                isOpen={deleteDialogOpen}
                onClose={() => setDeleteDialogOpen(false)}
                onConfirm={confirmDelete}
                title="Delete User?"
                message={`Are you sure you want to delete the user "${deleteTarget?.name}"? This will also delete all their recipes and collections.`}
                confirmLabel="Delete"
            />

            {/* Role change confirmation */}
            <ConfirmDialog
                isOpen={roleDialogOpen}
                onClose={() => setRoleDialogOpen(false)}
                onConfirm={confirmRoleToggle}
                title="Change User Role?"
                message={
                    roleTarget
                        ? `Are you sure you want to ${roleTarget.currentRole === 'ADMIN' ? 'demote' : 'promote'} "${roleTarget.name}" to ${roleTarget.currentRole === 'ADMIN' ? 'User' : 'Admin'}?`
                        : ''
                }
                confirmLabel={roleTarget?.currentRole === 'ADMIN' ? 'Demote' : 'Promote'}
            />
        </div>
    );
};

export default UsersManager;