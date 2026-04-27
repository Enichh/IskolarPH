package com.example.iskolarphh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iskolarphh.service.SupabaseVerificationService;
import com.example.iskolarphh.utils.NetworkUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String TAG = "EmailVerification";
    private static final String PREFS_NAME = "EmailVerificationPrefs";
    private static final String KEY_VERIFIED_EMAILS = "verified_emails";
    private static final long RESEND_COOLDOWN_MS = 60000; // 60 seconds cooldown
    private static final int CODE_EXPIRY_SECONDS = 300; // 5 minutes

    private FirebaseAuth mAuth;
    private SupabaseVerificationService verificationService;
    private SharedPreferences prefs;

    private TextView tvEmail;
    private TextView tvStatus;
    private TextView tvTimer;
    private TextView tvMessage;
    private TextInputLayout tilCode;
    private TextInputEditText etCode;
    private Button btnResend;
    private Button btnVerifyCode;
    private Button btnContinue;
    private Button btnLogout;
    private ProgressBar progressBar;

    private CountDownTimer countDownTimer;
    private CountDownTimer codeExpiryTimer;
    private OnBackPressedCallback backPressedCallback;
    private boolean isCodeSent = false;
    private String currentEmail;
    private String verificationType = "registration";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();
        verificationService = new SupabaseVerificationService(this);
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize views
        initViews();

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        currentEmail = user.getEmail();
        tvEmail.setText(currentEmail);

        // Check if already verified via Supabase
        if (isEmailVerifiedInSupabase(currentEmail)) {
            navigateToMain();
            return;
        }

        // Determine verification type based on intent or user state
        verificationType = getIntent().getStringExtra("verification_type");
        if (verificationType == null) {
            verificationType = "registration";
        }

        // Setup button click listeners
        btnResend.setOnClickListener(v -> sendVerificationCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        btnContinue.setOnClickListener(v -> attemptContinue());
        btnLogout.setOnClickListener(v -> logout());

        // Handle back press
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(EmailVerificationActivity.this, "Please verify your email or logout", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Auto-send verification code on load
        sendVerificationCode();
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvMessage = findViewById(R.id.tvMessage);
        tilCode = findViewById(R.id.tilCode);
        etCode = findViewById(R.id.etCode);
        btnResend = findViewById(R.id.btnResend);
        btnVerifyCode = findViewById(R.id.btnVerifyCode);
        btnContinue = findViewById(R.id.btnContinue);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Check if email is already verified in Supabase (for registration only)
     */
    private boolean isEmailVerifiedInSupabase(String email) {
        // Only check persistent verification for registration
        if ("registration".equals(verificationType)) {
            String verifiedEmails = prefs.getString(KEY_VERIFIED_EMAILS, "");
            return verifiedEmails.contains(email + ",");
        }
        // For login, always require verification code (2FA)
        return false;
    }

    /**
     * Mark email as verified in local storage (for registration only)
     */
    private void markEmailAsVerified(String email) {
        // Only persist verification for registration
        if ("registration".equals(verificationType)) {
            String verifiedEmails = prefs.getString(KEY_VERIFIED_EMAILS, "");
            if (!verifiedEmails.contains(email + ",")) {
                verifiedEmails += email + ",";
                prefs.edit().putString(KEY_VERIFIED_EMAILS, verifiedEmails).apply();
            }
        }
        // For login, verification is session-based (2FA)
    }

    /**
     * Send verification code via Supabase Edge Function
     */
    private void sendVerificationCode() {
        if (currentEmail == null) return;

        showLoading(true);
        btnResend.setEnabled(false);

        // Get real IP address for security tracking
        NetworkUtils.getPublicIpAddress(ipAddress -> {
            verificationService.generateCode(currentEmail, verificationType, ipAddress,
                new SupabaseVerificationService.VerificationCallback() {
                    @Override
                    public void onSuccess(String message, int expiresIn) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            isCodeSent = true;
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Verification code sent! Check your email.",
                                    Toast.LENGTH_LONG).show();
                            startResendCooldown();
                            startCodeExpiryTimer(expiresIn);
                            tvMessage.setText("Enter the 6-digit code sent to your email");
                            tilCode.setVisibility(View.VISIBLE);
                            btnVerifyCode.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            btnResend.setEnabled(true);
                            Log.e(TAG, "Failed to send code: " + error);
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Failed to send code: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
        });
    }

    /**
     * Verify the entered 6-digit code
     */
    private void verifyCode() {
        String code = etCode.getText().toString().trim();

        if (code.isEmpty() || code.length() != 6) {
            tilCode.setError("Please enter a 6-digit code");
            return;
        }

        tilCode.setError(null);
        showLoading(true);
        btnVerifyCode.setEnabled(false);

        verificationService.verifyCode(currentEmail, code, verificationType,
                new SupabaseVerificationService.VerifyCodeCallback() {
                    @Override
                    public void onSuccess(String message, int attemptsRemaining) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            markEmailAsVerified(currentEmail);
                            updateVerificationStatus(true);
                            
                            String successMsg = "registration".equals(verificationType) 
                                    ? "Email verified successfully!" 
                                    : "Code verified! Login complete.";
                            Toast.makeText(EmailVerificationActivity.this,
                                    successMsg,
                                    Toast.LENGTH_SHORT).show();

                            // Cancel expiry timer
                            if (codeExpiryTimer != null) {
                                codeExpiryTimer.cancel();
                            }
                        });
                    }

                    @Override
                    public void onError(String error, int attemptsRemaining) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            btnVerifyCode.setEnabled(true);

                            if (attemptsRemaining > 0) {
                                tilCode.setError(error + " (" + attemptsRemaining + " attempts remaining)");
                            } else {
                                tilCode.setError("Too many attempts. Request a new code.");
                                etCode.setText("");
                            }

                            Toast.makeText(EmailVerificationActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    /**
     * Resend verification code
     */
    private void resendCode() {
        if (currentEmail == null) return;

        showLoading(true);
        btnResend.setEnabled(false);

        String ipAddress = "127.0.0.1";

        verificationService.resendCode(currentEmail, verificationType, ipAddress,
                new SupabaseVerificationService.VerificationCallback() {
                    @Override
                    public void onSuccess(String message, int expiresIn) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            isCodeSent = true;
                            Toast.makeText(EmailVerificationActivity.this,
                                    "New code sent! Check your email.",
                                    Toast.LENGTH_LONG).show();
                            startResendCooldown();

                            // Reset expiry timer
                            if (codeExpiryTimer != null) {
                                codeExpiryTimer.cancel();
                            }
                            startCodeExpiryTimer(expiresIn);

                            etCode.setText("");
                            tilCode.setError(null);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            btnResend.setEnabled(true);
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Failed to resend: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    /**
     * Update UI based on verification status
     */
    private void updateVerificationStatus(boolean isVerified) {
        if (isVerified) {
            tvStatus.setText("Status: Verified");
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
            
            String message = "registration".equals(verificationType)
                    ? "Your email is verified! Click continue to proceed."
                    : "Verification complete! Click continue to access the app.";
            tvMessage.setText(message);
            
            tilCode.setVisibility(View.GONE);
            btnVerifyCode.setVisibility(View.GONE);
            btnResend.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Status: Not Verified");
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);
        }
    }

    /**
     * Attempt to continue to main app
     */
    private void attemptContinue() {
        // For registration, check persistent verification
        if ("registration".equals(verificationType)) {
            if (isEmailVerifiedInSupabase(currentEmail)) {
                navigateToMain();
            } else {
                Toast.makeText(this, "Please verify your email first", Toast.LENGTH_LONG).show();
            }
        } else {
            // For login (2FA), if we reached this point, verification was successful
            navigateToMain();
        }
    }

    /**
     * Start cooldown timer for resend button
     */
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

    /**
     * Start timer for code expiry
     */
    private void startCodeExpiryTimer(int seconds) {
        codeExpiryTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Could show countdown in UI if desired
            }

            @Override
            public void onFinish() {
                tilCode.setError("Code expired. Please request a new code.");
                etCode.setText("");
            }
        }.start();
    }

    /**
     * Logout user and return to login
     */
    private void logout() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(EmailVerificationActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(EmailVerificationActivity.this, MainActivity.class);
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
        if (backPressedCallback != null) {
            backPressedCallback.remove();
        }
    }
}
