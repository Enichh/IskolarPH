package com.example.iskolarphh.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.lifecycle.LiveData;
import com.example.iskolarphh.callback.DeleteCallback;
import com.example.iskolarphh.callback.InsertCallback;
import com.example.iskolarphh.callback.StudentCallback;
import com.example.iskolarphh.callback.UpdateCallback;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentRepository {

    private final StudentDao studentDao;
    private final ExecutorService executorService;
    private volatile boolean isShutdown = false;

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

    // Get student by Firebase UID - SYNC version for background threads only
    public Student getStudentByFirebaseUidSync(String firebaseUid) {
        return studentDao.getStudentByFirebaseUidSync(firebaseUid);
    }

    // Get student by Firebase UID - ASYNC version for UI thread
    public void getStudentByFirebaseUidAsync(String firebaseUid, StudentCallback callback) {
        executorService.execute(() -> {
            Student student = studentDao.getStudentByFirebaseUidSync(firebaseUid);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onStudentLoaded(student);
                }
            });
        });
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

    // Update location by firebaseUid - async with callback
    public void updateLocation(String firebaseUid, String location, UpdateCallback callback) {
        if (isShutdown) return;
        executorService.execute(() -> {
            Student student = studentDao.getStudentByFirebaseUidSync(firebaseUid);
            int result = 0;
            if (student != null) {
                student.setLocation(location);
                result = studentDao.update(student);
            }
            final int rowsAffected = result;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (callback != null) {
                    callback.onUpdateComplete(rowsAffected);
                }
            });
        });
    }

    /**
     * Shutdown the executor service. Call this when the repository is no longer needed.
     */
    public void shutdown() {
        isShutdown = true;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}