package com.example.iskolarphh.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.iskolarphh.database.AppDatabase;
import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import java.util.List;

public class StudentRepository {

    private final StudentDao studentDao;

    public StudentRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.studentDao = database.studentDao();
    }

    // Get all students
    public LiveData<List<Student>> getAllStudents() {
        return studentDao.getAllStudents();
    }

    // Get student by email (for login)
    public LiveData<Student> getStudentByEmail(String email) {
        return studentDao.getStudentByEmail(email);
    }

    // Insert new student (registration)
    public void insert(Student student) {
        new Thread(() -> studentDao.insert(student)).start();
    }

    // Update student
    public void update(Student student) {
        new Thread(() -> studentDao.update(student)).start();
    }

    // Delete student
    public void delete(Student student) {
        new Thread(() -> studentDao.delete(student)).start();
    }
}
