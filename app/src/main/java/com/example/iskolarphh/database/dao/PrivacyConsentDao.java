package com.example.iskolarphh.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.iskolarphh.database.entity.PrivacyConsent;

/**
 * DAO for Privacy Consent operations.
 * Manages consent records for Philippine Data Privacy Act compliance.
 */
@Dao
public interface PrivacyConsentDao {

    @Insert
    long insert(PrivacyConsent consent);

    @Update
    int update(PrivacyConsent consent);

    @Query("SELECT * FROM privacy_consents WHERE firebase_uid = :firebaseUid AND withdrawn = 0 LIMIT 1")
    LiveData<PrivacyConsent> getActiveConsentForUser(String firebaseUid);

    @Query("SELECT * FROM privacy_consents WHERE firebase_uid = :firebaseUid AND withdrawn = 0 LIMIT 1")
    PrivacyConsent getActiveConsentForUserSync(String firebaseUid);

    @Query("SELECT EXISTS(SELECT 1 FROM privacy_consents WHERE firebase_uid = :firebaseUid AND basic_consent_given = 1 AND withdrawn = 0)")
    boolean hasValidBasicConsent(String firebaseUid);

    @Query("SELECT EXISTS(SELECT 1 FROM privacy_consents WHERE firebase_uid = :firebaseUid AND location_consent_given = 1 AND withdrawn = 0)")
    boolean hasValidLocationConsent(String firebaseUid);

    @Query("UPDATE privacy_consents SET withdrawn = 1, withdrawal_timestamp = :timestamp WHERE firebase_uid = :firebaseUid AND withdrawn = 0")
    int withdrawConsent(String firebaseUid, String timestamp);
}
