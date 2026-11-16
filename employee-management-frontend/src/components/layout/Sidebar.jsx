import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  LayoutDashboard,
  Users,
  Fingerprint,
  CalendarCheck,
  CalendarDays,
  Coffee,
  FileText,
  Calendar,
  Settings,
  User,
  Clock,
  Shield,
  Smartphone,
  ChevronRight,
  X
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function Sidebar({ isOpen, onClose }) {
  const { isAdmin } = useAuth();

  const adminLinks = [
    { name: 'Dashboard', path: '/admin/dashboard', icon: LayoutDashboard },
    { name: 'Employees', path: '/admin/employees', icon: Users },
    { name: 'Biometrics', path: '/admin/biometrics', icon: Fingerprint },
    { name: 'Mobile Biometric Settings', path: '/admin/mobile-biometrics', icon: Smartphone },
    { name: 'Attendance', path: '/admin/attendance', icon: CalendarCheck },
    { name: 'Monthly Records', path: '/admin/monthly-records', icon: CalendarDays },
    { name: 'Payslips', path: '/admin/payslips', icon: FileText },
    { name: 'Holidays', path: '/admin/holidays', icon: Calendar },
    { name: 'Settings', path: '/admin/settings', icon: Settings },
  ];

  const employeeLinks = [
    { name: 'My Dashboard', path: '/employee/dashboard', icon: LayoutDashboard },
    { name: 'My Profile', path: '/employee/profile', icon: User },
    { name: 'My Attendance', path: '/employee/attendance', icon: CalendarCheck },
    { name: 'My Payslips', path: '/employee/payslips', icon: FileText },
    { name: 'Company Holidays', path: '/employee/holidays', icon: Calendar },
  ];

  const links = isAdmin ? adminLinks : employeeLinks;

  const content = (
    <div className="flex flex-col h-full bg-white dark:bg-slate-900 border-r border-slate-200 dark:border-slate-800 w-64 select-none">
      {/* Sidebar Header */}
      <div className="h-16 flex items-center justify-between px-6 border-b border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-2.5">
          <img src="/app-icon.svg" className="w-6 h-6 rounded-lg shadow-xs object-cover" alt="App Icon" />
          <span className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
            {isAdmin ? 'Admin Console' : 'Employee Portal'}
          </span>
        </div>

        <button
          onClick={onClose}
          className="md:hidden p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      {/* Navigation List */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <NavLink
              key={link.path}
              to={link.path}
              onClick={() => {
                if (window.innerWidth < 768) onClose();
              }}
              className={({ isActive }) =>
                `relative flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 group ${
                  isActive
                    ? 'text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-950/60 font-semibold shadow-xs'
                    : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100/70 dark:hover:bg-slate-800/60'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <Icon className={`w-4 h-4 transition-transform group-hover:scale-110 ${isActive ? 'text-primary-600 dark:text-primary-400' : 'text-slate-400 dark:text-slate-500'}`} />
                  <span className="flex-1 truncate">{link.name}</span>
                  {isActive && (
                    <motion.div
                      layoutId="sidebarActivePill"
                      className="absolute right-2 w-1.5 h-4 rounded-full bg-primary-600 dark:bg-primary-400"
                      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                    />
                  )}
                </>
              )}
            </NavLink>
          );
        })}
      </nav>

      {/* Footer Info */}
      <div className="p-4 border-t border-slate-100 dark:border-slate-800 text-[11px] text-slate-400 dark:text-slate-500 text-center">
        Smart Attendance Engine v1.0
      </div>
    </div>
  );

  return (
    <>
      {/* Desktop static sidebar */}
      <aside className="hidden md:block h-[calc(100vh-4rem)] sticky top-16 z-20 flex-shrink-0">
        {content}
      </aside>

      {/* Mobile drawer backdrop and panel */}
      <AnimatePresence>
        {isOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={onClose}
              className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs z-40 md:hidden"
            />
            <motion.div
              initial={{ x: '-100%' }}
              animate={{ x: 0 }}
              exit={{ x: '-100%' }}
              transition={{ type: 'spring', damping: 25, stiffness: 200 }}
              className="fixed inset-y-0 left-0 z-50 md:hidden"
            >
              {content}
            </motion.div>
          </>
        )}
      </AnimatePresence>
    </>
  );
}
