package com.example.iskolarphh.repository;

import org.junit.Test;

import static org.junit.Assert.*;

public class StudentRepositoryTest {

    @Test
    public void testInsertCallback_canBeImplemented() {
        StudentRepository.InsertCallback callback = new StudentRepository.InsertCallback() {
            @Override
            public void onInsertComplete(long id) {
                assertEquals(1, id);
            }
        };
        assertNotNull(callback);
        callback.onInsertComplete(1);
    }

    @Test
    public void testUpdateCallback_canBeImplemented() {
        StudentRepository.UpdateCallback callback = new StudentRepository.UpdateCallback() {
            @Override
            public void onUpdateComplete(int rowsAffected) {
                assertEquals(1, rowsAffected);
            }
        };
        assertNotNull(callback);
        callback.onUpdateComplete(1);
    }

    @Test
    public void testDeleteCallback_canBeImplemented() {
        StudentRepository.DeleteCallback callback = new StudentRepository.DeleteCallback() {
            @Override
            public void onDeleteComplete(int rowsAffected) {
                assertEquals(1, rowsAffected);
            }
        };
        assertNotNull(callback);
        callback.onDeleteComplete(1);
    }
}
