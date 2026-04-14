package com.example.iskolarphh.service;

import android.content.Context;

import com.example.iskolarphh.callback.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocationManager {
    
    private static volatile LocationManager instance;
    private final ExecutorService executorService;
    private FusedLocationProviderClient fusedLocationClient;
    
    private LocationManager() {
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public static LocationManager getInstance() {
        if (instance == null) {
            synchronized (LocationManager.class) {
                if (instance == null) {
                    instance = new LocationManager();
                }
            }
        }
        return instance;
    }
    
    private FusedLocationProviderClient getFusedLocationClient(Context context) {
        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        }
        return fusedLocationClient;
    }
    
    public void requestSingleLocationUpdate(Context context, LocationCallback callback) {
        executorService.execute(() -> {
            try {
                if (!LocationPermissionHandler.hasLocationPermissions(context)) {
                    callback.onPermissionDenied();
                    return;
                }

                if (!isGpsEnabled(context)) {
                    callback.onLocationError("GPS is disabled. Please enable GPS in settings.");
                    return;
                }
                
                FusedLocationProviderClient client = getFusedLocationClient(context);
                client.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationRetrieved("", location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onLocationError("Unable to fetch location. Please try again.");
                    }
                }).addOnFailureListener(e -> callback.onLocationError("Failed to get location: " + e.getMessage()));
            } catch (SecurityException e) {
                callback.onPermissionDenied();
            } catch (Exception e) {
                callback.onLocationError("Location error: " + e.getMessage());
            }
        });
    }
    
    private boolean isGpsEnabled(Context context) {
        android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return false;
        }
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }
}
