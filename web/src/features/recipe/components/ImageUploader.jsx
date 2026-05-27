import { useRef, useState, useEffect } from 'react';
import { Camera, Link, ImageIcon, Loader } from 'lucide-react';
import styles from '../CreateRecipe.module.css';

const ImageUploader = ({ mode, preview, urlInput, uploading, onImageChange }) => {
    const fileInputRef = useRef(null);
    const [dragOver, setDragOver] = useState(false);

    const [localUrl, setLocalUrl] = useState(urlInput || '');

    useEffect(() => {
        setLocalUrl(urlInput || '');
    }, [urlInput]);

    useEffect(() => {
        if (!preview) setLocalUrl('');
    }, [preview]);

    const handleFileSelected = (file) => {
        if (!file) return;
        onImageChange({ action: 'SELECT_FILE', file });
    };

    const handleUrlApply = () => {
        const trimmed = localUrl.trim();
        if (!trimmed) return;
        onImageChange({ action: 'APPLY_URL', url: trimmed });
    };

    const handleRemove = () => {
        onImageChange({ action: 'REMOVE' });
    };

    const handleSwitchMode = (newMode) => {
        if (newMode === mode) return;
        onImageChange({ action: 'SWITCH_MODE', mode: newMode });
    };

    return (
        <div className={styles.formGroup}>
            <label className={styles.formLabel}>Recipe Photo</label>

            <div className={styles.imageModeTabs}>
                <button
                    type="button"
                    className={`${styles.imageModeTab} ${mode === 'cloudinary' ? styles.imageModeTabActive : ''}`}
                    onClick={() => handleSwitchMode('cloudinary')}
                >
                    <Camera size={14} strokeWidth={2} className={styles.tabIcon} aria-hidden="true" />
                    Upload Photo
                </button>
                <button
                    type="button"
                    className={`${styles.imageModeTab} ${mode === 'url' ? styles.imageModeTabActive : ''}`}
                    onClick={() => handleSwitchMode('url')}
                >
                    <Link size={14} strokeWidth={2} className={styles.tabIcon} aria-hidden="true" />
                    Paste URL
                </button>
            </div>

            {mode === 'cloudinary' && (
                <>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept="image/jpeg,image/png,image/gif,image/webp"
                        className={styles.hiddenFileInput}
                        onChange={(e) => {
                            handleFileSelected(e.target.files?.[0]);
                            e.target.value = '';
                        }}
                    />

                    {preview ? (
                        <div className={styles.imagePreviewWrap}>
                            <img
                                src={preview}
                                alt="Recipe preview"
                                className={styles.imagePreview}
                                onError={handleRemove}
                            />
                            <div className={styles.imagePreviewOverlay}>
                                <button
                                    type="button"
                                    className={styles.imagePreviewChange}
                                    onClick={() => fileInputRef.current?.click()}
                                >
                                    <Camera size={13} strokeWidth={2} style={{ marginRight: 4 }} />
                                    Change Photo
                                </button>
                                <button
                                    type="button"
                                    className={styles.imagePreviewRemove}
                                    onClick={handleRemove}
                                >
                                    Remove
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div
                            className={`${styles.dropZone} ${dragOver ? styles.dropZoneActive : ''}`}
                            onClick={() => fileInputRef.current?.click()}
                            onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                            onDragLeave={() => setDragOver(false)}
                            onDrop={(e) => {
                                e.preventDefault();
                                setDragOver(false);
                                handleFileSelected(e.dataTransfer.files?.[0]);
                            }}
                            role="button"
                            tabIndex={0}
                            onKeyDown={(e) => e.key === 'Enter' && fileInputRef.current?.click()}
                        >
                            <ImageIcon size={36} strokeWidth={1.5} color="var(--text-light, #B09080)" />
                            <div className={styles.dropZoneText}>
                                <span className={styles.dropZonePrimary}>
                                    Drop an image here or{' '}
                                    <span className={styles.dropZoneLink}>browse files</span>
                                </span>
                                <span className={styles.dropZoneHint}>
                                    JPEG, PNG, GIF, WEBP · max 5 MB
                                </span>
                            </div>
                        </div>
                    )}

                    {uploading && (
                        <p className={styles.uploadingMessage}>
                            <Loader size={14} strokeWidth={2} className={styles.spin} />
                            Uploading…
                        </p>
                    )}
                </>
            )}

            {mode === 'url' && (
                <>
                    <div className={styles.urlInputRow}>
                        <input
                            className={styles.formInput}
                            type="url"
                            placeholder="https://example.com/photo.jpg"
                            value={localUrl}
                            onChange={(e) => setLocalUrl(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter') { e.preventDefault(); handleUrlApply(); }
                            }}
                        />
                        <button
                            type="button"
                            className={styles.urlApplyBtn}
                            onClick={handleUrlApply}
                            disabled={!localUrl.trim()}
                        >
                            Preview
                        </button>
                    </div>

                    {preview && (
                        <div className={styles.imagePreviewWrap} style={{ marginTop: 10 }}>
                            <img
                                src={preview}
                                alt="Recipe preview"
                                className={styles.imagePreview}
                                onError={handleRemove}
                            />
                            <div className={styles.imagePreviewOverlay}>
                                <button
                                    type="button"
                                    className={styles.imagePreviewRemove}
                                    onClick={handleRemove}
                                >
                                    Remove
                                </button>
                            </div>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default ImageUploader;