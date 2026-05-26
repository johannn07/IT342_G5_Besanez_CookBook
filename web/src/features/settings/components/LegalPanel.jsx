import { useNavigate } from 'react-router-dom';
import { ExternalLink } from 'lucide-react';
import { LEGAL_LINKS } from '../constants';
import styles from '../Settings.module.css';

const LegalPanel = () => {
    const navigate = useNavigate();

    return (
        <div className={styles.panel}>
            <div className={styles.panelHeader}>
                <h2 className={styles.panelTitle}>Legal</h2>
                <p className={styles.panelSubtitle}>Policies, terms, and contact information.</p>
            </div>

            <div className={styles.section}>
                <div className={styles.legalList}>
                    {LEGAL_LINKS.map(({ label, path, href, desc }) => (
                        <button
                            key={label}
                            className={styles.legalItem}
                            onClick={() => path ? navigate(path) : window.open(href, '_blank')}
                        >
                            <div>
                                <div className={styles.legalLabel}>{label}</div>
                                <div className={styles.legalDesc}>{desc}</div>
                            </div>
                            <ExternalLink size={14} strokeWidth={2} style={{ color: 'var(--text-light, #B09080)', flexShrink: 0 }} />
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default LegalPanel;