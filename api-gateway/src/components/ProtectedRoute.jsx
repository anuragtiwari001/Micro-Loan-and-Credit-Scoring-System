// src/components/ProtectedRoute.jsx
//
// Role-based route guard for React Router v6.
//
// Usage in App.jsx:
//
//   <Routes>
//     <Route path="/login"  element={<LoginPage />} />
//     <Route path="/register" element={<RegisterPage />} />
//
//     {/* USER routes */}
//     <Route element={<ProtectedRoute allowedRoles={['USER', 'ADMIN']} />}>
//       <Route path="/user/dashboard"  element={<UserDashboard />} />
//       <Route path="/user/apply"      element={<LoanApplication />} />
//       <Route path="/user/documents"  element={<DocumentUpload />} />
//     </Route>
//
//     {/* ADMIN-only routes */}
//     <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
//       <Route path="/admin/dashboard" element={<AdminDashboard />} />
//       <Route path="/admin/loans"     element={<AdminLoans />} />
//     </Route>
//
//     <Route path="/" element={<Navigate to="/login" replace />} />
//   </Routes>

import { Navigate, Outlet } from 'react-router-dom';
import { isLoggedIn, getRole } from '../api/authService';

/**
 * @param {string[]} allowedRoles — e.g. ['ADMIN'] or ['USER', 'ADMIN']
 */
const ProtectedRoute = ({ allowedRoles = [] }) => {
  if (!isLoggedIn()) {
    // Not authenticated at all — go to login
    return <Navigate to="/login" replace />;
  }

  const role = getRole();   // "USER" or "ADMIN"

  if (allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // Authenticated but wrong role — redirect to their own dashboard
    const redirect = role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
    return <Navigate to={redirect} replace />;
  }

  // Authorized — render the nested route
  return <Outlet />;
};

export default ProtectedRoute;