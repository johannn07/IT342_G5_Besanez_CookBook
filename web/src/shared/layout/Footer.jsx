import { UtensilsCrossed } from 'lucide-react';
import styles from './Footer.module.css';

const FacebookIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
        <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z" />
    </svg>
);

const InstagramIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="2" width="20" height="20" rx="5" ry="5" />
        <circle cx="12" cy="12" r="4" />
        <circle cx="17.5" cy="6.5" r="1" fill="currentColor" stroke="none" />
    </svg>
);

const GitHubIcon = () => (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0 1 12 6.844a9.59 9.59 0 0 1 2.504.337c1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.02 10.02 0 0 0 22 12.017C22 6.484 17.522 2 12 2z" />
    </svg>
);

const Footer = () => (
    <footer className={styles.footer}>
        <div className={styles.brand}>
            <div className={styles.logoIcon}>
                <UtensilsCrossed size={18} color="white" strokeWidth={2} />
            </div>
            <span className={styles.logoText}>CookBook</span>
        </div>

        <span className={styles.copyright}>© 2026 CookBook. All rights reserved.</span>

        <div className={styles.socials}>
            <a href="https://www.facebook.com/johntiklo6817/" target="_blank" rel="noopener noreferrer" className={styles.socialBtn} title="Facebook">
                <FacebookIcon />
            </a>
            <a href="https://www.instagram.com/iz_johannn/" target="_blank" rel="noopener noreferrer" className={styles.socialBtn} title="Instagram">
                <InstagramIcon />
            </a>
            <a href="https://github.com/Johann-107" target="_blank" rel="noopener noreferrer" className={styles.socialBtn} title="GitHub">
                <GitHubIcon />
            </a>
        </div>
    </footer>
);

export default Footer;