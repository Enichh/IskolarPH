package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import java.util.Collections;
import com.example.iskolarphh.callback.DeleteCallback;
import com.example.iskolarphh.callback.InsertCallback;
import com.example.iskolarphh.callback.UpdateCallback;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.service.ScholarshipFilterService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScholarshipRepository {

    private final ScholarshipDao scholarshipDao;
    private final ExecutorService executorService;
    private final ScholarshipFilterService filterService;

    public ScholarshipRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.scholarshipDao = database.scholarshipDao();
        this.executorService = Executors.newSingleThreadExecutor();
        this.filterService = new ScholarshipFilterService();
    }

    public LiveData<List<Scholarship>> getAllScholarships() {
        return scholarshipDao.getAllScholarships();
    }

    public LiveData<Scholarship> getScholarshipById(int id) {
        return scholarshipDao.getScholarshipById(id);
    }

    public LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location) {
        return scholarshipDao.searchAndFilterScholarships(searchQuery, location);
    }

    public LiveData<List<Scholarship>> getSavedScholarships() {
        return scholarshipDao.getSavedScholarships();
    }

    public LiveData<List<Scholarship>> getScholarshipsByDeadline() {
        return scholarshipDao.getScholarshipsByDeadline();
    }

    public LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location, Double studentGpa, boolean enableGpaFilter) {
        LiveData<List<Scholarship>> baseResults = scholarshipDao.searchAndFilterScholarships(searchQuery, location);

        if (enableGpaFilter && studentGpa != null) {
            return Transformations.map(baseResults, scholarships -> {
                if (scholarships == null) return Collections.emptyList();
                return filterService.filterByGpa(scholarships, studentGpa);
            });
        }
        return baseResults;
    }

    public void insert(Scholarship scholarship, InsertCallback callback) {
        executorService.execute(() -> {
            long id = scholarshipDao.insert(scholarship);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onInsertComplete(id);
                }
            });
        });
    }

    public void insert(Scholarship scholarship) {
        executorService.execute(() -> scholarshipDao.insert(scholarship));
    }

    public void update(Scholarship scholarship, UpdateCallback callback) {
        executorService.execute(() -> {
            int rowsAffected = scholarshipDao.update(scholarship);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onUpdateComplete(rowsAffected);
                }
            });
        });
    }

    public void update(Scholarship scholarship) {
        executorService.execute(() -> scholarshipDao.update(scholarship));
    }

    public void delete(Scholarship scholarship, DeleteCallback callback) {
        executorService.execute(() -> {
            int rowsAffected = scholarshipDao.delete(scholarship);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onDeleteComplete(rowsAffected);
                }
            });
        });
    }

    public void delete(Scholarship scholarship) {
        executorService.execute(() -> scholarshipDao.delete(scholarship));
    }
}
