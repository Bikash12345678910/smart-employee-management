import React, { useState, useEffect } from 'react';
import { adminApi } from '../../services/api';
import * as pdfjsLib from 'pdfjs-dist';
import pdfWorkerUrl from 'pdfjs-dist/build/pdf.worker.min.mjs?url';
import {
  Settings,
  Building,
  Clock,
  Shield,
  DollarSign,
  Save,
  CheckCircle2,
  AlertCircle,
  ToggleLeft,
  ToggleRight,
  Upload,
  FileText
} from 'lucide-react';
import { motion } from 'framer-motion';

pdfjsLib.GlobalWorkerOptions.workerSrc = pdfWorkerUrl;

export default function CompanySettings() {
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [error, setError] = useState(null);
  const [logoStatus, setLogoStatus] = useState('');

  const handleLogoFileUpload = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setError(null);
    setLogoStatus('Converting company logo to image format...');

    try {
      if (file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf')) {
        const arrayBuffer = await file.arrayBuffer();
        const pdf = await pdfjsLib.getDocument({ data: arrayBuffer }).promise;
        const page = await pdf.getPage(1);
        const viewport = page.getViewport({ scale: 2.0 });
        const canvas = document.createElement('canvas');
        canvas.width = viewport.width;
        canvas.height = viewport.height;
        const ctx = canvas.getContext('2d');
        await page.render({ canvasContext: ctx, viewport }).promise;
        const pngDataUrl = canvas.toDataURL('image/png');
        handleChange('companyLogoUrl', pngDataUrl);
        setLogoStatus('PDF converted to PNG image! Click "Save Company Configuration" to store in DB.');
      } else if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = () => {
          handleChange('companyLogoUrl', reader.result);
          setLogoStatus('Logo image loaded! Click "Save Company Configuration" to store in DB.');
        };
        reader.readAsDataURL(file);
      } else {
        setError('Please upload a PDF (.pdf) or image file (.png, .jpg) for the Company Logo.');
        setLogoStatus('');
      }
    } catch (err) {
      setError('Failed to convert PDF logo to image: ' + err.message);
      setLogoStatus('');
    }
  };

  const fetchSettings = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getSettings();
      if (res.success && res.data) {
        setSettings(res.data);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSettings();
  }, []);

  const handleChange = (field, value) => {
    setSettings(prev => ({ ...prev, [field]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setSavedSuccess(false);
    setError(null);
    try {
      const res = await adminApi.updateSettings(settings);
      if (res.success) {
        setSettings(res.data);
        setSavedSuccess(true);
        setTimeout(() => setSavedSuccess(false), 4000);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  if (loading && !settings) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-3">
        <div className="w-8 h-8 border-3 border-primary-500/20 border-t-primary-600 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Loading company settings...</p>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Settings className="w-6 h-6 text-primary-500" /> Company Configuration & Policy Settings
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Define corporate shift timing, grace periods, biometric toggles, and salary calculation rules.
          </p>
        </div>

        {savedSuccess && (
          <div className="px-3 py-1.5 rounded-xl bg-emerald-50 dark:bg-emerald-950 text-emerald-600 text-xs font-semibold flex items-center gap-1.5 border border-emerald-200 dark:border-emerald-800">
            <CheckCircle2 className="w-4 h-4" /> Policies Saved & Active
          </div>
        )}
      </div>

      {error && (
        <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/30 text-rose-500 text-xs flex items-center gap-2">
          <AlertCircle className="w-4 h-4" /> {error}
        </div>
      )}

      <form onSubmit={handleSave} className="space-y-6">
        {/* 1. Company Information */}
        <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800">
            <Building className="w-4 h-4 text-primary-500" /> 1. Company Information
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Company Legal Name *</label>
              <input
                type="text"
                required
                value={settings.companyName || ''}
                onChange={e => handleChange('companyName', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Company Official Email *</label>
              <input
                type="email"
                required
                value={settings.companyEmail || ''}
                onChange={e => handleChange('companyEmail', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Contact Phone Number *</label>
              <input
                type="text"
                required
                value={settings.contactNumber || ''}
                onChange={e => handleChange('contactNumber', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">
                Company Logo (Upload PDF &rarr; Auto-Converted &amp; Saved as PNG Image in DB)
              </label>
              <div className="flex items-center gap-3">
                <label className="flex-1 cursor-pointer px-3 py-2 rounded-xl border border-dashed border-primary-400 bg-primary-50/40 dark:bg-slate-800 text-primary-700 dark:text-primary-300 font-semibold flex items-center justify-center gap-2 hover:bg-primary-50 dark:hover:bg-slate-750 transition-colors">
                  <Upload className="w-4 h-4" />
                  <span>Upload Logo PDF (.pdf) or Image</span>
                  <input
                    type="file"
                    accept="application/pdf,.pdf,image/png,image/jpeg"
                    onChange={handleLogoFileUpload}
                    className="hidden"
                  />
                </label>
                {settings.companyLogoUrl && (
                  <img
                    src={settings.companyLogoUrl}
                    alt="Company Logo Preview"
                    className="w-12 h-12 rounded-xl object-contain bg-white border border-slate-200 dark:border-slate-700 p-1 shadow-xs"
                  />
                )}
              </div>
              {logoStatus && (
                <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium mt-1 flex items-center gap-1">
                  <FileText className="w-3 h-3" /> {logoStatus}
                </p>
              )}
            </div>

            <div className="sm:col-span-2">
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Headquarters Address *</label>
              <textarea
                required
                rows={2}
                value={settings.companyAddress || ''}
                onChange={e => handleChange('companyAddress', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              />
            </div>
          </div>
        </div>

        {/* 2. Shift & Timing Settings */}
        <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800">
            <Clock className="w-4 h-4 text-amber-500" /> 2. Shift & Working Hours Policy
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Shift Start Time *</label>
              <input
                type="time"
                step="1"
                required
                value={settings.shiftStartTime || '09:00:00'}
                onChange={e => handleChange('shiftStartTime', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Shift End Time *</label>
              <input
                type="time"
                step="1"
                required
                value={settings.shiftEndTime || '18:00:00'}
                onChange={e => handleChange('shiftEndTime', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Late Grace Period (Minutes) *</label>
              <input
                type="number"
                min="0"
                required
                value={settings.lateArrivalGraceMinutes || 15}
                onChange={e => handleChange('lateArrivalGraceMinutes', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Half-Day Threshold (Hours) *</label>
              <input
                type="number"
                min="1"
                required
                value={settings.halfDayThresholdHours || 4}
                onChange={e => handleChange('halfDayThresholdHours', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Full-Day Standard (Hours) *</label>
              <input
                type="number"
                min="1"
                required
                value={settings.fullDayWorkingHours || 8}
                onChange={e => handleChange('fullDayWorkingHours', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Weekly Off Day</label>
              <select
                value={settings.weeklyOffDays || 'SUNDAY'}
                onChange={e => handleChange('weeklyOffDays', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              >
                <option value="SUNDAY">Sunday</option>
                <option value="SATURDAY_SUNDAY">Saturday & Sunday</option>
                <option value="FRIDAY">Friday</option>
              </select>
            </div>
          </div>
        </div>

        {/* 3. Attendance & Biometric Permissions */}
        <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800">
            <Shield className="w-4 h-4 text-cyan-500" /> 3. Attendance Punch & Biometric Controls
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
              <div>
                <div className="font-semibold text-slate-800 dark:text-slate-200">Enable Face Attendance</div>
                <div className="text-[11px] text-slate-500">Allow AI camera live face recognition</div>
              </div>
              <button
                type="button"
                onClick={() => handleChange('enableFaceAttendance', !settings.enableFaceAttendance)}
                className={`text-2xl ${settings.enableFaceAttendance ? 'text-emerald-500' : 'text-slate-400'}`}
              >
                {settings.enableFaceAttendance ? <ToggleRight className="w-6 h-6" /> : <ToggleLeft className="w-6 h-6" />}
              </button>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
              <div>
                <div className="font-semibold text-slate-800 dark:text-slate-200">Enable Fingerprint Attendance</div>
                <div className="text-[11px] text-slate-500">Allow ZKTeco / R307 sensor punches</div>
              </div>
              <button
                type="button"
                onClick={() => handleChange('enableFingerprintAttendance', !settings.enableFingerprintAttendance)}
                className={`text-2xl ${settings.enableFingerprintAttendance ? 'text-emerald-500' : 'text-slate-400'}`}
              >
                {settings.enableFingerprintAttendance ? <ToggleRight className="w-6 h-6" /> : <ToggleLeft className="w-6 h-6" />}
              </button>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
              <div>
                <div className="font-semibold text-slate-800 dark:text-slate-200">Enable Password Attendance</div>
                <div className="text-[11px] text-slate-500">Allow fallback PIN / password punch</div>
              </div>
              <button
                type="button"
                onClick={() => handleChange('enablePasswordAttendance', !settings.enablePasswordAttendance)}
                className={`text-2xl ${settings.enablePasswordAttendance ? 'text-emerald-500' : 'text-slate-400'}`}
              >
                {settings.enablePasswordAttendance ? <ToggleRight className="w-6 h-6" /> : <ToggleLeft className="w-6 h-6" />}
              </button>
            </div>

            <div className="flex items-center justify-between p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700">
              <div>
                <div className="font-semibold text-slate-800 dark:text-slate-200">Allow Manual Correction</div>
                <div className="text-[11px] text-slate-500">Allow audited admin time overrides</div>
              </div>
              <button
                type="button"
                onClick={() => handleChange('allowManualAttendance', !settings.allowManualAttendance)}
                className={`text-2xl ${settings.allowManualAttendance ? 'text-emerald-500' : 'text-slate-400'}`}
              >
                {settings.allowManualAttendance ? <ToggleRight className="w-6 h-6" /> : <ToggleLeft className="w-6 h-6" />}
              </button>
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Max Breaks Allowed Per Day</label>
              <input
                type="number"
                min="1"
                max="10"
                value={settings.maxBreaksAllowed || 3}
                onChange={e => handleChange('maxBreaksAllowed', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Max Total Break Duration (Mins)</label>
              <input
                type="number"
                min="15"
                max="180"
                value={settings.maxBreakMinutes || 60}
                onChange={e => handleChange('maxBreakMinutes', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>
          </div>
        </div>

        {/* 4. Salary & Payroll Calculation Rules */}
        <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
          <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2 pb-2 border-b border-slate-100 dark:border-slate-800">
            <DollarSign className="w-4 h-4 text-emerald-500" /> 4. Currency & Payroll Calculation Rules
          </h2>

          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 text-xs">
            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Company Currency *</label>
              <select
                value={settings.currencyCode || 'INR'}
                onChange={e => {
                  const val = e.target.value;
                  handleChange('currencyCode', val);
                  handleChange('currencySymbol', val === 'INR' ? '₹' : '$');
                }}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-bold"
              >
                <option value="INR">Indian Rupee (INR - ₹)</option>
                <option value="USD">US Dollar (USD - $)</option>
              </select>
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Standard Monthly Working Days *</label>
              <input
                type="number"
                min="20"
                max="31"
                required
                value={settings.standardWorkingDays || 26}
                onChange={e => handleChange('standardWorkingDays', Number(e.target.value))}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Leave Deduction Rule</label>
              <select
                value={settings.leaveDeductionRate || 'PRO_RATA'}
                onChange={e => handleChange('leaveDeductionRate', e.target.value)}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
              >
                <option value="PRO_RATA">Pro-Rata (Salary / Working Days)</option>
                <option value="FIXED_DAILY">Fixed Deductible</option>
              </select>
            </div>

            <div>
              <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">
                Overtime Hourly Rate ({settings.currencySymbol || '₹'})
              </label>
              <input
                type="text"
                inputMode="decimal"
                placeholder="e.g. 200.00"
                value={settings.overtimeRatePerHour ?? ''}
                onChange={e => {
                  const val = e.target.value.replace(/[^0-9.]/g, '');
                  handleChange('overtimeRatePerHour', val);
                }}
                onBlur={e => {
                  const parsed = parseFloat(e.target.value);
                  handleChange('overtimeRatePerHour', isNaN(parsed) ? 0 : parsed);
                }}
                className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-mono"
              />
            </div>
          </div>
        </div>

        {/* Submit */}
        <div className="flex justify-end pt-2">
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/25 transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {saving ? "Updating Rules..." : "Save Company Configuration"}
          </button>
        </div>
      </form>
    </div>
  );
}
