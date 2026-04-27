package com.example.iskolarphh.service;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.ui.LocationPermissionDialog;
import com.example.iskolarphh.util.LocationConstants;
import com.example.iskolarphh.util.LocationPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LocationFlowManager {

    private final Context context;
    private final LocationPermissionHandler permissionHandler;
    private final LocationManager locationManager;
    private final GeocoderService geocoderService;
    private final StudentRepository studentRepository;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public LocationFlowManager(Context context, LocationManager locationManager, GeocoderService geocoderService, StudentRepository studentRepository) {
        this.context = context;
        this.permissionHandler = LocationPermissionHandler.getInstance();
        this.locationManager = locationManager;
        this.geocoderService = geocoderService;
        this.studentRepository = studentRepository;
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void checkAndRequestLocationPermission(Activity activity, LocationPermissionDialog.LocationPermissionCallback callback) {
        LocationPermissionDialog.show(context, callback);
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults, LocationCallback locationCallback) {
        if (requestCode == LocationConstants.LOCATION_PERMISSION_REQUEST_CODE) {
            LocationPreferences.setLocationGranted(context, true);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchAndSaveLocation(locationCallback);
            } else {
                LocationPreferences.saveLocation(context, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
                if (locationCallback != null) {
                    locationCallback.onPermissionDenied();
                }
            }
        }
    }

    public void fetchAndSaveLocation(LocationCallback locationCallback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            notifyError(locationCallback, "User not authenticated");
            return;
        }

        // Use modern ExecutorService to avoid blocking main thread
        executorService.execute(() -> {
            locationManager.requestSingleLocationUpdate(context, new LocationCallback() {
                @Override
                public void onLocationRetrieved(String location, double latitude, double longitude) {
                    handleGeocoding(locationCallback, firebaseUser, latitude, longitude);
                }

                @Override
                public void onLocationError(String errorMessage) {
                    saveDefaultLocationAndNotify(locationCallback, errorMessage);
                }

                @Override
                public void onPermissionDenied() {
                    saveDefaultLocationAndNotify(locationCallback, "Permission denied");
                }
            });
        });
    }

    private void handleGeocoding(LocationCallback locationCallback, FirebaseUser firebaseUser, double latitude, double longitude) {
        geocoderService.getLocationFromCoordinates(context, latitude, longitude, new LocationCallback() {
            @Override
            public void onLocationRetrieved(String locationString, double lat, double lng) {
                saveLocationAndNotify(locationCallback, firebaseUser, locationString, lat, lng);
            }

            @Override
            public void onLocationError(String errorMessage) {
                saveDefaultLocationAndNotify(locationCallback, errorMessage);
            }

            @Override
            public void onPermissionDenied() {
                saveDefaultLocationAndNotify(locationCallback, "Permission denied");
            }
        });
    }

    private void saveLocationAndNotify(LocationCallback locationCallback, FirebaseUser firebaseUser, String locationString, double lat, double lng) {
        LocationPreferences.saveLocation(context, locationString, LocationConstants.LOCATION_SOURCE_GPS);
        studentRepository.updateLocation(firebaseUser.getUid(), locationString, rowsAffected -> {
            if (locationCallback != null) {
                locationCallback.onLocationRetrieved(locationString, lat, lng);
            }
        });
    }

    private void saveDefaultLocationAndNotify(LocationCallback locationCallback, String errorMessage) {
        LocationPreferences.saveLocation(context, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
        notifyError(locationCallback, errorMessage);
    }

    private void notifyError(LocationCallback locationCallback, String errorMessage) {
        if (locationCallback != null) {
            locationCallback.onLocationError(errorMessage);
        }
    }

    public boolean hasLocationPermissions() {
        return LocationPermissionHandler.hasLocationPermissions(context);
    }

    public void requestLocationPermissions(Activity activity, int requestCode) {
        LocationPermissionHandler.checkAndRequestLocationPermissions(activity, requestCode);
    }
}
