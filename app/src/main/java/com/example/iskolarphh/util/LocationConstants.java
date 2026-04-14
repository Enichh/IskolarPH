package com.example.iskolarphh.util;

public final class LocationConstants {
    
    private LocationConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    
    public static final String PREF_LOCATION_GRANTED = "location_granted";
    public static final String PREF_LAST_LOCATION = "last_location";
    public static final String PREF_LOCATION_SOURCE = "location_source";
    
    public static final int MAX_GEOCODER_RESULTS = 1;
    public static final String DEFAULT_LOCATION = "Philippines";
    
    public static final String LOCATION_SOURCE_GPS = "GPS";
    public static final String LOCATION_SOURCE_MANUAL = "MANUAL";
}
