package com.example.iskolarphh.repository;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.iskolarphh.callback.InsertCallback;
import com.example.iskolarphh.callback.UpdateCallback;
import com.example.iskolarphh.callback.DeleteCallback;

public class StudentRepositoryTest {

    @Test
    public void testInsertCallback_canBeImplemented() {
        InsertCallback callback = new InsertCallback() {
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
        UpdateCallback callback = new UpdateCallback() {
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
        DeleteCallback callback = new DeleteCallback() {
            @Override
            public void onDeleteComplete(int rowsAffected) {
                assertEquals(1, rowsAffected);
            }
        };
        assertNotNull(callback);
        callback.onDeleteComplete(1);
    }
}
