package com.example.iskolarphh.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.iskolarphh.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseVerificationService {

    private static final String TAG = "SupabaseVerification";
    
    private final Context context;
    private final OkHttpClient client;

    public SupabaseVerificationService(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private String getSupabaseUrl() {
        return BuildConfig.SUPABASE_URL;
    }

    private String getSupabaseKey() {
        return BuildConfig.SUPABASE_PUBLISHABLE_KEY;
    }

    public interface VerificationCallback {
        void onSuccess(String message, int expiresIn);
        void onError(String error);
    }

    public interface VerifyCodeCallback {
        void onSuccess(String message, int attemptsRemaining);
        void onError(String error, int attemptsRemaining);
    }

    /**
     * Generate and send verification code to email
     */
    public void generateCode(String email, String type, String ipAddress, VerificationCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("type", type);
            json.put("ipAddress", ipAddress);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(getSupabaseUrl() + "/functions/v1/generate-code")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + getSupabaseKey())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "generateCode failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "generateCode response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (response.isSuccessful()) {
                            String message = jsonResponse.optString("message", "Code sent");
                            int expiresIn = jsonResponse.optInt("expiresIn", 300);
                            callback.onSuccess(message, expiresIn);
                        } else {
                            String error = jsonResponse.optString("error", "Failed to send code");
                            callback.onError(error);
                        }
                    } catch (JSONException e) {
                        callback.onError("Invalid response");
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Failed to create request");
        }
    }

    /**
     * Resend verification code
     */
    public void resendCode(String email, String type, String ipAddress, VerificationCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("type", type);
            json.put("ipAddress", ipAddress);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(getSupabaseUrl() + "/functions/v1/resend-code")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + getSupabaseKey())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "resendCode failed", e);
                    callback.onError("Network error: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "resendCode response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        if (response.isSuccessful()) {
                            String message = jsonResponse.optString("message", "New code sent");
                            int expiresIn = jsonResponse.optInt("expiresIn", 300);
                            callback.onSuccess(message, expiresIn);
                        } else {
                            String error = jsonResponse.optString("error", "Failed to resend code");
                            callback.onError(error);
                        }
                    } catch (JSONException e) {
                        callback.onError("Invalid response");
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Failed to create request");
        }
    }

    /**
     * Verify the 6-digit code
     */
    public void verifyCode(String email, String code, String type, VerifyCodeCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("code", code);
            json.put("type", type);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(getSupabaseUrl() + "/functions/v1/verify-code")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + getSupabaseKey())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "verifyCode failed", e);
                    callback.onError("Network error: " + e.getMessage(), 0);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "verifyCode response: " + responseBody);

                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        int attemptsRemaining = jsonResponse.optInt("attemptsRemaining", 0);

                        if (response.isSuccessful()) {
                            String message = jsonResponse.optString("message", "Code verified");
                            callback.onSuccess(message, attemptsRemaining);
                        } else {
                            String error = jsonResponse.optString("error", "Invalid code");
                            callback.onError(error, attemptsRemaining);
                        }
                    } catch (JSONException e) {
                        callback.onError("Invalid response", 0);
                    }
                }
            });
        } catch (JSONException e) {
            callback.onError("Failed to create request", 0);
        }
    }
}
