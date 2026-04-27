package com.example.iskolarphh.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.service.ScholarshipDatabaseSeeder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { Student.class, Scholarship.class }, version = 3, exportSchema = false)
@SuppressWarnings("deprecation")
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    public abstract StudentDao studentDao();

    public abstract ScholarshipDao scholarshipDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                            appContext,
                            AppDatabase.class,
                            "iskolarphh_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                        new ScholarshipDatabaseSeeder().seedScholarships(scholarshipDao, appContext);
                                    });
                                }

                                @Override
                                public void onOpen(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    databaseWriteExecutor.execute(() -> {
                                        ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                        new ScholarshipDatabaseSeeder().checkAndSeedScholarships(scholarshipDao, appContext);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}