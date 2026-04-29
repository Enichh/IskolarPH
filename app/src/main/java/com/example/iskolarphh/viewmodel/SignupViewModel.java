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
import com.example.iskolarphh.BuildConfig;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.repository.PrivacyConsentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupViewModel extends AndroidViewModel {

    private static final String PRIVACY_POLICY_VERSION = "1.0";

    private final FirebaseAuth mAuth;
    private final StudentRepository studentRepository;
    private final PrivacyConsentRepository privacyConsentRepository;
    private final MutableLiveData<SignupState> signupState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public SignupViewModel(Application application, PrivacyConsentRepository privacyConsentRepository) {
        this(application, privacyConsentRepository, new StudentRepository(application));
    }

    // Constructor with full dependency injection
    public SignupViewModel(Application application, PrivacyConsentRepository privacyConsentRepository, StudentRepository studentRepository) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
        this.studentRepository = studentRepository;
        this.privacyConsentRepository = privacyConsentRepository;
    }

    public LiveData<SignupState> getSignupState() {
        return signupState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void attemptSignup(String fullName, String email, String password, String confirmPassword,
                              boolean basicConsentGiven, boolean locationConsentGiven) {
        // Privacy Consent Validation (Philippine DPA Compliance)
        if (!basicConsentGiven) {
            errorMessage.postValue("Please agree to our Privacy Policy to create your account. This helps us protect your personal data.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            errorMessage.postValue("Please enter your full name as it appears on your school records.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Please enter your email address. We'll use this to verify your account and send important updates.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.postValue("The email address doesn't look right. Please check and enter a valid email like name@example.com");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.postValue("Please create a password to secure your account.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (password.length() < 6) {
            errorMessage.postValue("Your password should be at least 6 characters long for better security.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            errorMessage.postValue("Please confirm your password by entering it again.");
            signupState.postValue(SignupState.VALIDATION_ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.postValue("The passwords don't match. Please make sure both password fields are the same.");
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

                            // Store user data temporarily for insertion after verification
                            // Student will be inserted into Room database AFTER Supabase verification succeeds
                            // This prevents unverified users from being stored in the database

                            // Run on UI thread to signal success and navigate to verification
                            new Handler(Looper.getMainLooper()).post(() -> {
                                signupState.postValue(SignupState.SUCCESS);
                            });
                        }
                    } else {
                        String error = "We couldn't complete your registration. Please try again.";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getClass().getSimpleName();
                            switch (errorCode) {
                                case "FirebaseAuthWeakPasswordException":
                                    error = "Your password is too weak. Try using a mix of letters, numbers, and symbols.";
                                    break;
                                case "FirebaseAuthInvalidCredentialsException":
                                    error = "The email address doesn't look valid. Please check and try again.";
                                    break;
                                case "FirebaseAuthUserCollisionException":
                                    error = "An account already exists with this email. Try signing in instead.";
                                    break;
                                default:
                                    error = "Something went wrong: " + task.getException().getMessage();
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

    /**
     * Save privacy consent record for Philippine Data Privacy Act (RA 10173) compliance.
     * Records consent timestamp, app version, and privacy policy version.
     */
    private void savePrivacyConsent(String firebaseUid, boolean basicConsent, boolean locationConsent) {
        privacyConsentRepository.saveConsent(
                firebaseUid,
                basicConsent,
                locationConsent,
                BuildConfig.VERSION_NAME,
                PRIVACY_POLICY_VERSION,
                consentId -> {
                    android.util.Log.d("SignupViewModel", "Privacy consent saved with ID: " + consentId);
                }
        );
    }

    public enum SignupState {
        IDLE,
        LOADING,
        SUCCESS,
        ERROR,
        VALIDATION_ERROR
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown repository executors to prevent thread leaks
        if (studentRepository != null) {
            studentRepository.shutdown();
        }
        if (privacyConsentRepository != null) {
            privacyConsentRepository.shutdown();
        }
    }
}
