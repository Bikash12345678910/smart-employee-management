package com.company.smartbiometric.network;

import android.content.Context;

import com.company.smartbiometric.config.TerminalPrefs;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static AttendanceApiService create(Context context) {
        TerminalPrefs prefs = new TerminalPrefs(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder()
                            .header("Accept", "application/json")
                            .header("X-Terminal-Id", prefs.getTerminalId())
                            .header("X-Terminal-Key", prefs.getTerminalKey())
                            .header("X-Request-Nonce", UUID.randomUUID().toString())
                            .header("X-Request-Timestamp", String.valueOf(System.currentTimeMillis()));

                    String token = prefs.getAdminToken();
                    if (token != null && !token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(builder.build());
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(prefs.getBaseUrl())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(AttendanceApiService.class);
    }
}
