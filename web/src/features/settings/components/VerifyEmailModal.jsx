import { Mail, X, CheckCircle } from 'lucide-react';
import styles from '../Settings.module.css';

const VerifyEmailModal = ({
    email, step, verifyCode, setVerifyCode,
    sendingCode, verifying, onSendCode, onVerifyCode, onClose,
}) => (
    <div className={styles.modalOverlay} onClick={onClose}>
        <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <button className={styles.modalClose} onClick={onClose}>
                <X size={16} strokeWidth={2.5} />
            </button>

            <div className={styles.modalHeader}>
                <div className={styles.modalIconWrap}>
                    <Mail size={28} strokeWidth={1.6} style={{ color: 'var(--terracotta, #C97D4E)' }} />
                </div>
                <h3 className={styles.modalTitle}>Verify Your Email</h3>
                <p className={styles.modalSubtitle}>
                    {step === 'idle'
                        ? `Send a 6-digit code to ${email}`
                        : step === 'sent'
                            ? `Enter the code sent to ${email}`
                            : 'Email verified!'}
                </p>
            </div>

            <div className={styles.modalBody}>
                {step === 'verified' ? (
                    <div className={styles.verifiedState}>
                        <CheckCircle size={40} strokeWidth={1.5} style={{ color: '#4A8B4E' }} />
                        <p>Your email has been verified.</p>
                    </div>
                ) : step === 'idle' ? (
                    <button
                        className={styles.btnPrimary}
                        onClick={onSendCode}
                        disabled={sendingCode}
                        style={{ width: '100%' }}
                    >
                        {sendingCode ? 'Sending…' : 'Send Verification Code'}
                    </button>
                ) : (
                    <form onSubmit={onVerifyCode} className={styles.codeForm}>
                        <input
                            className={styles.codeInput}
                            type="text"
                            inputMode="numeric"
                            maxLength={6}
                            placeholder="000000"
                            value={verifyCode}
                            onChange={e => setVerifyCode(e.target.value.replace(/\D/g, '').slice(0, 6))}
                            autoFocus
                        />
                        <p className={styles.codeHint}>
                            Didn't receive it?{' '}
                            <button type="button" className={styles.resendBtn} onClick={onSendCode} disabled={sendingCode}>
                                {sendingCode ? 'Sending…' : 'Resend'}
                            </button>
                        </p>
                        <button
                            type="submit"
                            className={styles.btnPrimary}
                            disabled={verifyCode.length !== 6 || verifying}
                            style={{ width: '100%' }}
                        >
                            {verifying ? 'Verifying…' : 'Confirm Code'}
                        </button>
                    </form>
                )}
            </div>
        </div>
    </div>
);

export default VerifyEmailModal;