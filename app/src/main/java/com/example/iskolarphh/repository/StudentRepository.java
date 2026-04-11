package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentRepository {

    private final StudentDao studentDao;
    private final ExecutorService executorService;

    public StudentRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.studentDao = database.studentDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Get all students
    public LiveData<List<Student>> getAllStudents() {
        return studentDao.getAllStudents();
    }

    // Get student by email (for login)
    public LiveData<Student> getStudentByEmail(String email) {
        return studentDao.getStudentByEmail(email);
    }

    // Get student by Firebase UID
    public LiveData<Student> getStudentByFirebaseUid(String firebaseUid) {
        return studentDao.getStudentByFirebaseUid(firebaseUid);
    }

    // Insert new student (registration) - async with callback
    public void insert(Student student, InsertCallback callback) {
        executorService.execute(() -> {
            long id = studentDao.insert(student);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onInsertComplete(id);
                }
            });
        });
    }

    // Insert without callback (for background operations)
    public void insert(Student student) {
        executorService.execute(() -> studentDao.insert(student));
    }

    // Update student with callback
    public void update(Student student, UpdateCallback callback) {
        executorService.execute(() -> {
            int rowsAffected = studentDao.update(student);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onUpdateComplete(rowsAffected);
                }
            });
        });
    }

    // Update without callback
    public void update(Student student) {
        executorService.execute(() -> studentDao.update(student));
    }

    // Delete student with callback
    public void delete(Student student, DeleteCallback callback) {
        executorService.execute(() -> {
            int rowsAffected = studentDao.delete(student);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onDeleteComplete(rowsAffected);
                }
            });
        });
    }

    // Delete without callback
    public void delete(Student student) {
        executorService.execute(() -> studentDao.delete(student));
    }

    // Callback interfaces
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