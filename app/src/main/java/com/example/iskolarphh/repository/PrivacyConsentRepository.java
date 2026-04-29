package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.iskolarphh.callback.InsertCallback;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.PrivacyConsentDao;
import com.example.iskolarphh.database.entity.PrivacyConsent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for Privacy Consent operations.
 * Manages consent records for Philippine Data Privacy Act (RA 10173) compliance.
 */
public class PrivacyConsentRepository {

    private final PrivacyConsentDao privacyConsentDao;
    private final ExecutorService executorService;
    private volatile boolean isShutdown = false;

    public PrivacyConsentRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.privacyConsentDao = database.privacyConsentDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Save privacy consent for a user
     */
    public void saveConsent(String firebaseUid, boolean basicConsent, boolean locationConsent,
                           String appVersion, String privacyPolicyVersion, InsertCallback callback) {
        executorService.execute(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());

            PrivacyConsent consent = new PrivacyConsent(
                    firebaseUid,
                    basicConsent,
                    locationConsent,
                    timestamp,
                    appVersion,
                    privacyPolicyVersion
            );

            long id = privacyConsentDao.insert(consent);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onInsertComplete(id);
                }
            });
        });
    }

    /**
     * Get active consent for a user
     */
    public LiveData<PrivacyConsent> getActiveConsent(String firebaseUid) {
        return privacyConsentDao.getActiveConsentForUser(firebaseUid);
    }

    /**
     * Check if user has valid basic consent
     */
    public boolean hasValidBasicConsent(String firebaseUid) {
        return privacyConsentDao.hasValidBasicConsent(firebaseUid);
    }

    /**
     * Check if user has valid location consent
     */
    public boolean hasValidLocationConsent(String firebaseUid) {
        return privacyConsentDao.hasValidLocationConsent(firebaseUid);
    }

    /**
     * Withdraw user consent (for privacy compliance)
     */
    public void withdrawConsent(String firebaseUid, Runnable callback) {
        if (isShutdown) return;
        executorService.execute(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
            privacyConsentDao.withdrawConsent(firebaseUid, timestamp);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.run();
                }
            });
        });
    }

    /**
     * Shutdown the executor service. Call this when the repository is no longer needed.
     */
    public void shutdown() {
        isShutdown = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
