package com.company.smartbiometric.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.company.smartbiometric.R;
import com.company.smartbiometric.biometric.BiometricCapabilityHelper;
import com.company.smartbiometric.config.TerminalPrefs;
import com.company.smartbiometric.gesture.AttendanceTapController;
import com.company.smartbiometric.model.ApiModels;
import com.company.smartbiometric.network.ApiClient;
import com.company.smartbiometric.network.AttendanceApiService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Shared POCO M2 Pro Attendance Kiosk Terminal Activity.
 * - Exactly 1 tap => CHECK-IN
 * - 4 consecutive quick taps => CHECK-OUT
 * - Individual identity verified via Employee Code + 1:1 CameraX Front-Camera Face + Blink Liveness
 * - Optional device-level BiometricPrompt confirmation (explicitly does not identify individuals)
 */
public class MainKioskActivity extends AppCompatActivity {

    private TextView tvCompanyName;
    private TextView tvTerminalSubtitle;
    private TextView tvLiveTime;
    private TextView tvLiveDate;
    private TextView tvHardwareStatus;
    private Spinner spinnerEmployees;
    private EditText etEmployeeCode;
    private TextView tvSelectedEmployeeStatus;
    private Button btnAttendanceAction;
    private TextView tvTapFeedback;
    private LinearLayout cardResultBanner;
    private TextView tvVerificationStatusTitle;
    private TextView tvVerificationMessage;

