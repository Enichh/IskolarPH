package com.example.iskolarphh.viewmodel;

import android.app.Application;
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
            // User is logged in but still needs verification code
            loginState.postValue(LoginState.NOT_VERIFIED);
        } else {
            loginState.postValue(LoginState.LOGGED_OUT);
        }
    }

    public void attemptLogin(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            errorMessage.postValue("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.postValue("Password is required");
            return;
        }

        if (password.length() < 6) {
            errorMessage.postValue("Password must be at least 6 characters");
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
                        String error = "Authentication failed";
                        if (task.getException() != null) {
                            String errorCode = task.getException().getClass().getSimpleName();
                            switch (errorCode) {
                                case "FirebaseAuthInvalidUserException":
                                    error = "No account found with this email";
                                    break;
                                case "FirebaseAuthInvalidCredentialsException":
                                    error = "Invalid password";
                                    break;
                                default:
                                    error = task.getException().getMessage();
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
            errorMessage.postValue("Please enter your email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        errorMessage.postValue("Password reset email sent to " + email);
                        loginState.postValue(LoginState.RESET_SENT);
                    } else {
                        errorMessage.postValue("Failed to send reset email: " + task.getException().getMessage());
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
