// src/pages/LoginPage.jsx
//
// Complete login page showing the correct pattern for:
//  - Calling the updated login API (returns { token, role })
//  - Loading state during the request
//  - Error display using response.data.error
//  - Role-based navigation after login

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/authService';

const LoginPage = () => {
  const navigate = useNavigate();

  const [email,    setEmail]    = useState('');
  const [password, setPassword] = useState('');
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const { role } = await login(email, password);

      // Role-based routing — no JWT decoding needed
      if (role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/user/dashboard');
      }

    } catch (err) {
      // err.response.data is the ApiResponse error shape:
      // { success: false, error: "Invalid credentials", statusCode: 400 }
      const message = err.response?.data?.error
                   || err.message
                   || 'Login failed. Please try again.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleLogin}>
        <h2>Login</h2>

        {error && <div className="error-banner">{error}</div>}

        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
      </form>
    </div>
  );
};

export default LoginPage;