import { useState, useRef } from 'react';
import { useAuth } from '../auth/AuthContext';
import DefaultHeader from '../../shared/layout/DefaultHeader';
import { uploadImage } from '../../shared/api/image';
import userAPI from './user';
import {
    Camera,
    Loader2,
    Save,
    CheckCircle,
    AlertTriangle,
    Pencil,
} from 'lucide-react';
import styles from './Profile.module.css';

const COOKING_LEVELS = [
    { value: 'BEGINNER', label: 'Beginner' },
    { value: 'INTERMEDIATE', label: 'Intermediate' },
    { value: 'ADVANCED', label: 'Advanced' },
];

const Profile = () => {
    const { user, refreshUser } = useAuth();
    const fileInputRef = useRef(null);

    const [isEditing, setIsEditing] = useState(false);

    const [form, setForm] = useState({
        firstName: user?.firstName || '',
        lastName: user?.lastName || '',
        email: user?.email || '',
        birthdate: user?.birthdate || '',
        cookingLevel: user?.cookingLevel || 'BEGINNER',
    });

    const [formSnapshot, setFormSnapshot] = useState({ ...form });
    const [profileImage, setProfileImage] = useState(user?.profileImage || null);
    const [uploadingImage, setUploadingImage] = useState(false);

    const [message, setMessage] = useState({ text: '', type: '' });
    const [savingProfile, setSavingProfile] = useState(false);

    const showMessage = (text, type = 'success') => {
        setMessage({ text, type });
        setTimeout(() => setMessage({ text: '', type: '' }), 3000);
    };

    const handleStartEdit = () => {
        setFormSnapshot({ ...form });
        setIsEditing(true);
    };

    const handleCancelEdit = () => {
        setForm({ ...formSnapshot });
        setIsEditing(false);
    };

    const handleFileChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        if (!file.type.startsWith('image/')) {
            showMessage('Please select a valid image file (JPEG, PNG, GIF, WEBP).', 'error');
            return;
        }
        if (file.size > 5 * 1024 * 1024) {
            showMessage('Image must be smaller than 5 MB.', 'error');
            return;
        }

        const previewUrl = URL.createObjectURL(file);
        setProfileImage(previewUrl);
        setUploadingImage(true);

        try {
            const folder = `users/${user.userId}/profiles`;
            const uploadRes = await uploadImage(file, folder);
            const cloudinaryUrl = uploadRes.data.url;

            await userAPI.updateProfileImage(cloudinaryUrl);
            setProfileImage(cloudinaryUrl);
            showMessage('Profile photo updated!');
            await refreshUser();
        } catch (err) {
            setProfileImage(user?.profileImage || null);
            showMessage(err.response?.data?.message || 'Failed to upload photo.', 'error');
        } finally {
            setUploadingImage(false);
            e.target.value = '';
        }
    };

    const handleRemoveProfileImage = async () => {
        setUploadingImage(true);
        try {
            await userAPI.updateProfileImage('');
            setProfileImage(null);
            showMessage('Profile photo removed.');
            await refreshUser();
        } catch (err) {
            showMessage(err.response?.data?.message || 'Failed to remove photo.', 'error');
        } finally {
            setUploadingImage(false);
        }
    };

    const handleProfileSubmit = async (e) => {
        e.preventDefault();
        setSavingProfile(true);
        try {
            await userAPI.updateUser(user.userId, {
                firstName: form.firstName,
                lastName: form.lastName,
                email: form.email,
                birthdate: form.birthdate || null,
                cookingLevel: form.cookingLevel,
                password: 'placeholder-not-updated',
            });
            setFormSnapshot({ ...form });
            setIsEditing(false);
            showMessage('Profile updated successfully!');
            await refreshUser();
        } catch (err) {
            showMessage(err.response?.data?.message || 'Failed to update profile.', 'error');
        } finally {
            setSavingProfile(false);
        }
    };

    const getInitials = () => {
        const first = (form.firstName || user?.firstName || '')[0] || '';
        const last = (form.lastName || user?.lastName || '')[0] || '';
        return (first + last).toUpperCase() || 'U';
    };

    const formatBirthdate = (dateStr) => {
        if (!dateStr) return '—';
        try {
            return new Date(dateStr + 'T00:00:00').toLocaleDateString('en-US', {
                month: 'long', day: 'numeric', year: 'numeric',
            });
        } catch {
            return dateStr;
        }
    };

    const cookingLevelLabel = (value) =>
        COOKING_LEVELS.find(l => l.value === value)?.label || value;

    return (
        <>
            <DefaultHeader user={{ ...user, profileImage }} />
            <div className={styles.page}>
                {message.text && (
                    <div className={`${styles.message} ${styles[message.type]}`}>
                        {message.type === 'success'
                            ? <CheckCircle size={16} strokeWidth={2} />
                            : <AlertTriangle size={16} strokeWidth={2} />}
                        {message.text}
                    </div>
                )}

                <div className={styles.profileBody}>

                    {/* ── Left Sidebar ── */}
                    <div className={styles.left}>
                        <div className={styles.avatarSection}>

                            <div
                                className={styles.avatarWrapper}
                                onClick={() => !uploadingImage && fileInputRef.current?.click()}
                                title="Change photo"
                            >
                                {profileImage ? (
                                    <img
                                        src={profileImage}
                                        alt={getInitials()}
                                        className={styles.bigAvatarImg}
                                        onError={() => setProfileImage(null)}
                                    />
                                ) : (
                                    <div className={styles.bigAvatar}>{getInitials()}</div>
                                )}

                                <div className={`${styles.cameraOverlay} ${uploadingImage ? styles.uploading : ''}`}>
                                    {uploadingImage
                                        ? <Loader2 size={22} strokeWidth={2} style={{ animation: 'spin 1s linear infinite' }} />
                                        : <Camera size={22} strokeWidth={2} />}
                                </div>
                            </div>

                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/jpeg,image/png,image/gif,image/webp"
                                className={styles.hiddenFileInput}
                                onChange={handleFileChange}
                            />

                            <div className={styles.avatarBtns}>
                                <button
                                    className={styles.btnGhost}
                                    onClick={() => fileInputRef.current?.click()}
                                    disabled={uploadingImage}
                                >
                                    <Camera size={13} strokeWidth={2} style={{ marginRight: 5 }} />
                                    {uploadingImage ? 'Uploading…' : 'Upload Photo'}
                                </button>
                                {profileImage && !uploadingImage && (
                                    <button
                                        className={styles.btnGhostOutline}
                                        onClick={handleRemoveProfileImage}
                                    >
                                        Remove
                                    </button>
                                )}
                            </div>

                            <p className={styles.uploadHint}>JPEG, PNG, GIF or WEBP · max 5 MB</p>
                        </div>

                        <div className={styles.accountInfo}>
                            <span className={styles.aiLabel}>Email</span>
                            <span className={styles.aiValue}>{user?.email || '—'}</span>

                            <span className={styles.aiLabel}>Date of Birth</span>
                            <span className={styles.aiValue}>
                                {formatBirthdate(form.birthdate || user?.birthdate)}
                            </span>

                            <span className={styles.aiLabel}>Cooking Level</span>
                            <span className={styles.aiValue}>
                                {cookingLevelLabel(form.cookingLevel || user?.cookingLevel)}
                            </span>

                            <span className={styles.aiLabel}>Member Since</span>
                            <span className={styles.aiValue}>
                                {user?.createdAt
                                    ? new Date(user?.createdAt).toLocaleDateString('en-US', {
                                        month: 'long', year: 'numeric',
                                    })
                                    : '—'}
                            </span>
                        </div>
                    </div>

                    {/* ── Right Content ── */}
                    <div className={styles.right}>

                        <div className={styles.profileSection}>
                            <div className={styles.sectionHeader}>
                                <h4 className={styles.sectionTitle}>Personal Information</h4>
                                {!isEditing && (
                                    <button
                                        type="button"
                                        className={styles.btnEditInfo}
                                        onClick={handleStartEdit}
                                    >
                                        <Pencil size={13} strokeWidth={2} style={{ marginRight: 5 }} />
                                        Edit Information
                                    </button>
                                )}
                            </div>

                            <form onSubmit={handleProfileSubmit}>
                                <div className={styles.formRow}>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>First Name</label>
                                        <input
                                            className={`${styles.formInput} ${isEditing ? styles.inputEditable : styles.inputReadonly}`}
                                            type="text"
                                            value={form.firstName}
                                            onChange={e => setForm({ ...form, firstName: e.target.value })}
                                            placeholder="First name"
                                            required maxLength={50}
                                            readOnly={!isEditing}
                                        />
                                    </div>
                                    <div className={styles.formGroup}>
                                        <label className={styles.formLabel}>Last Name</label>
                                        <input
                                            className={`${styles.formInput} ${isEditing ? styles.inputEditable : styles.inputReadonly}`}
                                            type="text"
                                            value={form.lastName}
                                            onChange={e => setForm({ ...form, lastName: e.target.value })}
                                            placeholder="Last name"
                                            required maxLength={50}
                                            readOnly={!isEditing}
                                        />
                                    </div>
                                </div>

                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>
                                        Email{' '}
                                        <span className={styles.verifiedBadge}>✓ Verified</span>
                                    </label>
                                    <input
                                        className={`${styles.formInput} ${styles.inputReadonly}`}
                                        type="email"
                                        value={form.email}
                                        disabled
                                        style={{ opacity: 0.6 }}
                                    />
                                </div>

                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>Date of Birth</label>
                                    <input
                                        className={`${styles.formInput} ${isEditing ? styles.inputEditable : styles.inputReadonly}`}
                                        type="date"
                                        value={form.birthdate || ''}
                                        onChange={e => setForm({ ...form, birthdate: e.target.value })}
                                        max={new Date().toISOString().split('T')[0]}
                                        readOnly={!isEditing}
                                    />
                                </div>

                                <div className={styles.formGroup}>
                                    <label className={styles.formLabel}>Cooking Skill Level</label>
                                    {isEditing ? (
                                        <select
                                            className={styles.selectInput}
                                            value={form.cookingLevel}
                                            onChange={e => setForm({ ...form, cookingLevel: e.target.value })}
                                        >
                                            {COOKING_LEVELS.map(l => (
                                                <option key={l.value} value={l.value}>{l.label}</option>
                                            ))}
                                        </select>
                                    ) : (
                                        <input
                                            className={`${styles.formInput} ${styles.inputReadonly}`}
                                            type="text"
                                            value={cookingLevelLabel(form.cookingLevel)}
                                            readOnly
                                        />
                                    )}
                                </div>

                                {isEditing && (
                                    <div className={styles.formActions}>
                                        <button
                                            type="submit"
                                            className={styles.btnPrimary}
                                            disabled={savingProfile}
                                        >
                                            {savingProfile ? 'Saving…' : (
                                                <><Save size={14} strokeWidth={2} style={{ marginRight: 6 }} />Save Changes</>
                                            )}
                                        </button>
                                        <button
                                            type="button"
                                            className={styles.btnOutline}
                                            onClick={handleCancelEdit}
                                            disabled={savingProfile}
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                )}
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default Profile;