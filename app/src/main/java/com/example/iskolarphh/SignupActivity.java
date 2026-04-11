package com.example.iskolarphh;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnSignup;
    private TextView tvLoginLink;
    private StudentRepository studentRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();
        studentRepository = new StudentRepository(this);

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
    }

    private void attemptSignup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
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

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        btnSignup.setEnabled(false);

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Parse full name with improved logic
                            String[] nameParts = parseFullName(fullName);
                            String firstName = nameParts[0];
                            String middleInitial = nameParts[1];
                            String lastName = nameParts[2];

                            // Create student record with Firebase UID using 6-argument constructor
                            Student student = new Student(
                                    user.getUid(),
                                    firstName,
                                    lastName,
                                    middleInitial,
                                    "", // location - will be filled later
                                    0.0  // gpa - will be filled later
                            );
                            
                            // Set email for local database
                            student.setEmail(email);
                            // Store password hash for local authentication fallback
                            student.setPassword(password);

                            // Save to Room database with callback
                            studentRepository.insert(student, () -> {
                                // Run on UI thread
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    btnSignup.setEnabled(true);
                                    Toast.makeText(SignupActivity.this, 
                                            "Account created successfully!", 
                                            Toast.LENGTH_SHORT).show();
                                    navigateToMain();
                                });
                            });
                        }
                    } else {
                        btnSignup.setEnabled(true);
                        String errorMessage = "Registration failed";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getClass().getSimpleName();
                            switch (errorCode) {
                                case "FirebaseAuthWeakPasswordException":
                                    errorMessage = "Password is too weak";
                                    break;
                                case "FirebaseAuthInvalidCredentialsException":
                                    errorMessage = "Invalid email address";
                                    break;
                                case "FirebaseAuthUserCollisionException":
                                    errorMessage = "An account with this email already exists";
                                    break;
                                default:
                                    errorMessage = task.getException().getMessage();
                                    break;
                            }
                        }
                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
    
    /**
     * Parse full name into first name, middle initial, and last name
     * Handles various formats: "John Doe", "John M Doe", "John Michael Doe"
     * @param fullName The full name string
     * @return String array [firstName, middleInitial, lastName]
     */
    private String[] parseFullName(String fullName) {
        String[] result = new String[3];
        result[0] = ""; // firstName
        result[1] = ""; // middleInitial
        result[2] = ""; // lastName
        
        if (fullName == null || fullName.trim().isEmpty()) {
            return result;
        }
        
        String[] parts = fullName.trim().split("\\s+");
        
        if (parts.length == 1) {
            // Only first name
            result[0] = parts[0];
        } else if (parts.length == 2) {
            // First and last name
            result[0] = parts[0];
            result[2] = parts[1];
        } else {
            // First name, middle name(s), last name
            result[0] = parts[0];
            result[2] = parts[parts.length - 1];
            // Middle initial from the first middle name
            result[1] = String.valueOf(parts[1].charAt(0));
        }
        
        return result;
    }

    private void navigateToMain() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}