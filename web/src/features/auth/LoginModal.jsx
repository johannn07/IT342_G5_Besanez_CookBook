import { useState, useEffect, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { Eye, EyeOff, X } from 'lucide-react';
import styles from './Login.module.css';
import GoogleLoginButton from './GoogleLoginButton';

const LoginModal = ({ isOpen, onClose, onSwitchToRegister, externalError = '' }) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [isClosing, setIsClosing] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleClose = useCallback(() => {
    setIsClosing(true);
    setTimeout(() => {
      setIsClosing(false);
      onClose();
    }, 300);
  }, [onClose]);

  useEffect(() => {
    if (isOpen && externalError) {
      setError(externalError);
    }
  }, [isOpen, externalError]);

  useEffect(() => {
    if (!isOpen) {
      setEmail('');
      setPassword('');
      setError('');
      setShowPassword(false);
      setRememberMe(false);
      setIsClosing(false);
    }
  }, [isOpen]);

  useEffect(() => {
    const handleEscKey = (e) => {
      if (e.key === 'Escape' && isOpen && !isClosing) {
        handleClose();
      }
    };
    document.addEventListener('keydown', handleEscKey);
    return () => document.removeEventListener('keydown', handleEscKey);
  }, [isOpen, isClosing, handleClose]);

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }
    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen && !isClosing) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const result = await login(email, password);

    if (result.success) {
      handleClose();
      navigate('/dashboard');
    } else {
      setError(result.error);
    }
  };

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !isClosing) {
      handleClose();
    }
  };

  const handleRegisterClick = (e) => {
    e.preventDefault();
    handleClose();
    setTimeout(() => {
      onSwitchToRegister();
    }, 300);
  };

  return (
    <div
      className={`${styles.modalOverlay} ${isClosing ? styles.fadeOut : ''}`}
      onClick={handleOverlayClick}
    >
      <div className={styles.modalContainer}>
        <button className={styles.closeButton} onClick={handleClose} aria-label="Close modal">
          <X size={18} strokeWidth={2.5} />
        </button>

        <div className={styles.welcomeContainer}>
          <h1 className={styles.welcomeTitle}>Welcome Back!</h1>
          <p className={styles.welcomeSubtitle}>Sign in to access your recipes</p>
        </div>

        <div className={styles.loginContainer}>

          {error && (
            <div className={styles.errorMessage}>
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className={styles.loginForm}>
            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={`${styles.formInput} ${email ? styles.inputActive : ''}`}
                placeholder="Enter your email"
                required
                autoFocus
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.formLabel}>Password</label>
              <div className={styles.passwordInputContainer}>
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={`${styles.formInput} ${styles.passwordInput} ${password ? styles.passwordInputActive : ''}`}
                  placeholder="Enter your password"
                  required
                />
                <button
                  type="button"
                  className={styles.passwordToggle}
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                >
                  {showPassword
                    ? <EyeOff size={16} strokeWidth={2} />
                    : <Eye size={16} strokeWidth={2} />}
                </button>
              </div>
            </div>

            <div className={styles.formOptions}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className={styles.checkbox}
                />
                <span>Remember me</span>
              </label>
              <Link to="/settings?section=security" className={styles.forgotLink}>
                Forgot Password?
              </Link>
            </div>

            <button type="submit" className={styles.submitButton}>
              Sign In
            </button>
          </form>

          <div className={styles.divider}>
            <hr className={styles.lineBreak} />
            <p className={styles.registerLinkContainer}>or</p>
            <hr className={styles.lineBreak} />
          </div>

          <div className={styles.registerLinkContainer}>
            <GoogleLoginButton />
          </div>

          <p className={styles.registerLinkContainer}>
            Don't have an account?{' '}
            <button
              onClick={handleRegisterClick}
              className={styles.registerLink}
            >
              Sign Up
            </button>
          </p>

        </div>
      </div>
    </div>
  );
};

export default LoginModal;