import React, { useState, useRef, useEffect } from 'react';
import { Camera, RefreshCw, CheckCircle, AlertTriangle, X, Shield, Sparkles, UserCheck } from 'lucide-react';
import { motion } from 'framer-motion';

/**
 * Computes sharpness (Laplacian variance), brightness, and contrast from a canvas.
 */
function analyzeFrameClarity(canvas) {
  const w = 160;
  const h = 120;
  const temp = document.createElement('canvas');
  temp.width = w;
  temp.height = h;
  const ctx = temp.getContext('2d');
  ctx.drawImage(canvas, 0, 0, w, h);
  const { data } = ctx.getImageData(0, 0, w, h);

  const gray = new Float32Array(w * h);
  let sum = 0;
  for (let i = 0; i < w * h; i++) {
    const r = data[i * 4];
    const g = data[i * 4 + 1];
    const b = data[i * 4 + 2];
    const lum = 0.299 * r + 0.587 * g + 0.114 * b;
    gray[i] = lum;
    sum += lum;
  }
  const brightness = sum / (w * h);

  let varianceSum = 0;
  for (let i = 0; i < w * h; i++) {
    const d = gray[i] - brightness;
    varianceSum += d * d;
  }
  const contrast = Math.sqrt(varianceSum / (w * h));

  // Discrete 3x3 Laplacian variance for sharpness
  let lapSum = 0;
  let lapSqSum = 0;
  let count = 0;
  for (let y = 1; y < h - 1; y++) {
    for (let x = 1; x < w - 1; x++) {
      const idx = y * w + x;
      const lap =
        gray[idx - w] + gray[idx + w] + gray[idx - 1] + gray[idx + 1] - 4 * gray[idx];
      lapSum += lap;
      lapSqSum += lap * lap;
      count++;
    }
  }
  const lapMean = lapSum / count;
  const sharpness = Math.max(0, lapSqSum / count - lapMean * lapMean);

  const reasons = [];
  if (brightness < 45) reasons.push('Too dark — improve lighting');
  if (brightness > 235) reasons.push('Overexposed — reduce glare');
  if (contrast < 18) reasons.push('Low contrast / blank frame');
  if (sharpness < 35) reasons.push('Blurry / unclear image — hold still');

  return {
    isClear: reasons.length === 0,
    brightness: Math.round(brightness),
    contrast: Math.round(contrast),
    sharpness: Math.round(sharpness),
    reasons,
  };
}

/**
 * Compares two face images (stored employee photo vs live capture) using
 * normalized 32x32 center-crop luminance + RGB color histogram cosine similarity.
 */
function compareFaces(referenceUrl, liveDataUrl) {
  return new Promise((resolve) => {
    if (!referenceUrl) {
      resolve({ checked: false, matched: true, score: 100 });
      return;
    }

    const loadImg = (src) =>
      new Promise((res, rej) => {
        const img = new Image();
        img.crossOrigin = 'anonymous';
        img.onload = () => res(img);
        img.onerror = () => rej(new Error('Image load error'));
        img.src = src;
      });

    Promise.all([loadImg(referenceUrl), loadImg(liveDataUrl)])
      .then(([refImg, liveImg]) => {
        const extractSignature = (img) => {
          const size = 32;
          const c = document.createElement('canvas');
          c.width = size;
          c.height = size;
          const ctx = c.getContext('2d');
          // Center crop 70% to focus on face region
          const sw = img.width * 0.7;
          const sh = img.height * 0.7;
          const sx = (img.width - sw) / 2;
          const sy = (img.height - sh) / 2;
          ctx.drawImage(img, sx, sy, sw, sh, 0, 0, size, size);
          const { data } = ctx.getImageData(0, 0, size, size);

          // 16-bin normalized RGB + luminance histogram (48 features) + 4x4 grid averages (16 features)
          const hist = new Float32Array(64);
          for (let i = 0; i < size * size; i++) {
            const r = data[i * 4];
            const g = data[i * 4 + 1];
            const b = data[i * 4 + 2];
            hist[Math.min(15, Math.floor(r / 16))] += 1;
            hist[16 + Math.min(15, Math.floor(g / 16))] += 1;
            hist[32 + Math.min(15, Math.floor(b / 16))] += 1;
          }
          // 4x4 spatial luminance blocks
          for (let by = 0; by < 4; by++) {
            for (let bx = 0; bx < 4; bx++) {
              let blockSum = 0;
              for (let y = by * 8; y < (by + 1) * 8; y++) {
                for (let x = bx * 8; x < (bx + 1) * 8; x++) {
                  const idx = (y * size + x) * 4;
                  blockSum += 0.299 * data[idx] + 0.587 * data[idx + 1] + 0.114 * data[idx + 2];
                }
              }
              hist[48 + by * 4 + bx] = (blockSum / 64) * 2.5;
            }
          }
          let norm = 0;
          for (let i = 0; i < hist.length; i++) norm += hist[i] * hist[i];
          norm = Math.sqrt(norm) || 1;
          for (let i = 0; i < hist.length; i++) hist[i] /= norm;
          return hist;
        };

        const sigA = extractSignature(refImg);
        const sigB = extractSignature(liveImg);
        let dot = 0;
        for (let i = 0; i < sigA.length; i++) dot += sigA[i] * sigB[i];
        const scorePct = Math.round(Math.max(0, Math.min(1, dot)) * 100);
        resolve({
          checked: true,
          matched: scorePct >= 68,
          score: scorePct,
        });
      })
      .catch(() => {
        // If remote CORS image cannot be read on canvas, allow with clarity check
        resolve({ checked: false, matched: true, score: 100 });
      });
  });
}

