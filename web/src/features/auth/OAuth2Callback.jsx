// OAuth2Callback.jsx
import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

const OAuth2Callback = () => {
    const navigate = useNavigate();
    const { loginWithGoogle } = useAuth();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;

        try {
            const params = new URLSearchParams(window.location.search);
            const token = params.get('token');

            if (token) {
                processed.current = true;
                window.history.replaceState({}, document.title, window.location.pathname);

                const userData = {
                    userId: params.get('userId'),
                    email: params.get('email'),
                    firstName: params.get('firstName'),
                    lastName: params.get('lastName'),
                };

                loginWithGoogle(token, userData);
                navigate('/dashboard', { replace: true });
            } else {
                navigate('/?error=true', { replace: true });
            }
        } catch (err) {
            console.error('OAuth2Callback error:', err);
            navigate('/?error=true', { replace: true });
        }
    }, [navigate, loginWithGoogle]);

    return (
        <div style={{ textAlign: 'center', marginTop: '50px' }}>
            <p>Signing you in with Google…</p>
        </div>
    );
};

export default OAuth2Callback;