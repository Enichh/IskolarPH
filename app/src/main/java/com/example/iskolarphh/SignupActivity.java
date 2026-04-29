package com.example.iskolarphh;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.viewmodel.SignupViewModel;

public class SignupActivity extends AppCompatActivity {

    private SignupViewModel signupViewModel;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private CheckBox cbBasicConsent;
    private CheckBox cbLocationConsent;
    private TextView tvConsentWarning;
    private Button btnSignup;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        ViewModelFactory factory = new ViewModelFactory(getApplication());
        signupViewModel = new ViewModelProvider(this, factory).get(SignupViewModel.class);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbBasicConsent = findViewById(R.id.cbBasicConsent);
        cbLocationConsent = findViewById(R.id.cbLocationConsent);
        tvConsentWarning = findViewById(R.id.tvConsentWarning);
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        setupConsentCheckboxes();
        btnSignup.setOnClickListener(v -> attemptSignup());
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        observeViewModel();
    }

    private void observeViewModel() {
        signupViewModel.getSignupState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    btnSignup.setEnabled(false);
                    break;
                case SUCCESS:
                    btnSignup.setEnabled(true);
                    DialogManager.showSuccessDialog(SignupActivity.this,
                            "Welcome to IskolarPH!",
                            "Your account has been created. Please check your email to verify your account.");
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            navigateToEmailVerification();
                        }
                    }, 1500); // Wait for the dialog to auto-dismiss before navigating
                    break;
                case ERROR:
                case VALIDATION_ERROR:
                    btnSignup.setEnabled(true);
                    break;
                default:
                    break;
            }
        });

        signupViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                DialogManager.showErrorDialog(SignupActivity.this, "Registration Issue", error);
            }
        });
    }

    private void setupConsentCheckboxes() {
        // Enable/disable signup button based on consent
        android.widget.CompoundButton.OnCheckedChangeListener consentListener = (buttonView, isChecked) -> {
            boolean basicConsent = cbBasicConsent.isChecked();
            btnSignup.setEnabled(basicConsent);

            if (basicConsent) {
                tvConsentWarning.setVisibility(android.view.View.GONE);
            }
        };

        cbBasicConsent.setOnCheckedChangeListener(consentListener);
        cbLocationConsent.setOnCheckedChangeListener(consentListener);
    }

    private void attemptSignup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        boolean basicConsent = cbBasicConsent.isChecked();
        boolean locationConsent = cbLocationConsent.isChecked();

        // Show warning if consent not given
        if (!basicConsent) {
            tvConsentWarning.setVisibility(android.view.View.VISIBLE);
            DialogManager.showErrorDialog(this,
                    "Privacy Policy Required",
                    "To create your account, we need your consent to our Privacy Policy. This helps us protect your data in compliance with Philippine data privacy laws.");
            return;
        }

        tvConsentWarning.setVisibility(android.view.View.GONE);
        signupViewModel.attemptSignup(fullName, email, password, confirmPassword, basicConsent, locationConsent);
    }



    private void navigateToEmailVerification() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        boolean basicConsent = cbBasicConsent.isChecked();
        boolean locationConsent = cbLocationConsent.isChecked();

        Intent intent = new Intent(SignupActivity.this, EmailVerificationActivity.class);
        intent.putExtra("verification_type", "registration");
        intent.putExtra("full_name", fullName);
        intent.putExtra("email", email);
        intent.putExtra("basic_consent", basicConsent);
        intent.putExtra("location_consent", locationConsent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}