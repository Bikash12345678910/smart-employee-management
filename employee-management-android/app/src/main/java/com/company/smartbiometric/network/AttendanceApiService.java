package com.company.smartbiometric.network;

import com.company.smartbiometric.model.ApiModels;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AttendanceApiService {

    @POST("api/auth/login")
    Call<ApiModels.ApiResponse<ApiModels.AuthData>> loginAdmin(
            @Body ApiModels.LoginRequest request);

    @GET("api/terminal/config")
    Call<ApiModels.ApiResponse<ApiModels.TerminalConfig>> getTerminalConfig();

    @GET("api/terminal/employees")
    Call<ApiModels.ApiResponse<List<ApiModels.TerminalEmployee>>> getTerminalEmployees();

    @POST("api/terminal/attendance/punch")
    Call<ApiModels.ApiResponse<ApiModels.PunchResponse>> punchAttendance(
            @Body ApiModels.PunchRequest request);

    @POST("api/terminal/biometric/enroll")
    Call<ApiModels.ApiResponse<Map<String, Object>>> enrollBiometric(
            @Body ApiModels.EnrollmentRequest request);

    @DELETE("api/terminal/biometric/{employeeCode}")
    Call<ApiModels.ApiResponse<String>> revokeBiometric(
            @Path("employeeCode") String employeeCode);

    @GET("api/terminal/reports/daily")
    Call<ApiModels.ApiResponse<ApiModels.DailyReportSummary>> getDailyReport(
            @Query("date") String date);

    @GET("api/terminal/reports/export-csv")
    Call<ResponseBody> exportDailyReportCsv(
            @Query("date") String date);
}
