import { X, Trash2 } from 'lucide-react';
import styles from '../Settings.module.css';

const DeleteAccountModal = ({ email, confirmEmail, setConfirmEmail, deleting, onDelete, onClose }) => {
    const match = confirmEmail === email;

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()}>
                <button className={styles.modalClose} onClick={onClose}>
                    <X size={16} strokeWidth={2.5} />
                </button>

                <div className={`${styles.modalHeader} ${styles.modalHeaderDanger}`}>
                    <div className={styles.modalIconWrap}>
                        <Trash2 size={28} strokeWidth={1.6} style={{ color: '#E05555' }} />
                    </div>
                    <h3 className={`${styles.modalTitle} ${styles.modalTitleDanger}`}>Delete Account</h3>
                    <p className={styles.modalSubtitle}>
                        This will permanently delete your account, recipes, and collections.
                    </p>
                </div>

                <div className={styles.modalBody}>
                    <div className={styles.formGroup}>
                        <label className={styles.formLabel}>
                            Type <strong>{email}</strong> to confirm
                        </label>
                        <input
                            className={styles.formInput}
                            type="email"
                            placeholder={email}
                            value={confirmEmail}
                            onChange={e => setConfirmEmail(e.target.value)}
                            autoFocus
                        />
                    </div>

                    <div className={styles.modalActions}>
                        <button className={styles.btnOutline} onClick={onClose} disabled={deleting}>
                            Cancel
                        </button>
                        <button
                            className={styles.btnDangerModal}
                            onClick={onDelete}
                            disabled={!match || deleting}
                        >
                            {deleting ? 'Deleting…' : 'Delete Account'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DeleteAccountModal;