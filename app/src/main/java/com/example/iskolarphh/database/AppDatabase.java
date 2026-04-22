package com.example.iskolarphh.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.dao.StudentDao;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.database.dao.ScholarshipDao;
import com.example.iskolarphh.database.entity.Scholarship;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
                                        seedScholarships(scholarshipDao, appContext);
                                    });
                                }

                                @Override
                                public void onOpen(androidx.sqlite.db.SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    databaseWriteExecutor.execute(() -> {
                                        ScholarshipDao scholarshipDao = INSTANCE.scholarshipDao();
                                        checkAndSeedScholarships(scholarshipDao, appContext);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static void checkAndSeedScholarships(ScholarshipDao scholarshipDao, Context context) {
        try {
            List<Scholarship> scholarships = scholarshipDao.getAllScholarshipsSync();
            if (scholarships == null || scholarships.isEmpty()) {
                android.util.Log.d("AppDatabase", "Database is empty, seeding scholarships");
                seedScholarships(scholarshipDao, context);
            } else {
                android.util.Log.d("AppDatabase", "Database has " + scholarships.size() + " scholarships, skipping seed");
            }
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error checking scholarships", e);
        }
    }

    private static void seedScholarships(ScholarshipDao scholarshipDao, Context context) {
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.scholarships);
            InputStreamReader reader = new InputStreamReader(inputStream);
            Gson gson = new Gson();
            Type scholarshipListType = new TypeToken<List<Scholarship>>() {}.getType();
            List<Scholarship> scholarships = gson.fromJson(reader, scholarshipListType);
            
            android.util.Log.d("AppDatabase", "Seeding " + scholarships.size() + " scholarships");
            
            for (Scholarship scholarship : scholarships) {
                scholarshipDao.insert(scholarship);
            }
            
            android.util.Log.d("AppDatabase", "Scholarships seeded successfully");
            
            reader.close();
            inputStream.close();
        } catch (Exception e) {
            android.util.Log.e("AppDatabase", "Error seeding scholarships", e);
            e.printStackTrace();
        }
    }
}