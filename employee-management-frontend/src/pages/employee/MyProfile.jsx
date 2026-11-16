import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import {
  User,
  Mail,
  Phone,
  MapPin,
  Calendar,
  Briefcase,
  CreditCard,
  Fingerprint,
  Camera,
  ShieldCheck,
  Edit3,
  Save,
  X,
  AlertCircle,
  CheckCircle2,
  Building,
  Hash,
  Eye,
  EyeOff
} from 'lucide-react';
import { employeeApi } from '../../services/api';
import Badge from '../../components/common/Badge';

export default function MyProfile() {
  const [profile, setProfile] = useState(null);
  const [biometricStatus, setBiometricStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState(null);
  const [showMaskedAadhaar, setShowMaskedAadhaar] = useState(false);

  // Editable fields
  const [formData, setFormData] = useState({
    phoneNumber: '',
    address: '',
    profilePhotoUrl: '',
  });

  useEffect(() => {
    fetchProfileData();
  }, []);

  const fetchProfileData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [profRes, bioRes] = await Promise.all([
        employeeApi.getProfile(),
        employeeApi.getBiometricStatus().catch(() => ({ data: null })),
      ]);

      if (profRes.success && profRes.data) {
        setProfile(profRes.data);
        setFormData({
          phoneNumber: profRes.data.phoneNumber || '',
          address: profRes.data.address || '',
          profilePhotoUrl: profRes.data.profilePhotoUrl || '',
        });
      }
      if (bioRes && bioRes.data) {
        setBiometricStatus(bioRes.data);
      }
    } catch (err) {
      setError(err.message || 'Failed to load profile details.');
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    try {
      setSaving(true);
      setMessage(null);
      setError(null);
      const res = await employeeApi.updateProfile(formData);
      if (res.success) {
        setProfile(res.data);
        setEditing(false);
        setMessage('Profile updated successfully!');
        setTimeout(() => setMessage(null), 4000);
      }
    } catch (err) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="w-10 h-10 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="p-8 text-center text-slate-500 dark:text-slate-400">
        <AlertCircle className="w-12 h-12 mx-auto text-amber-500 mb-3" />
        <h3 className="text-lg font-bold text-slate-800 dark:text-slate-100">Profile Not Found</h3>
        <p className="text-sm">Unable to retrieve employee profile data.</p>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header Banner */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-indigo-700 via-indigo-600 to-purple-600 p-8 text-white shadow-xl">
        <div className="absolute -top-12 -right-12 w-48 h-48 bg-white/10 rounded-full blur-2xl"></div>
        <div className="relative z-10 flex flex-col md:flex-row items-center gap-6">
          <div className="relative group">
            <img
              src={
                profile.profilePhotoUrl ||
                `https://ui-avatars.com/api/?name=${encodeURIComponent(
                  profile.fullName || 'Employee'
                )}&background=ffffff&color=4f46e5&size=128`
              }
              alt={profile.fullName}
              className="w-28 h-28 rounded-full border-4 border-white/30 shadow-lg object-cover bg-indigo-900"
            />
            {profile.employmentStatus === 'ACTIVE' && (
              <span className="absolute bottom-1 right-1 w-5 h-5 bg-emerald-500 border-2 border-white rounded-full"></span>
            )}
          </div>

          <div className="flex-1 text-center md:text-left space-y-1">
            <div className="flex flex-wrap items-center justify-center md:justify-start gap-2">
              <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight">{profile.fullName}</h1>
              <Badge variant={profile.employmentStatus === 'ACTIVE' ? 'success' : 'danger'}>
                {profile.employmentStatus}
              </Badge>
            </div>
            <p className="text-xs text-indigo-200">
              Employee ID: <span className="font-mono font-bold tracking-wider">{profile.employeeNumber}</span> | Joined: {profile.joiningDate}
            </p>
          </div>

          <div>
            {!editing ? (
              <button
                onClick={() => setEditing(true)}
                className="flex items-center gap-2 px-5 py-2.5 bg-white/20 hover:bg-white/30 backdrop-blur-md text-white rounded-xl font-medium transition shadow-sm border border-white/20"
              >
                <Edit3 className="w-4 h-4" />
                Edit Contact Info
              </button>
            ) : (
              <button
                onClick={() => {
                  setEditing(false);
                  setFormData({
                    phoneNumber: profile.phoneNumber || '',
                    address: profile.address || '',
                    profilePhotoUrl: profile.profilePhotoUrl || '',
                  });
                }}
                className="flex items-center gap-2 px-4 py-2 bg-slate-900/40 hover:bg-slate-900/60 text-white rounded-xl text-sm font-medium transition"
              >
                <X className="w-4 h-4" />
                Cancel Editing
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Notifications */}
      {message && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-300 flex items-center gap-3 text-sm shadow-sm"
        >
          <CheckCircle2 className="w-5 h-5 flex-shrink-0" />
          <span>{message}</span>
        </motion.div>
      )}

      {error && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className="p-4 rounded-xl bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-300 flex items-center gap-3 text-sm shadow-sm"
        >
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </motion.div>
      )}

      {/* Main Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Personal & Employment */}
        <div className="lg:col-span-2 space-y-6">
          {/* Edit Form / Details Section */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
              <User className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
              Personal & Contact Information
            </h2>

            {editing ? (
              <form onSubmit={handleSave} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
                      Phone Number
                    </label>
                    <input
                      type="text"
                      value={formData.phoneNumber}
                      onChange={(e) => setFormData({ ...formData, phoneNumber: e.target.value })}
                      placeholder="+91 9876543210"
                      required
                      className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
                      Profile Photo URL
                    </label>
                    <input
                      type="url"
                      value={formData.profilePhotoUrl}
                      onChange={(e) => setFormData({ ...formData, profilePhotoUrl: e.target.value })}
                      placeholder="https://example.com/photo.jpg"
                      className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1">
                    Residential Address
                  </label>
                  <textarea
                    rows={3}
                    value={formData.address}
                    onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                    placeholder="Enter complete residential address"
                    required
                    className="w-full px-3.5 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
                  ></textarea>
                </div>

                <div className="flex justify-end gap-3 pt-3 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setEditing(false)}
                    className="px-4 py-2 border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-300 rounded-xl text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={saving}
                    className="flex items-center gap-2 px-5 py-2 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white rounded-xl text-sm font-medium transition shadow-md shadow-indigo-600/20"
                  >
                    <Save className="w-4 h-4" />
                    {saving ? 'Saving Changes...' : 'Save Profile Changes'}
                  </button>
                </div>
              </form>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <User className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 font-medium">Full Legal Name</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white mt-0.5">{profile.fullName}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <Mail className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 font-medium">Official Email ID</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white mt-0.5">{profile.email}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <Phone className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 font-medium">Contact Phone</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white mt-0.5">{profile.phoneNumber || 'Not provided'}</p>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <div className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <Calendar className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 font-medium">Date of Birth & Gender</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white mt-0.5">
                      {profile.dateOfBirth || 'N/A'} &bull; {profile.gender || 'Not specified'}
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-3 md:col-span-2">
                  <div className="p-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                    <MapPin className="w-5 h-5" />
                  </div>
                  <div>
                    <p className="text-xs text-slate-500 font-medium">Residential Address</p>
                    <p className="text-sm font-semibold text-slate-900 dark:text-white mt-0.5 leading-relaxed">
                      {profile.address || 'Address not registered'}
                    </p>
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Employment Organization Details */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-5 flex items-center gap-2">
              <Briefcase className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
              Company & Job Position
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                <span className="text-xs text-slate-500 block mb-1">Employee ID</span>
                <span className="text-sm font-mono font-bold text-indigo-600 dark:text-indigo-400">
                  {profile.employeeNumber}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                <span className="text-xs text-slate-500 block mb-1">Date of Joining</span>
                <span className="text-sm font-medium text-slate-900 dark:text-white">
                  {profile.joiningDate}
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                <span className="text-xs text-slate-500 block mb-1">Status</span>
                <span className="inline-block mt-0.5">
                  <Badge variant={profile.employmentStatus === 'ACTIVE' ? 'success' : 'danger'}>
                    {profile.employmentStatus}
                  </Badge>
                </span>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800">
                <span className="text-xs text-slate-500 block mb-1">Contract / Shift</span>
                <span className="text-sm font-medium text-slate-900 dark:text-white">
                  Standard General Shift
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Biometrics & Protected Financials */}
        <div className="space-y-6">
          {/* Biometrics Status */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900 dark:text-white mb-4 flex items-center gap-2">
              <ShieldCheck className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
              Biometric Enrollment
            </h2>

            <div className="space-y-4">
              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-lg bg-indigo-50 dark:bg-indigo-950/40 text-indigo-600 dark:text-indigo-400">
                    <Camera className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-slate-900 dark:text-white">Face Recognition</h4>
                    <p className="text-xs text-slate-500">Live camera identity registration</p>
                  </div>
                </div>
                <Badge variant={biometricStatus?.faceRegistered ? 'success' : 'neutral'}>
                  {biometricStatus?.faceRegistered ? 'Registered' : 'Pending'}
                </Badge>
              </div>

              <div className="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-2.5 rounded-lg bg-purple-50 dark:bg-purple-950/40 text-purple-600 dark:text-purple-400">
                    <Fingerprint className="w-5 h-5" />
                  </div>
                  <div>
                    <h4 className="text-sm font-semibold text-slate-900 dark:text-white">Fingerprint</h4>
                    <p className="text-xs text-slate-500">ZKTeco / R307 sensor hash</p>
                  </div>
                </div>
                <Badge variant={biometricStatus?.fingerprintRegistered ? 'success' : 'neutral'}>
                  {biometricStatus?.fingerprintRegistered ? 'Registered' : 'Pending'}
                </Badge>
              </div>
            </div>

            <p className="text-xs text-slate-500 dark:text-slate-400 mt-4 leading-relaxed">
              * Biometric templates are stored as cryptographic SHA-256 hashes for privacy compliance. Contact your company administrator to re-enroll or update biometric credentials.
            </p>
          </div>

          {/* Masked Sensitive Financials */}
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-indigo-600 dark:text-indigo-400" />
                Financial Records
              </h2>
              <span className="text-xs text-emerald-600 dark:text-emerald-400 font-semibold flex items-center gap-1">
                <ShieldCheck className="w-3.5 h-3.5" />
                Protected
              </span>
            </div>

            <div className="space-y-3">
              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 flex justify-between items-center text-sm">
                <span className="text-slate-500 text-xs uppercase font-semibold">Aadhaar Card</span>
                <span className="font-mono font-medium text-slate-800 dark:text-slate-200">
                  {profile.aadhaarNumberMasked || 'XXXX-XXXX-XXXX'}
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 flex justify-between items-center text-sm">
                <span className="text-slate-500 text-xs uppercase font-semibold">PAN Card</span>
                <span className="font-mono font-medium text-slate-800 dark:text-slate-200">
                  {profile.panCardNumber ? `${profile.panCardNumber.slice(0, 2)}XXXXX${profile.panCardNumber.slice(-2)}` : 'XXXXXXXXXX'}
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 flex justify-between items-center text-sm">
                <span className="text-slate-500 text-xs uppercase font-semibold">Bank Account</span>
                <span className="font-mono font-medium text-slate-800 dark:text-slate-200">
                  {profile.bankAccountNumberMasked || 'XXXXXXXXXXXX'}
                </span>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 flex justify-between items-center text-sm">
                <span className="text-slate-500 text-xs uppercase font-semibold">Bank IFSC</span>
                <span className="font-mono font-medium text-slate-800 dark:text-slate-200">
                  {profile.ifscCode || 'N/A'}
                </span>
              </div>
            </div>

            <div className="mt-4 p-3 rounded-xl bg-amber-50 dark:bg-amber-950/20 border border-amber-200 dark:border-amber-800 text-xs text-amber-700 dark:text-amber-300">
              Personal banking credentials and salary details are strictly controlled by HR & Accounts department.
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
