import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('company_auth_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('company_auth_token'));
  const [loading, setLoading] = useState(false);

  const login = async (username, password, rememberMe = false) => {
    setLoading(true);
    try {
      const res = await authApi.login({ username, password, rememberMe });
      if (res.success && res.data) {
        const { token, ...userData } = res.data;
        setToken(token);
        setUser(userData);
        localStorage.setItem('company_auth_token', token);
        localStorage.setItem('company_auth_user', JSON.stringify(userData));
        return { success: true, user: userData };
      }
      return { success: false, message: res.message || 'Login failed' };
    } catch (err) {
      return { success: false, message: err.message || 'Authentication error' };
    } finally {
      setLoading(false);
    }
  };

  const loginWithOtp = async (target, type, code) => {
    setLoading(true);
    try {
      const res = await authApi.loginWithOtp(target, type, code);
      if (res.success && res.data) {
        const { token, ...userData } = res.data;
        setToken(token);
        setUser(userData);
        localStorage.setItem('company_auth_token', token);
        localStorage.setItem('company_auth_user', JSON.stringify(userData));
        return { success: true, user: userData };
      }
      return { success: false, message: res.message || 'OTP Login failed' };
    } catch (err) {
      return { success: false, message: err.message || 'OTP authentication error' };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('company_auth_token');
    localStorage.removeItem('company_auth_user');
  };

  const updateCurrentUser = (partial) => {
    setUser(prev => {
      const updated = { ...prev, ...partial };
      localStorage.setItem('company_auth_user', JSON.stringify(updated));
      return updated;
    });
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';
  const isEmployee = user?.role === 'ROLE_EMPLOYEE' || user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        loginWithOtp,
        logout,
        updateCurrentUser,
        isAdmin,
        isEmployee,
        isAuthenticated: !!token && !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
