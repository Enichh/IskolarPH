package com.example.iskolarphh.service;

import android.content.Context;
import com.example.iskolarphh.R;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScholarshipDatabaseSeeder {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void checkAndSeedScholarships(ScholarshipDao scholarshipDao, Context context) {
        executorService.execute(() -> {
            try {
                List<Scholarship> scholarships = scholarshipDao.getAllScholarshipsSync();
                if (scholarships == null || scholarships.isEmpty()) {
                    android.util.Log.d("ScholarshipDatabaseSeeder", "Database is empty, seeding scholarships");
                    seedScholarships(scholarshipDao, context);
                } else {
                    android.util.Log.d("ScholarshipDatabaseSeeder", "Database has " + scholarships.size() + " scholarships, skipping seed");
                }
            } catch (Exception e) {
                android.util.Log.e("ScholarshipDatabaseSeeder", "Error checking scholarships", e);
            }
        });
    }

    public void seedScholarships(ScholarshipDao scholarshipDao, Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.scholarships);
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type scholarshipListType = new TypeToken<List<Scholarship>>() {}.getType();
            List<Scholarship> scholarships = gson.fromJson(reader, scholarshipListType);
            
            android.util.Log.d("ScholarshipDatabaseSeeder", "Seeding " + scholarships.size() + " scholarships");
            
            for (Scholarship scholarship : scholarships) {
                scholarshipDao.insert(scholarship);
            }
            
            android.util.Log.d("ScholarshipDatabaseSeeder", "Scholarships seeded successfully");
            
            reader.close();
            inputStream.close();
        } catch (Exception e) {
            android.util.Log.e("ScholarshipDatabaseSeeder", "Error seeding scholarships", e);
            e.printStackTrace();
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
