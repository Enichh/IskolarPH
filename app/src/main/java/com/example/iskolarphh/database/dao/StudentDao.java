package com.example.iskolarphh.database.dao;

import androidx.room.*;
import com.example.iskolarphh.database.entity.Student;
import java.util.List;

@Dao
public interface StudentDao {

    //CREATE
    @Insert
    void insert(Student student);

    //READ (ALL)
    @Query("SELECT * FROM students")
    List<Student> getAllStudents();

    //READ (SINGLE)
    @Query("SELECT * FROM students WHERE email = :email LIMIT 1")
    Student getStudentByEmail(String email);

    //UPDATE
    @Update
    void update(Student student);

    //DELETE
    @Delete
    void delete(Student student);
}