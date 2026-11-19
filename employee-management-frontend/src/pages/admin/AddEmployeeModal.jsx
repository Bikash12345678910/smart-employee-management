import React, { useState, useEffect } from 'react';
import { adminApi, authApi } from '../../services/api';
import FaceScannerModal from '../../components/biometrics/FaceScannerModal';
import FingerprintScannerModal from '../../components/biometrics/FingerprintScannerModal';
import {
  User,
  Phone,
  Mail,
  Building,
  CreditCard,
  Fingerprint,
  Camera,
  Lock,
  CheckCircle2,
  AlertCircle,
  X,
  Sparkles,
  ShieldCheck,
  Check
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function AddEmployeeModal({ isOpen, onClose, onSuccess, departments = [], initialData = null }) {
  const isEdit = !!initialData;

  // Form State
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    profilePhotoUrl: '',
    dateOfBirth: '1995-01-15',
    gender: 'MALE',
    phoneNumber: '',
    email: '',
    address: '',

    phoneOtp: '',
    emailOtp: '',

    employeeCode: '',
    joiningDate: new Date().toISOString().split('T')[0],
    departmentId: '',
    designationId: '',
    employmentStatus: 'ACTIVE',
    agreedMonthlySalary: '6500.00',

    aadhaarNumber: '',
    panNumber: '',
    bankAccountNumber: '',
    bankIfscCode: 'HDFC0001234',
    bankName: 'HDFC Bank',

    liveFaceData: '',
    registerFace: false,
    registerFingerprint: false,
    fingerprintDeviceId: '',

    username: '',
    password: 'Employee@123',
    confirmPassword: 'Employee@123',
  });

  const [designations, setDesignations] = useState([]);
  const [devices, setDevices] = useState([]);

  // Verification states
  const [phoneOtpSent, setPhoneOtpSent] = useState(false);
  const [emailOtpSent, setEmailOtpSent] = useState(false);
  const [phoneVerified, setPhoneVerified] = useState(false);
  const [emailVerified, setEmailVerified] = useState(false);
  const [phoneStatusMsg, setPhoneStatusMsg] = useState('');
  const [emailStatusMsg, setEmailStatusMsg] = useState('');
  const [sendingPhoneOtp, setSendingPhoneOtp] = useState(false);
  const [sendingEmailOtp, setSendingEmailOtp] = useState(false);

  // Direct SMS & Gmail Gateway Credentials (Admin can enter directly here)
  const [gatewayConfig, setGatewayConfig] = useState(() => {
    try {
      const saved = localStorage.getItem('otp_gateway_config');
      if (saved) return JSON.parse(saved);
    } catch (_) {}
    return {
      smsProvider: 'FAST2SMS',
      fast2SmsApiKey: '',
      twilioAccountSid: '',
      twilioAuthToken: '',
      twilioFromNumber: '',
      gmailSenderEmail: '',
      gmailAppPassword: '',
    };
  });

  const updateGatewayField = (field, value) => {
    setGatewayConfig(prev => {
      const next = { ...prev, [field]: value };
      localStorage.setItem('otp_gateway_config', JSON.stringify(next));
      return next;
    });
  };

  // Biometric Modals
  const [showFaceModal, setShowFaceModal] = useState(false);
  const [showFpModal, setShowFpModal] = useState(false);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (initialData) {
      setFormData({
        firstName: initialData.firstName || '',
        lastName: initialData.lastName || '',
        profilePhotoUrl: initialData.profilePhotoUrl || '',
        dateOfBirth: initialData.dateOfBirth || '1995-01-15',
        gender: initialData.gender || 'MALE',
        phoneNumber: initialData.phoneNumber || '',
        email: initialData.email || '',
        address: initialData.address || '',
        phoneOtp: '123456',
        emailOtp: '123456',
        employeeCode: initialData.employeeCode || '',
        joiningDate: initialData.joiningDate || '',
        departmentId: initialData.departmentId || '',
        designationId: initialData.designationId || '',
        employmentStatus: initialData.employmentStatus || 'ACTIVE',
        agreedMonthlySalary: initialData.agreedMonthlySalary || '',
        aadhaarNumber: initialData.aadhaarNumber || '987654321012',
        panNumber: initialData.panNumber || 'ABCDE1234F',
        bankAccountNumber: initialData.bankAccountNumber || '987654321098',
        bankIfscCode: initialData.bankIfscCode || 'HDFC0001234',
        bankName: initialData.bankName || 'HDFC Bank',
        liveFaceData: '',
        registerFace: false,
        registerFingerprint: false,
        fingerprintDeviceId: '',
        username: initialData.username || '',
        password: '',
        confirmPassword: '',
      });
      setPhoneVerified(true);
      setEmailVerified(true);
    } else {
      resetForm();
    }
  }, [initialData, isOpen]);

  useEffect(() => {
    adminApi.getDesignations().then(res => {
      if (res.success && res.data) {
        setDesignations(res.data);
        if (res.data.length > 0 && !formData.designationId) {
          setFormData(prev => ({ ...prev, designationId: res.data[0].id, departmentId: departments[0]?.id || 1 }));
        }
      }
    });
  }, [departments]);

  useEffect(() => {
    adminApi.getBiometricDevices().then(res => {
      if (res.success && res.data) {
        setDevices(res.data);
        if (res.data.length > 0) {
          setFormData(prev => ({ ...prev, fingerprintDeviceId: res.data[0].id }));
        }
      }
    });
  }, []);

  const resetForm = () => {
    setFormData({
      firstName: '',
      lastName: '',
      profilePhotoUrl: '',
      dateOfBirth: '1995-01-15',
      gender: 'MALE',
      phoneNumber: '',
      email: '',
      address: '',
      phoneOtp: '',
      emailOtp: '',
      employeeCode: '',
      joiningDate: new Date().toISOString().split('T')[0],
      departmentId: departments[0]?.id || '',
      designationId: '',
      employmentStatus: 'ACTIVE',
      agreedMonthlySalary: '6500.00',
      aadhaarNumber: '',
      panNumber: '',
      bankAccountNumber: '',
      bankIfscCode: 'HDFC0001234',
      bankName: 'HDFC Bank',
      liveFaceData: '',
      registerFace: false,
      registerFingerprint: false,
      fingerprintDeviceId: devices[0]?.id || '',
      username: '',
      password: 'Employee@123',
      confirmPassword: 'Employee@123',
    });
    setPhoneOtpSent(false);
    setEmailOtpSent(false);
    setPhoneVerified(false);
    setEmailVerified(false);
    setError(null);
  };

  const handleSendPhoneOtp = async () => {
    if (!formData.phoneNumber || formData.phoneNumber.trim().length < 8) {
      setPhoneStatusMsg("Please enter a valid mobile number first.");
      return;
    }
    setSendingPhoneOtp(true);
    setPhoneStatusMsg('');
    try {
      const res = await authApi.sendOtp(formData.phoneNumber.trim(), "PHONE", gatewayConfig);
      setPhoneOtpSent(true);
      setFormData(prev => ({ ...prev, phoneOtp: '' }));
      if (res.data?.deliveredLive) {
        setPhoneStatusMsg(`6-digit SMS OTP sent directly to ${formData.phoneNumber.trim()} via ${res.data.channel}. Enter the OTP received on phone.`);
      } else {
        setPhoneStatusMsg(`SMS Gateway key not entered yet. Enter Fast2SMS / Twilio details above to send real SMS. (Dev Server OTP: ${res.data?.code || 'check console'})`);
      }
    } catch (e) {
      setPhoneStatusMsg("Failed to send SMS OTP: " + e.message);
    } finally {
      setSendingPhoneOtp(false);
    }
  };

  const handleVerifyPhoneOtp = async () => {
    if (!formData.phoneOtp || formData.phoneOtp.trim().length !== 6) {
      setPhoneStatusMsg("Please enter the 6-digit OTP received on the phone.");
      return;
    }
    try {
      await authApi.verifyOtp(formData.phoneNumber.trim(), "PHONE", formData.phoneOtp.trim());
      setPhoneVerified(true);
      setPhoneStatusMsg("Phone number verified successfully!");
    } catch (e) {
      setPhoneStatusMsg("Invalid or expired 6-digit OTP.");
    }
  };

  const handleSendEmailOtp = async () => {
    if (!formData.email || !formData.email.includes('@')) {
      setEmailStatusMsg("Please enter a valid employee Gmail / email address first.");
      return;
    }
    setSendingEmailOtp(true);
    setEmailStatusMsg('');
    try {
      const res = await authApi.sendOtp(formData.email.trim(), "EMAIL", gatewayConfig);
      setEmailOtpSent(true);
      setFormData(prev => ({ ...prev, emailOtp: '' }));
      if (res.data?.deliveredLive) {
        setEmailStatusMsg(`6-digit OTP sent directly to ${formData.email.trim()} via Gmail! Check inbox and enter OTP.`);
      } else {
        setEmailStatusMsg(`Gmail credentials not entered yet. Enter Sender Gmail & 16-char App Password above to send real email. (Dev Server OTP: ${res.data?.code || 'check console'})`);
      }
    } catch (e) {
      setEmailStatusMsg("Failed to send Gmail OTP: " + e.message);
    } finally {
      setSendingEmailOtp(false);
    }
  };

  const handleVerifyEmailOtp = async () => {
    if (!formData.emailOtp || formData.emailOtp.trim().length !== 6) {
      setEmailStatusMsg("Please enter the 6-digit OTP received in Gmail.");
      return;
    }
    try {
      await authApi.verifyOtp(formData.email.trim(), "EMAIL", formData.emailOtp.trim());
      setEmailVerified(true);
      setEmailStatusMsg("Email verified successfully!");
    } catch (e) {
      setEmailStatusMsg("Invalid or expired 6-digit OTP.");
    }
  };

  const handleFaceCaptured = (base64) => {
    setFormData(prev => ({
      ...prev,
      liveFaceData: base64,
      registerFace: true,
      profilePhotoUrl: prev.profilePhotoUrl || base64
    }));
  };

  const handleFingerprintScanned = (fpData) => {
    setFormData(prev => ({
      ...prev,
      registerFingerprint: true,
      fingerprintDeviceId: fpData.deviceId
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!isEdit && formData.password !== formData.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setSubmitting(true);
    try {
      if (isEdit) {
        await adminApi.updateEmployee(initialData.id, {
          firstName: formData.firstName,
          lastName: formData.lastName,
          phoneNumber: formData.phoneNumber,
          address: formData.address,
          profilePhotoUrl: formData.profilePhotoUrl,
          departmentId: Number(formData.departmentId),
          designationId: Number(formData.designationId),
          employmentStatus: formData.employmentStatus,
          agreedMonthlySalary: parseFloat(formData.agreedMonthlySalary),
          bankAccountNumber: formData.bankAccountNumber,
          bankIfscCode: formData.bankIfscCode,
          bankName: formData.bankName
        });
      } else {
        await adminApi.createEmployee({
          ...formData,
          departmentId: Number(formData.departmentId),
          designationId: Number(formData.designationId),
          agreedMonthlySalary: parseFloat(formData.agreedMonthlySalary),
          username: formData.username || `${formData.firstName.toLowerCase()}.${formData.lastName.toLowerCase()}`
        });
      }
      onSuccess();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/70 backdrop-blur-sm overflow-y-auto">
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="w-full max-w-3xl bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 my-8 overflow-hidden"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-850">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-primary-100 dark:bg-primary-950 text-primary-600 dark:text-primary-400">
              <User className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-bold text-slate-900 dark:text-slate-100 text-base">
                {isEdit ? "Edit Employee Information" : "Register New Company Employee"}
              </h3>
              <p className="text-xs text-slate-500">Complete personal, biometric, and payroll profile.</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {error && (
          <div className="mx-6 mt-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-rose-500 text-xs flex items-center gap-2">
            <AlertCircle className="w-4 h-4 flex-shrink-0" />
            <span>{error}</span>
          </div>
        )}

        {/* Multi-Section Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
          {/* SECTION 1: Personal Information */}
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
              <User className="w-3.5 h-3.5" /> 1. Personal Information
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">First Name *</label>
                <input
                  type="text"
                  required
                  value={formData.firstName}
                  onChange={e => setFormData({ ...formData, firstName: e.target.value })}
                  placeholder="e.g. Robert"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Last Name *</label>
                <input
                  type="text"
                  required
                  value={formData.lastName}
                  onChange={e => setFormData({ ...formData, lastName: e.target.value })}
                  placeholder="e.g. Vance"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Date of Birth *</label>
                <input
                  type="date"
                  required
                  value={formData.dateOfBirth}
                  onChange={e => setFormData({ ...formData, dateOfBirth: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Gender *</label>
                <select
                  value={formData.gender}
                  onChange={e => setFormData({ ...formData, gender: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Profile Photo URL</label>
                <input
                  type="text"
                  value={formData.profilePhotoUrl}
                  onChange={e => setFormData({ ...formData, profilePhotoUrl: e.target.value })}
                  placeholder="https://..."
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                />
              </div>

              <div className="sm:col-span-2">
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Residential Address *</label>
                <textarea
                  required
                  rows={2}
                  value={formData.address}
                  onChange={e => setFormData({ ...formData, address: e.target.value })}
                  placeholder="Street, City, State, Postal Code"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
                />
              </div>
            </div>
          </div>

          {/* SECTION 2: Verification (Direct Phone SMS & Gmail OTP) */}
          <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
              <ShieldCheck className="w-3.5 h-3.5" /> 2. Direct Phone SMS &amp; Gmail OTP Verification
            </h4>

            {/* Gateway Credentials Input Box (Saved locally so admin enters once) */}
            <div className="p-3.5 rounded-2xl bg-indigo-50/60 dark:bg-slate-800/80 border border-indigo-200/70 dark:border-slate-700 space-y-2.5">
              <div className="text-[11px] font-bold text-indigo-700 dark:text-indigo-300">
                Live Delivery Gateway Settings (Enter your Gmail &amp; SMS Gateway credentials to send OTP directly)
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                <div>
                  <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                    Sender Gmail Address (for Email OTP)
                  </label>
                  <input
                    type="email"
                    value={gatewayConfig.gmailSenderEmail}
                    onChange={e => updateGatewayField('gmailSenderEmail', e.target.value)}
                    placeholder="yourcompany@gmail.com"
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100"
                  />
                </div>

                <div>
                  <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                    Gmail 16-Char App Password (Google Account &rarr; Security)
                  </label>
                  <input
                    type="password"
                    value={gatewayConfig.gmailAppPassword}
                    onChange={e => updateGatewayField('gmailAppPassword', e.target.value)}
                    placeholder="xxxx xxxx xxxx xxxx"
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                  />
                </div>

                <div>
                  <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                    SMS Gateway Provider (for Phone OTP)
                  </label>
                  <select
                    value={gatewayConfig.smsProvider}
                    onChange={e => updateGatewayField('smsProvider', e.target.value)}
                    className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100"
                  >
                    <option value="FAST2SMS">Fast2SMS (India Mobile Numbers)</option>
                    <option value="TWILIO">Twilio (Global SMS)</option>
                  </select>
                </div>

                {gatewayConfig.smsProvider === 'FAST2SMS' ? (
                  <div>
                    <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                      Fast2SMS API Key
                    </label>
                    <input
                      type="password"
                      value={gatewayConfig.fast2SmsApiKey}
                      onChange={e => updateGatewayField('fast2SmsApiKey', e.target.value)}
                      placeholder="Paste your Fast2SMS API Key"
                      className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                    />
                  </div>
                ) : (
                  <>
                    <div>
                      <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                        Twilio Account SID
                      </label>
                      <input
                        type="text"
                        value={gatewayConfig.twilioAccountSid}
                        onChange={e => updateGatewayField('twilioAccountSid', e.target.value)}
                        placeholder="ACxxxxxxxxxxxxxxxx"
                        className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                        Twilio Auth Token
                      </label>
                      <input
                        type="password"
                        value={gatewayConfig.twilioAuthToken}
                        onChange={e => updateGatewayField('twilioAuthToken', e.target.value)}
                        placeholder="Twilio Auth Token"
                        className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                        Twilio From Phone Number
                      </label>
                      <input
                        type="tel"
                        value={gatewayConfig.twilioFromNumber}
                        onChange={e => updateGatewayField('twilioFromNumber', e.target.value)}
                        placeholder="+1xxxxxxxxxx"
                        className="w-full text-xs px-2.5 py-1.5 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100"
                      />
                    </div>
                  </>
                )}
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Phone Verification */}
              <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 space-y-2">
                <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                  Employee Phone Number *
                </label>
                <div className="flex gap-2">
                  <input
                    type="tel"
                    required
                    value={formData.phoneNumber}
                    onChange={e => setFormData({ ...formData, phoneNumber: e.target.value })}
                    placeholder="+91 9876543210"
                    className="flex-1 text-xs px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100"
                  />
                  {!phoneVerified && (
                    <button
                      type="button"
                      disabled={sendingPhoneOtp}
                      onClick={handleSendPhoneOtp}
                      className="px-2.5 py-1.5 rounded-xl bg-primary-600 text-white text-[11px] font-medium hover:bg-primary-700 whitespace-nowrap disabled:opacity-50"
                    >
                      {sendingPhoneOtp ? "Sending..." : phoneOtpSent ? "Resend" : "Send OTP"}
                    </button>
                  )}
                </div>

                {phoneOtpSent && !phoneVerified && (
                  <div className="flex gap-2 pt-1">
                    <input
                      type="text"
                      maxLength={6}
                      placeholder="Enter 6-digit SMS OTP"
                      value={formData.phoneOtp}
                      onChange={e => setFormData({ ...formData, phoneOtp: e.target.value.replace(/\D/g, '') })}
                      className="flex-1 text-xs px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                    />
                    <button
                      type="button"
                      onClick={handleVerifyPhoneOtp}
                      className="px-3 py-1.5 rounded-xl bg-emerald-600 text-white text-[11px] font-semibold hover:bg-emerald-700"
                    >
                      Verify
                    </button>
                  </div>
                )}

                {phoneStatusMsg && (
                  <div className={`text-[11px] font-medium ${phoneVerified ? 'text-emerald-600' : 'text-amber-600 dark:text-amber-400'}`}>
                    {phoneStatusMsg}
                  </div>
                )}

                {phoneVerified && (
                  <div className="text-[11px] text-emerald-600 font-semibold flex items-center gap-1">
                    <Check className="w-3.5 h-3.5" /> Phone Verified Successfully
                  </div>
                )}
              </div>

              {/* Email Verification */}
              <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700 space-y-2">
                <label className="block text-[11px] font-semibold text-slate-700 dark:text-slate-300">
                  Employee Gmail / Email Address *
                </label>
                <div className="flex gap-2">
                  <input
                    type="email"
                    required
                    value={formData.email}
                    onChange={e => setFormData({ ...formData, email: e.target.value })}
                    placeholder="employee@gmail.com"
                    className="flex-1 text-xs px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100"
                  />
                  {!emailVerified && (
                    <button
                      type="button"
                      disabled={sendingEmailOtp}
                      onClick={handleSendEmailOtp}
                      className="px-2.5 py-1.5 rounded-xl bg-primary-600 text-white text-[11px] font-medium hover:bg-primary-700 whitespace-nowrap disabled:opacity-50"
                    >
                      {sendingEmailOtp ? "Sending..." : emailOtpSent ? "Resend" : "Send OTP"}
                    </button>
                  )}
                </div>

                {emailOtpSent && !emailVerified && (
                  <div className="flex gap-2 pt-1">
                    <input
                      type="text"
                      maxLength={6}
                      placeholder="Enter 6-digit Gmail OTP"
                      value={formData.emailOtp}
                      onChange={e => setFormData({ ...formData, emailOtp: e.target.value.replace(/\D/g, '') })}
                      className="flex-1 text-xs px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-900 dark:text-slate-100 font-mono"
                    />
                    <button
                      type="button"
                      onClick={handleVerifyEmailOtp}
                      className="px-3 py-1.5 rounded-xl bg-emerald-600 text-white text-[11px] font-semibold hover:bg-emerald-700"
                    >
                      Verify
                    </button>
                  </div>
                )}

                {emailStatusMsg && (
                  <div className={`text-[11px] font-medium ${emailVerified ? 'text-emerald-600' : 'text-amber-600 dark:text-amber-400'}`}>
                    {emailStatusMsg}
                  </div>
                )}

                {emailVerified && (
                  <div className="text-[11px] text-emerald-600 font-semibold flex items-center gap-1">
                    <Check className="w-3.5 h-3.5" /> Email Verified Successfully
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* SECTION 3: Employment Information */}
          <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
              <Building className="w-3.5 h-3.5" /> 3. Employment & Payroll Configuration
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">
                  Employee Code (Auto-assigned)
                </label>
                <input
                  type="text"
                  value={formData.employeeCode}
                  onChange={e => setFormData({ ...formData, employeeCode: e.target.value })}
                  placeholder="Auto-generated (e.g. EMP007)"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/80 text-primary-600 dark:text-primary-400 font-mono font-bold"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Designation *</label>
                <select
                  required
                  value={formData.designationId}
                  onChange={e => setFormData({ ...formData, designationId: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                >
                  {designations.map(d => (
                    <option key={d.id} value={d.id}>{d.title}</option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Joining Date *</label>
                <input
                  type="date"
                  required
                  value={formData.joiningDate}
                  onChange={e => setFormData({ ...formData, joiningDate: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Agreed Monthly Salary ($) *</label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={formData.agreedMonthlySalary}
                  onChange={e => setFormData({ ...formData, agreedMonthlySalary: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Employment Status</label>
                <select
                  value={formData.employmentStatus}
                  onChange={e => setFormData({ ...formData, employmentStatus: e.target.value })}
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                >
                  <option value="ACTIVE">Active</option>
                  <option value="PROBATION">Probation</option>
                  <option value="SUSPENDED">Suspended</option>
                  <option value="TERMINATED">Terminated</option>
                </select>
              </div>
            </div>
          </div>

          {/* SECTION 4: Financial & Compliance Info */}
          <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
              <CreditCard className="w-3.5 h-3.5" /> 4. Financial & Compliance (Masked in APIs)
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Aadhaar Card Number *</label>
                <input
                  type="text"
                  required
                  maxLength={16}
                  value={formData.aadhaarNumber}
                  onChange={e => setFormData({ ...formData, aadhaarNumber: e.target.value })}
                  placeholder="12-digit Aadhaar Number"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">PAN Card Number *</label>
                <input
                  type="text"
                  required
                  maxLength={10}
                  value={formData.panNumber}
                  onChange={e => setFormData({ ...formData, panNumber: e.target.value.toUpperCase() })}
                  placeholder="10-digit PAN (e.g. ABCDE1234F)"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono uppercase"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Bank Account Number *</label>
                <input
                  type="text"
                  required
                  value={formData.bankAccountNumber}
                  onChange={e => setFormData({ ...formData, bankAccountNumber: e.target.value })}
                  placeholder="Account Number"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
                />
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Bank IFSC / Branch Code *</label>
                <input
                  type="text"
                  required
                  value={formData.bankIfscCode}
                  onChange={e => setFormData({ ...formData, bankIfscCode: e.target.value.toUpperCase() })}
                  placeholder="e.g. HDFC0001234"
                  className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono uppercase"
                />
              </div>
            </div>
          </div>

          {/* SECTION 5: Biometrics Enrolment */}
          <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800">
            <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
              <Fingerprint className="w-3.5 h-3.5" /> 5. Biometric Sensor Enrolment
            </h4>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {/* Face Biometric */}
              <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-850 border border-slate-200 dark:border-slate-700 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="font-semibold text-xs text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
                      <Camera className="w-4 h-4 text-cyan-500" /> Live Face Capture
                    </span>
                    {formData.liveFaceData ? (
                      <span className="text-[10px] font-semibold text-emerald-600 bg-emerald-50 dark:bg-emerald-950 px-2 py-0.5 rounded-full flex items-center gap-1">
                        <Check className="w-3 h-3" /> Captured
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-400">Pending</span>
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500">
                    Use high-resolution camera to enroll face recognition vector.
                  </p>
                </div>

                <div className="mt-3">
                  <button
                    type="button"
                    onClick={() => setShowFaceModal(true)}
                    className="w-full py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors flex items-center justify-center gap-1.5 shadow-xs"
                  >
                    <Camera className="w-3.5 h-3.5" />
                    {formData.liveFaceData ? "Retake Face Capture" : "Open Camera & Enroll Face"}
                  </button>
                </div>
              </div>

              {/* Fingerprint Biometric */}
              <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-850 border border-slate-200 dark:border-slate-700 flex flex-col justify-between">
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="font-semibold text-xs text-slate-800 dark:text-slate-200 flex items-center gap-1.5">
                      <Fingerprint className="w-4 h-4 text-emerald-500" /> Fingerprint Registration
                    </span>
                    {formData.registerFingerprint ? (
                      <span className="text-[10px] font-semibold text-emerald-600 bg-emerald-50 dark:bg-emerald-950 px-2 py-0.5 rounded-full flex items-center gap-1">
                        <Check className="w-3 h-3" /> Template Enrolled
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-400">Pending</span>
                    )}
                  </div>
                  <p className="text-[11px] text-slate-500">
                    Connect ZKTeco or R307 sensor gateway to capture fingerprint ridge hash.
                  </p>
                </div>

                <div className="mt-3">
                  <button
                    type="button"
                    onClick={() => setShowFpModal(true)}
                    className="w-full py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-600 text-slate-700 dark:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-700 transition-colors flex items-center justify-center gap-1.5 shadow-xs"
                  >
                    <Fingerprint className="w-3.5 h-3.5" />
                    {formData.registerFingerprint ? "Re-scan Fingerprint" : "Connect Sensor & Scan"}
                  </button>
                </div>
              </div>
            </div>
          </div>

          {/* SECTION 6: Login Credentials */}
          {!isEdit && (
            <div className="space-y-3 pt-3 border-t border-slate-100 dark:border-slate-800">
              <h4 className="text-xs font-bold uppercase tracking-wider text-primary-600 dark:text-primary-400 flex items-center gap-1.5">
                <Lock className="w-3.5 h-3.5" /> 6. Employee Login Account
              </h4>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Username *</label>
                  <input
                    type="text"
                    required
                    value={formData.username}
                    onChange={e => setFormData({ ...formData, username: e.target.value })}
                    placeholder="e.g. robert.vance"
                    className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Password *</label>
                  <input
                    type="password"
                    required
                    value={formData.password}
                    onChange={e => setFormData({ ...formData, password: e.target.value })}
                    className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                  />
                </div>

                <div>
                  <label className="block text-[11px] font-semibold text-slate-600 dark:text-slate-400 mb-1">Confirm Password *</label>
                  <input
                    type="password"
                    required
                    value={formData.confirmPassword}
                    onChange={e => setFormData({ ...formData, confirmPassword: e.target.value })}
                    className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
                  />
                </div>
              </div>
            </div>
          )}

          {/* Footer Buttons */}
          <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
            <button
              type="button"
              onClick={resetForm}
              className="text-xs text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 font-medium transition-colors"
            >
              Reset Form
            </button>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-2.5 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/25 transition-all flex items-center gap-2 disabled:opacity-50"
              >
                <CheckCircle2 className="w-4 h-4" />
                {submitting ? "Saving Employee..." : isEdit ? "Update Employee" : "Complete Registration"}
              </button>
            </div>
          </div>
        </form>

        {/* Biometric Scanning Sub-modals */}
        <FaceScannerModal
          isOpen={showFaceModal}
          onClose={() => setShowFaceModal(false)}
          onCapture={handleFaceCaptured}
          title="Employee Face Enrolment"
          subtitle="Capturing biometric feature vector for AI turnstiles"
        />

        <FingerprintScannerModal
          isOpen={showFpModal}
          onClose={() => setShowFpModal(false)}
          onScanComplete={handleFingerprintScanned}
          devices={devices}
        />
      </motion.div>
    </div>
  );
}
