package com.example.iskolarphh;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iskolarphh.service.SupabaseVerificationService;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.utils.NetworkUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PasswordResetActivity extends AppCompatActivity {

    private static final String TAG = "PasswordReset";
    private static final long RESEND_COOLDOWN_MS = 60000; // 60 seconds
    private static final int CODE_EXPIRY_SECONDS = 300; // 5 minutes

    private FirebaseAuth mAuth;
    private SupabaseVerificationService verificationService;

    // Views
    private ProgressBar progressBar;
    private LinearLayout layoutEmail, layoutCode, layoutPassword;
    private TextInputLayout tilEmail, tilCode, tilNewPassword, tilConfirmPassword;
    private TextInputEditText etEmail, etCode, etNewPassword, etConfirmPassword;
    private TextView tvEmailDisplay, tvTimer;
    private Button btnSendCode, btnVerifyCode, btnResend, btnResetPassword;
    private TextView btnBackToLogin;

    private CountDownTimer countDownTimer;
    private CountDownTimer codeExpiryTimer;
    private String currentEmail;
    private int attemptsRemaining = 5;
    private boolean isCodeVerified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        mAuth = FirebaseAuth.getInstance();
        verificationService = new SupabaseVerificationService(this);

        initViews();
        setupListeners();

        // Check if email was passed from login
        String passedEmail = getIntent().getStringExtra("email");
        if (passedEmail != null && !passedEmail.isEmpty()) {
            etEmail.setText(passedEmail);
        }
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutCode = findViewById(R.id.layoutCode);
        layoutPassword = findViewById(R.id.layoutPassword);

        tilEmail = findViewById(R.id.tilEmail);
        tilCode = findViewById(R.id.tilCode);
        tilNewPassword = findViewById(R.id.tilNewPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etEmail = findViewById(R.id.etEmail);
        etCode = findViewById(R.id.etCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tvEmailDisplay = findViewById(R.id.tvEmailDisplay);
        tvTimer = findViewById(R.id.tvTimer);

        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnResend = findViewById(R.id.btnResend);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupListeners() {
        btnSendCode.setOnClickListener(v -> sendResetCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        btnResend.setOnClickListener(v -> resendCode());
        // Password reset is now handled automatically after code verification
        btnBackToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void sendResetCode() {
        currentEmail = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(currentEmail)) {
            tilEmail.setError("Email is required");
            return;
        }

        tilEmail.setError(null);
        showLoading(true);
        btnSendCode.setEnabled(false);

        // Get real IP address for security tracking
        NetworkUtils.getPublicIpAddress(ipAddress -> {
            // Use "login" type for password reset verification
            verificationService.generateCode(currentEmail, "login", ipAddress,
                new SupabaseVerificationService.VerificationCallback() {
                    @Override
                    public void onSuccess(String message, int expiresIn) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            DialogManager.showSuccessDialog(PasswordResetActivity.this,
                                "Code Sent",
                                "We've sent a verification code to your email. Please check your inbox and spam folder.");
                            
                            // Switch to code verification layout
                            layoutEmail.setVisibility(View.GONE);
                            layoutCode.setVisibility(View.VISIBLE);
                            tvEmailDisplay.setText("Code sent to: " + currentEmail);
                            
                            startResendCooldown();
                            startCodeExpiryTimer(expiresIn);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            btnSendCode.setEnabled(true);
                            Log.e(TAG, "Failed to send code: " + error);
                            DialogManager.showErrorDialog(PasswordResetActivity.this,
                                    "Couldn't Send Code",
                                    "We couldn't send the verification code. " + error + "\n\nPlease try again or check your internet connection.");
                        });
                    }
                });
        });
    }

    private void resendCode() {
        if (currentEmail == null) return;

        showLoading(true);
        btnResend.setEnabled(false);

        // Get real IP address for security tracking
        NetworkUtils.getPublicIpAddress(ipAddress -> {
            verificationService.resendCode(currentEmail, "login", ipAddress,
                new SupabaseVerificationService.VerificationCallback() {
                    @Override
                    public void onSuccess(String message, int expiresIn) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            DialogManager.showSuccessDialog(PasswordResetActivity.this,
                                "New Code Sent",
                                "A new verification code has been sent to your email.");
                            
                            etCode.setText("");
                            tilCode.setError(null);
                            attemptsRemaining = 5;
                            
                            if (codeExpiryTimer != null) {
                                codeExpiryTimer.cancel();
                            }
                            startCodeExpiryTimer(expiresIn);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            btnResend.setEnabled(true);
                            DialogManager.showErrorDialog(PasswordResetActivity.this,
                                    "Couldn't Resend Code",
                                    "We couldn't resend the code. " + error + "\n\nPlease wait a moment and try again.");
                        });
                    }
                });
        });
    }

    private void verifyCode() {
        String code = etCode.getText().toString().trim();

        if (code.isEmpty() || code.length() != 6) {
            tilCode.setError("Please enter a 6-digit code");
            return;
        }

        tilCode.setError(null);
        showLoading(true);
        btnVerifyCode.setEnabled(false);

        verificationService.verifyCode(currentEmail, code, "login",
                new SupabaseVerificationService.VerifyCodeCallback() {
                    @Override
                    public void onSuccess(String message, int remaining) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            isCodeVerified = true;
                            attemptsRemaining = remaining;
                            
                            // Cancel timers
                            if (codeExpiryTimer != null) {
                                codeExpiryTimer.cancel();
                            }
                            if (countDownTimer != null) {
                                countDownTimer.cancel();
                            }
                            
                            // Code verified successfully, send Firebase reset email immediately
                            DialogManager.showSuccessDialog(PasswordResetActivity.this,
                                    "Code Verified",
                                    "Your code is verified! We're sending a password reset link to your email now.");
                            
                            // Send Firebase password reset email
                            mAuth.sendPasswordResetEmail(currentEmail)
                                    .addOnCompleteListener(resetTask -> {
                                        if (resetTask.isSuccessful()) {
                                            DialogManager.showSuccessDialog(PasswordResetActivity.this,
                                                    "Reset Link Sent",
                                                    "We've sent a password reset link to your email. Please check your inbox to complete the process.");
                                            navigateToLogin();
                                        } else {
                                            String error = resetTask.getException() != null
                                                    ? resetTask.getException().getMessage()
                                                    : "Failed to send reset email";
                                            DialogManager.showErrorDialog(PasswordResetActivity.this,
                                                    "Couldn't Send Reset Link",
                                                    error + "\n\nPlease try again or contact support if the problem persists.");
                                            // Show code input again for retry
                                            layoutCode.setVisibility(View.VISIBLE);
                                        }
                                    });
                        });
                    }

                    @Override
                    public void onError(String error, int remaining) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            attemptsRemaining = remaining;
                            btnVerifyCode.setEnabled(true);
                            
                            if (remaining > 0) {
                                tilCode.setError(error + " (" + remaining + " attempts remaining)");
                            } else {
                                tilCode.setError("Too many attempts. Request a new code.");
                                etCode.setText("");
                            }
                            
                            DialogManager.showErrorDialog(PasswordResetActivity.this,
                                    "Verification Failed",
                                    error + "\n\nPlease check the code and try again, or request a new code.");
                        });
                    }
                });
    }

    // Password reset is now handled directly after code verification
    // This method is no longer needed

    private void startResendCooldown() {
        btnResend.setEnabled(false);
        tvTimer.setVisibility(View.VISIBLE);

        countDownTimer = new CountDownTimer(RESEND_COOLDOWN_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Resend available in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                btnResend.setEnabled(true);
                tvTimer.setVisibility(View.GONE);
                tvTimer.setText("");
            }
        }.start();
    }

    private void startCodeExpiryTimer(int seconds) {
        codeExpiryTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Optional: Show countdown
            }

            @Override
            public void onFinish() {
                tilCode.setError("Code expired. Please request a new code.");
                etCode.setText("");
                btnVerifyCode.setEnabled(false);
            }
        }.start();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(PasswordResetActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (codeExpiryTimer != null) {
            codeExpiryTimer.cancel();
        }
    }
}
