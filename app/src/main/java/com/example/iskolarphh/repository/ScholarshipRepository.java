package com.example.iskolarphh.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ScholarshipRepository {

    private final List<Scholarship> hardcodedScholarships;

    public ScholarshipRepository(Context context) {
        this.hardcodedScholarships = new ArrayList<>();
        seedData();
    }

    private void seedData() {
        Scholarship s1 = new Scholarship("DOST-SEI Undergraduate Scholarship",
                "Provides financial assistance to talented students in the STEM field.",
                7000.0, "Department of Science and Technology",
                "Incoming freshman, STEM strand, or top 5% of non-STEM.", "2026-12-31", true, "https://www.sei.dost.gov.ph/");
        s1.setId(1);
        s1.setLocation("National");
        hardcodedScholarships.add(s1);

        Scholarship s2 = new Scholarship("CHED Scholarship Program (CSP)",
                "Offers support for tertiary education in various priority courses.",
                6000.0, "Commission on Higher Education",
                "Filipino citizen, GWA of 90% or above.", "2026-11-15", true, "https://ched.gov.ph/");
        s2.setId(2);
        s2.setLocation("Visayas");
        hardcodedScholarships.add(s2);

        Scholarship s3 = new Scholarship("SM Foundation Scholarship",
                "For deserving students in public high schools.",
                5000.0, "SM Foundation",
                "Public high school graduate, GWA of 88% or above.", "2027-01-20", true, "https://sm-foundation.org/");
        s3.setId(3);
        s3.setLocation("Luzon");
        hardcodedScholarships.add(s3);

        Scholarship s4 = new Scholarship("TESDA Training for Work Scholarship",
                "Focuses on technical-vocational skills training.",
                3000.0, "TESDA",
                "At least 18 years old, high school graduate.", "2026-10-30", true, "https://www.tesda.gov.ph/");
        s4.setId(4);
        s4.setLocation("Mindanao");
        hardcodedScholarships.add(s4);
    }

    public LiveData<List<Scholarship>> getAllScholarships() {
        MutableLiveData<List<Scholarship>> data = new MutableLiveData<>();
        data.setValue(hardcodedScholarships);
        return data;
    }

    public LiveData<Scholarship> getScholarshipById(int id) {
        MutableLiveData<Scholarship> data = new MutableLiveData<>();
        for (Scholarship s : hardcodedScholarships) {
            if (s.getId() == id) {
                data.setValue(s);
                break;
            }
        }
        return data;
    }

    public LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location) {
        MutableLiveData<List<Scholarship>> data = new MutableLiveData<>();
        List<Scholarship> filtered = hardcodedScholarships.stream()
                .filter(s -> (searchQuery == null || searchQuery.isEmpty() ||
                        s.getScholarshipName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                        s.getDescription().toLowerCase().contains(searchQuery.toLowerCase())))
                .filter(s -> (location == null || s.getLocation().equalsIgnoreCase(location)))
                .collect(Collectors.toList());
        data.setValue(filtered);
        return data;
    }

    public void insert(Scholarship scholarship) {}
    public void update(Scholarship scholarship) {}
    public void delete(Scholarship scholarship) {}
}
