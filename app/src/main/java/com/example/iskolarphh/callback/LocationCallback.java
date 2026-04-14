package com.example.iskolarphh.callback;

public interface LocationCallback {
    
    void onLocationRetrieved(String location, double latitude, double longitude);
    
    void onLocationError(String errorMessage);
    
    void onPermissionDenied();
}
