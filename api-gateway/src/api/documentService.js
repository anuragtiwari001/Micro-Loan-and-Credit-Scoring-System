// src/api/documentService.js
//
// KYC document upload service.
// Uses multipart/form-data — axios sets the Content-Type boundary automatically
// when you pass FormData, so do NOT set it manually.

import { API, unwrap } from './axiosConfig';

/**
 * Uploads a KYC document to the document-service.
 *
 * @param {File}   file — browser File object from <input type="file">
 * @param {string} type — "PAN" | "AADHAAR" | "SALARY_SLIP" | "BANK_STATEMENT"
 * @returns {Promise<DocumentResponse>} — { id, documentType, fileUrl }
 *
 * Usage:
 *   const doc = await uploadDocument(fileInput.files[0], 'PAN');
 *   console.log(doc.fileUrl); // Cloudinary HTTPS URL
 */
export const uploadDocument = async (file, type) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('type', type);

  const response = await API.post('/api/v1/docs/upload', formData, {
    headers: {
      // Let axios set Content-Type: multipart/form-data; boundary=...
      // IMPORTANT: Do NOT manually set 'Content-Type': 'multipart/form-data'
      // — without the boundary string the server will reject the request.
      'Content-Type': undefined,
    },
  });

  return unwrap(response);
};

/**
 * Admin: fetches all uploaded documents for a user by email.
 *
 * @param {string} email
 * @returns {Promise<DocumentResponse[]>}
 */
export const getDocumentsByEmail = async (email) => {
  const response = await API.get(`/api/v1/docs/admin/${email}`);
  return unwrap(response);
};