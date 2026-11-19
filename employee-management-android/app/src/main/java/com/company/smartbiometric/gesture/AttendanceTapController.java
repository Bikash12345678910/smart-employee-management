package com.company.smartbiometric.gesture;

import android.os.Handler;
import android.os.Looper;

/**
 * Deterministic Multi-Tap Controller for Shared Attendance Kiosk:
 * - Exactly 1 tap (settled after timeout window) => CHECK_IN
 * - Exactly 4 consecutive quick taps within timeout window => CHECK_OUT (triggers immediately on 4th tap)
 * - 2 or 3 taps => Rejected as ambiguous with clear user guidance (prevents accidental check-ins/check-outs)
 * - Locked during active biometric/API verification to prevent accidental duplicates.
 */
public class AttendanceTapController {

    public interface Listener {
        void onTapProgress(int currentCount);
        void onCheckInTriggered();
        void onCheckOutTriggered();
        void onInvalidTapCount(int count);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Listener listener;
    private int tapTimeoutMs;
    private int tapCount = 0;
    private boolean locked = false;

    private final Runnable evaluateRunnable = new Runnable() {
        @Override
        public void run() {
            int settledCount = tapCount;
            tapCount = 0;
            if (locked) {
                return;
            }
            if (settledCount == 1) {
                locked = true;
                listener.onCheckInTriggered();
            } else if (settledCount == 2 || settledCount == 3) {
                listener.onInvalidTapCount(settledCount);
            }
        }
    };

    public AttendanceTapController(int tapTimeoutMs, Listener listener) {
        this.tapTimeoutMs = Math.max(600, tapTimeoutMs);
        this.listener = listener;
    }

    public void setTapTimeoutMs(int tapTimeoutMs) {
        this.tapTimeoutMs = Math.max(600, tapTimeoutMs);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (locked) {
            handler.removeCallbacks(evaluateRunnable);
            tapCount = 0;
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void onButtonTap() {
        if (locked) {
            return;
        }

        handler.removeCallbacks(evaluateRunnable);
        tapCount++;
        listener.onTapProgress(tapCount);

        if (tapCount >= 4) {
            // Immediately trigger CHECK-OUT on 4th consecutive quick tap
            tapCount = 0;
            locked = true;
            listener.onCheckOutTriggered();
            return;
        }

        // Wait for configurable tap timeout window before finalizing 1-tap CHECK-IN
        handler.postDelayed(evaluateRunnable, tapTimeoutMs);
    }

    public void reset() {
        handler.removeCallbacks(evaluateRunnable);
        tapCount = 0;
        locked = false;
    }
}
