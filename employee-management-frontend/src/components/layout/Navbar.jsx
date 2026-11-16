import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { Sun, Moon, LogOut, User as UserIcon, Shield, Clock, Menu, X, Fingerprint } from 'lucide-react';
import { motion } from 'framer-motion';

export default function Navbar({ onToggleSidebar }) {
  const { user, logout, isAdmin } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const [time, setTime] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const formattedTime = time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const formattedDate = time.toLocaleDateString([], { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' });

  return (
    <header className="sticky top-0 z-30 h-16 glass-panel border-b border-slate-200 dark:border-slate-800 transition-colors">
      <div className="flex items-center justify-between h-full px-4 md:px-6">
        {/* Left Side: Mobile toggle & Brand */}
        <div className="flex items-center gap-3">
          <button
            onClick={onToggleSidebar}
            className="p-2 rounded-xl text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 md:hidden transition-colors"
          >
            <Menu className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-2.5">
            <img src="/app-icon.svg" className="w-9 h-9 rounded-xl shadow-md border border-slate-700/30 object-cover" alt="App Icon" />
            <div>
              <span className="font-bold text-base md:text-lg tracking-tight bg-gradient-to-r from-primary-600 to-indigo-600 dark:from-primary-400 dark:to-indigo-300 bg-clip-text text-transparent">
                ApexBio
              </span>
              <span className="hidden sm:inline-block text-xs text-slate-400 dark:text-slate-500 ml-1.5 font-medium">
                Enterprise HRM
              </span>
            </div>
          </div>
        </div>

        {/* Center: Live Date & Clock */}
        <div className="hidden lg:flex items-center gap-2 px-3 py-1.5 rounded-full bg-slate-100 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/50 text-xs text-slate-600 dark:text-slate-300 font-medium">
          <Clock className="w-3.5 h-3.5 text-primary-500" />
          <span>{formattedDate}</span>
          <span className="text-slate-300 dark:text-slate-600">•</span>
          <span className="font-mono text-primary-600 dark:text-primary-400">{formattedTime}</span>
        </div>

        {/* Right Side: Theme Toggle & User Info */}
        <div className="flex items-center gap-2 sm:gap-3">
          {/* Dark / Light Toggle */}
          <button
            onClick={toggleTheme}
            className="p-2 rounded-xl text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
            title={theme === 'dark' ? 'Switch to Light Mode' : 'Switch to Dark Mode'}
          >
            {theme === 'dark' ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-slate-600" />}
          </button>

          {/* User Profile Info */}
          <div className="flex items-center gap-2.5 pl-2 border-l border-slate-200 dark:border-slate-800">
            <div className="relative">
              {user?.photoUrl ? (
                <img
                  src={user.photoUrl}
                  alt={user.employeeName || user.username}
                  className="w-8 h-8 rounded-full object-cover ring-2 ring-primary-500/20"
                />
              ) : (
                <div className="w-8 h-8 rounded-full bg-primary-100 dark:bg-primary-950 text-primary-600 dark:text-primary-400 font-semibold flex items-center justify-center text-xs">
                  {user?.employeeName ? user.employeeName.charAt(0).toUpperCase() : 'U'}
                </div>
              )}
              <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-emerald-500 border-2 border-white dark:border-slate-900 rounded-full" />
            </div>

            <div className="hidden sm:block text-left">
              <div className="text-xs font-semibold text-slate-800 dark:text-slate-200 truncate max-w-[130px]">
                {user?.employeeName || user?.username}
              </div>
              <div className="text-[10px] text-slate-500 dark:text-slate-400 flex items-center gap-1 font-medium">
                {isAdmin ? (
                  <span className="text-primary-600 dark:text-primary-400 font-semibold flex items-center gap-0.5">
                    <Shield className="w-2.5 h-2.5" /> Admin
                  </span>
                ) : (
                  <span>{user?.employeeCode || 'Employee'}</span>
                )}
              </div>
            </div>

            <button
              onClick={logout}
              className="p-2 ml-1 rounded-xl text-rose-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors"
              title="Logout"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
