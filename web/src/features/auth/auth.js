import api from '../../shared/api/api';

// ─── Auth ─────────────────────────────────────────────────────────────────────
// POST /api/auth/register
// Body: { firstName, lastName, birthdate, email, password }
// Response: { success, message, user }
export const register = (userData) =>
  api.post('/api/auth/register', userData);

// POST /api/auth/login
// Body: { email, password }
// Response: { success, message, token, type, user }
export const login = (credentials) =>
  api.post('/api/auth/login', credentials);

// POST /api/auth/logout
export const logout = () =>
  api.post('/api/auth/logout');

// POST /api/auth/change-password
// Body: { oldPassword, newPassword }
// Header: Authorization Bearer <token>
export const changePassword = (passwordData) =>
  api.post('/api/auth/change-password', passwordData);

// POST /api/auth/forgot-password
// Body: { email, newPassword }
export const forgotPassword = (data) =>
  api.post('/api/auth/forgot-password', data);

// POST /api/auth/send-verification-code
// Body: { email }
export const sendVerificationCode = (data) =>
  api.post('/api/auth/send-verification-code', data);

// POST /api/auth/verify-code
// Body: { email, code }
export const verifyCode = (data) =>
  api.post('/api/auth/verify-code', data);

// ─── Current User ─────────────────────────────────────────────────────────────
// GET /api/user/me
// Header: Authorization Bearer <token>
// Response: { userId, email, firstName, lastName }
export const getMe = () =>
  api.get('/api/user/me');

const authAPI = {
  register,
  login,
  logout,
  changePassword,
  forgotPassword,
  getMe,
  getProfile: getMe,
  sendVerificationCode,
  verifyCode,
};

export default authAPI;