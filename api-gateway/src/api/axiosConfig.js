// src/api/axiosConfig.js
//
// ═══════════════════════════════════════════════════════════════════════════
// CHANGE FROM ORIGINAL:
//   Original code created 5 separate Axios instances pointing at 5 different
//   ports (8081-8086). This means the frontend was tightly coupled to every
//   service's port — a deployment and maintenance nightmare.
//
//   FIXED: All traffic now goes through the API Gateway at port 8080.
//   One base URL. One interceptor. One place to change if the server moves.
//
//   Frontend never talks to microservices directly.
//   The gateway handles routing:
//     /api/v1/users/**   → user-service     :8081
//     /api/v1/data/**    → data-service     :8082
//     /api/v1/credit/**  → credit-service   :8083
//     /api/v1/loan/**    → loan-service     :8085
//     /api/v1/docs/**    → document-service :8086
// ═══════════════════════════════════════════════════════════════════════════

import axios from 'axios';

// ── Single API instance through gateway ──────────────────────────────────────
// Change VITE_API_BASE_URL in .env to point at staging/prod without touching code
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const API = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── Request interceptor: attach JWT automatically ─────────────────────────────
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor: global error handling ───────────────────────────────
API.interceptors.response.use(
  // Pass successful responses through unchanged
  (response) => response,

  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      // Token expired or invalid — clear storage and redirect to login
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      window.location.href = '/login';
      return Promise.reject(error);
    }

    if (status === 403) {
      // Authenticated but not authorized (e.g. USER hitting an ADMIN endpoint)
      console.error('Access denied — insufficient role');
      return Promise.reject(new Error('Access denied'));
    }

    if (status >= 500) {
      console.error('Server error:', error.response?.data?.error || 'Unknown error');
    }

    return Promise.reject(error);
  }
);

// ── Helper: extract the actual payload from ApiResponse wrapper ───────────────
// All APIs return { success, message, data, statusCode, timestamp }
// Use this instead of manually doing response.data.data everywhere
export const unwrap = (response) => {
  const body = response.data;
  if (!body.success) {
    throw new Error(body.error || 'Request failed');
  }
  return body.data;
};