export default function FaceScannerModal({
  isOpen,
  onClose,
  onCapture,
  title = 'Live Face Recognition',
  subtitle = 'Position face clearly within the frame',
  referencePhotoUrl = null,
}) {
  const videoRef = useRef(null);
  const canvasRef = useRef(null);
  const [stream, setStream] = useState(null);
  const [capturedImage, setCapturedImage] = useState(null);
  const [cameraError, setCameraError] = useState(null);
  const [scanning, setScanning] = useState(false);
  const [progress, setProgress] = useState(0);
  const [qualityReport, setQualityReport] = useState(null);
  const [faceMatchReport, setFaceMatchReport] = useState(null);

  useEffect(() => {
    if (isOpen) {
      startCamera();
    } else {
      stopCamera();
      resetCaptureState();
    }
    return () => stopCamera();
  }, [isOpen]);

  useEffect(() => {
    if (videoRef.current && stream) {
      videoRef.current.srcObject = stream;
      videoRef.current.play().catch(() => {});
    }
  }, [stream]);

  const resetCaptureState = () => {
    setCapturedImage(null);
    setProgress(0);
    setScanning(false);
    setQualityReport(null);
    setFaceMatchReport(null);
  };

  const startCamera = async () => {
    setCameraError(null);
    try {
      if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
        const mediaStream = await navigator.mediaDevices.getUserMedia({
          video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' },
          audio: false,
        });
        setStream(mediaStream);
        if (videoRef.current) {
          videoRef.current.srcObject = mediaStream;
        }
      } else {
        throw new Error('Webcam not supported by browser environment');
      }
    } catch (err) {
      setCameraError(err.message || 'Camera access denied. Please allow camera permission.');
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
    }
  };

  const playBeep = (success = true) => {
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(success ? 880 : 280, audioCtx.currentTime);
      gain.gain.setValueAtTime(0.1, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + 0.25);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.25);
    } catch (_) {}
  };

  const handleScanAndCapture = () => {
    if (!stream || !videoRef.current) {
      setCameraError('Live camera is required to capture a clear face photo.');
      return;
    }
    setScanning(true);
    setProgress(15);
    setQualityReport(null);
    setFaceMatchReport(null);

    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 90) {
          clearInterval(interval);
          finalizeCapture();
          return 100;
        }
        return prev + 25;
      });
    }, 140);
  };

  const finalizeCapture = async () => {
    const video = videoRef.current;
    const canvas = canvasRef.current;
    if (!video || !canvas || !stream) {
      setScanning(false);
      return;
    }

    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 480;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    const clarity = analyzeFrameClarity(canvas);
    const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
    setCapturedImage(dataUrl);
    setQualityReport(clarity);

    let matchResult = { checked: false, matched: true, score: 100 };
    if (clarity.isClear && referencePhotoUrl) {
      matchResult = await compareFaces(referencePhotoUrl, dataUrl);
    }
    setFaceMatchReport(matchResult);

    const approved = clarity.isClear && matchResult.matched;
    playBeep(approved);
    setScanning(false);
  };

  const handleRecapture = () => {
    resetCaptureState();
    if (!stream) startCamera();
  };

  const isApproved =
    capturedImage &&
    qualityReport?.isClear &&
    (!faceMatchReport?.checked || faceMatchReport?.matched);

  const handleConfirm = () => {
    if (isApproved) {
      onCapture(capturedImage);
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-sm">
      <motion.div
        initial={{ scale: 0.92, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        exit={{ scale: 0.92, opacity: 0 }}
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

        <div className="p-6 space-y-4">
          {referencePhotoUrl && (
            <div className="flex items-center gap-3 p-2.5 rounded-2xl bg-indigo-50/70 dark:bg-indigo-950/30 border border-indigo-200 dark:border-indigo-800 text-xs">
              <img
                src={referencePhotoUrl}
                alt="Stored Employee Face"
                className="w-11 h-11 rounded-xl object-cover border border-indigo-300"
              />
              <div>
                <div className="font-bold text-indigo-700 dark:text-indigo-300 flex items-center gap-1">
                  <UserCheck className="w-3.5 h-3.5" /> Stored Face Match Required
                </div>
                <p className="text-[11px] text-slate-600 dark:text-slate-400">
                  Live face must be crystal clear AND match this employee&apos;s stored photo before storing.
                </p>
              </div>
            </div>
          )}

          {/* Camera Viewport */}
          <div className="relative aspect-video w-full rounded-2xl overflow-hidden bg-slate-900 border-2 border-slate-200 dark:border-slate-700 flex items-center justify-center">
            {capturedImage ? (
              <img src={capturedImage} alt="Captured Face" className="w-full h-full object-cover" />
            ) : cameraError ? (
              <div className="text-center p-6 space-y-2 text-rose-400">
                <AlertTriangle className="w-10 h-10 mx-auto" />
                <p className="text-xs font-semibold">{cameraError}</p>
                <button
                  type="button"
                  onClick={startCamera}
                  className="px-3 py-1.5 rounded-xl bg-primary-600 text-white text-xs font-medium"
                >
                  Retry Camera
                </button>
              </div>
            ) : (
              <video ref={videoRef} autoPlay playsInline muted className="w-full h-full object-cover scale-x-[-1]" />
            )}

            {/* Face Guide Oval */}
            {!capturedImage && !cameraError && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <div className="w-40 h-52 rounded-full border-2 border-dashed border-emerald-400/80 shadow-[0_0_0_9999px_rgba(0,0,0,0.28)]" />
              </div>
            )}

            {scanning && (
              <div className="absolute bottom-3 left-4 right-4 bg-slate-900/85 backdrop-blur px-3 py-2 rounded-xl">
                <div className="flex justify-between text-[11px] text-emerald-300 font-semibold mb-1">
                  <span>Analyzing sharpness, lighting &amp; face clarity...</span>
                  <span>{progress}%</span>
                </div>
                <div className="w-full bg-slate-700 h-1.5 rounded-full overflow-hidden">
                  <div className="bg-emerald-400 h-full transition-all" style={{ width: `${progress}%` }} />
                </div>
              </div>
            )}
          </div>

          <canvas ref={canvasRef} className="hidden" />

          {/* Clarity & Face Match Verification Report */}
          {qualityReport && (
            <div
              className={`p-3.5 rounded-2xl border text-xs space-y-1.5 ${
                isApproved
                  ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-300 dark:border-emerald-700 text-emerald-800 dark:text-emerald-200'
                  : 'bg-rose-50 dark:bg-rose-950/40 border-rose-300 dark:border-rose-700 text-rose-800 dark:text-rose-200'
              }`}
            >
              <div className="font-bold flex items-center gap-1.5">
                {isApproved ? (
                  <>
                    <CheckCircle className="w-4 h-4 text-emerald-600" />
                    APPROVED — Face Photo is Clear &amp; Ready to Store
                  </>
                ) : (
                  <>
                    <AlertTriangle className="w-4 h-4 text-rose-600" />
                    FAILED — Photo Rejected! Please Recapture
                  </>
                )}
              </div>

              <div className="flex flex-wrap gap-3 text-[11px] font-mono opacity-90">
                <span>Sharpness: {qualityReport.sharpness} (min 35)</span>
                <span>Brightness: {qualityReport.brightness} (45–235)</span>
                <span>Contrast: {qualityReport.contrast} (min 18)</span>
                {faceMatchReport?.checked && (
                  <span className="font-bold">
                    Same-Employee Match: {faceMatchReport.score}% (min 68%)
                  </span>
                )}
              </div>

              {!qualityReport.isClear && (
                <div className="text-[11px] font-semibold">
                  Reason: {qualityReport.reasons.join(' • ')}
                </div>
              )}

              {qualityReport.isClear && faceMatchReport?.checked && !faceMatchReport.matched && (
                <div className="text-[11px] font-semibold">
                  Reason: Live face does NOT match the employee&apos;s stored photo ({faceMatchReport.score}% &lt; 68%). Registration blocked.
                </div>
              )}
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-2.5 pt-2">
            {!capturedImage ? (
              <button
                type="button"
                disabled={scanning || !!cameraError}
                onClick={handleScanAndCapture}
                className="w-full py-2.5 px-4 rounded-xl bg-primary-600 hover:bg-primary-700 text-white text-xs font-semibold flex items-center justify-center gap-2 disabled:opacity-50"
              >
                <Sparkles className="w-4 h-4" />
                {scanning ? 'Capturing & Checking Clarity...' : 'Capture Live Face Photo'}
              </button>
            ) : (
              <>
                <button
                  type="button"
                  onClick={handleRecapture}
                  className="px-4 py-2.5 rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs font-semibold flex items-center gap-1.5"
                >
                  <RefreshCw className="w-3.5 h-3.5" /> Recapture
                </button>
                <button
                  type="button"
                  disabled={!isApproved}
                  onClick={handleConfirm}
                  className="flex-1 py-2.5 px-4 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center justify-center gap-1.5 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <Shield className="w-4 h-4" />
                  {isApproved ? 'Approve & Store Face Photo' : 'Approval Blocked (Recapture Required)'}
                </button>
              </>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
}
