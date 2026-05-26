import { Mail, Globe, Trash2, CheckCircle, AlertTriangle } from 'lucide-react';
import styles from '../Settings.module.css';

const AccountPanel = ({ user, emailVerified, onOpenVerify, onOpenDelete }) => {
    const isGoogleUser = !user?.password;

    return (
        <div className={styles.panel}>
            <div className={styles.panelHeader}>
                <h2 className={styles.panelTitle}>Account</h2>
                <p className={styles.panelSubtitle}>Manage your email and account settings.</p>
            </div>

            {/* Email section */}
            <div className={styles.section}>
                <h3 className={styles.sectionTitle}>
                    <Mail size={15} strokeWidth={2} style={{ marginRight: 7, verticalAlign: 'text-bottom' }} />
                    Email Address
                </h3>

                <div className={styles.emailRow}>
                    <div className={styles.emailInfo}>
                        <span className={styles.emailValue}>{user?.email}</span>
                        {emailVerified ? (
                            <span className={styles.badgeVerified}>
                                <CheckCircle size={12} strokeWidth={2.5} style={{ marginRight: 4 }} />
                                Verified
                            </span>
                        ) : (
                            <span className={styles.badgeUnverified}>
                                <AlertTriangle size={11} strokeWidth={2.5} style={{ marginRight: 4 }} />
                                Unverified
                            </span>
                        )}
                    </div>
                    {!emailVerified && (
                        <button className={styles.btnVerify} onClick={onOpenVerify}>
                            Verify Email
                        </button>
                    )}
                </div>

                {!emailVerified && (
                    <p className={styles.sectionHint}>
                        Verify your email to secure your account and enable account recovery.
                    </p>
                )}
            </div>

            {/* Connected accounts section */}
            <div className={styles.section}>
                <h3 className={styles.sectionTitle}>
                    <Globe size={15} strokeWidth={2} style={{ marginRight: 7, verticalAlign: 'text-bottom' }} />
                    Connected Accounts
                </h3>

                <div className={styles.connectedRow}>
                    <div className={styles.connectedInfo}>
                        <img
                            src="https://developers.google.com/identity/images/g-logo.png"
                            alt="Google"
                            width={18}
                            height={18}
                        />
                        <div>
                            <div className={styles.connectedName}>Google</div>
                            <div className={styles.connectedEmail}>{user?.email}</div>
                        </div>
                    </div>
                    <span className={isGoogleUser ? styles.badgeConnected : styles.badgeNotConnected}>
                        {isGoogleUser ? 'Connected' : 'Not connected'}
                    </span>
                </div>
            </div>

            {/* Danger zone */}
            <div className={`${styles.section} ${styles.dangerSection}`}>
                <h3 className={styles.dangerTitle}>
                    <Trash2 size={15} strokeWidth={2} style={{ marginRight: 7, verticalAlign: 'text-bottom' }} />
                    Delete Account
                </h3>
                <p className={styles.dangerText}>
                    Permanently delete your account, all recipes, and collections. This cannot be undone.
                </p>
                <button className={styles.btnDanger} onClick={onOpenDelete}>
                    Delete My Account
                </button>
            </div>
        </div>
    );
};

export default AccountPanel;