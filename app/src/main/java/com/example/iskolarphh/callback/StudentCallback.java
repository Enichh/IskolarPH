package com.example.iskolarphh.callback;

import com.example.iskolarphh.database.entity.Student;

/**
 * Callback interface for async student retrieval operations.
 * Used when fetching student data from background thread to main thread.
 */
public interface StudentCallback {
    void onStudentLoaded(Student student);
}
