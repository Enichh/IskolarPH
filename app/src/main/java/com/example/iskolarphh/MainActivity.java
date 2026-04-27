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
import com.example.iskolarphh.service.LocationFlowManager;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.repository.StudentRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.iskolarphh.util.LocationConstants;
import com.example.iskolarphh.util.LocationPreferences;
import com.example.iskolarphh.util.PerformanceMonitor;

public class MainActivity extends AppCompatActivity {

    private static final String PREF_DIALOG_SHOWN = "location_dialog_shown";
    private BottomNavigationView bottomNav;
    private LocationFlowManager locationFlowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long startTime = System.nanoTime();
        
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Start performance monitoring
        PerformanceMonitor.startMonitoring();
        PerformanceMonitor.logMemoryUsage("MainActivity.onCreate()");

        initializeViews();
        setupListeners();
        initializeLocationManager();
        checkLocationPermissionFlow();
        handleInitialNavigation();
        
        PerformanceMonitor.logMethodExecutionTime("MainActivity.onCreate()", startTime);
    }

    private void initializeViews() {
        bottomNav = findViewById(R.id.bottom_navigation);
    }

    private void setupListeners() {
        bottomNav.setOnItemSelectedListener(navListener);
        
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> {
            ChatbotDialog dialog = new ChatbotDialog();
            dialog.show(getSupportFragmentManager(), "ChatbotDialog");
        });
    }

    private void initializeLocationManager() {
        // Lazy initialization to avoid blocking main thread
        new android.os.Handler().post(() -> {
            locationFlowManager = new LocationFlowManager(this, 
                new LocationManager(), 
                new GeocoderService(), 
                new StudentRepository(this));
        });
    }

    private void handleInitialNavigation() {
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
        locationFlowManager.checkAndRequestLocationPermission(this, new LocationPermissionDialog.LocationPermissionCallback() {
            @Override
            public void onPermissionAllowed() {
                locationFlowManager.requestLocationPermissions(MainActivity.this, LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
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
        locationFlowManager.handlePermissionResult(requestCode, permissions, grantResults, null);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PerformanceMonitor.stopMonitoring();
    }
}
