// src/api/creditService.js

import { API, unwrap } from './axiosConfig';

/**
 * Returns the authenticated user's credit score.
 *
 * USER  response: { normalizedCreditScore: 744 }
 * ADMIN response: full PredictionResponse with featureImportance, reasonCodes, etc.
 *
 * @returns {Promise<UserScoreResponse | PredictionResponse>}
 */
export const getCreditScore = async () => {
  const response = await API.get('/api/v1/credit/score');
  return unwrap(response);
};

/**
 * Admin: get full ML credit analysis for any user by their userId.
 *
 * @param {number} userId — the user's id from gla_users.user table
 * @returns {Promise<PredictionResponse>}
 */
export const getAdminCreditScore = async (userId) => {
  const response = await API.post('/api/v1/credit/admin/score', { userId });
  return unwrap(response);
};