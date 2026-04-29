package com.example.iskolarphh.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;

@Dao
public interface ScholarshipDao {

    // CREATE
    @Insert
    long insert(Scholarship scholarship);

    // READ (ALL)
    @Query("SELECT * FROM scholarships")
    LiveData<List<Scholarship>> getAllScholarships();

    @Query("SELECT * FROM scholarships")
    List<Scholarship> getAllScholarshipsSync();

    // READ (SINGLE)
    @Query("SELECT * FROM scholarships WHERE id = :id LIMIT 1")
    LiveData<Scholarship> getScholarshipById(int id);

    @Query("SELECT * FROM scholarships WHERE " +
            "(:searchQuery IS NULL OR :searchQuery = '' OR scholarship_name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%') AND " +
            "(:location IS NULL OR :location = '' OR location = :location) " +
            "ORDER BY id ASC")
    LiveData<List<Scholarship>> searchAndFilterScholarships(String searchQuery, String location);

    @Query("SELECT * FROM scholarships WHERE is_saved = 1")
    LiveData<List<Scholarship>> getSavedScholarships();

    @Query("SELECT * FROM scholarships ORDER BY application_deadline ASC")
    LiveData<List<Scholarship>> getScholarshipsByDeadline();

    // UPDATE
    @Update
    int update(Scholarship scholarship);

    // DELETE
    @Delete
    int delete(Scholarship scholarship);
}
