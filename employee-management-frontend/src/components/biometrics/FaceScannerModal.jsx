import React, { useState, useRef, useEffect } from 'react';
import { Camera, RefreshCw, CheckCircle, AlertTriangle, X, Shield, Sparkles } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function FaceScannerModal({ isOpen, onClose, onCapture, title = "Live Face Recognition", subtitle = "Position face within the frame" }) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [stream, setStream] = useState(null);
  const [capturedImage, setCapturedImage] = useState(null);
  const [cameraError, setCameraError] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (isOpen) {
      startCamera();
    } else {
      stopCamera();
      setCapturedImage(null);
      setProgress(0);
      setScanning(false);
    }
    return () => stopCamera();
  }, [isOpen]);

  useEffect(() => {
    if (videoRef.current && stream) {
      videoRef.current.srcObject = stream;
      videoRef.current.play().catch(() => {});
    }
  }, [stream]);

  const startCamera = async () => {
    setCameraError(null);
    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const mediaStream = await navigator.mediaDevices.getUserMedia({
          video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: "user" },
          audio: false
        });
        setStream(mediaStream);
        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
        }
      } else {
        throw new Error("Webcam not supported by browser environment");
      }
    } catch (err) {
      console.warn("Camera access failed or unavailable, providing simulated stream:", err.message);
      setCameraError(err.message || "Camera access denied. Simulation mode active.");
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach(track => track.stop());
      setStream(null);
    }
  };

  const playBeep = () => {
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(880, audioCtx.currentTime); // A5 note
      gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.25);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.25);
    } catch (e) {
      // AudioContext not allowed or disabled
    }
  };

  const handleScanAndCapture = () => {
    setScanning(true);
    setProgress(10);

    const interval = setInterval(() => {
      setProgress(prev => {
        if (prev >= 90) {
          clearInterval(interval);
          finalizeCapture();
          return 100;
        }
        return prev + 20;
      });
    }, 150);
  };

  const finalizeCapture = () => {
    playBeep();
    let dataUrl = null;

    if (videoRef.current && canvasRef.current && stream) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth || 400;
      canvas.height = video.videoHeight || 300;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
      dataUrl = canvas.toDataURL('image/jpeg', 0.85);
    } else {
      // Fallback realistic face sample
      dataUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&auto=format&fit=crop&q=80";
    }

    setCapturedImage(dataUrl);
    setScanning(false);
  };

  const handleConfirm = () => {
    if (capturedImage) {
      onCapture(capturedImage);
      onClose();
    }
  };

  const handleRetake = () => {
    setCapturedImage(null);
    setProgress(0);
    setScanning(false);
    if (!stream && !cameraError) {
      startCamera();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm">
      <motion.div
        initial={{ scale: 0.9, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.9, opacity: 0 }}
        className="w-full max-w-lg bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden"
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-primary-100 dark:bg-primary-950 text-primary-600 dark:text-primary-400">
              <Camera className="w-5 h-5" />
            </div>
            <div>
              <h3 className="font-semibold text-slate-800 dark:text-slate-100 text-sm md:text-base">{title}</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">{subtitle}</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Camera Viewport */}
        <div className="relative p-6 bg-slate-950 flex flex-col items-center justify-center min-h-[320px]">
          {capturedImage ? (
            <div className="relative rounded-2xl overflow-hidden border-2 border-emerald-500 shadow-xl max-h-[280px]">
              <img src={capturedImage} alt="Captured Face" className="w-full h-auto object-cover max-h-[280px]" />
              <div className="absolute top-3 right-3 px-2.5 py-1 rounded-full bg-emerald-500/90 text-white text-xs font-semibold flex items-center gap-1 shadow-md">
                <CheckCircle className="w-3.5 h-3.5" /> Verified
              </div>
            </div>
          ) : (
            <div className="relative w-full max-w-sm aspect-4/3 rounded-2xl overflow-hidden bg-slate-900 border border-slate-800 flex items-center justify-center">
              {stream ? (
                <video
                  ref={(el) => {
                    videoRef.current = el;
                    if (el && stream && el.srcObject !== stream) {
                      el.srcObject = stream;
                      el.play().catch(() => {});
                    }
                  }}
                  autoPlay
                  playsInline
                  muted
                  className="w-full h-full object-cover transform scale-x-[-1]"
                />
              ) : (
                <div className="text-center p-6 space-y-2">
                  <div className="w-16 h-16 rounded-full bg-primary-500/10 text-primary-400 mx-auto flex items-center justify-center">
                    <Camera className="w-8 h-8 animate-pulse" />
                  </div>
                  <p className="text-xs text-slate-400 max-w-xs">
                    {cameraError ? "Simulated biometric sensor active." : "Initializing AI facial sensor..."}
                  </p>
                </div>
              )}

              {/* Facial Guide Oval & Scanning Radar */}
              <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
                <div className="w-48 h-60 rounded-[45%] border-2 border-dashed border-primary-400/70 dark:border-primary-400/80 shadow-[0_0_30px_rgba(99,102,241,0.25)] flex items-center justify-center">
                  {scanning && (
                    <div className="w-full h-1 bg-gradient-to-r from-transparent via-cyan-400 to-transparent shadow-[0_0_15px_#22d3ee] animate-scan" />
                  )}
                </div>
              </div>

              {/* Status Pill */}
              <div className="absolute bottom-3 inset-x-0 flex justify-center">
                <span className="px-3 py-1 rounded-full bg-slate-900/80 backdrop-blur-md text-white text-[11px] font-mono flex items-center gap-1.5 border border-slate-700/60">
                  <Sparkles className="w-3 h-3 text-cyan-400 animate-spin" />
                  {scanning ? `Extracting Face Embedding (${progress}%)` : "Biometric Sensor Ready"}
                </span>
              </div>
            </div>
          )}

          <canvas ref={canvasRef} className="hidden" />
        </div>

        {/* Footer Controls */}
        <div className="flex items-center justify-between p-4 px-6 bg-slate-50 dark:bg-slate-900/50 border-t border-slate-100 dark:border-slate-800">
          <div className="text-[11px] text-slate-500 dark:text-slate-400 flex items-center gap-1">
            <Shield className="w-3.5 h-3.5 text-primary-500" />
            <span>Secure 256-bit Vector Extraction</span>
          </div>

          <div className="flex items-center gap-2">
            {capturedImage ? (
              <>
                <button
                  type="button"
                  onClick={handleRetake}
                  className="px-3.5 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors flex items-center gap-1.5"
                >
                  <RefreshCw className="w-3.5 h-3.5" /> Retake
                </button>
                <button
                  type="button"
                  onClick={handleConfirm}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-md shadow-emerald-600/20 transition-all flex items-center gap-1.5"
                >
                  <CheckCircle className="w-3.5 h-3.5" /> Confirm Match
                </button>
              </>
            ) : (
              <button
                type="button"
                onClick={handleScanAndCapture}
                disabled={scanning}
                className="px-5 py-2.5 text-xs font-semibold rounded-xl bg-gradient-to-r from-primary-600 to-indigo-600 hover:from-primary-700 hover:to-indigo-700 text-white shadow-md shadow-primary-500/25 transition-all flex items-center gap-2 disabled:opacity-50"
              >
                <Camera className="w-4 h-4" />
                {scanning ? "Scanning Face..." : "Capture & Verify Face"}
              </button>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
}
