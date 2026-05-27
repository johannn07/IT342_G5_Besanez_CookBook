import { uploadImage } from '../../shared/api/image';

// ─── Concrete Strategy 1: Cloudinary file upload ─────────────────────────────
export class CloudinaryStrategy {
    label = 'Upload Photo';
    icon = '📷';

    async resolve({ file, userId }) {
        if (!file) return null;
        const folder = userId ? `users/${userId}/recipes` : 'recipes';
        const response = await uploadImage(file, folder);
        return response.data.url;
    }

    validate(file) {
        if (!file) return null;
        if (!file.type?.startsWith('image/')) {
            return 'Please select a valid image file (JPEG, PNG, GIF, WEBP).';
        }
        if (file.size > 5 * 1024 * 1024) {
            return 'Image must be smaller than 5 MB.';
        }
        return null;
    }
}

// ─── Concrete Strategy 2: URL paste ───────────────────────────────────────────
export class URLStrategy {
    label = 'Paste URL';
    icon = '🔗';

    async resolve({ url }) {
        return url?.trim() || null;
    }

    validate(url) {
        if (!url) return null;
        try {
            new URL(url);
            return null;
        } catch {
            return 'Please enter a valid URL (e.g. https://example.com/photo.jpg).';
        }
    }
}

// ─── Strategy Context ─────────────────────────────────────────────────────────
export class ImageUploadContext {
    constructor(strategy = new CloudinaryStrategy()) {
        this._strategy = strategy;
    }

    setStrategy(strategy) {
        this._strategy = strategy;
    }

    get label() { return this._strategy.label; }
    get icon() { return this._strategy.icon; }

    validate(value) {
        return this._strategy.validate(value);
    }

    async resolve(state) {
        return this._strategy.resolve(state);
    }
}

// ─── Available strategies registry ────────────────────────────────────────────
export const IMAGE_STRATEGIES = {
    cloudinary: new CloudinaryStrategy(),
    url: new URLStrategy(),
};