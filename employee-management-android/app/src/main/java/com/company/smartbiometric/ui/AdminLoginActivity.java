package com.company.smartbiometric.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.company.smartbiometric.R;
import com.company.smartbiometric.config.TerminalPrefs;
import com.company.smartbiometric.model.ApiModels;
import com.company.smartbiometric.network.ApiClient;
import com.company.smartbiometric.network.AttendanceApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etServerBaseUrl;
    private TextView tvLoginError;
    private Button btnDoLogin;
    private TerminalPrefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        prefs = new TerminalPrefs(this);

        etUsername = findViewById(R.id.etAdminUsername);
        etPassword = findViewById(R.id.etAdminPassword);
        etServerBaseUrl = findViewById(R.id.etServerBaseUrl);
        tvLoginError = findViewById(R.id.tvLoginError);
        btnDoLogin = findViewById(R.id.btnDoAdminLogin);
        Button btnBack = findViewById(R.id.btnBackToKiosk);

        etServerBaseUrl.setText(prefs.getBaseUrl());
        etUsername.setText("admin");

        btnBack.setOnClickListener(v -> finish());
        btnDoLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String url = etServerBaseUrl.getText().toString().trim();
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            showError("Please enter Server URL, Admin Username, and Password.");
            return;
        }

        prefs.setBaseUrl(url);
        tvLoginError.setVisibility(View.GONE);
        btnDoLogin.setEnabled(false);
        btnDoLogin.setText("Signing in...");

        AttendanceApiService api = ApiClient.create(this);
        api.loginAdmin(new ApiModels.LoginRequest(user, pass)).enqueue(new Callback<ApiModels.ApiResponse<ApiModels.AuthData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiModels.ApiResponse<ApiModels.AuthData>> call,
                                   @NonNull Response<ApiModels.ApiResponse<ApiModels.AuthData>> response) {
                btnDoLogin.setEnabled(true);
                btnDoLogin.setText("Sign In to Admin Console");

                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    ApiModels.AuthData auth = response.body().data;
                    if (!"ROLE_ADMIN".equals(auth.role)) {
                        showError("Access denied: Administrator privileges required.");
                        return;
                    }
                    prefs.setAdminToken(auth.token, auth.username);
                    startActivity(new Intent(AdminLoginActivity.this, AdminDashboardActivity.class));
                    finish();
                } else {
                    showError("Invalid admin credentials (HTTP " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiModels.ApiResponse<ApiModels.AuthData>> call, @NonNull Throwable t) {
                btnDoLogin.setEnabled(true);
                btnDoLogin.setText("Sign In to Admin Console");
                showError("Connection failed: " + t.getMessage());
            }
        });
    }

    private void showError(String msg) {
        tvLoginError.setText(msg);
        tvLoginError.setVisibility(View.VISIBLE);
    }
}
