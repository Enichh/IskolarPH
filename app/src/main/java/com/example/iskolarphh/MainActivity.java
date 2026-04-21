package com.example.iskolarphh;

import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.iskolarphh.ui.CatalogFragment;
import com.example.iskolarphh.ui.DashboardFragment;
import com.example.iskolarphh.ui.ProfileFragment;
import com.example.iskolarphh.ui.LocationPermissionDialog;
import com.example.iskolarphh.ui.ChatbotDialog;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.LocationPermissionHandler;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.util.LocationConstants;
import com.example.iskolarphh.util.LocationPreferences;
import com.example.iskolarphh.repository.StudentRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DIALOG_SHOWN = "location_dialog_shown";
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> {
            ChatbotDialog dialog = new ChatbotDialog();
            dialog.show(getSupportFragmentManager(), "ChatbotDialog");
        });

        checkLocationPermissionFlow();

        // Handle navigation from other activities
        if (getIntent().hasExtra("extra_navigate_to")) {
            handleIntentNavigation(getIntent());
        } else {
            // Load default fragment (Dashboard)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentNavigation(intent);
    }

    private void handleIntentNavigation(Intent intent) {
        if (intent != null && intent.hasExtra("extra_navigate_to")) {
            int navigateTo = intent.getIntExtra("extra_navigate_to", -1);
            if (navigateTo != -1) {
                bottomNav.setSelectedItemId(navigateTo);
            }
        }
    }

    private void checkLocationPermissionFlow() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        boolean dialogShown = LocationPreferences.isLocationGranted(this);
        if (!dialogShown) {
            showLocationPermissionDialog();
        }
    }

    private void showLocationPermissionDialog() {
        LocationPermissionDialog.show(this, new LocationPermissionDialog.LocationPermissionCallback() {
            @Override
            public void onPermissionAllowed() {
                LocationPermissionHandler.checkAndRequestLocationPermissions(MainActivity.this,
                        LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
            }

            @Override
            public void onPermissionDenied() {
                LocationPreferences.setLocationGranted(MainActivity.this, true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationConstants.LOCATION_PERMISSION_REQUEST_CODE) {
            LocationPreferences.setLocationGranted(this, true);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            }
        }
    }

    private void fetchUserLocation() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        LocationManager.getInstance().requestSingleLocationUpdate(this, new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                GeocoderService.getInstance().getLocationFromCoordinates(MainActivity.this, latitude, longitude, new LocationCallback() {
                    @Override
                    public void onLocationRetrieved(String locationString, double lat, double lng) {
                        LocationPreferences.saveLocation(MainActivity.this, locationString, LocationConstants.LOCATION_SOURCE_GPS);
                        StudentRepository studentRepository = new StudentRepository(MainActivity.this);
                        studentRepository.updateLocation(firebaseUser.getUid(), locationString, rowsAffected -> {
                        });
                    }

                    @Override
                    public void onLocationError(String errorMessage) {
                        LocationPreferences.saveLocation(MainActivity.this, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
                    }

                    @Override
                    public void onPermissionDenied() {
                        LocationPreferences.saveLocation(MainActivity.this, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
                    }
                });
            }

            @Override
            public void onLocationError(String errorMessage) {
                LocationPreferences.saveLocation(MainActivity.this, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
            }

            @Override
            public void onPermissionDenied() {
                LocationPreferences.saveLocation(MainActivity.this, LocationConstants.DEFAULT_LOCATION, LocationConstants.LOCATION_SOURCE_MANUAL);
            }
        });
    }

    private BottomNavigationView.OnItemSelectedListener navListener = new BottomNavigationView.OnItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            int id = item.getItemId(); // ✅ added

            if (id == R.id.nav_home) {
                selectedFragment = new DashboardFragment();

            } else if (id == R.id.nav_search) {
                selectedFragment = new CatalogFragment();

            } else if (id == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        }
    };
}
