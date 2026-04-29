package com.example.iskolarphh.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.iskolarphh.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

/**
 * Centralized dialog manager for consistent user feedback across the app.
 * Provides friendly error messages, confirmation dialogs, and success notifications.
 * All operations are thread-safe and automatically run on the UI thread.
 */
public class DialogManager {

    private static final long DIALOG_DISMISS_DELAY_MS = 1500;

    private DialogManager() {
        // Private constructor to prevent instantiation
    }

    private static boolean isSafeToDisplayDialog(Context context) {
        if (context == null) return false;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            return !activity.isFinishing() && !activity.isDestroyed();
        }
        return true;
    }

    /**
     * Shows a friendly error dialog with a clear message and actionable guidance.
     *
     * @param context The context to show the dialog in
     * @param title The dialog title
     * @param message The error message explaining what went wrong
     * @param actionLabel Optional action button label (null for no action)
     * @param actionCallback Optional action callback (null for no action)
     */
    public static void showErrorDialog(@NonNull Context context,
                                       @NonNull String title,
                                       @NonNull String message,
                                       @Nullable String actionLabel,
                                       @Nullable Runnable actionCallback) {
        runOnUiThread(() -> {
            if (!isSafeToDisplayDialog(context)) return;

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> {
                        if (actionCallback != null) {
                            actionCallback.run();
                        }
                    });

            if (actionLabel != null && !actionLabel.isEmpty()) {
                builder.setNegativeButton(actionLabel, (dialog, which) -> {
                    if (actionCallback != null) {
                        actionCallback.run();
                    }
                });
            }

            builder.show();
        });
    }

    /**
     * Shows a simplified error dialog with just an OK button.
     */
    public static void showErrorDialog(@NonNull Context context,
                                       @NonNull String title,
                                       @NonNull String message) {
        showErrorDialog(context, title, message, null, null);
    }

    /**
     * Shows a friendly validation error dialog for form validation failures.
     */
    public static void showValidationError(@NonNull Context context,
                                          @NonNull String fieldName,
                                          @NonNull String guidance) {
        String title = "Please check your " + fieldName;
        String message = guidance + "\n\nNeed help? Tap OK to continue.";
        showErrorDialog(context, title, message);
    }

    /**
     * Shows a confirmation dialog for important actions.
     *
     * @param context The context to show the dialog in
     * @param title The dialog title
     * @param message The confirmation message explaining the action
     * @param confirmButtonText The text for the confirm button
     * @param cancelButtonText The text for the cancel button
     * @param onConfirm Callback when user confirms
     * @param onCancel Callback when user cancels (optional)
     */
    public static void showConfirmationDialog(@NonNull Context context,
                                               @NonNull String title,
                                               @NonNull String message,
                                               @NonNull String confirmButtonText,
                                               @NonNull String cancelButtonText,
                                               @NonNull Runnable onConfirm,
                                               @Nullable Runnable onCancel) {
        runOnUiThread(() -> {
            if (!isSafeToDisplayDialog(context)) return;

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(confirmButtonText, (dialog, which) -> {
                        onConfirm.run();
                    })
                    .setNegativeButton(cancelButtonText, (dialog, which) -> {
                        if (onCancel != null) {
                            onCancel.run();
                        }
                    })
                    .setCancelable(false);

            builder.show();
        });
    }

    /**
     * Shows a success dialog that auto-dismisses after a short delay.
     *
     * @param context The context to show the dialog in
     * @param title The success title
     * @param message The success message
     */
    public static void showSuccessDialog(@NonNull Context context,
                                          @NonNull String title,
                                          @NonNull String message) {
        runOnUiThread(() -> {
            if (!isSafeToDisplayDialog(context)) return;

            AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(R.string.dialog_ok, null)
                    .create();

            dialog.show();

            // Auto-dismiss after delay
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dialog.isShowing() && isSafeToDisplayDialog(context)) {
                    dialog.dismiss();
                }
            }, DIALOG_DISMISS_DELAY_MS);
        });
    }

    /**
     * Shows a success Snackbar (preferred over Toast for success messages).
     */
    public static void showSuccessSnackbar(@NonNull android.view.View view,
                                              @NonNull String message) {
        runOnUiThread(() -> {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        });
    }

    /**
     * Shows a friendly network error dialog with retry option.
     */
    public static void showNetworkError(@NonNull Context context,
                                        @NonNull Runnable onRetry) {
        showErrorDialog(context,
                "Connection Issue",
                "We're having trouble connecting to our servers. Please check your internet connection and try again.",
                "Retry",
                onRetry);
    }

    /**
     * Shows an authentication error dialog with clear guidance.
     */
    public static void showAuthError(@NonNull Context context,
                                      @NonNull String specificError) {
        String message = specificError + "\n\n" +
                "Please double-check your information and try again. " +
                "If you continue to have trouble, you can reset your password.";
        showErrorDialog(context, "Sign In Issue", message);
    }

    /**
     * Shows a logout confirmation dialog.
     */
    public static void showLogoutConfirmation(@NonNull Context context,
                                               @NonNull Runnable onConfirm,
                                               @Nullable Runnable onCancel) {
        showConfirmationDialog(context,
                "Sign Out?",
                "You'll need to sign back in with your email and password to access your account.",
                "Sign Out",
                "Stay Signed In",
                onConfirm,
                onCancel);
    }

    /**
     * Shows a scholarship save confirmation dialog.
     */
    public static void showSaveScholarshipConfirmation(@NonNull Context context,
                                                        @NonNull String scholarshipName,
                                                        boolean isSaving,
                                                        @NonNull Runnable onConfirm,
                                                        @Nullable Runnable onCancel) {
        String title = isSaving ? "Save Scholarship?" : "Remove from Saved?";
        String message = isSaving
                ? "\"" + scholarshipName + "\" will be added to your saved scholarships for easy access."
                : "\"" + scholarshipName + "\" will be removed from your saved scholarships.";
        String confirmText = isSaving ? "Save" : "Remove";
        String cancelText = "Cancel";

        showConfirmationDialog(context, title, message, confirmText, cancelText, onConfirm, onCancel);
    }

    /**
     * Shows a profile save success notification.
     */
    public static void showProfileSaveSuccess(@NonNull Context context,
                                               @Nullable android.view.View anchorView) {
        if (anchorView != null) {
            showSuccessSnackbar(anchorView, "Profile updated successfully!");
        } else {
            showSuccessDialog(context, "Success", "Your profile has been updated.");
        }
    }

    /**
     * Helper method to ensure UI operations run on main thread.
     */
    private static void runOnUiThread(Runnable action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action.run();
        } else {
            new Handler(Looper.getMainLooper()).post(action);
        }
    }
}
