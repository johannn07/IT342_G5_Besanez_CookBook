import { createContext, useState, useContext, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import authAPI from './auth';
import AuthEvents, { AUTH_EVENTS } from '../../shared/patterns/AuthEventEmitter';

const AuthContext = createContext({});

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = async () => {
            const token = localStorage.getItem('token');
            if (!token) { setLoading(false); return; }
            try {
                const response = await authAPI.getMe();
                setUser(response.data);
            } catch (error) {
                console.error('Auth check failed:', error);
                localStorage.removeItem('token');
            } finally {
                setLoading(false);
            }
        };
        checkAuth();
    }, []);

    useEffect(() => {
        const unsubscribe = AuthEvents.on(AUTH_EVENTS.TOKEN_EXPIRED, () => {
            setUser(null);
            navigate('/', { replace: true });
        });
        return unsubscribe;
    }, [navigate]);

    const login = async (email, password) => {
        try {
            const response = await authAPI.login({ email, password });
            const { token, user: userData } = response.data;
            if (token) {
                localStorage.setItem('token', token);
                const meRes = await authAPI.getMe();
                setUser(meRes.data);
                AuthEvents.emit(AUTH_EVENTS.USER_LOGIN, meRes.data);
                return { success: true }
            }
            return { success: false, error: 'Login failed — no token received.' };
        } catch (error) {
            return {
                success: false,
                error: error.response?.data?.message || 'Something went wrong. Please try again.',
            };
        }
    };

    const register = async (userData) => {
        try {
            const response = await authAPI.register(userData);
            if (response.data?.success || response.status === 201) {
                return { success: true, requiresLogin: true, message: response.data?.message || 'Registration successful!' };
            }
            return { success: false, error: response.data?.message || 'Registration failed.' };
        } catch (error) {
            return { success: false, error: error.response?.data?.message || 'Something went wrong.' };
        }
    };

    const logout = async () => {
        try { await authAPI.logout(); } catch (_) { /* always clear */ }
        localStorage.removeItem('token');
        setUser(null);
        AuthEvents.emit(AUTH_EVENTS.USER_LOGOUT);
    };

    const refreshUser = useCallback(async () => {
        try {
            const response = await authAPI.getMe();
            setUser(response.data);
            AuthEvents.emit(AUTH_EVENTS.USER_UPDATED, response.data);
            return { success: true };
        } catch (error) {
            console.error('refreshUser failed:', error);
            return { success: false };
        }
    }, []);

    const changePassword = async (oldPassword, newPassword) => {
        try {
            await authAPI.changePassword({ oldPassword, newPassword });
            return { success: true };
        } catch (error) {
            return { success: false, error: error.response?.data?.message || 'Failed to change password.' };
        }
    };

    const resetPassword = async (email, newPassword) => {
        try {
            await authAPI.forgotPassword({ email, newPassword });
            return { success: true };
        } catch (error) {
            return { success: false, error: error.response?.data?.message || 'Failed to reset password.' };
        }
    };

    const loginWithGoogle = useCallback(async (token, userData) => {
        localStorage.setItem('token', token);
        try {
            const meRes = await authAPI.getMe();
            setUser(meRes.data);
            AuthEvents.emit(AUTH_EVENTS.USER_LOGIN, meRes.data);
        } catch {
            setUser(userData);
            AuthEvents.emit(AUTH_EVENTS.USER_LOGIN, userData);
        }
    }, []);

    return (
        <AuthContext.Provider
            value={{ user, loading, login, register, logout, changePassword, resetPassword, refreshUser, loginWithGoogle }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);