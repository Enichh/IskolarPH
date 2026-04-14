package com.example.iskolarphh.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;

import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.util.LocationConstants;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeocoderService {
    
    private static volatile GeocoderService instance;
    private final ExecutorService executorService;
    
    private GeocoderService() {
        this.executorService = Executors.newSingleThreadExecutor();
    }
    
    public static GeocoderService getInstance() {
        if (instance == null) {
            synchronized (GeocoderService.class) {
                if (instance == null) {
                    instance = new GeocoderService();
                }
            }
        }
        return instance;
    }
    
    public void getLocationFromCoordinates(Context context, double latitude, double longitude, LocationCallback callback) {
        executorService.execute(() -> {
            try {
                if (!Geocoder.isPresent()) {
                    callback.onLocationError("Geocoder service not available");
                    return;
                }
                
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, LocationConstants.MAX_GEOCODER_RESULTS);
                
                if (addresses == null || addresses.isEmpty()) {
                    callback.onLocationError("No address found for given coordinates");
                    return;
                }
                
                Address address = addresses.get(0);
                String locationString = buildLocationString(address);
                callback.onLocationRetrieved(locationString, latitude, longitude);
                
            } catch (IOException e) {
                callback.onLocationError("Geocoder failed: " + e.getMessage());
            } catch (Exception e) {
                callback.onLocationError("Error converting coordinates: " + e.getMessage());
            }
        });
    }
    
    private String buildLocationString(Address address) {
        StringBuilder sb = new StringBuilder();
        
        if (address.getLocality() != null) {
            sb.append(address.getLocality());
        }
        if (address.getAdminArea() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getAdminArea());
        }
        if (address.getCountryName() != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCountryName());
        }
        
        return sb.length() > 0 ? sb.toString() : LocationConstants.DEFAULT_LOCATION;
    }
    
    public void shutdown() {
        executorService.shutdown();
    }
}
