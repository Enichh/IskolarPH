package com.example.iskolarphh;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.iskolarphh.repository.CatalogFragment;
import com.example.iskolarphh.repository.DashboardFragment;
import com.example.iskolarphh.repository.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
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
