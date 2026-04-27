package com.example.iskolarphh.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.iskolarphh.MainActivity;
import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Locale;

public class ScholarshipDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCHOLARSHIP_ID = "extra_scholarship_id";
    public static final String EXTRA_NAVIGATE_TO = "extra_navigate_to";
    private ScholarshipRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarshipdetails);

        repository = new ScholarshipRepository(this);

        setupNavigation();

        int scholarshipId = getIntent().getIntExtra(EXTRA_SCHOLARSHIP_ID, -1);
        if (scholarshipId != -1) {
            repository.getScholarshipById(scholarshipId).observe(this, this::displayDetails);
        }
        // ✅ ADD THIS PART (Back button function)
        android.widget.ImageView btnBack = findViewById(R.id.btn_back); // get the back button from XML

        btnBack.setOnClickListener(v -> {
            finish(); // go back to previous activity (activity_catalog)
        });
    }

    private void setupNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_search); // Detail is related to Catalog/Search
        
        bottomNav.setOnItemSelectedListener(item -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                intent.putExtra(EXTRA_NAVIGATE_TO, R.id.nav_home);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_search) {
                intent.putExtra(EXTRA_NAVIGATE_TO, R.id.nav_search);
                startActivity(intent);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                intent.putExtra(EXTRA_NAVIGATE_TO, R.id.nav_profile);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void displayDetails(Scholarship scholarship) {
        if (scholarship == null) return;

        TextView tvHeader = findViewById(R.id.tvHeader);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvDetails = findViewById(R.id.tvDetails);
        TextView tvNotes = findViewById(R.id.tvNotes);

        tvHeader.setText(scholarship.getScholarshipName());
        tvDescription.setText(scholarship.getDescription());

        String details = String.format(Locale.getDefault(),
                "Provider: %s\nAmount: ₱%.2f\nLocation: %s\nDeadline: %s",
                scholarship.getProviderOrganization(),
                scholarship.getAwardAmount(),
                scholarship.getLocation(),
                scholarship.getApplicationDeadline());
        tvDetails.setText(details);

        tvNotes.setText(scholarship.getEligibilityCriteria());
    }
}
