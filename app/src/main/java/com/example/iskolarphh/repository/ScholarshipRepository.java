package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;

public class ScholarshipRepository {

    private final ScholarshipDao scholarshipDao;
    private final Handler mainHandler;

    public ScholarshipRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.scholarshipDao = database.scholarshipDao();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Get all scholarships (auto-updates with LiveData)
    public LiveData<List<Scholarship>> getAllScholarships() {
        return scholarshipDao.getAllScholarships();
    }

    // Get single scholarship by ID
    public LiveData<Scholarship> getScholarshipById(int id) {
        return scholarshipDao.getScholarshipById(id);
    }

    // Insert new scholarship (runs on background thread)
    public void insert(Scholarship scholarship) {
        new Thread(() -> scholarshipDao.insert(scholarship)).start();
    }

    // Update scholarship
    public void update(Scholarship scholarship) {
        new Thread(() -> scholarshipDao.update(scholarship)).start();
    }

    // Delete scholarship
    public void delete(Scholarship scholarship) {
        new Thread(() -> scholarshipDao.delete(scholarship)).start();
    }
}