    private TerminalPrefs prefs;
    private AttendanceTapController tapController;
    private final List<ApiModels.TerminalEmployee> employeeList = new ArrayList<>();
    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private boolean pendingBiometricPromptUsed = false;
    private String pendingAction = null;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault());

    private final Runnable clockRunnable = new Runnable() {
        @Override
        public void run() {
            Date now = new Date();
            tvLiveTime.setText(timeFormat.format(now));
            tvLiveDate.setText(dateFormat.format(now));
            clockHandler.postDelayed(this, 1000);
        }
    };

    private final ActivityResultLauncher<Intent> faceVerifyLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double[] vector = data.getDoubleArrayExtra(FaceVerificationActivity.RESULT_FACE_VECTOR);
                    boolean livenessPassed = data.getBooleanExtra(FaceVerificationActivity.RESULT_LIVENESS_PASSED, false);
                    String challenge = data.getStringExtra(FaceVerificationActivity.RESULT_LIVENESS_CHALLENGE);
                    String empCode = data.getStringExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_CODE);
                    String action = data.getStringExtra(FaceVerificationActivity.EXTRA_ACTION_TYPE);

                    submitPunchToServer(empCode, action, vector, livenessPassed, challenge, pendingBiometricPromptUsed);
                } else {
                    tapController.setLocked(false);
                    tvTapFeedback.setText("Verification Cancelled • Ready");
                    showStatusBanner("Verification Cancelled", "Face verification was cancelled before completion.", false, true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        enableImmersiveKioskMode();
        setContentView(R.layout.activity_main_kiosk);

        prefs = new TerminalPrefs(this);

        tvCompanyName = findViewById(R.id.tvCompanyName);
        tvTerminalSubtitle = findViewById(R.id.tvTerminalSubtitle);
        tvLiveTime = findViewById(R.id.tvLiveTime);
        tvLiveDate = findViewById(R.id.tvLiveDate);
        tvHardwareStatus = findViewById(R.id.tvHardwareStatus);
        spinnerEmployees = findViewById(R.id.spinnerEmployees);
        etEmployeeCode = findViewById(R.id.etEmployeeCode);
        tvSelectedEmployeeStatus = findViewById(R.id.tvSelectedEmployeeStatus);
        btnAttendanceAction = findViewById(R.id.btnAttendanceAction);
        tvTapFeedback = findViewById(R.id.tvTapFeedback);
        cardResultBanner = findViewById(R.id.cardResultBanner);
        tvVerificationStatusTitle = findViewById(R.id.tvVerificationStatusTitle);
        tvVerificationMessage = findViewById(R.id.tvVerificationMessage);

        Button btnAdminLogin = findViewById(R.id.btnAdminLogin);
        Button btnRefresh = findViewById(R.id.btnRefreshEmployees);

        tvHardwareStatus.setText(BiometricCapabilityHelper.getCapabilitySummary(this));

        setupTapController();

        btnAttendanceAction.setOnClickListener(v -> tapController.onButtonTap());
        btnRefresh.setOnClickListener(v -> {
            loadTerminalConfig();
            loadEmployees();
        });
        btnAdminLogin.setOnClickListener(v -> startActivity(new Intent(this, AdminLoginActivity.class)));

        spinnerEmployees.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < employeeList.size()) {
                    ApiModels.TerminalEmployee emp = employeeList.get(position);
                    etEmployeeCode.setText(emp.employeeCode);
                    String faceState = Boolean.TRUE.equals(emp.faceEnrolled) ? "Face Enrolled" : "Face NOT Enrolled (Ask Admin)";
                    tvSelectedEmployeeStatus.setText("Status: " + emp.todayStatus + " • " + faceState);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupTapController() {
        tapController = new AttendanceTapController(prefs.getTapTimeoutMs(), new AttendanceTapController.Listener() {
            @Override
            public void onTapProgress(int currentCount) {
                if (currentCount == 1) {
                    tvTapFeedback.setText("1 Tap detected... (Hold for CHECK-IN, or tap 3 more times for CHECK-OUT)");
                } else if (currentCount < 4) {
                    tvTapFeedback.setText("Tap " + currentCount + " of 4 for CHECK-OUT...");
                }
            }

            @Override
            public void onCheckInTriggered() {
                tvTapFeedback.setText("1-Tap CHECK-IN Initiated");
                initiateVerificationFlow("CHECK_IN");
            }

            @Override
            public void onCheckOutTriggered() {
                tvTapFeedback.setText("4-Tap CHECK-OUT Initiated");
                initiateVerificationFlow("CHECK_OUT");
            }

            @Override
            public void onInvalidTapCount(int count) {
                tvTapFeedback.setText("Ignored " + count + " taps • Use 1 Tap (Check-In) or 4 Quick Taps (Check-Out)");
                showStatusBanner("Ambiguous Tap Count (" + count + ")",
                        "To prevent accidental attendance, use exactly 1 TAP for Check-In or 4 QUICK TAPS for Check-Out.",
                        false, true);
            }
        });
    }

    private void initiateVerificationFlow(String action) {
        String empCode = etEmployeeCode.getText().toString().trim().toUpperCase();
        if (empCode.isEmpty()) {
            tapController.setLocked(false);
            showStatusBanner("Employee ID Required", "Please select or enter your Employee Code first.", false, false);
            return;
        }

        ApiModels.TerminalEmployee matched = null;
        for (ApiModels.TerminalEmployee e : employeeList) {
            if (empCode.equalsIgnoreCase(e.employeeCode)) {
                matched = e;
                break;
            }
        }

        if (matched != null && !Boolean.TRUE.equals(matched.faceEnrolled)) {
            tapController.setLocked(false);
            showStatusBanner("Face Enrollment Required",
                    "Employee " + empCode + " does not have a face template enrolled yet. Please ask Admin to enroll with biometric consent.",
                    false, false);
            return;
        }

        pendingAction = action;
        pendingBiometricPromptUsed = false;
        String empName = matched != null ? matched.fullName : empCode;

        // Layer 1 (Optional Device Presence Check): Android BiometricPrompt if enabled & available
        if (prefs.isBiometricPromptEnabled() && BiometricCapabilityHelper.isStrongFingerprintAvailable(this)) {
            BiometricCapabilityHelper.authenticateWithFingerprint(
                    this,
                    action.equals("CHECK_IN") ? "Step 1: Fingerprint Check (Check-In)" : "Step 1: Fingerprint Check (Check-Out)",
                    "Employee: " + empCode + " • Next step: Front Camera Face Verification",
                    new BiometricCapabilityHelper.Callback() {
                        @Override
                        public void onBiometricSuccess() {
                            pendingBiometricPromptUsed = true;
                            launchFrontCameraVerification(empCode, empName, action);
                        }

                        @Override
                        public void onBiometricFallbackOrUnavailable(String reason) {
                            pendingBiometricPromptUsed = false;
                            launchFrontCameraVerification(empCode, empName, action);
                        }

                        @Override
                        public void onBiometricCancelled(String reason) {
                            // Allow proceeding directly to mandatory 1:1 Face Verification
                            pendingBiometricPromptUsed = false;
                            launchFrontCameraVerification(empCode, empName, action);
                        }
                    });
        } else {
            launchFrontCameraVerification(empCode, empName, action);
        }
    }

    private void launchFrontCameraVerification(String empCode, String empName, String action) {
        Intent intent = new Intent(this, FaceVerificationActivity.class);
        intent.putExtra(FaceVerificationActivity.EXTRA_MODE, "VERIFY");
        intent.putExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_CODE, empCode);
        intent.putExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_NAME, empName);
        intent.putExtra(FaceVerificationActivity.EXTRA_ACTION_TYPE, action);
        faceVerifyLauncher.launch(intent);
    }

    private void submitPunchToServer(
            String empCode, String action, double[] vector,
            boolean livenessPassed, String livenessChallenge, boolean biometricPromptUsed) {

        showStatusBanner("Verifying with Server...", "Matching encrypted 1:1 face template & recording server timestamp...", false, true);

        ApiModels.PunchRequest req = new ApiModels.PunchRequest();
        req.employeeCode = empCode;
        req.action = action;
        req.faceVector = vector;
        req.livenessPassed = livenessPassed;
        req.livenessScore = livenessPassed ? 0.96 : 0.0;
        req.livenessChallenge = livenessChallenge;
        req.biometricPromptUsed = biometricPromptUsed;
        req.verificationMethod = biometricPromptUsed ? "FACE_AND_BIOMETRIC_PROMPT" : "FACE_LIVENESS";
        req.nonce = UUID.randomUUID().toString();
        req.clientTimestampMs = System.currentTimeMillis();

        AttendanceApiService api = ApiClient.create(this);
        api.punchAttendance(req).enqueue(new Callback<ApiModels.ApiResponse<ApiModels.PunchResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<ApiModels.PunchResponse>> call,
                                   @NonNull Response<ApiModels.ApiResponse<ApiModels.PunchResponse>> response) {
                tapController.setLocked(false);
                tvTapFeedback.setText("Ready for Next Employee");

                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    ApiModels.PunchResponse data = response.body().data;
                    String pct = data.matchScore != null ? String.format(Locale.US, "%.1f%%", data.matchScore * 100.0) : "OK";
                    String details = data.employeeName + " (" + data.employeeCode + ")\n"
                            + data.message + "\n"
                            + "Server Time: " + data.serverTimestamp + " • Face Match: " + pct;
                    showStatusBanner("SUCCESS: " + data.action, details, true, false);
                    loadEmployees();
                } else {
                    String errMsg = "Attendance rejected by server (HTTP " + response.code() + ")";
                    try {
                        if (response.errorBody() != null) {
                            String raw = response.errorBody().string();
                            if (raw.contains("\"message\"")) {
                                int start = raw.indexOf("\"message\":\"") + 11;
                                int end = raw.indexOf("\"", start);
                                if (start > 10 && end > start) {
                                    errMsg = raw.substring(start, end);
                                }
                            }
                        }
                    } catch (Exception ignored) {}
                    showStatusBanner("VERIFICATION FAILED", errMsg, false, false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<ApiModels.PunchResponse>> call, @NonNull Throwable t) {
                tapController.setLocked(false);
                tvTapFeedback.setText("Network Error • Ready");
                showStatusBanner("SERVER UNREACHABLE",
                        "Could not reach Spring Boot server (" + prefs.getBaseUrl() + "): " + t.getMessage(),
                        false, false);
            }
        });
    }

    private void loadTerminalConfig() {
        AttendanceApiService api = ApiClient.create(this);
        api.getTerminalConfig().enqueue(new Callback<ApiModels.ApiResponse<ApiModels.TerminalConfig>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<ApiModels.TerminalConfig>> call,
                                   @NonNull Response<ApiModels.ApiResponse<ApiModels.TerminalConfig>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiModels.TerminalConfig cfg = response.body().data;
                    tvCompanyName.setText(cfg.companyName != null ? cfg.companyName : "Smart Attendance");
                    tvTerminalSubtitle.setText(cfg.terminalCode + " • " + cfg.deviceModel);
                    if (cfg.tapTimeoutMs != null) {
                        prefs.setTapTimeoutMs(cfg.tapTimeoutMs);
                        tapController.setTapTimeoutMs(cfg.tapTimeoutMs);
                    }
                    if (cfg.biometricPromptEnabled != null) {
                        prefs.setBiometricPromptEnabled(cfg.biometricPromptEnabled);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<ApiModels.TerminalConfig>> call, @NonNull Throwable t) {
                tvTerminalSubtitle.setText("Offline • Tap Admin to set Server URL");
            }
        });
    }

    private void loadEmployees() {
        AttendanceApiService api = ApiClient.create(this);
        api.getTerminalEmployees().enqueue(new Callback<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> call,
                                   @NonNull Response<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    employeeList.clear();
                    for (ApiModels.TerminalEmployee emp : response.body().data) {
                        if (Boolean.TRUE.equals(emp.isActive)) {
                            employeeList.add(emp);
                        }
                    }
                    ArrayAdapter<ApiModels.TerminalEmployee> adapter = new ArrayAdapter<>(
                            MainKioskActivity.this,
                            android.R.layout.simple_spinner_dropdown_item,
                            employeeList);
                    spinnerEmployees.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> call, @NonNull Throwable t) {
                tvSelectedEmployeeStatus.setText("Cannot sync employees from " + prefs.getBaseUrl());
            }
        });
    }

    private void showStatusBanner(String title, String message, boolean isSuccess, boolean isNeutral) {
        tvVerificationStatusTitle.setText(title);
        tvVerificationMessage.setText(message);
        if (isNeutral) {
            cardResultBanner.setBackgroundColor(Color.parseColor("#1E293B"));
            tvVerificationStatusTitle.setTextColor(Color.parseColor("#FCD34D"));
        } else if (isSuccess) {
            cardResultBanner.setBackgroundColor(Color.parseColor("#064E3B"));
            tvVerificationStatusTitle.setTextColor(Color.parseColor("#34D399"));
        } else {
            cardResultBanner.setBackgroundColor(Color.parseColor("#7F1D1D"));
            tvVerificationStatusTitle.setTextColor(Color.parseColor("#FCA5A5"));
        }
    }

    private void enableImmersiveKioskMode() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableImmersiveKioskMode();
        clockHandler.post(clockRunnable);
        tapController.setTapTimeoutMs(prefs.getTapTimeoutMs());
        loadTerminalConfig();
        loadEmployees();
    }

    @Override
    protected void onPause() {
        super.onPause();
        clockHandler.removeCallbacks(clockRunnable);
    }
}
