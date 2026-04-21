package com.example.iskolarphh.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity(tableName = "students")
public class Student {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "firebase_uid")
    private String firebaseUid;

    @ColumnInfo(name = "first_name")
    private String firstName;

    @ColumnInfo(name = "last_name")
    private String lastName;

    @ColumnInfo(name = "middle_initial")
    private String middleInitial;

    @ColumnInfo(name = "location")
    private String location;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "gpa")
    private double gpa;

    @ColumnInfo(name = "course")
    private String course; // e.g., "BSCS", "BSECE", "STEM"

    // Constructors
    public Student() {}

    // Constructor for initial registration with Firebase
    public Student(String firebaseUid, String firstName, String lastName, String middleInitial, 
                   String location, String email, String password, double gpa) {
        this.firebaseUid = firebaseUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
        this.location = location;
        this.email = email;
        this.setPassword(password);
        this.gpa = gpa;
    }

    // Constructor for Firebase registration (used by SignupActivity)
    public Student(String firebaseUid, String firstName, String lastName, String middleInitial, 
                   String location, double gpa) {
        this.firebaseUid = firebaseUid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.middleInitial = middleInitial;
        this.location = location;
        this.gpa = gpa;
    }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getMiddleInitial() { return middleInitial; }
    public void setMiddleInitial(String middleInitial) { this.middleInitial = middleInitial; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    // Setter for password that hashes the input
    public void setPassword(String password) {
        this.passwordHash = hashPassword(password);
    }

    // Getter for password (returns hash - only for comparison purposes)
    public String getPassword() {
        return passwordHash;
    }

    public double getGpa() { return gpa; }
    public void setGpa(double gpa) { this.gpa = gpa; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    // Password hashing method using SHA-256
    private String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}