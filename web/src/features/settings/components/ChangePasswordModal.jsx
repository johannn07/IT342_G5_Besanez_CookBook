import { useState } from 'react';
import {
    KeyRound,
    X,
    Eye,
    EyeOff,
    AlertTriangle,
    CheckCircle,
} from 'lucide-react';
import authAPI from '../../auth/auth';
import styles from '../Settings.module.css';

const ChangePasswordModal = ({ email, onClose }) => {
    const [tab, setTab] = useState('current');
    const [form, setForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [showOld, setShowOld] = useState(false);
    const [showNew, setShowNew] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [codeStep, setCodeStep] = useState('idle');
    const [code, setCode] = useState('');
    const [sendingCode, setSendingCode] = useState(false);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);

    const passwordsMatch = form.newPassword === form.confirmPassword;
    const passwordValid = form.newPassword.length >= 8;

    const handleSendCode = async () => {
        setSendingCode(true);
        setError('');
        try {
            await authAPI.sendVerificationCode({ email });
            setCodeStep('sent');
        } catch {
            setError('Failed to send code. Try again.');
        } finally {
            setSendingCode(false);
        }
    };

    const handleSubmitCurrent = async (e) => {
        e.preventDefault();
        if (!passwordsMatch || !passwordValid) return;
        setSaving(true);
        setError('');
        try {
            await authAPI.changePassword({
                oldPassword: form.oldPassword,
                newPassword: form.newPassword,
            });
            setSuccess(true);
            setTimeout(onClose, 1400);
        } catch (err) {
            setError(err.response?.data?.message || 'Incorrect current password.');
        } finally {
            setSaving(false);
        }
    };

    const handleSubmitCode = async (e) => {
        e.preventDefault();
        if (!passwordsMatch || !passwordValid || code.length !== 6) return;
        setSaving(true);
        setError('');
        try {
            await authAPI.changePassword({
                email,
                verificationCode: code,
                newPassword: form.newPassword,
            });
            setSuccess(true);
            setTimeout(onClose, 1400);
        } catch (err) {
            setError(err.response?.data?.message || 'Invalid code.');
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className={styles.modalOverlay} onClick={onClose}>
            <div className={styles.modal} onClick={e => e.stopPropagation()} style={{ maxWidth: 460 }}>
                <button className={styles.modalClose} onClick={onClose}>
                    <X size={16} strokeWidth={2.5} />
                </button>

                <div className={styles.modalHeader}>
                    <div className={styles.modalIconWrap}>
                        <KeyRound size={28} strokeWidth={1.6} style={{ color: 'var(--terracotta, #C97D4E)' }} />
                    </div>
                    <h3 className={styles.modalTitle}>Change Password</h3>
                    <p className={styles.modalSubtitle}>Choose how you want to verify your identity.</p>
                </div>

                <div className={styles.modalBody}>
                    {success ? (
                        <div className={styles.verifiedState}>
                            <CheckCircle size={40} strokeWidth={1.5} style={{ color: '#4A8B4E' }} />
                            <p>Password changed successfully.</p>
                        </div>
                    ) : (
                        <>
                            <div className={styles.tabRow}>
                                <button
                                    className={`${styles.tab} ${tab === 'current' ? styles.tabActive : ''}`}
                                    onClick={() => { setTab('current'); setError(''); }}
                                >
                                    Current Password
                                </button>
                                <button
                                    className={`${styles.tab} ${tab === 'code' ? styles.tabActive : ''}`}
                                    onClick={() => { setTab('code'); setError(''); }}
                                >
                                    Email Code
                                </button>
                            </div>

                            {error && (
                                <div className={styles.inlineError}>
                                    <AlertTriangle size={13} strokeWidth={2} />
                                    {error}
                                </div>
                            )}

                            {tab === 'current' && (
                                <form onSubmit={handleSubmitCurrent} className={styles.codeForm}>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Current Password</label>
                                        <div className={styles.pwWrap}>
                                            <input
                                                className={styles.formInput}
                                                type={showOld ? 'text' : 'password'}
                                                placeholder="Enter current password"
                                                value={form.oldPassword}
                                                onChange={e => setForm({ ...form, oldPassword: e.target.value })}
                                                required
                                            />
                                            <button type="button" className={styles.eyeBtn} onClick={() => setShowOld(v => !v)}>
                                                {showOld ? <EyeOff size={15} /> : <Eye size={15} />}
                                            </button>
                                        </div>
                                    </div>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>New Password</label>
                                        <div className={styles.pwWrap}>
                                            <input
                                                className={styles.formInput}
                                                type={showNew ? 'text' : 'password'}
                                                placeholder="Min. 8 characters"
                                                value={form.newPassword}
                                                onChange={e => setForm({ ...form, newPassword: e.target.value })}
                                                required minLength={8}
                                            />
                                            <button type="button" className={styles.eyeBtn} onClick={() => setShowNew(v => !v)}>
                                                {showNew ? <EyeOff size={15} /> : <Eye size={15} />}
                                            </button>
                                        </div>
                                    </div>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Confirm New Password</label>
                                        <div className={styles.pwWrap}>
                                            <input
                                                className={styles.formInput}
                                                type={showConfirm ? 'text' : 'password'}
                                                placeholder="Repeat new password"
                                                value={form.confirmPassword}
                                                onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                                                required
                                            />
                                            <button type="button" className={styles.eyeBtn} onClick={() => setShowConfirm(v => !v)}>
                                                {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
                                            </button>
                                        </div>
                                        {form.confirmPassword && !passwordsMatch && (
                                            <p className={styles.mismatch}>Passwords do not match</p>
                                        )}
                                        {form.confirmPassword && passwordsMatch && form.newPassword && (
                                            <p className={styles.matchText}>Passwords match ✓</p>
                                        )}
                                    </div>
                                    <button
                                        type="submit"
                                        className={styles.btnPrimary}
                                        disabled={saving || !passwordsMatch || !passwordValid || !form.oldPassword}
                                        style={{ width: '100%' }}
                                    >
                                        {saving ? 'Saving…' : 'Update Password'}
                                    </button>
                                </form>
                            )}

                            {tab === 'code' && (
                                <form onSubmit={handleSubmitCode} className={styles.codeForm}>
                                    {codeStep === 'idle' ? (
                                        <>
                                            <p className={styles.codeHint} style={{ textAlign: 'left', color: 'var(--text-mid, #7A5C46)' }}>
                                                A 6-digit code will be sent to <strong>{email}</strong>.
                                            </p>
                                            <button
                                                type="button"
                                                className={styles.btnPrimary}
                                                onClick={handleSendCode}
                                                disabled={sendingCode}
                                                style={{ width: '100%' }}
                                            >
                                                {sendingCode ? 'Sending…' : 'Send Code to Email'}
                                            </button>
                                        </>
                                    ) : (
                                        <>
                                            <div className={styles.formGroup}>
                                                <label className={styles.formLabel}>Verification Code</label>
                                                <input
                                                    className={styles.codeInput}
                                                    type="text"
                                                    inputMode="numeric"
                                                    maxLength={6}
                                                    placeholder="000000"
                                                    value={code}
                                                    onChange={e => setCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                                                    autoFocus
                                                />
                                                <p className={styles.codeHint}>
                                                    Didn't receive it?{' '}
                                                    <button type="button" className={styles.resendBtn} onClick={handleSendCode} disabled={sendingCode}>
                                                        {sendingCode ? 'Sending…' : 'Resend'}
                                                    </button>
                                                </p>
                                            </div>
                                            <div className={styles.formGroup}>
                                                <label className={styles.formLabel}>New Password</label>
                                                <div className={styles.pwWrap}>
                                                    <input
                                                        className={styles.formInput}
                                                        type={showNew ? 'text' : 'password'}
                                                        placeholder="Min. 8 characters"
                                                        value={form.newPassword}
                                                        onChange={e => setForm({ ...form, newPassword: e.target.value })}
                                                        required minLength={8}
                                                    />
                                                    <button type="button" className={styles.eyeBtn} onClick={() => setShowNew(v => !v)}>
                                                        {showNew ? <EyeOff size={15} /> : <Eye size={15} />}
                                                    </button>
                                                </div>
                                            </div>
                                            <div className={styles.formGroup}>
                                                <label className={styles.formLabel}>Confirm New Password</label>
                                                <div className={styles.pwWrap}>
                                                    <input
                                                        className={styles.formInput}
                                                        type={showConfirm ? 'text' : 'password'}
                                                        placeholder="Repeat new password"
                                                        value={form.confirmPassword}
                                                        onChange={e => setForm({ ...form, confirmPassword: e.target.value })}
                                                        required
                                                    />
                                                    <button type="button" className={styles.eyeBtn} onClick={() => setShowConfirm(v => !v)}>
                                                        {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
                                                    </button>
                                                </div>
                                                {form.confirmPassword && !passwordsMatch && (
                                                    <p className={styles.mismatch}>Passwords do not match</p>
                                                )}
                                                {form.confirmPassword && passwordsMatch && form.newPassword && (
                                                    <p className={styles.matchText}>Passwords match ✓</p>
                                                )}
                                            </div>
                                            <button
                                                type="submit"
                                                className={styles.btnPrimary}
                                                disabled={saving || code.length !== 6 || !passwordsMatch || !passwordValid}
                                                style={{ width: '100%' }}
                                            >
                                                {saving ? 'Saving…' : 'Update Password'}
                                            </button>
                                        </>
                                    )}
                                </form>
                            )}
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ChangePasswordModal;