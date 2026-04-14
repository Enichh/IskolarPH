package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScholarshipRepository {

    private final ScholarshipDao scholarshipDao;
    private final ExecutorService executorService;

    public ScholarshipRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.scholarshipDao = database.scholarshipDao();
        this.executorService = Executors.newSingleThreadExecutor();
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

    public LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location, Double studentGpa, boolean enableGpaFilter) {
        if (enableGpaFilter && studentGpa != null) {
            LiveData<List<Scholarship>> baseResults = scholarshipDao.searchAndFilterScholarships(searchQuery, location);
            executorService.execute(() -> {
                List<Scholarship> filtered = filterByGpa(baseResults.getValue(), studentGpa);
                new Handler(Looper.getMainLooper()).post(() -> {
                });
            });
            return baseResults;
        }
        return scholarshipDao.searchAndFilterScholarships(searchQuery, location);
    }

    private List<Scholarship> filterByGpa(List<Scholarship> scholarships, double studentGpa) {
        if (scholarships == null) {
            return new ArrayList<>();
        }
        List<Scholarship> filtered = new ArrayList<>();
        for (Scholarship scholarship : scholarships) {
            Double requiredGpa = parseGpaFromEligibility(scholarship.getEligibilityCriteria());
            if (requiredGpa == null || studentGpa >= requiredGpa) {
                filtered.add(scholarship);
            }
        }
        return filtered;
    }

    private Double parseGpaFromEligibility(String eligibilityCriteria) {
        if (eligibilityCriteria == null) {
            return null;
        }
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)%");
        java.util.regex.Matcher matcher = pattern.matcher(eligibilityCriteria);
        if (matcher.find()) {
            try {
                double percentage = Double.parseDouble(matcher.group(1));
                return percentage / 100.0 * 4.0;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        java.util.regex.Pattern gpaPattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
        java.util.regex.Matcher gpaMatcher = gpaPattern.matcher(eligibilityCriteria);
        if (gpaMatcher.find()) {
            try {
                return Double.parseDouble(gpaMatcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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

    public interface InsertCallback {
        void onInsertComplete(long id);
    }

    public interface UpdateCallback {
        void onUpdateComplete(int rowsAffected);
    }

    public interface DeleteCallback {
        void onDeleteComplete(int rowsAffected);
    }
}
