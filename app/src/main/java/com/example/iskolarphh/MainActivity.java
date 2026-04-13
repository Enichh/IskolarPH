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

import com.example.iskolarphh.ui.CatalogFragment;
import com.example.iskolarphh.ui.DashboardFragment;
import com.example.iskolarphh.ui.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.iskolarphh.ui.ChatbotDialog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> {
            ChatbotDialog dialog = new ChatbotDialog();
            dialog.show(getSupportFragmentManager(), "ChatbotDialog");
        });
        
        // Load default fragment (Dashboard)
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DashboardFragment())
                .commit();
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
