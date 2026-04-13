package com.example.iskolarphh.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = { Student.class, Scholarship.class }, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;
    private static final ExecutorService databaseWriteExecutor = Executors.newSingleThreadExecutor();

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
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                        seedScholarships(scholarshipDao);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void seedScholarships(ScholarshipDao scholarshipDao) {
        Scholarship s1 = new Scholarship("DOST-SEI Undergraduate Scholarship",
                "Provides financial assistance to talented students in the STEM field.",
                7000.0, "Department of Science and Technology",
                "Incoming freshman, STEM strand, or top 5% of non-STEM.", "2026-12-31", true, "https://www.sei.dost.gov.ph/");
        s1.setLocation("National");
        scholarshipDao.insert(s1);

        Scholarship s2 = new Scholarship("CHED Scholarship Program (CSP)",
                "Offers support for tertiary education in various priority courses.",
                6000.0, "Commission on Higher Education",
                "Filipino citizen, GWA of 90% or above.", "2026-11-15", true, "https://ched.gov.ph/");
        s2.setLocation("Visayas");
        scholarshipDao.insert(s2);

        Scholarship s3 = new Scholarship("SM Foundation Scholarship",
                "For deserving students in public high schools.",
                5000.0, "SM Foundation",
                "Public high school graduate, GWA of 88% or above.", "2027-01-20", true, "https://sm-foundation.org/");
        s3.setLocation("Luzon");
        scholarshipDao.insert(s3);

        Scholarship s4 = new Scholarship("TESDA Training for Work Scholarship",
                "Focuses on technical-vocational skills training.",
                3000.0, "TESDA",
                "At least 18 years old, high school graduate.", "2026-10-30", true, "https://www.tesda.gov.ph/");
        s4.setLocation("Mindanao");
        scholarshipDao.insert(s4);
    }
}