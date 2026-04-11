package com.example.iskolarphh.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;

@Database(entities = { Student.class, Scholarship.class }, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract StudentDao studentDao();

    public abstract ScholarshipDao scholarshipDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "iskolarphh_database")
                            .fallbackToDestructiveMigration() // Recreate DB on version change
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}