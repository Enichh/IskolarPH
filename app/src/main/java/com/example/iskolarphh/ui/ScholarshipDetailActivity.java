package com.example.iskolarphh.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.repository.ScholarshipRepository;

import java.util.Locale;

public class ScholarshipDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCHOLARSHIP_ID = "extra_scholarship_id";
    private ScholarshipRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarshipdetails);

        repository = new ScholarshipRepository(this);

        int scholarshipId = getIntent().getIntExtra(EXTRA_SCHOLARSHIP_ID, -1);
        if (scholarshipId != -1) {
            repository.getScholarshipById(scholarshipId).observe(this, this::displayDetails);
        }
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
