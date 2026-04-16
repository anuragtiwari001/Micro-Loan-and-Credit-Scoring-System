// src/api/authService.js
//
// Authentication service — register, login, logout, session helpers.
//
// LOGIN RESPONSE CHANGE:
//   Original: login returned a raw JWT string in response.data.data
//   Updated:  login returns { token, role } in response.data.data
//             This means:
//               - Frontend knows the role without decoding the JWT
//               - Routing to /user/dashboard or /admin/dashboard is immediate
//               - No jwt-decode library dependency needed in the frontend

import { API, unwrap } from './axiosConfig';

// ── Register ──────────────────────────────────────────────────────────────────

/**
 * Creates a new USER account.
 *
 * @param {string} email
 * @param {string} password
 * @returns {Promise<string>} success message
 */
export const register = async (email, password) => {
  const response = await API.post('/api/v1/users/register', { email, password });
  return unwrap(response);   // returns "User registered successfully"
};

// ── Login ─────────────────────────────────────────────────────────────────────

/**
 * Authenticates the user and stores the JWT + role in localStorage.
 *
 * @param {string} email
 * @param {string} password
 * @returns {Promise<{ token: string, role: string }>}
 */
export const login = async (email, password) => {
  const response = await API.post('/api/v1/users/login', { email, password });
  const { token, role } = unwrap(response);  // { token: "eyJ...", role: "USER" }

  // Store in localStorage
  // NOTE: For higher security use httpOnly cookies + a /refresh endpoint.
  // localStorage is acceptable for academic/demo projects.
  localStorage.setItem('token', token);
  localStorage.setItem('role', role);

  return { token, role };
};

// ── Logout ────────────────────────────────────────────────────────────────────

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  window.location.href = '/login';
};

// ── Session helpers ───────────────────────────────────────────────────────────

/** Returns true if a token exists in localStorage */
export const isLoggedIn = () => !!localStorage.getItem('token');

/** Returns "USER", "ADMIN", or null */
export const getRole = () => localStorage.getItem('role');

/** Returns true if the stored role is ADMIN */
export const isAdmin = () => getRole() === 'ADMIN';

/** Returns true if the stored role is USER */
export const isUser  = () => getRole() === 'USER';