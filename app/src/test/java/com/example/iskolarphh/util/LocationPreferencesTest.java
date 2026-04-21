package com.example.iskolarphh.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LocationPreferencesTest {

    @Mock
    private Context mockContext;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);
        when(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
    }

    @Test
    public void testSaveLocation_savesToPreferences() {
        LocationPreferences.saveLocation(mockContext, "Manila", LocationConstants.LOCATION_SOURCE_GPS);

        verify(mockContext).getSharedPreferences(eq("location_prefs"), eq(Context.MODE_PRIVATE));
        verify(mockEditor).putString(LocationConstants.PREF_LAST_LOCATION, "Manila");
        verify(mockEditor).putString(LocationConstants.PREF_LOCATION_SOURCE, LocationConstants.LOCATION_SOURCE_GPS);
        verify(mockEditor).apply();
    }

    @Test
    public void testGetLastLocation_returnsSavedLocation() {
        when(mockSharedPreferences.getString(LocationConstants.PREF_LAST_LOCATION, LocationConstants.DEFAULT_LOCATION))
            .thenReturn("Cebu");

        String location = LocationPreferences.getLastLocation(mockContext);

        assertEquals("Cebu", location);
        verify(mockSharedPreferences).getString(LocationConstants.PREF_LAST_LOCATION, LocationConstants.DEFAULT_LOCATION);
    }

    @Test
    public void testGetLastLocation_noSavedLocation_returnsDefault() {
        when(mockSharedPreferences.getString(LocationConstants.PREF_LAST_LOCATION, LocationConstants.DEFAULT_LOCATION))
            .thenReturn(LocationConstants.DEFAULT_LOCATION);

        String location = LocationPreferences.getLastLocation(mockContext);

        assertEquals(LocationConstants.DEFAULT_LOCATION, location);
    }

    @Test
    public void testGetLocationSource_returnsSavedSource() {
        when(mockSharedPreferences.getString(LocationConstants.PREF_LOCATION_SOURCE, LocationConstants.LOCATION_SOURCE_MANUAL))
            .thenReturn(LocationConstants.LOCATION_SOURCE_GPS);

        String source = LocationPreferences.getLocationSource(mockContext);

        assertEquals(LocationConstants.LOCATION_SOURCE_GPS, source);
    }

    @Test
    public void testGetLocationSource_noSavedSource_returnsDefault() {
        when(mockSharedPreferences.getString(LocationConstants.PREF_LOCATION_SOURCE, LocationConstants.LOCATION_SOURCE_MANUAL))
            .thenReturn(LocationConstants.LOCATION_SOURCE_MANUAL);

        String source = LocationPreferences.getLocationSource(mockContext);

        assertEquals(LocationConstants.LOCATION_SOURCE_MANUAL, source);
    }

    @Test
    public void testIsLocationGranted_returnsTrue() {
        when(mockSharedPreferences.getBoolean(LocationConstants.PREF_LOCATION_GRANTED, false))
            .thenReturn(true);

        boolean granted = LocationPreferences.isLocationGranted(mockContext);

        assertTrue(granted);
    }

    @Test
    public void testIsLocationGranted_returnsFalse() {
        when(mockSharedPreferences.getBoolean(LocationConstants.PREF_LOCATION_GRANTED, false))
            .thenReturn(false);

        boolean granted = LocationPreferences.isLocationGranted(mockContext);

        assertFalse(granted);
    }

    @Test
    public void testSetLocationGranted_savesToPreferences() {
        LocationPreferences.setLocationGranted(mockContext, true);

        verify(mockEditor).putBoolean(LocationConstants.PREF_LOCATION_GRANTED, true);
        verify(mockEditor).apply();
    }

    @Test
    public void testSetLocationGranted_false_savesToPreferences() {
        LocationPreferences.setLocationGranted(mockContext, false);

        verify(mockEditor).putBoolean(LocationConstants.PREF_LOCATION_GRANTED, false);
        verify(mockEditor).apply();
    }
}
