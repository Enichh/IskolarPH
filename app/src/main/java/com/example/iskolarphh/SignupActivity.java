package com.example.iskolarphh;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.viewmodel.SignupViewModel;

public class SignupActivity extends AppCompatActivity {

    private SignupViewModel signupViewModel;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
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
        btnSignup = findViewById(R.id.btnSignup);
        tvLoginLink = findViewById(R.id.tvLoginLink);

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
                    Toast.makeText(SignupActivity.this,
                            "Account created! Please verify your email.",
                            Toast.LENGTH_SHORT).show();
                    navigateToEmailVerification();
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
                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptSignup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        signupViewModel.attemptSignup(fullName, email, password, confirmPassword);
    }



    private void navigateToEmailVerification() {
        Intent intent = new Intent(SignupActivity.this, EmailVerificationActivity.class);
        intent.putExtra("verification_type", "registration");
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