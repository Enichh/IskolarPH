package com.example.iskolarphh;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.example.iskolarphh.database.AppDatabase;

public class IskolarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize database in background to avoid blocking during activity transitions
        new Handler(Looper.getMainLooper()).post(() -> {
            new Thread(() -> {
                AppDatabase.getInstance(this);
            }).start();
        });
    }
}
