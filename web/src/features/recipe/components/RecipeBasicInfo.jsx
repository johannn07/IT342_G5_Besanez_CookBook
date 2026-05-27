import styles from '../CreateRecipe.module.css';
import ImageUploader from './ImageUploader';
import { FileText } from 'lucide-react';

const RecipeBasicInfo = ({ form, onChange, imageMode, imagePreview, imageFile, urlInput, uploading, onImageChange }) => {

    return (
        <section className={styles.formSection}>
            <h3 className={styles.sectionTitle}>
                <FileText size={17} strokeWidth={2} className={styles.inlineIcon} />
                Basic Information
            </h3>

            <div className={styles.formGroup}>
                <label className={styles.formLabel}>Recipe Title *</label>
                <input
                    className={`${styles.formInput} ${form.name ? styles.inputActive : ''}`}
                    type="text"
                    placeholder="What's the recipe called?"
                    value={form.name}
                    onChange={(e) => onChange({ name: e.target.value })}
                    required
                />
            </div>

            <div className={styles.formGroup}>
                <label className={styles.formLabel}>Description</label>
                <textarea
                    className={styles.formTextarea}
                    placeholder="Briefly describe your recipe…"
                    value={form.description}
                    onChange={(e) => onChange({ description: e.target.value })}
                    rows={3}
                />
            </div>

            <div className={styles.formRow3}>
                {[
                    { label: 'Prep Time (min)', field: 'prepTimeMinutes', disabled: false },
                    { label: 'Cook Time (min)', field: 'cookTimeMinutes', disabled: false },
                    { label: 'Total Time (min)', field: 'totalTimeMinutes', disabled: true },
                ].map(({ label, field, disabled }) => (
                    <div className={styles.formGroup} key={field}>
                        <label className={styles.formLabel}>
                            {label}
                            {disabled && (
                                <span className={styles.autoLabel}> (auto)</span>
                            )}
                        </label>
                        <input
                            className={styles.formInput}
                            type="number"
                            min="0"
                            placeholder="0"
                            value={form[field]}
                            onChange={(e) => onChange({ [field]: e.target.value })}
                            disabled={disabled}
                        />
                    </div>
                ))}
            </div>

            <ImageUploader
                mode={imageMode}
                preview={imagePreview}
                file={imageFile}
                urlInput={urlInput}
                uploading={uploading}
                onImageChange={onImageChange}
            />
        </section>
    );
};

export default RecipeBasicInfo;