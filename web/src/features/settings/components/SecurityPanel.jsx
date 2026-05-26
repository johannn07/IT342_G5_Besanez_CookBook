import { useState } from 'react';
import { KeyRound } from 'lucide-react';
import ChangePasswordModal from './ChangePasswordModal';
import styles from '../Settings.module.css';

const SecurityPanel = ({ user }) => {
    const [showModal, setShowModal] = useState(false);

    return (
        <div className={styles.panel}>
            <div className={styles.panelHeader}>
                <h2 className={styles.panelTitle}>Security</h2>
                <p className={styles.panelSubtitle}>Manage your password and login security.</p>
            </div>

            <div className={styles.section}>
                <h3 className={styles.sectionTitle}>
                    <KeyRound size={15} strokeWidth={2} style={{ marginRight: 7, verticalAlign: 'text-bottom' }} />
                    Password
                </h3>
                <div className={styles.securityRow}>
                    <div>
                        <div className={styles.securityLabel}>Account Password</div>
                        <div className={styles.securityHint}>
                            {user?.password === undefined
                                ? 'Google Sign-In account — no password set.'
                                : 'Last changed: unknown'}
                        </div>
                    </div>
                    <button className={styles.btnVerify} onClick={() => setShowModal(true)}>
                        Change Password
                    </button>
                </div>
            </div>

            {showModal && (
                <ChangePasswordModal
                    email={user?.email}
                    onClose={() => setShowModal(false)}
                />
            )}
        </div>
    );
};

export default SecurityPanel;