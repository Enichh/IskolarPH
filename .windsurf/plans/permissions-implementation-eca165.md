# IskolarPH Permissions Implementation Plan

This plan implements camera permission for profile photos, GPS location for scholarship filtering, and biometric fingerprint authentication for quick login, following Android best practices with point-of-use permission requests.

## 1. Permission Architecture & Best Practices

**Point-of-Use Permission Model (Recommended by Android):**
- Request camera permission when user taps "Upload Photo" in profile
- Request location permission when user opens scholarship catalog or enables location-based filtering
- Request biometric permission only on devices with fingerprint hardware, during optional fingerprint setup

**Permission Rationale Flow:**
1. Check if permission is granted
2. If not granted, show explanation dialog explaining why the feature needs it
3. Request permission with system dialog
4. Handle user response (grant, deny, or "don't ask again")

**Required Permissions in AndroidManifest.xml:**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.USE_BIOMETRIC" />
```

## 2. Camera Permission for Profile Photos

**Implementation:**
- Create `PermissionManager` utility class to handle all permission requests
- Add camera permission request to `ProfileFragment` when user taps "Change Photo"
- Use Android Photo Picker (Android 13+) or Camera Intent for photo capture
- Store photos in Firebase Storage with Firebase UID as folder key

**User Flow:**
1. User navigates to Profile
2. Taps "Upload Photo" button
3. System shows permission dialog if not previously granted
4. If denied, show rationale explaining why photo is needed
5. After grant, open camera or gallery picker
6. Upload selected photo to Firebase Storage
7. Save photo URL to Room database (Student.photoUrl field)

**Technical Implementation:**
```java
// Check and request camera permission
if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
        != PackageManager.PERMISSION_GRANTED) {
    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
        showRationaleDialog("Camera access is needed to take profile photos");
    } else {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
    }
}
```

## 3. GPS Location for Scholarship Filtering

**Implementation:**
- Request location permission when user opens CatalogFragment or enables location filter
- Use FusedLocationProviderClient for accurate location with battery efficiency
- Cache location locally to avoid repeated GPS requests
- Filter scholarships by calculating distance from user location

**User Flow:**
1. User opens Catalog (scholarship list)
2. System requests location permission with rationale: "Location helps find scholarships near you"
3. If granted, get current location and filter scholarships within 50km radius
4. Show location indicator in UI with option to manually change location
5. Store location preference in Room database

**Technical Implementation:**
```java
// Location permission with coarse + fine options
String[] locationPermissions = {
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
};

// Use FusedLocationProvider for accuracy
FusedLocationProviderClient fusedLocationClient = 
    LocationServices.getFusedLocationProviderClient(this);

fusedLocationClient.getLastLocation()
    .addOnSuccessListener(location -> {
        if (location != null) {
            filterScholarshipsByLocation(location.getLatitude(), location.getLongitude());
        }
    });
```

**Distance Calculation:**
- Haversine formula for accurate distance between coordinates
- Filter scholarships where distance <= user preference (default 50km)
- Allow users to adjust search radius in settings

## 4. Biometric Fingerprint Authentication

**Implementation:**
- Add Biometric library dependency
- Create optional "Enable Fingerprint" toggle in settings
- Store encrypted credentials using Android Keystore (never store raw password)
- Use BiometricPrompt API for consistent UI across devices

**User Flow:**
1. After successful email/password login, ask: "Enable fingerprint for faster login?"
2. If user agrees, show biometric prompt to verify
3. On success, encrypt and store credentials in Keystore
4. Next login: show fingerprint option alongside email/password
5. On fingerprint success, auto-login with stored credentials

**Security Best Practices:**
- Never store plaintext passwords
- Use Android Keystore with hardware-backed encryption when available
- Implement CryptoObject with cipher for authentication
- Allow users to disable fingerprint anytime in settings
- Clear stored credentials on app uninstall or explicit logout

**Technical Implementation:**
```java
// Check device supports biometric
BiometricManager biometricManager = BiometricManager.from(context);
if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) 
        == BiometricManager.BIOMETRIC_SUCCESS) {
    // Device supports fingerprint
}

// Show biometric prompt
BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor, callback);
BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
    .setTitle("Login with Fingerprint")
    .setSubtitle("Touch the fingerprint sensor")
    .setNegativeButtonText("Use Password")
    .build();
biometricPrompt.authenticate(promptInfo);
```

## 5. PermissionManager Utility Class

Create a centralized permission handler to avoid code duplication:

```java
public class PermissionManager {
    
    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
            == PackageManager.PERMISSION_GRANTED;
    }
    
    public static boolean hasBiometricPermission(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) 
            == BiometricManager.BIOMETRIC_SUCCESS;
    }
    
    public static void showRationaleDialog(Context context, String message, 
            DialogInterface.OnClickListener onProceed) {
        new AlertDialog.Builder(context)
            .setTitle("Permission Required")
            .setMessage(message)
            .setPositiveButton("Grant", onProceed)
            .setNegativeButton("Cancel", null)
            .show();
    }
}
```

## 6. Dependencies to Add

```kotlin
// In app/build.gradle.kts
dependencies {
    // Existing Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage") // For profile photos
    
    // Biometric
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Location Services
    implementation("com.google.android.gms:play-services-location:21.0.1")
    
    // Image loading (for profile photos)
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
```

## 7. Database Schema Updates

Add fields to Student entity:
```java
@ColumnInfo(name = "photo_url")
private String photoUrl;

@ColumnInfo(name = "latitude")
private Double latitude;

@ColumnInfo(name = "longitude")
private Double longitude;

@ColumnInfo(name = "fingerprint_enabled")
private boolean fingerprintEnabled;
```

## 8. Implementation Phases

**Phase 1: Camera Permission**
1. Add CAMERA permission to manifest
2. Create PermissionManager utility
3. Integrate photo picker in ProfileFragment
4. Upload to Firebase Storage
5. Load and display photo with Glide

**Phase 2: Location Permission**
1. Add location permissions to manifest
2. Integrate FusedLocationProvider in CatalogFragment
3. Calculate scholarship distances
4. Add location filter UI toggle
5. Cache location preferences

**Phase 3: Biometric Authentication**
1. Add biometric dependency and permission
2. Create BiometricAuthManager class
3. Add "Enable Fingerprint" toggle in settings
4. Store encrypted credentials in Keystore
5. Show biometric prompt on login (if enabled)

## 9. Testing Strategy

- Test permissions on Android 10, 12, 13, 14 (different permission behaviors)
- Test "Don't ask again" scenario (user must go to Settings)
- Test biometric on devices with/without fingerprint hardware
- Test location permission with GPS on/off
- Test permission denial flows (graceful degradation)

## 10. Edge Cases Handled

- User denies permission → Show feature unavailable message with settings link
- User selects "Don't ask again" → Guide to app settings manually
- Biometric hardware unavailable → Hide fingerprint option automatically
- Location services disabled → Prompt to enable GPS
- No internet during photo upload → Queue for retry with WorkManager
- Biometric authentication fails → Fallback to password login

---
*Plan created for: eca165*
