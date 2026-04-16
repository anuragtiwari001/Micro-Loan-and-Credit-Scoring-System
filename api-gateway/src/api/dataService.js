// src/api/dataService.js
//
// Financial data submission and update.

import { API, unwrap } from './axiosConfig';

/**
 * Submits financial profile data for the first time.
 * Call once; use updateData() for subsequent changes.
 *
 * @param {Object} data — matches UserFinancialData entity fields:
 *   { income, expenses, employmentType, existingLoans,
 *     contactNumber, panNumber, aadharNumber,
 *     missedPaymentsCount, totalTransactions, employmentLengthMonths,
 *     averageBalance, monthlyBalanceHistory, salaryHistory }
 *
 * monthlyBalanceHistory and salaryHistory are comma-separated strings:
 *   "80000,82000,79000,85000,83000"
 *
 * @returns {Promise<UserFinancialData>}
 */
export const submitData = async (data) => {
  const response = await API.post('/api/v1/data/submit', data);
  return unwrap(response);
};

/**
 * Updates all financial fields for the authenticated user.
 * Requires an existing record (call submitData first).
 *
 * @param {Object} data — same shape as submitData
 * @returns {Promise<UserFinancialData>}
 */
export const updateData = async (data) => {
  const response = await API.put('/api/v1/data/update', data);
  return unwrap(response);
};