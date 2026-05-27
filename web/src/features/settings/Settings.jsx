import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import authAPI from '../auth/auth';
import userAPI from '../profile/user';
import {
    CheckCircle,
    AlertTriangle,
} from 'lucide-react';
import { NAV_ITEMS } from './constants';
import AccountPanel from './components/AccountPanel';
import SecurityPanel from './components/SecurityPanel';
import LegalPanel from './components/LegalPanel';
import VerifyEmailModal from './components/VerifyEmailModal';
import DeleteAccountModal from './components/DeleteAccountModal';
import styles from './Settings.module.css';

const Settings = () => {
    const { user, logout, refreshUser } = useAuth();
    const navigate = useNavigate();
    const [activeSection, setActiveSection] = useState('account');
    const [message, setMessage] = useState({ text: '', type: '' });

    const [showVerifyModal, setShowVerifyModal] = useState(false);
    const [verifyStep, setVerifyStep] = useState('idle');
    const [verifyCode, setVerifyCode] = useState('');
    const [sendingCode, setSendingCode] = useState(false);
    const [verifying, setVerifying] = useState(false);
    const [emailVerified, setEmailVerified] = useState(user?.emailVerified ?? false);

    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deleteConfirmEmail, setDeleteConfirmEmail] = useState('');
    const [deleting, setDeleting] = useState(false);

    const showMsg = (text, type = 'success') => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 3500);
    };

    const handleOpenVerify = () => {
        setVerifyStep('idle');
        setVerifyCode('');
        setShowVerifyModal(true);
    };

    const handleSendCode = async () => {
        setSendingCode(true);
        try {
            await authAPI.sendVerificationCode({ email: user.email });
            setVerifyStep('sent');
        } catch {
            showMsg('Failed to send code. Try again.', 'error');
        } finally {
            setSendingCode(false);
        }
    };

    const handleVerifyCode = async (e) => {
        e.preventDefault();
        if (verifyCode.length !== 6) return;
        setVerifying(true);
        try {
            await authAPI.verifyCode({ email: user.email, code: verifyCode });
            setVerifyStep('verified');
            setEmailVerified(true);
            await refreshUser();
            setTimeout(() => {
                setShowVerifyModal(false);
                setVerifyStep('idle');
                setVerifyCode('');
                showMsg('Email verified successfully!');
            }, 1200);
        } catch {
            showMsg('Incorrect code. Try again.', 'error');
        } finally {
            setVerifying(false);
        }
    };

    const handleDeleteAccount = async () => {
        if (deleteConfirmEmail !== user?.email) return;
        setDeleting(true);
        try {
            await userAPI.deleteUser(user.userId);
            await logout();
            navigate('/');
        } catch {
            showMsg('Failed to delete account. Try again.', 'error');
            setDeleting(false);
        }
    };

    return (
        <>
            <DefaultHeader user={user} />

            <div className={styles.page}>
                <aside className={styles.sidebar}>
                    <p className={styles.sidebarHeading}>Settings</p>
                    <nav className={styles.sidebarNav}>
                        {NAV_ITEMS.map(({ id, label, icon: Icon }) => (
                            <button
                                key={id}
                                className={`${styles.navItem} ${activeSection === id ? styles.navItemActive : ''}`}
                                onClick={() => setActiveSection(id)}
                            >
                                <Icon size={15} strokeWidth={2} />
                                {label}
                            </button>
                        ))}
                    </nav>
                </aside>

                <main className={styles.content}>
                    {message.text && (
                        <div className={`${styles.messageBanner} ${styles[message.type]}`}>
                            {message.type === 'success'
                                ? <CheckCircle size={15} strokeWidth={2} />
                                : <AlertTriangle size={15} strokeWidth={2} />}
                            {message.text}
                        </div>
                    )}

                    {activeSection === 'account' && (
                        <AccountPanel
                            user={user}
                            emailVerified={emailVerified}
                            onOpenVerify={handleOpenVerify}
                            onOpenDelete={() => setShowDeleteModal(true)}
                        />
                    )}

                    {activeSection === 'security' && (
                        <SecurityPanel user={user} />
                    )}

                    {activeSection === 'legal' && (
                        <LegalPanel />
                    )}
                </main>
            </div>

            {showVerifyModal && (
                <VerifyEmailModal
                    email={user?.email}
                    step={verifyStep}
                    verifyCode={verifyCode}
                    setVerifyCode={setVerifyCode}
                    sendingCode={sendingCode}
                    verifying={verifying}
                    onSendCode={handleSendCode}
                    onVerifyCode={handleVerifyCode}
                    onClose={() => {
                        setShowVerifyModal(false);
                        setVerifyStep('idle');
                        setVerifyCode('');
                    }}
                />
            )}

            {showDeleteModal && (
                <DeleteAccountModal
                    email={user?.email}
                    confirmEmail={deleteConfirmEmail}
                    setConfirmEmail={setDeleteConfirmEmail}
                    deleting={deleting}
                    onDelete={handleDeleteAccount}
                    onClose={() => {
                        setShowDeleteModal(false);
                        setDeleteConfirmEmail('');
                    }}
                />
            )}
        </>
    );
};

export default Settings;