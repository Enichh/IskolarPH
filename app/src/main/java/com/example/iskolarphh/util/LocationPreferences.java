package com.example.iskolarphh.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class LocationPreferences {

    private static final String PREFS_NAME = "location_prefs";

    private LocationPreferences() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void saveLocation(Context context, String location, String source) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(LocationConstants.PREF_LAST_LOCATION, location)
                .putString(LocationConstants.PREF_LOCATION_SOURCE, source)
                .apply();
    }

    public static String getLastLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LocationConstants.PREF_LAST_LOCATION, LocationConstants.DEFAULT_LOCATION);
    }

    public static String getLocationSource(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(LocationConstants.PREF_LOCATION_SOURCE, LocationConstants.LOCATION_SOURCE_MANUAL);
    }

    public static boolean isLocationGranted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(LocationConstants.PREF_LOCATION_GRANTED, false);
    }

    public static void setLocationGranted(Context context, boolean granted) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(LocationConstants.PREF_LOCATION_GRANTED, granted)
                .apply();
    }
}
