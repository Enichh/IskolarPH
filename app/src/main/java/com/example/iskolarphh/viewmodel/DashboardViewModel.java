package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.ScholarshipFilterService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipFilterService filterService;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executorService;

    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();
    private final MutableLiveData<List<Scholarship>> recommendedScholarships = new MutableLiveData<>();
    private final MutableLiveData<String> welcomeMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Observers to prevent memory leaks
    private Observer<Student> studentObserver;
    private Observer<List<Scholarship>> scholarshipsObserver;
    private LiveData<Student> studentLiveData;
    private LiveData<List<Scholarship>> scholarshipsLiveData;

    // Flag to prevent executor tasks after ViewModel is cleared
    private volatile boolean isCleared = false;

    public DashboardViewModel(Application application) {
        this(application, 
             new ScholarshipRepository(application),
             new StudentRepository(application),
             Executors.newSingleThreadExecutor());
    }

    // Constructor with dependency injection
    public DashboardViewModel(Application application,
                              ScholarshipRepository scholarshipRepository,
                              StudentRepository studentRepository,
                              ExecutorService executorService) {
        super(application);
        this.scholarshipRepository = scholarshipRepository;
        this.studentRepository = studentRepository;
        this.executorService = executorService;
        this.filterService = new ScholarshipFilterService();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Student> getCurrentStudent() {
        return currentStudent;
    }

    public LiveData<List<Scholarship>> getRecommendedScholarships() {
        return recommendedScholarships;
    }

    public LiveData<String> getWelcomeMessage() {
        return welcomeMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<Scholarship>> getSavedScholarships() {
        return scholarshipRepository.getSavedScholarships();
    }

    public LiveData<List<Scholarship>> getScholarshipsByDeadline() {
        return scholarshipRepository.getScholarshipsByDeadline();
    }

    public LiveData<List<Scholarship>> searchByCourse(String course) {
        return scholarshipRepository.searchAndFilterScholarships(course, null);
    }

    public void loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Remove existing observer from previous LiveData
            if (studentObserver != null && studentLiveData != null) {
                studentLiveData.removeObserver(studentObserver);
            }
            
            studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            
            studentObserver = student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                    welcomeMessage.postValue("Welcome, " + student.getFirstName() + "!");
                    loadRecommendationsAsync(student);
                }
            };
            
            // Use observeForever with stored LiveData reference for proper cleanup
            studentLiveData.observeForever(studentObserver);
        }
    }

    private void loadRecommendationsAsync(Student student) {
        // Guard against executing after ViewModel is cleared
        if (isCleared || executorService.isShutdown()) {
            return;
        }

        isLoading.postValue(true);

        // Remove existing observer from previous LiveData
        if (scholarshipsObserver != null && scholarshipsLiveData != null) {
            scholarshipsLiveData.removeObserver(scholarshipsObserver);
        }

        // Load scholarships
        scholarshipsLiveData = scholarshipRepository.getAllScholarships();

        scholarshipsObserver = scholarships -> {
            if (scholarships != null && student != null) {
                // Guard against executing after ViewModel is cleared
                if (isCleared || executorService.isShutdown()) {
                    isLoading.postValue(false);
                    return;
                }
                // Perform filtering in background thread
                executorService.execute(() -> {
                    try {
                        List<Scholarship> recommended = getRecommendedScholarships(scholarships, student);
                        // Post result to main thread
                        recommendedScholarships.postValue(recommended);
                    } catch (Exception e) {
                        errorMessage.postValue("Error loading recommendations: " + e.getMessage());
                    } finally {
                        isLoading.postValue(false);
                    }
                });
            } else {
                isLoading.postValue(false);
            }
        };
        
        // Use observeForever with stored LiveData reference for proper cleanup
        scholarshipsLiveData.observeForever(scholarshipsObserver);
    }

    private List<Scholarship> getRecommendedScholarships(List<Scholarship> allScholarships, Student student) {
        List<Scholarship> locationBased = new ArrayList<>();
        
        // Step 1: Filter by location first (priority)
        // Load national scholarships if user has no location, or location-based if available
        for (Scholarship scholarship : allScholarships) {
            boolean locationMatch = false;
            
            // Check if user has location data
            String studentLocation = student.getLocation();
            if (studentLocation == null || studentLocation.trim().isEmpty()) {
                // User has no location - show only national scholarships
                locationMatch = scholarship.getLocation() != null && 
                              scholarship.getLocation().equalsIgnoreCase("National");
            } else {
                // User has location - show national + location-specific scholarships
                locationMatch = (scholarship.getLocation() != null &&
                               (scholarship.getLocation().equalsIgnoreCase("National") ||
                                scholarship.getLocation().equalsIgnoreCase(studentLocation.trim())));
            }
            
            if (locationMatch && scholarship.isActive()) {
                locationBased.add(scholarship);
            }
        }
        
        // Step 2: If student has valid GPA (> 0), further filter by GPA
        if (student.getGpa() > 0) {
            List<Scholarship> gpaFiltered = new ArrayList<>();
            for (Scholarship scholarship : locationBased) {
                Double requiredGpa = filterService.parseGpaFromEligibility(scholarship.getEligibilityCriteria());
                boolean gpaMatch = (requiredGpa == null || student.getGpa() >= requiredGpa);
                
                if (gpaMatch) {
                    gpaFiltered.add(scholarship);
                }
            }
            // Use GPA-filtered results if we have any, otherwise fall back to location-based
            if (!gpaFiltered.isEmpty()) {
                locationBased = gpaFiltered;
            }
        }
        
        // Step 3: Limit to top 3 recommendations
        if (locationBased.size() > 3) {
            return locationBased.subList(0, 3);
        }
        
        return locationBased;
    }

    @Override
    protected void onCleared() {
        // Set flag first to prevent new executor tasks
        isCleared = true;
        // Remove observers before shutting down executor
        if (studentObserver != null && studentLiveData != null) {
            studentLiveData.removeObserver(studentObserver);
            studentObserver = null;
        }
        if (scholarshipsObserver != null && scholarshipsLiveData != null) {
            scholarshipsLiveData.removeObserver(scholarshipsObserver);
            scholarshipsObserver = null;
        }
        // Clear LiveData references
        studentLiveData = null;
        scholarshipsLiveData = null;
        // Shutdown executor last
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        super.onCleared();
    }

    private String getCurrentUserUid() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : "";
    }
}
