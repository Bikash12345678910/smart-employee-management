package com.company.smartbiometric.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.company.smartbiometric.R;

public class TerminalPrefs {

    private static final String PREFS_NAME = "smart_biometric_terminal_prefs";
    private static final String KEY_BASE_URL = "api_base_url";
    private static final String KEY_TERMINAL_ID = "terminal_id";
    private static final String KEY_TERMINAL_KEY = "terminal_key";
    private static final String KEY_TAP_TIMEOUT_MS = "tap_timeout_ms";
    private static final String KEY_ADMIN_TOKEN = "admin_jwt_token";
    private static final String KEY_ADMIN_USERNAME = "admin_username";
    private static final String KEY_BIOMETRIC_PROMPT_ENABLED = "biometric_prompt_enabled";

    private final SharedPreferences prefs;
    private final Context context;

    public TerminalPrefs(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getBaseUrl() {
        String url = prefs.getString(KEY_BASE_URL, context.getString(R.string.default_api_base_url));
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    public void setBaseUrl(String baseUrl) {
        String clean = baseUrl.trim();
        if (!clean.endsWith("/")) {
            clean = clean + "/";
        }
        prefs.edit().putString(KEY_BASE_URL, clean).apply();
    }

    public String getTerminalId() {
        return prefs.getString(KEY_TERMINAL_ID, context.getString(R.string.default_terminal_id));
    }

    public void setTerminalId(String terminalId) {
        prefs.edit().putString(KEY_TERMINAL_ID, terminalId.trim()).apply();
    }

    public String getTerminalKey() {
        return prefs.getString(KEY_TERMINAL_KEY, context.getString(R.string.default_terminal_key));
    }

    public void setTerminalKey(String terminalKey) {
        prefs.edit().putString(KEY_TERMINAL_KEY, terminalKey.trim()).apply();
    }

    public int getTapTimeoutMs() {
        return prefs.getInt(KEY_TAP_TIMEOUT_MS, 1400);
    }

    public void setTapTimeoutMs(int timeoutMs) {
        int clamped = Math.max(600, Math.min(3500, timeoutMs));
        prefs.edit().putInt(KEY_TAP_TIMEOUT_MS, clamped).apply();
    }

    public boolean isBiometricPromptEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC_PROMPT_ENABLED, true);
    }

    public void setBiometricPromptEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_PROMPT_ENABLED, enabled).apply();
    }

    public String getAdminToken() {
        return prefs.getString(KEY_ADMIN_TOKEN, null);
    }

    public void setAdminToken(String token, String username) {
        prefs.edit()
                .putString(KEY_ADMIN_TOKEN, token)
                .putString(KEY_ADMIN_USERNAME, username)
                .apply();
    }

    public String getAdminUsername() {
        return prefs.getString(KEY_ADMIN_USERNAME, "admin");
    }

    public void clearAdminSession() {
        prefs.edit().remove(KEY_ADMIN_TOKEN).remove(KEY_ADMIN_USERNAME).apply();
    }
}
