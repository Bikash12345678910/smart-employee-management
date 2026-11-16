import React from 'react';
import { StatusBadge } from '../../components/common/Badge';
import {
  User,
  Mail,
  Phone,
  MapPin,
  Calendar,
  Building,
  CreditCard,
  Fingerprint,
  Camera,
  ShieldCheck,
  X,
  Printer
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function EmployeeProfileModal({ isOpen, onClose, employee }) {
  if (!isOpen || !employee) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm">
      <motion.div
        initial={{ scale: 0.95, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.95, opacity: 0 }}
        className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden"
      >
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-850">
          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-800 dark:text-slate-100 text-sm">Employee Profile Dossier</span>
            <span className="font-mono text-xs font-semibold text-primary-600 dark:text-primary-400">
              #{employee.employeeCode}
            </span>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => window.print()}
              className="p-1.5 rounded-xl text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
              title="Print Dossier"
            >
              <Printer className="w-4 h-4" />
            </button>
            <button
              onClick={onClose}
              className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 max-h-[75vh] overflow-y-auto">
          {/* Avatar & Key Employment Stats */}
          <div className="flex flex-col sm:flex-row items-center gap-5 p-5 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800">
            <img
              src={employee.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"}
              alt={employee.fullName}
              className="w-20 h-20 rounded-2xl object-cover ring-2 ring-primary-500/20 shadow-md"
            />
            <div className="space-y-1 text-center sm:text-left">
              <div className="flex items-center justify-center sm:justify-start gap-2">
                <h3 className="text-lg font-bold text-slate-900 dark:text-slate-100">
                  {employee.fullName}
                </h3>
                <StatusBadge status={employee.employmentStatus} />
              </div>
              <p className="text-xs font-semibold text-primary-600 dark:text-primary-400">
                {employee.designationTitle} • {employee.departmentName}
              </p>
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-3 text-xs text-slate-500 pt-1">
                <span className="flex items-center gap-1"><Mail className="w-3.5 h-3.5" /> {employee.email}</span>
                <span className="flex items-center gap-1"><Phone className="w-3.5 h-3.5" /> {employee.phoneNumber}</span>
              </div>
            </div>
          </div>

          {/* Details Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            {/* Employment Details */}
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-850 border border-slate-200 dark:border-slate-800 space-y-2.5">
              <h4 className="font-bold uppercase tracking-wider text-[11px] text-slate-400 flex items-center gap-1.5">
                <Building className="w-3.5 h-3.5 text-primary-500" /> Employment Details
              </h4>
              <div className="space-y-1.5">
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Employee Code</span>
                  <span className="font-mono font-bold text-slate-800 dark:text-slate-200">{employee.employeeCode}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Joining Date</span>
                  <span className="font-medium text-slate-800 dark:text-slate-200">{employee.joiningDate}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Agreed Salary</span>
                  <span className="font-mono font-bold text-emerald-600 dark:text-emerald-400">
                    ${Number(employee.agreedMonthlySalary).toLocaleString()}/month
                  </span>
                </div>
                <div className="flex justify-between py-1">
                  <span className="text-slate-500">Department Code</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">{employee.departmentCode}</span>
                </div>
              </div>
            </div>

            {/* Financial Details (Role-Protected Masked) */}
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-850 border border-slate-200 dark:border-slate-800 space-y-2.5">
              <h4 className="font-bold uppercase tracking-wider text-[11px] text-slate-400 flex items-center gap-1.5">
                <CreditCard className="w-3.5 h-3.5 text-emerald-500" /> Financial & Identification
              </h4>
              <div className="space-y-1.5">
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Aadhaar (Protected)</span>
                  <span className="font-mono font-semibold text-slate-800 dark:text-slate-200">{employee.aadhaarMasked}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">PAN Card (Protected)</span>
                  <span className="font-mono font-semibold text-slate-800 dark:text-slate-200">{employee.panMasked}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Bank Account</span>
                  <span className="font-mono font-semibold text-slate-800 dark:text-slate-200">{employee.bankAccountMasked}</span>
                </div>
                <div className="flex justify-between py-1">
                  <span className="text-slate-500">Bank Name / IFSC</span>
                  <span className="font-medium text-slate-800 dark:text-slate-200">{employee.bankName || 'HDFC Bank'} ({employee.bankIfscCode})</span>
                </div>
              </div>
            </div>

            {/* Personal Details */}
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-850 border border-slate-200 dark:border-slate-800 space-y-2.5">
              <h4 className="font-bold uppercase tracking-wider text-[11px] text-slate-400 flex items-center gap-1.5">
                <User className="w-3.5 h-3.5 text-indigo-500" /> Personal Information
              </h4>
              <div className="space-y-1.5">
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Date of Birth</span>
                  <span className="text-slate-800 dark:text-slate-200">{employee.dateOfBirth}</span>
                </div>
                <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                  <span className="text-slate-500">Gender</span>
                  <span className="text-slate-800 dark:text-slate-200">{employee.gender}</span>
                </div>
                <div className="flex justify-between py-1">
                  <span className="text-slate-500">Address</span>
                  <span className="text-slate-800 dark:text-slate-200 text-right max-w-[180px] truncate">{employee.address}</span>
                </div>
              </div>
            </div>

            {/* Biometric Credentials */}
            <div className="p-4 rounded-2xl bg-white dark:bg-slate-850 border border-slate-200 dark:border-slate-800 space-y-2.5">
              <h4 className="font-bold uppercase tracking-wider text-[11px] text-slate-400 flex items-center gap-1.5">
                <ShieldCheck className="w-3.5 h-3.5 text-cyan-500" /> Biometric Registrations
              </h4>
              <div className="space-y-2">
                <div className="flex items-center justify-between p-2 rounded-xl bg-slate-50 dark:bg-slate-800">
                  <span className="flex items-center gap-1.5 text-slate-700 dark:text-slate-300">
                    <Camera className="w-4 h-4 text-cyan-500" /> Face Enrolment
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${employee.isFaceRegistered ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600' : 'bg-slate-200 text-slate-500'}`}>
                    {employee.isFaceRegistered ? "Enrolled" : "Not Enrolled"}
                  </span>
                </div>

                <div className="flex items-center justify-between p-2 rounded-xl bg-slate-50 dark:bg-slate-800">
                  <span className="flex items-center gap-1.5 text-slate-700 dark:text-slate-300">
                    <Fingerprint className="w-4 h-4 text-emerald-500" /> Fingerprint Enrolment
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${employee.isFingerprintRegistered ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600' : 'bg-slate-200 text-slate-500'}`}>
                    {employee.isFingerprintRegistered ? "Enrolled (Slot Active)" : "Not Enrolled"}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div className="px-6 py-4 bg-slate-50 dark:bg-slate-850 border-t border-slate-100 dark:border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-5 py-2 text-xs font-semibold rounded-xl bg-slate-800 dark:bg-slate-700 text-white hover:bg-slate-700 dark:hover:bg-slate-600 transition-colors"
          >
            Close Dossier
          </button>
        </div>
      </motion.div>
    </div>
  );
}
