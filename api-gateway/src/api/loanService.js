// src/api/loanService.js
//
// All loan-related API calls.
// Every function uses the unwrap() helper to extract response.data.data
// from the uniform ApiResponse wrapper, so callers get the actual payload.

import { API, unwrap } from './axiosConfig';

// ── User ──────────────────────────────────────────────────────────────────────

/**
 * Submits a new loan application.
 *
 * @param {Object} loanData — matches LoanRequest DTO:
 *   { amount, tenure, loanType, loanPurposeDescription,
 *     bankName, accountNumber, accountType, ifscCode,
 *     monthlyIncome, requestedEMI }
 * @returns {Promise<LoanApplication>}
 */
export const applyLoan = async (loanData) => {
  const response = await API.post('/api/v1/loan/apply', loanData);
  return unwrap(response);
};

/**
 * Returns all loan applications for the authenticated user with full
 * status details (PENDING / APPROVED with offer / REJECTED with reason).
 *
 * @returns {Promise<UserDashboardResponse[]>}
 */
export const getUserDashboard = async () => {
  const response = await API.get('/api/v1/loan/dashboard');
  return unwrap(response);
};

// ── Admin ─────────────────────────────────────────────────────────────────────

/** Returns all loan applications in the system */
export const getAllLoans = async () => {
  const response = await API.get('/api/v1/admin/loans');
  return unwrap(response);
};

/** Returns loan + documents for the given loanId */
export const getLoanDetails = async (loanId) => {
  const response = await API.get(`/api/v1/admin/loans/details/${loanId}`);
  return unwrap(response);
};

/**
 * Returns loan + documents + live ML credit score + recommendation.
 *
 * @param {number} loanId
 * @returns {Promise<AdminDashboardResponse>}
 */
export const getAdminDashboard = async (loanId) => {
  const response = await API.get(`/api/v1/admin/loans/dashboard/${loanId}`);
  return unwrap(response);
};

/**
 * Approves a loan with full offer details.
 *
 * @param {number} loanId
 * @param {Object} approvalData — matches ApprovalRequest DTO:
 *   { approvedAmount, interestRate, processingFee,
 *     branchVisitDate, branchVisitTimeSlot, branchName,
 *     branchAddress, branchContactNumber, approvalRemarks }
 * @returns {Promise<LoanApplication>}
 */
export const approveLoan = async (loanId, approvalData) => {
  const response = await API.put(
    `/api/v1/admin/loans/approve/${loanId}`,
    approvalData
  );
  return unwrap(response);
};

/**
 * Rejects a loan with a structured reason.
 *
 * @param {number} loanId
 * @param {Object} rejectionData — matches RejectionRequest DTO:
 *   { rejectionReason, rejectionMessage, reapplyEligibleDate }
 * @returns {Promise<LoanApplication>}
 */
export const rejectLoan = async (loanId, rejectionData) => {
  const response = await API.put(
    `/api/v1/admin/loans/reject/${loanId}`,
    rejectionData
  );
  return unwrap(response);
};