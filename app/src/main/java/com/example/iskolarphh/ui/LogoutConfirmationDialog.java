package com.example.iskolarphh.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.iskolarphh.R;

public class LogoutConfirmationDialog extends DialogFragment {

    public interface LogoutConfirmationListener {
        void onLogoutConfirmed();
        void onLogoutCancelled();
    }

    private LogoutConfirmationListener listener;

    public void setLogoutConfirmationListener(LogoutConfirmationListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create custom dialog without title
        Dialog dialog = new Dialog(requireContext(), R.style.CustomDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_logout_confirmation);

        // Set dialog width and make it non-cancelable from outside clicks
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.setCanceledOnTouchOutside(false);

        // Initialize views
        TextView tvTitle = dialog.findViewById(R.id.tvLogoutTitle);
        TextView tvMessage = dialog.findViewById(R.id.tvLogoutMessage);
        Button btnCancel = dialog.findViewById(R.id.btnCancelLogout);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirmLogout);

        // Set click listeners
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLogoutCancelled();
            }
            dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLogoutConfirmed();
            }
            dismiss();
        });

        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
