package com.example.iskolarphh.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.iskolarphh.database.entity.Student;
import java.util.List;

@Dao
public interface StudentDao {

    // CREATE - returns the new row id
    @Insert
    long insert(Student student);

    // READ (ALL)
    @Query("SELECT * FROM students")
    LiveData<List<Student>> getAllStudents();

    // READ (BY EMAIL)
    @Query("SELECT * FROM students WHERE email = :email LIMIT 1")
    LiveData<Student> getStudentByEmail(String email);

    // READ (BY FIREBASE UID)
    @Query("SELECT * FROM students WHERE firebase_uid = :firebaseUid LIMIT 1")
    LiveData<Student> getStudentByFirebaseUid(String firebaseUid);

    // READ (BY FIREBASE UID) - SYNC VERSION FOR BACKGROUND THREADS
    @Query("SELECT * FROM students WHERE firebase_uid = :firebaseUid LIMIT 1")
    Student getStudentByFirebaseUidSync(String firebaseUid);

    // UPDATE - returns number of rows updated
    @Update
    int update(Student student);

    // DELETE - returns number of rows deleted
    @Delete
    int delete(Student student);
}