import React, { useEffect, useState } from 'react';
import { motion, useSpring, useTransform } from 'framer-motion';
import { ChevronRight } from 'lucide-react';

export default function StatsCard({
  title,
  value = 0,
  icon: Icon,
  color = "primary", // primary, emerald, amber, rose, indigo, purple
  onViewList,
  buttonLabel = "View List",
  subtitle
}) {
  const [displayValue, setDisplayValue] = useState(0);

  // Animated Counter
  useEffect(() => {
    let start = 0;
    const end = parseInt(value, 10) || 0;
    if (end === 0) {
      setDisplayValue(0);
      return;
    }
    const duration = 800; // ms
    const increment = end / (duration / 25);

    const timer = setInterval(() => {
      start += increment;
      if (start >= end) {
        setDisplayValue(end);
        clearInterval(timer);
      } else {
        setDisplayValue(Math.floor(start));
      }
    }, 25);

    return () => clearInterval(timer);
  }, [value]);

  const colorStyles = {
    primary: {
      gradient: "from-primary-500/10 to-indigo-500/10",
      border: "hover:border-primary-500/50",
      iconBg: "bg-primary-500/10 text-primary-600 dark:text-primary-400",
      badge: "text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-950/60",
      btn: "text-primary-600 dark:text-primary-400 hover:bg-primary-50 dark:hover:bg-primary-950/50"
    },
    emerald: {
      gradient: "from-emerald-500/10 to-teal-500/10",
      border: "hover:border-emerald-500/50",
      iconBg: "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400",
      badge: "text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-950/60",
      btn: "text-emerald-600 dark:text-emerald-400 hover:bg-emerald-50 dark:hover:bg-emerald-950/50"
    },
    amber: {
      gradient: "from-amber-500/10 to-orange-500/10",
      border: "hover:border-amber-500/50",
      iconBg: "bg-amber-500/10 text-amber-600 dark:text-amber-400",
      badge: "text-amber-600 dark:text-amber-400 bg-amber-50 dark:bg-amber-950/60",
      btn: "text-amber-600 dark:text-amber-400 hover:bg-amber-50 dark:hover:bg-amber-950/50"
    },
    rose: {
      gradient: "from-rose-500/10 to-pink-500/10",
      border: "hover:border-rose-500/50",
      iconBg: "bg-rose-500/10 text-rose-600 dark:text-rose-400",
      badge: "text-rose-600 dark:text-rose-400 bg-rose-50 dark:bg-rose-950/60",
      btn: "text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/50"
    },
    purple: {
      gradient: "from-purple-500/10 to-fuchsia-500/10",
      border: "hover:border-purple-500/50",
      iconBg: "bg-purple-500/10 text-purple-600 dark:text-purple-400",
      badge: "text-purple-600 dark:text-purple-400 bg-purple-50 dark:bg-purple-950/60",
      btn: "text-purple-600 dark:text-purple-400 hover:bg-purple-50 dark:hover:bg-purple-950/50"
    },
    blue: {
      gradient: "from-blue-500/10 to-cyan-500/10",
      border: "hover:border-blue-500/50",
      iconBg: "bg-blue-500/10 text-blue-600 dark:text-blue-400",
      badge: "text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/60",
      btn: "text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/50"
    }
  };

  const style = colorStyles[color] || colorStyles.primary;

  return (
    <motion.div
      whileHover={{ y: -4, scale: 1.01 }}
      transition={{ type: "spring", stiffness: 300, damping: 20 }}
      className={`glass-card p-5 relative overflow-hidden flex flex-col justify-between border ${style.border} group`}
    >
      {/* Background Gradient Accent */}
      <div className={`absolute top-0 right-0 w-32 h-32 bg-gradient-to-br ${style.gradient} rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none group-hover:scale-125 transition-transform duration-500`} />

      <div>
        <div className="flex items-center justify-between">
          <span className="text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
            {title}
          </span>
          <div className={`p-2.5 rounded-2xl ${style.iconBg} transition-transform group-hover:rotate-6`}>
            {Icon && <Icon className="w-5 h-5" />}
          </div>
        </div>

        <div className="mt-3 flex items-baseline gap-2">
          <div className="text-3xl font-extrabold tracking-tight text-slate-900 dark:text-slate-50 font-mono">
            {displayValue}
          </div>
          {subtitle && (
            <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">
              {subtitle}
            </span>
          )}
        </div>
      </div>

      <div className="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800/80 flex items-center justify-between">
        <button
          onClick={onViewList}
          className={`text-xs font-semibold px-2.5 py-1.5 rounded-xl ${style.btn} flex items-center gap-1 transition-colors`}
        >
          <span>{buttonLabel}</span>
          <ChevronRight className="w-3.5 h-3.5 group-hover:translate-x-0.5 transition-transform" />
        </button>

        <span className="text-[10px] text-slate-400 font-mono">Today</span>
      </div>
    </motion.div>
  );
}
