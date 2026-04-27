package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.ScholarshipFilterService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ScholarshipFilterService filterService;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();
    private final MutableLiveData<List<Scholarship>> recommendedScholarships = new MutableLiveData<>();
    private final MutableLiveData<String> welcomeMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DashboardViewModel(Application application) {
        this(application, 
             new ScholarshipRepository(application),
             new StudentRepository(application));
    }

    // Constructor with dependency injection
    public DashboardViewModel(Application application,
                              ScholarshipRepository scholarshipRepository,
                              StudentRepository studentRepository) {
        super(application);
        this.scholarshipRepository = scholarshipRepository;
        this.studentRepository = studentRepository;
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
            LiveData<Student> studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentLiveData.observeForever(student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                    welcomeMessage.postValue("Welcome, " + student.getFirstName() + "!");
                    loadRecommendations(student);
                }
            });
        }
    }

    private void loadRecommendations(Student student) {
        scholarshipRepository.getAllScholarships().observeForever(scholarships -> {
            if (scholarships != null && student != null) {
                List<Scholarship> recommended = getRecommendedScholarships(scholarships, student);
                recommendedScholarships.postValue(recommended);
            }
        });
    }

    private List<Scholarship> getRecommendedScholarships(List<Scholarship> allScholarships, Student student) {
        List<Scholarship> recommended = new ArrayList<>();
        
        for (Scholarship scholarship : allScholarships) {
            Double requiredGpa = filterService.parseGpaFromEligibility(scholarship.getEligibilityCriteria());
            
            boolean gpaMatch = (requiredGpa == null || student.getGpa() >= requiredGpa);
            boolean locationMatch = (scholarship.getLocation().equalsIgnoreCase("National") || 
                                   scholarship.getLocation().equalsIgnoreCase(student.getLocation()) ||
                                   student.getLocation() == null);
            
            if (gpaMatch && locationMatch && scholarship.isActive()) {
                recommended.add(scholarship);
            }
        }
        
        if (recommended.size() > 3) {
            return recommended.subList(0, 3);
        }
        
        return recommended;
    }
}
