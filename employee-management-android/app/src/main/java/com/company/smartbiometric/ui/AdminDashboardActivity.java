package com.company.smartbiometric.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.company.smartbiometric.R;
import com.company.smartbiometric.config.TerminalPrefs;
import com.company.smartbiometric.model.ApiModels;
import com.company.smartbiometric.network.ApiClient;
import com.company.smartbiometric.network.AttendanceApiService;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {

    private TextView tvReportDate;
    private TextView tvStatPresent;
    private TextView tvStatAbsent;
    private TextView tvStatLate;
    private TextView tvStatOut;
    private RecyclerView rvAdminEmployees;

    private TerminalPrefs prefs;
    private final List<ApiModels.TerminalEmployee> employees = new ArrayList<>();
    private EmployeeAdapter adapter;

    private final ActivityResultLauncher<Intent> faceEnrollLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    double[] vector = data.getDoubleArrayExtra(FaceVerificationActivity.RESULT_FACE_VECTOR);
                    double quality = data.getDoubleExtra(FaceVerificationActivity.RESULT_QUALITY_SCORE, 0.92);
                    String empCode = data.getStringExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_CODE);
                    uploadFaceEnrollment(empCode, vector, quality);
                } else {
                    Toast.makeText(this, "Face enrollment cancelled.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        prefs = new TerminalPrefs(this);

        tvReportDate = findViewById(R.id.tvReportDate);
        tvStatPresent = findViewById(R.id.tvStatPresent);
        tvStatAbsent = findViewById(R.id.tvStatAbsent);
        tvStatLate = findViewById(R.id.tvStatLate);
        tvStatOut = findViewById(R.id.tvStatOut);
        rvAdminEmployees = findViewById(R.id.rvAdminEmployees);

        Button btnRefresh = findViewById(R.id.btnRefreshAdmin);
        Button btnExportCsv = findViewById(R.id.btnExportCsv);
        Button btnSettings = findViewById(R.id.btnTerminalSettings);
        Button btnLogout = findViewById(R.id.btnAdminLogout);

        adapter = new EmployeeAdapter();
        rvAdminEmployees.setLayoutManager(new LinearLayoutManager(this));
        rvAdminEmployees.setAdapter(adapter);

        btnRefresh.setOnClickListener(v -> loadDashboardData());
        btnExportCsv.setOnClickListener(v -> exportDailyCsv());
        btnSettings.setOnClickListener(v -> showTerminalSettingsDialog());
        btnLogout.setOnClickListener(v -> {
            prefs.clearAdminSession();
            finish();
        });

        loadDashboardData();
    }

    private void loadDashboardData() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        AttendanceApiService api = ApiClient.create(this);

        api.getDailyReport(today).enqueue(new Callback<ApiModels.ApiResponse<ApiModels.DailyReportSummary>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<ApiModels.DailyReportSummary>> call,
                                   @NonNull Response<ApiModels.ApiResponse<ApiModels.DailyReportSummary>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    ApiModels.DailyReportSummary r = response.body().data;
                    tvReportDate.setText("Daily Report: " + r.date + " (Total: " + r.totalEmployees + ")");
                    tvStatPresent.setText("Present\n" + r.presentCount);
                    tvStatAbsent.setText("Absent\n" + r.absentCount);
                    tvStatLate.setText("Late\n" + r.lateCount);
                    tvStatOut.setText("Out\n" + r.checkedOutCount);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<ApiModels.DailyReportSummary>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Report load error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        api.getTerminalEmployees().enqueue(new Callback<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> call,
                                   @NonNull Response<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().data != null) {
                    employees.clear();
                    employees.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Employees load error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConsentAndEnroll(ApiModels.TerminalEmployee emp) {
        CheckBox cbConsent = new CheckBox(this);
        cbConsent.setText("Employee " + emp.fullName + " has read and explicitly consented.");
        cbConsent.setPadding(32, 24, 32, 12);

        new AlertDialog.Builder(this)
                .setTitle("Mandatory Biometric Consent")
                .setMessage(getString(R.string.biometric_consent_text))
                .setView(cbConsent)
                .setPositiveButton("Proceed to Face Capture", (dialog, which) -> {
                    if (!cbConsent.isChecked()) {
                        Toast.makeText(this, "Explicit consent checkbox is required before enrollment.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Intent intent = new Intent(this, FaceVerificationActivity.class);
                    intent.putExtra(FaceVerificationActivity.EXTRA_MODE, "ENROLL");
                    intent.putExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_CODE, emp.employeeCode);
                    intent.putExtra(FaceVerificationActivity.EXTRA_EMPLOYEE_NAME, emp.fullName);
                    faceEnrollLauncher.launch(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void uploadFaceEnrollment(String empCode, double[] vector, double quality) {
        ApiModels.EnrollmentRequest req = new ApiModels.EnrollmentRequest();
        req.employeeCode = empCode;
        req.faceVector = vector;
        req.consentGiven = true;
        req.consentTextVersion = "CONSENT_V1.0";
        req.qualityScore = quality;
        req.enrolledByAdmin = prefs.getAdminUsername();

        ApiClient.create(this).enrollBiometric(req).enqueue(new Callback<ApiModels.ApiResponse<Map<String, Object>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<Map<String, Object>>> call,
                                   @NonNull Response<ApiModels.ApiResponse<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Face enrolled securely (AES-256) for " + empCode, Toast.LENGTH_LONG).show();
                    loadDashboardData();
                } else {
                    Toast.makeText(AdminDashboardActivity.this,
                            "Enrollment failed (HTTP " + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<Map<String, Object>>> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void revokeFaceEnrollment(ApiModels.TerminalEmployee emp) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Biometric Template")
                .setMessage("Remove encrypted face biometric template for " + emp.fullName + " (" + emp.employeeCode + ")?")
                .setPositiveButton("Delete", (d, w) -> {
                    ApiClient.create(this).revokeBiometric(emp.employeeCode).enqueue(new Callback<ApiModels.ApiResponse<String>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiModels.ApiResponse<String>> call,
                                               @NonNull Response<ApiModels.ApiResponse<String>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(AdminDashboardActivity.this, "Biometric deleted for " + emp.employeeCode, Toast.LENGTH_SHORT).show();
                                loadDashboardData();
                            } else {
                                Toast.makeText(AdminDashboardActivity.this, "No active enrollment found.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiModels.ApiResponse<String>> call, @NonNull Throwable t) {
                            Toast.makeText(AdminDashboardActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportDailyCsv() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        ApiClient.create(this).exportDailyReportCsv(today).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        File outFile = new File(getExternalFilesDir(null), "attendance-" + today + ".csv");
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.write(response.body().bytes());
                        }
                        Toast.makeText(AdminDashboardActivity.this,
                                "CSV saved to: " + outFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(AdminDashboardActivity.this, "Save error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminDashboardActivity.this, "Export failed (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(AdminDashboardActivity.this, "Export error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTerminalSettingsDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 12);

        EditText etUrl = new EditText(this);
        etUrl.setHint("Server API URL");
        etUrl.setText(prefs.getBaseUrl());
        layout.addView(etUrl);

        EditText etTermId = new EditText(this);
        etTermId.setHint("Terminal Code");
        etTermId.setText(prefs.getTerminalId());
        layout.addView(etTermId);

        EditText etTimeout = new EditText(this);
        etTimeout.setHint("Tap Timeout ms (600-3500)");
        etTimeout.setText(String.valueOf(prefs.getTapTimeoutMs()));
        layout.addView(etTimeout);

        CheckBox cbBio = new CheckBox(this);
        cbBio.setText("Enable BiometricPrompt Fingerprint Layer");
        cbBio.setChecked(prefs.isBiometricPromptEnabled());
        layout.addView(cbBio);

        new AlertDialog.Builder(this)
                .setTitle("Shared Terminal Configuration")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    prefs.setBaseUrl(etUrl.getText().toString());
                    prefs.setTerminalId(etTermId.getText().toString());
                    try {
                        prefs.setTapTimeoutMs(Integer.parseInt(etTimeout.getText().toString().trim()));
                    } catch (Exception ignored) {}
                    prefs.setBiometricPromptEnabled(cbBio.isChecked());
                    Toast.makeText(this, "Terminal settings saved!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.VH> {

        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvStatus;
            Button btnEnroll, btnRevoke;

            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvEmpItemName);
                tvStatus = v.findViewById(R.id.tvEmpItemStatus);
                btnEnroll = v.findViewById(R.id.btnEnrollFace);
                btnRevoke = v.findViewById(R.id.btnRevokeFace);
            }
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee_admin, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ApiModels.TerminalEmployee emp = employees.get(position);
            h.tvName.setText(emp.fullName + " (" + emp.employeeCode + ")");
            String faceText = Boolean.TRUE.equals(emp.faceEnrolled) ? "Face Enrolled (AES-256)" : "Face Not Enrolled";
            h.tvStatus.setText(faceText + " • Today: " + emp.todayStatus);
            h.btnEnroll.setOnClickListener(v -> showConsentAndEnroll(emp));
            h.btnRevoke.setOnClickListener(v -> revokeFaceEnrollment(emp));
        }

        @Override
        public int getItemCount() {
            return employees.size();
        }
    }
}
