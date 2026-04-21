package com.example.iskolarphh.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scholarships")
public class Scholarship {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "scholarship_name")
    private String scholarshipName;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "award_amount")
    private double awardAmount;

    @ColumnInfo(name = "provider_organization")
    private String providerOrganization;

    @ColumnInfo(name = "eligibility_criteria")
    private String eligibilityCriteria;

    @ColumnInfo(name = "application_deadline")
    private String applicationDeadline; // Stored as ISO 8601 string (e.g., "2026-12-31")

    @ColumnInfo(name = "location")
    private String location; // (e.g., "Luzon", "Visayas", "Mindanao")

    @ColumnInfo(name = "is_saved")
    private boolean isSaved;

    @ColumnInfo(name = "is_active")
    private boolean isActive;

    @ColumnInfo(name = "application_url")
    private String applicationUrl;

    // Constructors
    public Scholarship() {}

    public Scholarship(String scholarshipName, String description, double awardAmount,
                       String providerOrganization, String eligibilityCriteria,
                       String applicationDeadline, boolean isActive, String applicationUrl) {
        this.scholarshipName = scholarshipName;
        this.description = description;
        this.awardAmount = awardAmount;
        this.providerOrganization = providerOrganization;
        this.eligibilityCriteria = eligibilityCriteria;
        this.applicationDeadline = applicationDeadline;
        this.isActive = isActive;
        this.applicationUrl = applicationUrl;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getScholarshipName() { return scholarshipName; }
    public void setScholarshipName(String scholarshipName) { this.scholarshipName = scholarshipName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAwardAmount() { return awardAmount; }
    public void setAwardAmount(double awardAmount) { this.awardAmount = awardAmount; }

    public String getProviderOrganization() { return providerOrganization; }
    public void setProviderOrganization(String providerOrganization) { this.providerOrganization = providerOrganization; }

    public String getEligibilityCriteria() { return eligibilityCriteria; }
    public void setEligibilityCriteria(String eligibilityCriteria) { this.eligibilityCriteria = eligibilityCriteria; }

    public String getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(String applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isSaved() { return isSaved; }
    public void setSaved(boolean saved) { isSaved = saved; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }
}