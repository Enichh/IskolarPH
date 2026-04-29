package com.example.iskolarphh.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.dao.PrivacyConsentDao;
import com.example.iskolarphh.database.entity.PrivacyConsent;
import com.example.iskolarphh.service.ScholarshipDatabaseSeeder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { Student.class, Scholarship.class, PrivacyConsent.class }, version = 5, exportSchema = false)
@SuppressWarnings("deprecation")
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static volatile boolean isInitializing = false;
    private static final Object LOCK = new Object();
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

    // Migration from version 4 to 5: Add profile_photo_path column to students table
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE students ADD COLUMN profile_photo_path TEXT");
        }
    };

    public abstract StudentDao studentDao();

    public abstract ScholarshipDao scholarshipDao();

    public abstract PrivacyConsentDao privacyConsentDao();

    public static void initializeAsync(Context context) {
        if (INSTANCE == null && !isInitializing) {
            synchronized (LOCK) {
                if (INSTANCE == null && !isInitializing) {
                    isInitializing = true;
                    Context appContext = context.getApplicationContext();
                    new Thread(() -> {
                        INSTANCE = Room.databaseBuilder(
                                appContext,
                                AppDatabase.class,
                                "iskolarphh_database")
                                .addMigrations(MIGRATION_4_5)
                                .addCallback(new RoomDatabase.Callback() {
                                    @Override
                                    public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                        super.onCreate(db);
                                        databaseWriteExecutor.execute(() -> {
                                            ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                            new ScholarshipDatabaseSeeder().seedScholarships(scholarshipDao, appContext);
                                        });
                                    }
                                })
                                .build();
                    }).start();
                }
            }
        }
    }

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    // Block only during first initialization - should not happen if Application called initializeAsync()
                    Context appContext = context.getApplicationContext();
                    INSTANCE = Room.databaseBuilder(
                            appContext,
                            AppDatabase.class,
                            "iskolarphh_database")
                            .addMigrations(MIGRATION_4_5)
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                        new ScholarshipDatabaseSeeder().seedScholarships(scholarshipDao, appContext);
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