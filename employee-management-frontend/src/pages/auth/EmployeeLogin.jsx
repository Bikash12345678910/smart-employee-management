import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { authApi } from '../../services/api';
import {
  Eye, EyeOff, Lock, ArrowRight, AlertCircle, CheckCircle2,
  Sparkles, Building, Phone, Mail, KeyRound
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function EmployeeLogin() {
  const [loginMode, setLoginMode] = useState('PASSWORD'); // PASSWORD | PHONE_OTP | EMAIL_OTP

  // Password mode state
  const [employeeId, setEmployeeId] = useState('EMP001');
  const [password, setPassword] = useState('Employee@123');
  const [showPassword, setShowPassword] = useState(false);

  // OTP mode state
  const [otpTarget, setOtpTarget] = useState('');
  const [otpCode, setOtpCode] = useState('');
  const [otpSent, setOtpSent] = useState(false);
  const [otpSuccessMsg, setOtpSuccessMsg] = useState(null);
  const [sendingOtp, setSendingOtp] = useState(false);

  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const { login, loginWithOtp } = useAuth();
  const navigate = useNavigate();

  const switchMode = (mode) => {
    setLoginMode(mode);
    setError(null);
    setOtpSuccessMsg(null);
    setOtpSent(false);
    setOtpCode('');
    if (mode === 'PHONE_OTP') {
      setOtpTarget('+1 (555) 999-8877');
    } else if (mode === 'EMAIL_OTP') {
      setOtpTarget('biswanath.m@apextech.com');
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    const res = await login(employeeId, password, true);
    setLoading(false);

    if (res.success) {
      navigate('/employee/dashboard');
    } else {
      setError(res.message || 'Invalid Employee ID or password');
    }
  };

  const handleSendOtp = async () => {
    if (!otpTarget.trim()) {
      setError(loginMode === 'PHONE_OTP' ? 'Please enter your mobile number.' : 'Please enter your email address.');
      return;
    }
    setError(null);
    setOtpSuccessMsg(null);
    setSendingOtp(true);
    try {
      const type = loginMode === 'PHONE_OTP' ? 'PHONE' : 'EMAIL';
      const res = await authApi.sendOtp(otpTarget.trim(), type);
      if (res.success) {
        setOtpSent(true);
        setOtpSuccessMsg(res.message || `6-digit OTP sent to ${otpTarget.trim()}`);
      } else {
        setError(res.message || 'Failed to send OTP');
      }
    } catch (err) {
      setError(err.message || 'Error sending OTP');
    } finally {
      setSendingOtp(false);
    }
  };

  const handleVerifyOtpAndLogin = async (e) => {
    e.preventDefault();
    if (!otpCode.trim() || otpCode.trim().length !== 6) {
      setError('Please enter the 6-digit OTP.');
      return;
    }
    setError(null);
    setLoading(true);
    const type = loginMode === 'PHONE_OTP' ? 'PHONE' : 'EMAIL';
    const res = await loginWithOtp(otpTarget.trim(), type, otpCode.trim());
    setLoading(false);

    if (res.success) {
      navigate('/employee/dashboard');
    } else {
      setError(res.message || 'Invalid or expired 6-digit OTP');
    }
  };

  const handleFillDemo = () => {
    if (loginMode === 'PASSWORD') {
      setEmployeeId('EMP001');
      setPassword('Employee@123');
    } else if (loginMode === 'PHONE_OTP') {
      setOtpTarget('+1 (555) 999-8877');
    } else {
      setOtpTarget('biswanath.m@apextech.com');
    }
  };

  return (
    <div className="min-h-screen relative flex items-center justify-center p-4 overflow-hidden bg-slate-950">
      {/* Ambient background glow */}
      <motion.div
        animate={{ scale: [1, 1.25, 1], x: [0, -30, 0], y: [0, 40, 0] }}
        transition={{ duration: 20, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute top-1/3 -right-20 w-96 h-96 bg-emerald-600/15 rounded-full blur-3xl pointer-events-none"
      />
      <motion.div
        animate={{ scale: [1, 1.2, 1], x: [0, 30, 0], y: [0, -40, 0] }}
        transition={{ duration: 18, repeat: Infinity, ease: 'easeInOut' }}
        className="absolute bottom-1/4 -left-20 w-96 h-96 bg-primary-600/15 rounded-full blur-3xl pointer-events-none"
      />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut' }}
        className="w-full max-w-md relative z-10 glass-panel bg-slate-900/80 border border-slate-800 rounded-3xl p-8 sm:p-10 shadow-2xl backdrop-blur-xl"
      >
        {/* Header */}
        <div className="text-center space-y-3">
          <div className="flex justify-center">
            <img src="/app-icon.svg" className="w-16 h-16 rounded-2xl shadow-xl border border-slate-700/50 object-cover" alt="Smart Biometric Logo" />
          </div>

          <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-white">
            Employee Portal
          </h2>
          <p className="text-xs sm:text-sm text-slate-400">
            Attendance Punch, Payslips &amp; OTP Verification
          </p>
        </div>

        {/* Mode Switcher Tabs */}
        <div className="mt-6 grid grid-cols-3 gap-1.5 p-1 rounded-2xl bg-slate-800/80 border border-slate-700/60">
          <button
            type="button"
            onClick={() => switchMode('PASSWORD')}
            className={`py-2 text-xs font-semibold rounded-xl transition-all ${
              loginMode === 'PASSWORD'
                ? 'bg-emerald-600 text-white shadow'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            Password
          </button>
          <button
            type="button"
            onClick={() => switchMode('PHONE_OTP')}
            className={`py-2 text-xs font-semibold rounded-xl transition-all flex items-center justify-center gap-1 ${
              loginMode === 'PHONE_OTP'
                ? 'bg-emerald-600 text-white shadow'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Phone className="w-3 h-3" /> Phone OTP
          </button>
          <button
            type="button"
            onClick={() => switchMode('EMAIL_OTP')}
            className={`py-2 text-xs font-semibold rounded-xl transition-all flex items-center justify-center gap-1 ${
              loginMode === 'EMAIL_OTP'
                ? 'bg-emerald-600 text-white shadow'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Mail className="w-3 h-3" /> Email OTP
          </button>
        </div>

        {/* Error Alert */}
        {error && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            className="mt-5 p-3.5 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-400 text-xs flex items-center gap-2.5"
          >
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </motion.div>
        )}

        {/* Success Alert */}
        {otpSuccessMsg && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            className="mt-5 p-3.5 rounded-2xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-300 text-xs flex items-center gap-2.5"
          >
            <CheckCircle2 className="w-4 h-4 flex-shrink-0" />
            <span>{otpSuccessMsg}</span>
          </motion.div>
        )}

        {/* PASSWORD LOGIN FORM */}
        {loginMode === 'PASSWORD' ? (
          <form onSubmit={handlePasswordSubmit} className="mt-6 space-y-5">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Employee ID or Email
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <Building className="w-4 h-4" />
                </div>
                <input
                  type="text"
                  required
                  value={employeeId}
                  onChange={(e) => setEmployeeId(e.target.value)}
                  placeholder="EMP001 or john.doe@apextech.com"
                  className="w-full pl-10 pr-4 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
              </div>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                Password
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                  <Lock className="w-4 h-4" />
                </div>
                <input
                  type={showPassword ? 'text' : 'password'}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  className="w-full pl-10 pr-11 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-slate-200"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              type="submit"
              disabled={loading}
              className="w-full py-3 px-4 rounded-xl font-semibold text-sm text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-500/25 flex items-center justify-center gap-2 transition-all disabled:opacity-50"
            >
              {loading ? 'Signing in...' : <><span>Sign in to Employee Portal</span><ArrowRight className="w-4 h-4" /></>}
            </motion.button>
          </form>
        ) : (
          /* PHONE SMS OTP & EMAIL GMAIL OTP FORM */
          <form onSubmit={handleVerifyOtpAndLogin} className="mt-6 space-y-5">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-300">
                {loginMode === 'PHONE_OTP' ? 'Employee Mobile Number (SMS Gateway)' : 'Employee Email Address (Gmail OTP)'}
              </label>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    {loginMode === 'PHONE_OTP' ? <Phone className="w-4 h-4" /> : <Mail className="w-4 h-4" />}
                  </div>
                  <input
                    type={loginMode === 'PHONE_OTP' ? 'tel' : 'email'}
                    required
                    value={otpTarget}
                    onChange={(e) => setOtpTarget(e.target.value)}
                    placeholder={loginMode === 'PHONE_OTP' ? '+91 9876543210' : 'employee@gmail.com'}
                    className="w-full pl-10 pr-3 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white placeholder-slate-500 text-xs sm:text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <button
                  type="button"
                  onClick={handleSendOtp}
                  disabled={sendingOtp}
                  className="px-3.5 py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-semibold whitespace-nowrap disabled:opacity-50 transition-colors"
                >
                  {sendingOtp ? 'Sending...' : otpSent ? 'Resend OTP' : 'Send OTP'}
                </button>
              </div>
            </div>

            {otpSent && (
              <div className="space-y-1.5">
                <label className="block text-xs font-semibold text-slate-300">
                  Enter 6-Digit OTP
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                    <KeyRound className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    maxLength={6}
                    required
                    value={otpCode}
                    onChange={(e) => setOtpCode(e.target.value.replace(/\D/g, ''))}
                    placeholder="Enter 6-digit OTP"
                    className="w-full pl-10 pr-4 py-2.5 bg-slate-800/80 border border-slate-700 rounded-xl text-white tracking-widest font-mono text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
              </div>
            )}

            <motion.button
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              type="submit"
              disabled={loading || !otpSent}
              className="w-full py-3 px-4 rounded-xl font-semibold text-sm text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 hover:to-teal-500 shadow-lg shadow-emerald-500/25 flex items-center justify-center gap-2 transition-all disabled:opacity-50"
            >
              {loading
                ? 'Verifying OTP...'
                : <><span>Verify OTP &amp; Continue</span><ArrowRight className="w-4 h-4" /></>}
            </motion.button>
          </form>
        )}

        {/* Demo filler */}
        <div className="mt-6 pt-5 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
          <span>Demo account helper</span>
          <button
            type="button"
            onClick={handleFillDemo}
            className="px-2.5 py-1 rounded-lg bg-slate-800 hover:bg-slate-700 text-emerald-400 font-medium transition-colors flex items-center gap-1"
          >
            <Sparkles className="w-3 h-3 text-amber-400" /> Auto-fill Demo
          </button>
        </div>

        {/* Link back to Admin login */}
        <div className="mt-4 text-center">
          <button
            type="button"
            onClick={() => navigate('/admin/login')}
            className="text-xs text-slate-400 hover:text-white transition-colors"
          >
            Company Administrator? <span className="text-emerald-400 font-semibold underline underline-offset-4">Admin Login &rarr;</span>
          </button>
        </div>
      </motion.div>
    </div>
  );
}
