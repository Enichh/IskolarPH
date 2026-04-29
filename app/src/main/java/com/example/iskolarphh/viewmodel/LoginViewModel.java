package com.example.iskolarphh.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginViewModel extends AndroidViewModel {

    private static final String TAG = "LoginViewModel";
    private static final String PREFS_NAME = "EmailVerificationPrefs";
    private static final String KEY_VERIFIED_EMAILS = "verified_emails";
    private static final String KEY_LOGIN_SESSION = "login_session";
    private static final String KEY_SESSION_EMAIL = "session_email";
    private static final String KEY_SESSION_TIMESTAMP = "session_timestamp";
    private static final long SESSION_DURATION_MS = 24 * 60 * 60 * 1000; // 24 hours
    private final FirebaseAuth mAuth;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(Application application) {
        super(application);
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if user has a valid cached session
            if (hasValidCachedSession(currentUser.getEmail())) {
                Log.d(TAG, "User has valid cached session, navigating to main");
                loginState.postValue(LoginState.VERIFIED);
                return;
            }
            
            // Check if email is already verified
            if (isEmailVerified(currentUser.getEmail())) {
                Log.d(TAG, "User already verified, navigating to main");
                loginState.postValue(LoginState.VERIFIED);
            } else {
                // User is logged in but still needs verification code
                loginState.postValue(LoginState.NOT_VERIFIED);
            }
        } else {
            loginState.postValue(LoginState.LOGGED_OUT);
        }
    }

    private boolean isEmailVerified(String email) {
        if (email == null) return false;
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String verifiedEmails = prefs.getString(KEY_VERIFIED_EMAILS, "");
        return verifiedEmails.contains(email + ",");
    }
    
    private boolean hasValidCachedSession(String email) {
        if (email == null) return false;
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        boolean hasSession = prefs.getBoolean(KEY_LOGIN_SESSION, false);
        String sessionEmail = prefs.getString(KEY_SESSION_EMAIL, "");
        long sessionTimestamp = prefs.getLong(KEY_SESSION_TIMESTAMP, 0);
        
        long currentTime = System.currentTimeMillis();
        
        return hasSession && 
               email.equals(sessionEmail) && 
               (currentTime - sessionTimestamp) < SESSION_DURATION_MS;
    }
    
    public void cacheLoginSession(String email) {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LOGIN_SESSION, true);
        editor.putString(KEY_SESSION_EMAIL, email);
        editor.putLong(KEY_SESSION_TIMESTAMP, System.currentTimeMillis());
        editor.apply();
        Log.d(TAG, "Login session cached for email: " + email);
    }
    
    public void clearCachedSession() {
        SharedPreferences prefs = getApplication().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_LOGIN_SESSION);
        editor.remove(KEY_SESSION_EMAIL);
        editor.remove(KEY_SESSION_TIMESTAMP);
        editor.apply();
        Log.d(TAG, "Cached login session cleared");
    }

    public void attemptLogin(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Please enter the email address you used to register.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.postValue("Please enter your password.");
            return;
        }

        if (password.length() < 6) {
            errorMessage.postValue("Your password should be at least 6 characters. Please check and try again.");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Login successful, requiring verification code");
                            loginState.postValue(LoginState.NOT_VERIFIED); // Always require verification
                        }
                    } else {
                        String error = "We couldn't sign you in. Please check your connection and try again.";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getClass().getSimpleName();
                            switch (errorCode) {
                                case "FirebaseAuthInvalidUserException":
                                    error = "We couldn't find an account with this email. Please check your email or sign up for a new account.";
                                    break;
                                case "FirebaseAuthInvalidCredentialsException":
                                    error = "The password doesn't match. Please try again or reset your password if you've forgotten it.";
                                    break;
                                default:
                                    error = "Sign in issue: " + task.getException().getMessage();
                                    break;
                            }
                        }
                        errorMessage.postValue(error);
                        loginState.postValue(LoginState.ERROR);
                    }
                });
    }

    public void sendPasswordResetEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Please enter your email address so we can send you a reset link.");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        errorMessage.postValue("Password reset email sent to " + email);
                        loginState.postValue(LoginState.RESET_SENT);
                    } else {
                        errorMessage.postValue("We couldn't send the reset email. " + task.getException().getMessage());
                        loginState.postValue(LoginState.ERROR);
                    }
                });
    }

    public enum LoginState {
        LOGGED_OUT,
        SUCCESS,
        ERROR,
        VERIFIED,
        NOT_VERIFIED,
        RESET_SENT
    }
}
