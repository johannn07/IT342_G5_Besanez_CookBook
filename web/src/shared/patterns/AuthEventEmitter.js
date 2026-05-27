class AuthEventEmitter {
    constructor() {
        /** @type {Map<string, Set<Function>>} */
        this._listeners = new Map();
    }

    on(event, listener) {
        if (!this._listeners.has(event)) {
            this._listeners.set(event, new Set());
        }
        this._listeners.get(event).add(listener);
        return () => this.off(event, listener);
    }

    once(event, listener) {
        const wrapper = (...args) => {
            listener(...args);
            this.off(event, wrapper);
        };
        return this.on(event, wrapper);
    }

    off(event, listener) {
        this._listeners.get(event)?.delete(listener);
    }

    emit(event, payload) {
        this._listeners.get(event)?.forEach((listener) => {
            try {
                listener(payload);
            } catch (err) {
                console.error(`AuthEventEmitter: listener error for "${event}"`, err);
            }
        });
    }
}

const AuthEvents = new AuthEventEmitter();

export default AuthEvents;

export const AUTH_EVENTS = {
    TOKEN_EXPIRED: 'token:expired',
    USER_LOGIN: 'user:login',
    USER_LOGOUT: 'user:logout',
    USER_UPDATED: 'user:updated',
};