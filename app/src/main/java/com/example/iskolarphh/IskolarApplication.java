package com.example.iskolarphh;

import android.app.Application;

import com.example.iskolarphh.database.AppDatabase;

public class IskolarApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize database immediately in background to avoid blocking UI thread
        // This prevents the 5+ second freeze when first Activity accesses database
        AppDatabase.initializeAsync(this);
    }
}
