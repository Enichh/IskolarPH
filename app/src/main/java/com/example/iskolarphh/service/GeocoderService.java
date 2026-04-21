package com.example.iskolarphh.service;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Geocoder.GeocodeListener;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

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
        if (!Geocoder.isPresent()) {
            callback.onLocationError("Geocoder service not available");
            return;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocationAsync(geocoder, latitude, longitude, callback);
        } else {
            executorService.execute(() -> {
                try {
                    @SuppressWarnings("deprecation")
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, LocationConstants.MAX_GEOCODER_RESULTS);
                    handleGeocodingResult(addresses, latitude, longitude, callback);
                } catch (IOException e) {
                    callback.onLocationError("Geocoder failed: " + e.getMessage());
                } catch (Exception e) {
                    callback.onLocationError("Error converting coordinates: " + e.getMessage());
                }
            });
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private void getFromLocationAsync(Geocoder geocoder, double latitude, double longitude, LocationCallback callback) {
        geocoder.getFromLocation(latitude, longitude, LocationConstants.MAX_GEOCODER_RESULTS, new GeocodeListener() {
            @Override
            public void onGeocode(@NonNull List<Address> addresses) {
                handleGeocodingResult(addresses, latitude, longitude, callback);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                callback.onLocationError("Geocoder failed: " + errorMessage);
            }
        });
    }

    private void handleGeocodingResult(List<Address> addresses, double latitude, double longitude, LocationCallback callback) {
        if (addresses == null || addresses.isEmpty()) {
            callback.onLocationError("No address found for given coordinates");
            return;
        }

        Address address = addresses.get(0);
        String locationString = buildLocationString(address);
        callback.onLocationRetrieved(locationString, latitude, longitude);
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
