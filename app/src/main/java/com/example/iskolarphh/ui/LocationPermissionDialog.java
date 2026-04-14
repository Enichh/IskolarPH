package com.example.iskolarphh.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LocationPermissionDialog extends AlertDialog {

    private LocationPermissionCallback callback;

    public LocationPermissionDialog(@NonNull Context context, LocationPermissionCallback callback) {
        super(context);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static void show(Context context, LocationPermissionCallback callback) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle("Location Permission")
                .setMessage("Allow IskolarPH to use your current location to find scholarships near you?")
                .setPositiveButton("Allow", (dialog, which) -> {
                    if (callback != null) {
                        callback.onPermissionAllowed();
                    }
                })
                .setNegativeButton("Deny", (dialog, which) -> {
                    if (callback != null) {
                        callback.onPermissionDenied();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public interface LocationPermissionCallback {
        void onPermissionAllowed();
        void onPermissionDenied();
    }
}
