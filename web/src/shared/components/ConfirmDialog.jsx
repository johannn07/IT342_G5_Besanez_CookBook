import styles from './ConfirmDialog.module.css';
import { X } from 'lucide-react';

const ConfirmDialog = ({ isOpen, onClose, onConfirm, message, title, confirmLabel }) => {
    if (!isOpen) return null;

    return (
        <div className={styles.overlay} onClick={onClose}>
            <div className={styles.dialog} onClick={e => e.stopPropagation()}>
                <button className={styles.closeBtn} onClick={onClose}>
                    <X size={16} strokeWidth={2.5} />
                </button>

                <div className={styles.header}>
                    <h3 className={styles.title}>{title || 'Confirm'}</h3>
                </div>
                <div className={styles.body}>
                    <p className={styles.message}>{message}</p>
                    <div className={styles.actions}>
                        <button className={styles.btnCancel} onClick={onClose}>
                            Cancel
                        </button>
                        <button className={styles.btnConfirm} onClick={onConfirm}>
                            {confirmLabel || 'Delete'}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ConfirmDialog;