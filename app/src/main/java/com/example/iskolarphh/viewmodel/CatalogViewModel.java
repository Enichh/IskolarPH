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
import com.example.iskolarphh.utils.SearchDebounceHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private final ScholarshipRepository scholarshipRepository;
    private final StudentRepository studentRepository;
    private final ScholarshipFilterService filterService;
    private final FirebaseAuth firebaseAuth;
    private final SearchDebounceHelper searchDebounceHelper;

    private final MutableLiveData<List<Scholarship>> filteredScholarships = new MutableLiveData<>();
    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();
    private final MutableLiveData<String> locationHeader = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> filterStatus = new MutableLiveData<>();

    // Filter state
    private volatile String currentSearchQuery = "";
    private volatile String currentLocationFilter = null;
    private volatile boolean gpaFilterEnabled = false;
    private final MutableLiveData<Void> filterTrigger = new MutableLiveData<>();

    // Observers for cleanup
    private Observer<Student> studentObserver;
    private LiveData<Student> studentLiveData;

    public CatalogViewModel(Application application) {
        this(application, 
             new ScholarshipRepository(application),
             new StudentRepository(application),
             new ScholarshipFilterService(),
             new SearchDebounceHelper(400));
        filterTrigger.setValue(null);
    }

    // Constructor with dependency injection
    public CatalogViewModel(Application application,
                           ScholarshipRepository scholarshipRepository,
                           StudentRepository studentRepository,
                           ScholarshipFilterService filterService,
                           SearchDebounceHelper searchDebounceHelper) {
        super(application);
        this.scholarshipRepository = scholarshipRepository;
        this.studentRepository = studentRepository;
        this.filterService = filterService;
        this.searchDebounceHelper = searchDebounceHelper;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Scholarship>> getFilteredScholarships() {
        return filteredScholarships;
    }

    public LiveData<Student> getCurrentStudent() {
        return currentStudent;
    }

    public LiveData<String> getLocationHeader() {
        return locationHeader;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getFilterStatus() {
        return filterStatus;
    }

    public LiveData<Student> loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Remove existing observer if any
            if (studentObserver != null && studentLiveData != null) {
                studentLiveData.removeObserver(studentObserver);
            }
            
            studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentObserver = student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                    updateLocationHeader(student);
                    updateFilterStatus();
                }
            };
            studentLiveData.observeForever(studentObserver);
            return studentLiveData;
        }
        return null;
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query;
        updateFilterStatus();
        searchDebounceHelper.debounce(() -> filterTrigger.postValue(null));
    }

    public void setLocationFilter(String location) {
        currentLocationFilter = location;
        filterTrigger.setValue(null);
        updateFilterStatus();
    }

    public void toggleGpaFilter() {
        Student student = currentStudent.getValue();
        if (student == null) {
            errorMessage.postValue("Please complete your profile first");
            return;
        }
        gpaFilterEnabled = !gpaFilterEnabled;
        updateFilterStatus();
        filterTrigger.setValue(null);
    }

    public void refreshLocation() {
        Student student = currentStudent.getValue();
        if (student != null) {
            updateLocationHeader(student);
        }
    }

    public LiveData<List<Scholarship>> getScholarshipsLiveData() {
        return androidx.lifecycle.Transformations.switchMap(filterTrigger, trigger -> {
            Student student = currentStudent.getValue();
            Double studentGpa = student != null ? student.getGpa() : null;
            return scholarshipRepository.searchAndFilterScholarships(currentSearchQuery, currentLocationFilter, studentGpa, gpaFilterEnabled);
        });
    }

    public void setCurrentStudent(Student student) {
        currentStudent.setValue(student);
        updateLocationHeader(student);
        updateFilterStatus();
    }

    private void updateLocationHeader(Student student) {
        if (student != null && student.getLocation() != null && !student.getLocation().isEmpty()) {
            locationHeader.postValue("Scholarships near " + student.getLocation());
        } else {
            locationHeader.postValue("All Scholarships");
        }
    }

    private void updateFilterStatus() {
        StringBuilder status = new StringBuilder();
        
        // Location filter
        if (currentLocationFilter == null || currentLocationFilter.isEmpty()) {
            status.append("Location: All");
        } else {
            status.append("Location: ").append(currentLocationFilter);
        }
        
        // GPA filter
        if (gpaFilterEnabled) {
            Student student = currentStudent.getValue();
            if (student != null) {
                status.append(" | GPA Filter: ON (Your GPA: ").append(student.getGpa()).append(")");
            } else {
                status.append(" | GPA Filter: ON");
            }
        } else {
            status.append(" | GPA Filter: OFF");
        }
        
        filterStatus.postValue(status.toString());
    }

    public void updateScholarshipSaved(Scholarship scholarship) {
        scholarship.setSaved(!scholarship.isSaved());
        scholarshipRepository.update(scholarship);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up observer
        if (studentObserver != null && studentLiveData != null) {
            studentLiveData.removeObserver(studentObserver);
            studentObserver = null;
        }
        studentLiveData = null;
        // Shutdown debounce helper
        if (searchDebounceHelper != null) {
            searchDebounceHelper.shutdown();
        }
        // Shutdown repository executors
        if (scholarshipRepository != null) {
            scholarshipRepository.shutdown();
        }
        if (studentRepository != null) {
            studentRepository.shutdown();
        }
    }
}
