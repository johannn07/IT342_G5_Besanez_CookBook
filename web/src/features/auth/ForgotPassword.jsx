import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { UtensilsCrossed, Eye, EyeOff } from 'lucide-react';
import authAPI from './auth';
import styles from './ForgotPassword.module.css';

const ForgotPassword = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [email, setEmail] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleEmailSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            setStep(2);
        } finally {
            setLoading(false);
        }
    };

    const handlePasswordSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }
        if (newPassword.length < 8) {
            setError('Password must be at least 8 characters.');
            return;
        }

        setLoading(true);
        try {
            await authAPI.forgotPassword({ email, newPassword });
            navigate('/', { state: { resetSuccess: true } });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Failed to reset password. Check your email and try again.'
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className={styles.pageWrap}>
            <div className={styles.card}>
                <div className={styles.cardHeader}>
                    <div className={styles.logo}>
                        <div className={styles.logoIcon}>
                            <UtensilsCrossed size={20} color="white" strokeWidth={2} />
                        </div>
                        <span className={styles.logoText}>CookBook</span>
                    </div>
                    <h2 className={styles.title}>
                        {step === 1 ? 'Forgot Password?' : 'Set New Password'}
                    </h2>
                    <p className={styles.subtitle}>
                        {step === 1
                            ? 'Enter your email to reset your password.'
                            : `Enter a new password for ${email}`}
                    </p>
                </div>

                <div className={styles.cardBody}>
                    {error && <div className={styles.errorMsg}>{error}</div>}

                    {step === 1 && (
                        <form onSubmit={handleEmailSubmit} className={styles.form}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>Email Address</label>
                                <input
                                    className={`${styles.formInput} ${email ? styles.inputActive : ''}`}
                                    type="email"
                                    placeholder="your@email.com"
                                    value={email}
                                    onChange={e => setEmail(e.target.value)}
                                    required
                                    autoFocus
                                />
                            </div>
                            <button type="submit" className={styles.btnPrimary} disabled={loading}>
                                {loading ? 'Verifying…' : 'Continue →'}
                            </button>
                        </form>
                    )}

                    {step === 2 && (
                        <form onSubmit={handlePasswordSubmit} className={styles.form}>
                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>New Password</label>
                                <div className={styles.passwordWrap}>
                                    <input
                                        className={`${styles.formInput} ${newPassword ? styles.inputActive : ''}`}
                                        type={showNew ? 'text' : 'password'}
                                        placeholder="Min. 8 characters"
                                        value={newPassword}
                                        onChange={e => setNewPassword(e.target.value)}
                                        required
                                        autoFocus
                                        minLength={8}
                                    />
                                    <button
                                        type="button"
                                        className={styles.eyeBtn}
                                        onClick={() => setShowNew(!showNew)}
                                        aria-label={showNew ? 'Hide password' : 'Show password'}
                                    >
                                        {showNew
                                            ? <EyeOff size={16} strokeWidth={2} />
                                            : <Eye size={16} strokeWidth={2} />}
                                    </button>
                                </div>
                            </div>

                            <div className={styles.formGroup}>
                                <label className={styles.formLabel}>Confirm Password</label>
                                <div className={styles.passwordWrap}>
                                    <input
                                        className={`${styles.formInput} ${confirmPassword ? styles.inputActive : ''}`}
                                        type={showConfirm ? 'text' : 'password'}
                                        placeholder="Repeat password"
                                        value={confirmPassword}
                                        onChange={e => setConfirmPassword(e.target.value)}
                                        required
                                    />
                                    <button
                                        type="button"
                                        className={styles.eyeBtn}
                                        onClick={() => setShowConfirm(!showConfirm)}
                                        aria-label={showConfirm ? 'Hide password' : 'Show password'}
                                    >
                                        {showConfirm
                                            ? <EyeOff size={16} strokeWidth={2} />
                                            : <Eye size={16} strokeWidth={2} />}
                                    </button>
                                </div>
                                {confirmPassword && newPassword !== confirmPassword && (
                                    <p className={styles.mismatch}>Passwords do not match</p>
                                )}
                                {confirmPassword && newPassword === confirmPassword && (
                                    <p className={styles.match}>Passwords match ✓</p>
                                )}
                            </div>

                            <div className={styles.btnRow}>
                                <button type="button" className={styles.btnGhost} onClick={() => setStep(1)}>
                                    ← Back
                                </button>
                                <button type="submit" className={styles.btnPrimary} disabled={loading}>
                                    {loading ? 'Saving…' : 'Reset Password'}
                                </button>
                            </div>
                        </form>
                    )}

                    <p className={styles.loginLink}>
                        Remember your password?{' '}
                        <Link to="/" className={styles.link}>Sign In</Link>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ForgotPassword;