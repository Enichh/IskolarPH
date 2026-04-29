package com.example.iskolarphh.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Privacy Consent entity for Philippine Data Privacy Act (RA 10173) compliance.
 * Stores user consent records with timestamps and version tracking.
 */
@Entity(
    tableName = "privacy_consents",
    foreignKeys = @ForeignKey(
        entity = Student.class,
        parentColumns = "firebase_uid",
        childColumns = "firebase_uid",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("firebase_uid")}
)
public class PrivacyConsent {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "firebase_uid")
    private String firebaseUid;

    @ColumnInfo(name = "basic_consent_given")
    private boolean basicConsentGiven;

    @ColumnInfo(name = "location_consent_given")
    private boolean locationConsentGiven;

    @ColumnInfo(name = "consent_timestamp")
    private String consentTimestamp;

    @ColumnInfo(name = "app_version")
    private String appVersion;

    @ColumnInfo(name = "privacy_policy_version")
    private String privacyPolicyVersion;

    @ColumnInfo(name = "withdrawn")
    private boolean withdrawn;

    @ColumnInfo(name = "withdrawal_timestamp")
    private String withdrawalTimestamp;

    // Default constructor
    public PrivacyConsent() {}

    // Constructor for initial consent
    public PrivacyConsent(String firebaseUid, boolean basicConsentGiven, boolean locationConsentGiven,
                          String consentTimestamp, String appVersion, String privacyPolicyVersion) {
        this.firebaseUid = firebaseUid;
        this.basicConsentGiven = basicConsentGiven;
        this.locationConsentGiven = locationConsentGiven;
        this.consentTimestamp = consentTimestamp;
        this.appVersion = appVersion;
        this.privacyPolicyVersion = privacyPolicyVersion;
        this.withdrawn = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public boolean isBasicConsentGiven() { return basicConsentGiven; }
    public void setBasicConsentGiven(boolean basicConsentGiven) { this.basicConsentGiven = basicConsentGiven; }

    public boolean isLocationConsentGiven() { return locationConsentGiven; }
    public void setLocationConsentGiven(boolean locationConsentGiven) { this.locationConsentGiven = locationConsentGiven; }

    public String getConsentTimestamp() { return consentTimestamp; }
    public void setConsentTimestamp(String consentTimestamp) { this.consentTimestamp = consentTimestamp; }

    public String getAppVersion() { return appVersion; }
    public void setAppVersion(String appVersion) { this.appVersion = appVersion; }

    public String getPrivacyPolicyVersion() { return privacyPolicyVersion; }
    public void setPrivacyPolicyVersion(String privacyPolicyVersion) { this.privacyPolicyVersion = privacyPolicyVersion; }

    public boolean isWithdrawn() { return withdrawn; }
    public void setWithdrawn(boolean withdrawn) { this.withdrawn = withdrawn; }

    public String getWithdrawalTimestamp() { return withdrawalTimestamp; }
    public void setWithdrawalTimestamp(String withdrawalTimestamp) { this.withdrawalTimestamp = withdrawalTimestamp; }
}
