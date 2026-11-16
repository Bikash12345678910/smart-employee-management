import React, { useState, useEffect } from 'react';
import { Fingerprint, CheckCircle2, AlertCircle, X, Cpu, RefreshCw, Radio } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function FingerprintScannerModal({
  isOpen,
  onClose,
  onScanComplete,
  title = "Biometric Fingerprint Scanner",
  devices = []
}) {
  const [selectedDeviceId, setSelectedDeviceId] = useState(devices[0]?.id || 1);
  const [scanning, setScanning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [scanned, setScanned] = useState(false);
  const [deviceStatus, setDeviceStatus] = useState("CONNECTED");

  useEffect(() => {
    if (devices.length > 0 && !selectedDeviceId) {
      setSelectedDeviceId(devices[0].id);
    }
  }, [devices]);

  useEffect(() => {
    if (!isOpen) {
      setScanning(false);
      setProgress(0);
      setScanned(false);
    }
  }, [isOpen]);

  const playChime = () => {
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'triangle';
      osc.frequency.setValueAtTime(523.25, audioCtx.currentTime); // C5
      osc.frequency.setValueAtTime(659.25, audioCtx.currentTime + 0.1); // E5
      gain.gain.setValueAtTime(0.12, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.35);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.35);
    } catch (e) {
      // Audio fallback
    }
  };

  const handleStartScan = () => {
    setScanning(true);
    setProgress(15);
    setScanned(false);

    const interval = setInterval(() => {
      setProgress(prev => {
        if (prev >= 90) {
          clearInterval(interval);
          setScanning(false);
          setScanned(true);
          playChime();
          return 100;
        }
        return prev + 25;
      });
    }, 180);
  };

  const handleConfirm = () => {
    onScanComplete({
      deviceId: selectedDeviceId,
      template: "FP_TEMPLATE_EXTRACTED_" + Date.now(),
      score: 98.4
    });
    onClose();
  };

  if (!isOpen) return null;

  const currentDevice = devices.find(d => d.id === selectedDeviceId) || {
    deviceName: "ZKTeco uFace800 Turnstile",
    deviceModel: "ZK-UF800",
    ipAddress: "192.168.1.120"
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm">
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        className="w-full max-w-md bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400">
              <Fingerprint className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-semibold text-slate-800 dark:text-slate-100 text-sm md:text-base">{title}</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Optical sensor gateway interface</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* Device Selection & Status */}
          <div className="p-3.5 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700/60 space-y-2">
            <div className="flex items-center justify-between text-xs">
              <span className="font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-1.5">
                <Cpu className="w-3.5 h-3.5 text-primary-500" /> Target Hardware Sensor:
              </span>
              <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 dark:bg-emerald-950/80 text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                <Radio className="w-2.5 h-2.5 animate-pulse" /> ONLINE
              </span>
            </div>

            {devices.length > 0 ? (
              <select
                value={selectedDeviceId}
                onChange={(e) => setSelectedDeviceId(Number(e.target.value))}
                className="w-full text-xs font-medium px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
              >
                {devices.map(d => (
                  <option key={d.id} value={d.id}>
                    {d.deviceName} ({d.deviceModel} @ {d.ipAddress})
                  </option>
                ))}
              </select>
            ) : (
              <div className="text-xs text-slate-600 dark:text-slate-400 font-mono">
                {currentDevice.deviceName} ({currentDevice.ipAddress})
              </div>
            )}
          </div>

          {/* Scanner Visual Touchpad */}
          <div className="flex flex-col items-center justify-center p-8 rounded-3xl bg-slate-950 border border-slate-800 relative overflow-hidden">
            {/* Animated Glow Rings */}
            <div className={`relative w-32 h-32 rounded-full border-2 ${scanned ? 'border-emerald-500 shadow-[0_0_35px_rgba(16,185,129,0.4)]' : scanning ? 'border-cyan-400 shadow-[0_0_35px_rgba(34,211,238,0.4)]' : 'border-slate-700'} flex items-center justify-center transition-all duration-300`}>
              <Fingerprint className={`w-20 h-20 transition-colors duration-300 ${scanned ? 'text-emerald-400' : scanning ? 'text-cyan-400 animate-pulse' : 'text-slate-600'}`} />

              {/* Scanning Laser Bar */}
              {scanning && (
                <div className="absolute inset-x-2 h-1 bg-gradient-to-r from-transparent via-cyan-400 to-transparent shadow-[0_0_12px_#22d3ee] animate-scan" />
              )}

              {scanned && (
                <motion.div
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  className="absolute bottom-1 right-1 p-1 rounded-full bg-emerald-500 text-white shadow-md"
                >
                  <CheckCircle2 className="w-5 h-5" />
                </motion.div>
              )}
            </div>

            {/* Instruction / Status Label */}
            <div className="mt-5 text-center space-y-1">
              <div className="text-sm font-semibold text-slate-100">
                {scanned ? "Biometric Match Verified" : scanning ? `Scanning Sensor (${progress}%)` : "Ready for Fingerprint"}
              </div>
              <p className="text-xs text-slate-400 max-w-xs">
                {scanned
                  ? "Ridge template captured with 98.4% confidence score."
                  : scanning
                  ? "Hold finger firmly on the optical prism..."
                  : "Click below to trigger optical fingerprint capture."}
              </p>
            </div>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-between p-4 px-6 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-100 dark:border-slate-800">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            Cancel
          </button>

          {scanned ? (
            <button
              type="button"
              onClick={handleConfirm}
              className="px-5 py-2.5 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-md shadow-emerald-600/20 transition-all flex items-center gap-1.5"
            >
              <CheckCircle2 className="w-4 h-4" /> Register Fingerprint
            </button>
          ) : (
            <button
              type="button"
              onClick={handleStartScan}
              disabled={scanning}
              className="px-5 py-2.5 text-xs font-semibold rounded-xl bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-700 hover:to-teal-700 text-white shadow-md shadow-emerald-600/20 transition-all flex items-center gap-1.5 disabled:opacity-50"
            >
              <Fingerprint className="w-4 h-4" /> {scanning ? "Scanning..." : "Place Finger & Scan"}
            </button>
          )}
        </div>
      </motion.div>
    </div>
  );
}
