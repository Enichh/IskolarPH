package com.example.iskolarphh.service;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.iskolarphh.util.LocationConstants;

public class LocationPermissionHandler {
    
    private static LocationPermissionHandler instance;
    
    private LocationPermissionHandler() {
    }
    
    public static LocationPermissionHandler getInstance() {
        if (instance == null) {
            instance = new LocationPermissionHandler();
        }
        return instance;
    }
    
    public static boolean hasLocationPermissions(Context context) {
        int fineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        return fineLocation == PackageManager.PERMISSION_GRANTED && 
               coarseLocation == PackageManager.PERMISSION_GRANTED;
    }
    
    public static void checkAndRequestLocationPermissions(Activity activity, int requestCode) {
        if (!hasLocationPermissions(activity)) {
            ActivityCompat.requestPermissions(
                activity,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                requestCode
            );
        }
    }
    
    public static void checkAndRequestLocationPermissions(Activity activity) {
        checkAndRequestLocationPermissions(activity, LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
    }
    
    public static boolean shouldShowRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION);
    }
}
