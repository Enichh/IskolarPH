package com.example.iskolarphh;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerificationActivity extends AppCompatActivity {

    private static final String TAG = "EmailVerification";
    private static final long RESEND_COOLDOWN_MS = 60000; // 60 seconds cooldown

    private FirebaseAuth mAuth;
    private TextView tvEmail;
    private TextView tvStatus;
    private TextView tvTimer;
    private TextView tvMessage;
    private Button btnResend;
    private Button btnCheckStatus;
    private Button btnContinue;
    private Button btnLogout;
    private ProgressBar progressBar;

    private CountDownTimer countDownTimer;
    private boolean isEmailSent = false;
    private OnBackPressedCallback backPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        tvEmail = findViewById(R.id.tvEmail);
        tvStatus = findViewById(R.id.tvStatus);
        tvTimer = findViewById(R.id.tvTimer);
        tvMessage = findViewById(R.id.tvMessage);
        btnResend = findViewById(R.id.btnResend);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnContinue = findViewById(R.id.btnContinue);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        // Get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // No user logged in, redirect to login
            navigateToLogin();
            return;
        }

        // Display user's email
        tvEmail.setText(user.getEmail());

        // Check initial verification status
        updateVerificationStatus(user);

        // Setup button click listeners
        btnResend.setOnClickListener(v -> sendVerificationEmail());
        btnCheckStatus.setOnClickListener(v -> checkVerificationStatus());
        btnContinue.setOnClickListener(v -> attemptContinue());
        btnLogout.setOnClickListener(v -> logout());

        // Automatically send verification email if not already verified
        if (!user.isEmailVerified() && !isEmailSent) {
            sendVerificationEmail();
        }

        // Handle back press with new API - initialize once
        backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Prevent going back - user must verify email or logout
                Toast.makeText(EmailVerificationActivity.this, "Please verify your email or logout", Toast.LENGTH_SHORT).show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    /**
     * Send verification email using Firebase's built-in template
     */
    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showLoading(true);
        btnResend.setEnabled(false);

        // Firebase automatically uses the template configured in Console
        user.sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        showLoading(false);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent to " + user.getEmail());
                            Log.d(TAG, "Firebase reports success - if email not received, check:");
                            Log.d(TAG, "1. Spam/Junk folder in Gmail");
                            Log.d(TAG, "2. Firebase Console > Auth > Templates > Email verification is enabled");
                            Log.d(TAG, "3. Email/Password provider is enabled in Firebase Console");
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Verification email sent! Check inbox AND spam folder.",
                                    Toast.LENGTH_LONG).show();
                            isEmailSent = true;
                            startResendCooldown();
                            tvMessage.setText("Please check your email and click the verification link.");
                        } else {
                            Exception exception = task.getException();
                            Log.e(TAG, "sendEmailVerification failed", exception);
                            String errorMsg = "Failed to send: " + 
                                (exception != null ? exception.getMessage() : "Unknown error");
                            if (exception != null && exception.getMessage() != null &&
                                exception.getMessage().contains("blocked")) {
                                errorMsg = "Email sending blocked. Check Firebase Console authentication settings.";
                            }
                            Toast.makeText(EmailVerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            btnResend.setEnabled(true);
                        }
                    }
                });
    }

    /**
     * Check if email is verified by reloading user data
     */
    private void checkVerificationStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        showLoading(true);

        // Reload user to get fresh verification status
        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showLoading(false);

                if (task.isSuccessful()) {
                    // Get updated user data
                    FirebaseUser refreshedUser = mAuth.getCurrentUser();
                    if (refreshedUser != null) {
                        updateVerificationStatus(refreshedUser);

                        if (refreshedUser.isEmailVerified()) {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Email verified! You can now continue.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EmailVerificationActivity.this,
                                    "Email not verified yet. Please check your inbox.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Log.e(TAG, "reload failed", task.getException());
                    Toast.makeText(EmailVerificationActivity.this,
                            "Failed to check status. Please try again.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Update UI based on verification status
     */
    private void updateVerificationStatus(FirebaseUser user) {
        if (user.isEmailVerified()) {
            tvStatus.setText("Status: Verified");
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            btnContinue.setVisibility(View.VISIBLE);
            btnContinue.setEnabled(true);
            tvMessage.setText("Your email is verified! Click continue to proceed.");
            btnResend.setVisibility(View.GONE);
            tvTimer.setVisibility(View.GONE);
        } else {
            tvStatus.setText("Status: Not Verified");
            tvStatus.setTextColor(getColor(android.R.color.holo_red_dark));
            btnContinue.setVisibility(View.VISIBLE);
            btnContinue.setEnabled(false);
            tvMessage.setText("Please verify your email to continue. Check your inbox for the verification link.");
            btnResend.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Attempt to continue to main app
     */
    private void attemptContinue() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        // Double-check verification before proceeding
        user.reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    FirebaseUser refreshedUser = mAuth.getCurrentUser();
                    if (refreshedUser != null && refreshedUser.isEmailVerified()) {
                        navigateToMain();
                    } else {
                        Toast.makeText(EmailVerificationActivity.this,
                                "Email not verified yet. Please verify first.",
                                Toast.LENGTH_LONG).show();
                        updateVerificationStatus(refreshedUser != null ? refreshedUser : user);
                    }
                }
            }
        });
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
        if (backPressedCallback != null) {
            backPressedCallback.remove();
        }
    }
}