const GoogleLoginButton = () => {
    const handleGoogleLogin = () => {
        window.location.href = "https://cook-book-mu-flame.vercel.app/oauth2/authorization/google";
    };

    return (
        <button onClick={handleGoogleLogin} style={styles.button}>
            <img
                src="https://developers.google.com/identity/images/g-logo.png"
                alt="Google"
                style={styles.icon}
            />
            Continue with Google
        </button>
    );
};

const styles = {
    button: {
        display: "flex",
        alignItems: "center",
        gap: "10px",
        padding: "10px 20px",
        border: "1px solid var(--terracotta, #C97D4E)",
        borderRadius: "10px",
        backgroundColor: "#fff",
        color: "var(--terracotta, #C97D4E)",
        cursor: "pointer",
        fontSize: "15px",
        fontWeight: "600",
        width: "100%",
        justifyContent: "center",
    },
    icon: {
        width: "20px",
        height: "20px",
    },
};

export default GoogleLoginButton;