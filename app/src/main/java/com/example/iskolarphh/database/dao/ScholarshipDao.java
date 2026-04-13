package com.example.iskolarphh.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;

@Dao
public interface ScholarshipDao {

    // CREATE
    @Insert
    void insert(Scholarship scholarship);

    // READ (ALL)
    @Query("SELECT * FROM scholarships")
    LiveData<List<Scholarship>> getAllScholarships();

    // READ (SINGLE)
    @Query("SELECT * FROM scholarships WHERE id = :id LIMIT 1")
    LiveData<Scholarship> getScholarshipById(int id);

    @Query("SELECT * FROM scholarships WHERE " +
            "(:searchQuery IS NULL OR scholarship_name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') AND " +
            "(:location IS NULL OR location = :location)")
    LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location);

    // UPDATE
    @Update
    void update(Scholarship scholarship);

    // DELETE
    @Delete
    void delete(Scholarship scholarship);
}
