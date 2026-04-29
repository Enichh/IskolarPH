package com.example.iskolarphh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iskolarphh.service.SupabaseVerificationService;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.utils.NetworkUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String TAG = "EmailVerification";
    private static final String PREFS_NAME = "EmailVerificationPrefs";
    private static final String KEY_VERIFIED_EMAILS = "verified_emails";
    private static final String KEY_LOGIN_SESSION = "login_session";
    private static final String KEY_SESSION_EMAIL = "session_email";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";
    private static final long RESEND_COOLDOWN_MS = 60000; // 60 seconds cooldown
    private static final int CODE_EXPIRY_SECONDS = 300; // 5 minutes
    private static final long SESSION_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours

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
    private ProgressBar progressBar;

    private CountDownTimer countDownTimer;
    private CountDownTimer codeExpiryTimer;
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

        // Check if user has a valid cached session (for login type)
        if ("login".equals(verificationType) && hasValidCachedSession(currentEmail)) {
            Log.d(TAG, "User has valid cached session, navigating to main");
            navigateToMain();
            return;
        }
        
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
        
        // Setup auto-verification when code is entered
        etCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                String code = s.toString().trim();
                if (code.length() == 6) {
                    // Auto-verify when 6 digits are entered
                    verifyCode();
                }
            }
        });


        // Auto-send verification code on load (only if not already cached)
        sendVerificationCode();
    }
    
    private boolean hasValidCachedSession(String email) {
        if (email == null) return false;
        
        boolean hasSession = prefs.getBoolean(KEY_LOGIN_SESSION, false);
        String sessionEmail = prefs.getString(KEY_SESSION_EMAIL, "");
        long sessionTimestamp = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        
        long currentTime = System.currentTimeMillis();
        
        // Check if session is expired
        if (hasSession && (currentTime - sessionTimestamp) >= SESSION_DURATION_MS) {
            Log.d(TAG, "Session expired, clearing cache");
            clearCachedSession();
            return false;
        }
        
        return hasSession && 
               email.equals(sessionEmail) && 
               (currentTime - sessionTimestamp) < SESSION_DURATION_MS;
    }
    
    private void clearCachedSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_LOGIN_SESSION);
        editor.remove(KEY_SESSION_EMAIL);
        editor.remove(KEY_SESSION_TIMESTAMP);
        editor.apply();
        Log.d(TAG, "Cached login session cleared due to expiry");
    }
    
    private void cacheLoginSession(String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LOGIN_SESSION, true);
        editor.putString(KEY_SESSION_EMAIL, email);
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "Login session cached for email: " + email);
    }

    private void initViews() {
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvMessage = findViewById(R.id.tvMessage);
        tilCode = findViewById(R.id.tilCode);
        etCode = findViewById(R.id.etCode);
        btnResend = findViewById(R.id.btnResend);
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
                            DialogManager.showSuccessDialog(EmailVerificationActivity.this,
                                    "Code Sent",
                                    "We've sent a verification code to your email. Please check your inbox and spam folder.");
                            startResendCooldown();
                            startCodeExpiryTimer(expiresIn);
                            tvMessage.setText("Enter the 6-digit code sent to your email");
                            tilCode.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            btnResend.setEnabled(true);
                            Log.e(TAG, "Failed to send code: " + error);
                            DialogManager.showErrorDialog(EmailVerificationActivity.this,
                                    "Couldn't Send Code",
                                    "We couldn't send the verification code. " + error + "\n\nPlease try again or check your internet connection.");
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

        verificationService.verifyCode(currentEmail, code, verificationType,
                new SupabaseVerificationService.VerifyCodeCallback() {
                    @Override
                    public void onSuccess(String message, int attemptsRemaining) {
                        EmailVerificationActivity.this.runOnUiThread(() -> {
                            showLoading(false);
                            markEmailAsVerified(currentEmail);
                            
                            // Cache login session for login type
                            if ("login".equals(verificationType)) {
                                cacheLoginSession(currentEmail);
                            }
                            
                            updateVerificationStatus(true);
                            
                            String successTitle = "registration".equals(verificationType)
                                    ? "Email Verified!"
                                    : "Login Complete!";
                            String successMsg = "registration".equals(verificationType)
                                    ? "Your email has been successfully verified. Welcome to IskolarPH!"
                                    : "Your code is verified and you're now signed in. Welcome back!";
                            DialogManager.showSuccessDialog(EmailVerificationActivity.this,
                                    successTitle,
                                    successMsg);

                            // Auto-navigate to main app after successful verification
                            new android.os.Handler().postDelayed(() -> {
                                navigateToMain();
                            }, 1500); // 1.5 second delay to show success message

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

                            if (attemptsRemaining > 0) {
                                tilCode.setError(error + " (" + attemptsRemaining + " attempts remaining)");
                            } else {
                                tilCode.setError("Too many attempts. Request a new code.");
                                etCode.setText("");
                            }

                            DialogManager.showErrorDialog(EmailVerificationActivity.this,
                                    "Verification Failed",
                                    error + "\n\nPlease check the code and try again, or request a new code if it has expired.");
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
                            DialogManager.showSuccessDialog(EmailVerificationActivity.this,
                                    "New Code Sent",
                                    "A new verification code has been sent to your email.");
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
                            DialogManager.showErrorDialog(EmailVerificationActivity.this,
                                    "Couldn't Resend Code",
                                    "We couldn't resend the code. " + error + "\n\nPlease wait a moment and try again.");
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
            
            String message = "registration".equals(verificationType)
                    ? "Your email is verified! Redirecting to app..."
                    : "Verification complete! Redirecting to app...";
            tvMessage.setText(message);
            
            tilCode.setVisibility(View.GONE);
            btnResend.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Status: Not Verified");
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
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
    }
}
