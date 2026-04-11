package com.example.iskolarphh.database.dao;

import androidx.room.*;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;

@Dao
public interface ScholarshipDao {

    //CREATE
    @Insert
    void insert(Scholarship scholarship);

    //READ (ALL)
    @Query("SELECT * FROM scholarships")
    List<Scholarship> getAllScholarships();

    //READ (SINGLE)
    @Query("SELECT * FROM scholarships WHERE id = :id LIMIT 1")
    Scholarship getScholarshipById(int id);

    //UPDATE
    @Update
    void update(Scholarship scholarship);

    //DELETE
    @Delete
    void delete(Scholarship scholarship);
}
