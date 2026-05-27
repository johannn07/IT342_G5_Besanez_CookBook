import { Component } from 'react';

export function withErrorBoundary(WrappedComponent) {
    class ErrorBoundary extends Component {
        constructor(props) {
            super(props);
            this.state = { hasError: false, message: '' };
        }

        static getDerivedStateFromError(error) {
            return { hasError: true, message: error?.message || '' };
        }

        componentDidCatch(error, info) {
            console.error('ErrorBoundary caught an error:', error, info);
        }

        handleRetry = () => {
            this.setState({ hasError: false, message: '' });
        };

        render() {
            if (this.state.hasError) {
                return (
                    <div style={styles.center}>
                        <div style={styles.emoji}>⚠️</div>
                        <p style={{ ...styles.text, fontWeight: 600, fontSize: '1rem' }}>
                            Something went wrong
                        </p>
                        {this.state.message && (
                            <p style={{ ...styles.text, fontSize: '0.85rem', opacity: 0.7 }}>
                                {this.state.message}
                            </p>
                        )}
                        <button style={styles.retryBtn} onClick={this.handleRetry}>
                            Try again
                        </button>
                    </div>
                );
            }

            return <WrappedComponent {...this.props} />;
        }
    }

    ErrorBoundary.displayName =
        `withErrorBoundary(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

    return ErrorBoundary;
}

// ─── Shared inline styles ─────────────────────────────────────────────────────

const styles = {
    center: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '80px 24px',
        gap: '12px',
        minHeight: '300px',
        fontFamily: "'DM Sans', sans-serif",
        textAlign: 'center',
    },
    emoji: {
        fontSize: '3rem',
    },
    text: {
        fontSize: '0.9rem',
        color: 'var(--text-light, #B09080)',
        margin: 0,
    },
    retryBtn: {
        marginTop: '8px',
        background: 'var(--peach-light, #FAE3CC)',
        color: 'var(--terracotta-dark, #A05E33)',
        border: 'none',
        borderRadius: '10px',
        padding: '8px 20px',
        fontFamily: "'DM Sans', sans-serif",
        fontWeight: 600,
        fontSize: '0.875rem',
        cursor: 'pointer',
    },
};