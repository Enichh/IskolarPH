package com.example.iskolarphh.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupViewModel extends AndroidViewModel {

    private final FirebaseAuth mAuth;
    private final StudentRepository studentRepository;
    private final MutableLiveData<SignupState> signupState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SignupViewModel(Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        studentRepository = new StudentRepository(application);
    }

    public LiveData<SignupState> getSignupState() {
        return signupState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void attemptSignup(String fullName, String email, String password, String confirmPassword) {
        // Validation
        if (TextUtils.isEmpty(fullName)) {
            errorMessage.postValue("Full name is required");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Email is required");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.postValue("Please enter a valid email address");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.postValue("Password is required");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (password.length() < 6) {
            errorMessage.postValue("Password must be at least 6 characters");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            errorMessage.postValue("Please confirm your password");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.postValue("Passwords do not match");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        signupState.postValue(SignupState.LOADING);

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
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

                            // Save to Room database with callback
                            studentRepository.insert(student, id -> {
                                // Run on UI thread
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    signupState.postValue(SignupState.SUCCESS);
                                });
                            });
                        }
                    } else {
                        String error = "Registration failed";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getClass().getSimpleName();
                            switch (errorCode) {
                                case "FirebaseAuthWeakPasswordException":
                                    error = "Password is too weak";
                                    break;
                                case "FirebaseAuthInvalidCredentialsException":
                                    error = "Invalid email address";
                                    break;
                                case "FirebaseAuthUserCollisionException":
                                    error = "An account with this email already exists";
                                    break;
                                default:
                                    error = task.getException().getMessage();
                                    break;
                            }
                        }
                        errorMessage.postValue(error);
                        signupState.postValue(SignupState.ERROR);
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

    public enum SignupState {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR,
        VALIDATION_ERROR
    }
}
