package com.company.smartbiometric.biometric;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Hardware Biometric Helper for POCO M2 Pro (Side-mounted fingerprint sensor).
 *
 * IMPORTANT HARDWARE & ANDROID LIMITATION:
 * Android BiometricPrompt NEVER reveals which enrolled fingerprint belongs to which employee.
 * It only returns cryptographic success/failure against fingers enrolled in Android OS Settings.
 * Therefore, individual employee identity MUST be verified via Employee ID + 1:1 Front-Camera
 * Face Verification with Liveness Detection (CameraX + ML Kit). BiometricPrompt serves strictly
 * as an optional device-level hardware presence confirmation layer.
 */
public class BiometricCapabilityHelper {

    public interface Callback {
        void onBiometricSuccess();
        void onBiometricFallbackOrUnavailable(String reason);
        void onBiometricCancelled(String reason);
    }

    public static boolean isStrongFingerprintAvailable(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int status = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        return status == BiometricManager.BIOMETRIC_SUCCESS;
    }

    public static String getCapabilitySummary(Context context) {
        BiometricManager manager = BiometricManager.from(context);
        int strong = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
        int weak = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK);

        String fpStatus;
        if (strong == BiometricManager.BIOMETRIC_SUCCESS) {
            fpStatus = "Fingerprint Ready (OS-level only)";
        } else if (strong == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            fpStatus = "Fingerprint Sensor Present (No OS finger enrolled)";
        } else {
            fpStatus = "Fingerprint Unavailable";
        }

        String faceNote = (weak == BiometricManager.BIOMETRIC_SUCCESS)
                ? "CameraX 1:1 Face + Liveness Active"
                : "CameraX 1:1 Face + Liveness Active";

        return fpStatus + " • " + faceNote;
    }

    public static void authenticateWithFingerprint(
            FragmentActivity activity,
            String title,
            String subtitle,
            Callback callback) {

        if (!isStrongFingerprintAvailable(activity)) {
            callback.onBiometricFallbackOrUnavailable("Device fingerprint not enrolled in Android Settings");
            return;
        }

        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onBiometricSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                                || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                            callback.onBiometricCancelled(errString.toString());
                        } else {
                            callback.onBiometricFallbackOrUnavailable(errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // Let user retry inside BiometricPrompt dialog
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setDescription("Device-level fingerprint check. Individual identity is verified via Front Camera Face + Liveness.")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .setNegativeButtonText("Use Face Only")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
