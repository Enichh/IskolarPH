package com.example.iskolarphh.repository;

import org.junit.Test;

import static org.junit.Assert.*;

public class ScholarshipRepositoryTest {

    @Test
    public void testInsertCallback_interfaceExists() {
        assertNotNull(ScholarshipRepository.InsertCallback.class);
    }

    @Test
    public void testUpdateCallback_interfaceExists() {
        assertNotNull(ScholarshipRepository.UpdateCallback.class);
    }

    @Test
    public void testDeleteCallback_interfaceExists() {
        assertNotNull(ScholarshipRepository.DeleteCallback.class);
    }

    @Test
    public void testInsertCallback_canBeImplemented() {
        ScholarshipRepository.InsertCallback callback = new ScholarshipRepository.InsertCallback() {
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
        ScholarshipRepository.UpdateCallback callback = new ScholarshipRepository.UpdateCallback() {
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
        ScholarshipRepository.DeleteCallback callback = new ScholarshipRepository.DeleteCallback() {
            @Override
            public void onDeleteComplete(int rowsAffected) {
                assertEquals(1, rowsAffected);
            }
        };
        assertNotNull(callback);
        callback.onDeleteComplete(1);
    }
}
