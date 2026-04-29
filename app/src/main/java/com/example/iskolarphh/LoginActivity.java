package com.example.iskolarphh;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.viewmodel.LoginViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvSignupLink;
    private TextView tvForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        ViewModelFactory factory = new ViewModelFactory(getApplication());
        loginViewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignupLink = findViewById(R.id.tvSignupLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PasswordResetActivity.class);
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                intent.putExtra("email", email);
            }
            startActivity(intent);
        });

        observeViewModel();
        
        // Pre-fill email if user has cached session
        prefillEmailFromCache();
    }

    private void observeViewModel() {
        loginViewModel.getLoginState().observe(this, state -> {
            switch (state) {
                case SUCCESS:
                    // Success is handled by navigation - no dialog needed for smooth UX
                    navigateToMain();
                    break;
                case VERIFIED:
                    navigateToMain();
                    break;
                case NOT_VERIFIED:
                    DialogManager.showErrorDialog(LoginActivity.this,
                            "Verification Required",
                            "For your security, we need to verify your email before you can access your account.",
                            null,
                            () -> navigateToEmailVerification());
                    break;
                case ERROR:
                    btnLogin.setEnabled(true);
                    break;
                case RESET_SENT:
                    btnLogin.setEnabled(true);
                    break;
                default:
                    break;
            }
        });

        loginViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                DialogManager.showAuthError(LoginActivity.this, error);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        loginViewModel.checkUserStatus();
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        btnLogin.setEnabled(false);
        loginViewModel.attemptLogin(email, password);
    }


    private void navigateToEmailVerification() {
        Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
        intent.putExtra("verification_type", "login");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToEmailVerificationForRegistration() {
        Intent intent = new Intent(LoginActivity.this, EmailVerificationActivity.class);
        intent.putExtra("verification_type", "registration");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void prefillEmailFromCache() {
        SharedPreferences prefs = getSharedPreferences("EmailVerificationPrefs", Context.MODE_PRIVATE);
        String sessionEmail = prefs.getString("session_email", "");
        boolean hasSession = prefs.getBoolean("login_session", false);
        long sessionTimestamp = prefs.getLong("session_timestamp", 0);
        
        // Only pre-fill if session is still valid
        long currentTime = System.currentTimeMillis();
        long sessionDuration = 24 * 60 * 60 * 1000; // 24 hours
        
        if (hasSession && !sessionEmail.isEmpty() && (currentTime - sessionTimestamp) < sessionDuration) {
            etEmail.setText(sessionEmail);
            Log.d(TAG, "Prefilled email from cache: " + sessionEmail);
        }
    }
    
    public void logout() {
        loginViewModel.clearCachedSession();
        FirebaseAuth.getInstance().signOut();
        Log.d(TAG, "User logged out, session cleared");
    }
}