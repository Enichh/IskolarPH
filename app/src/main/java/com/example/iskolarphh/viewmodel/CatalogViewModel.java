package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.ScholarshipRepository;
import java.util.List;

public class CatalogViewModel extends AndroidViewModel {

    private final ScholarshipRepository scholarshipRepository;
    private final FilterViewModel filterViewModel;
    private final LocationViewModel locationViewModel;
    private final StudentViewModel studentViewModel;

    private final MutableLiveData<List<Scholarship>> filteredScholarships = new MutableLiveData<>();

    public CatalogViewModel(Application application) {
        this(application, 
             new com.example.iskolarphh.repository.ScholarshipRepository(application),
             new FilterViewModel(application),
             new LocationViewModel(application),
             new StudentViewModel(application));
    }

    // Constructor with dependency injection
    public CatalogViewModel(Application application,
                           ScholarshipRepository scholarshipRepository,
                           FilterViewModel filterViewModel,
                           LocationViewModel locationViewModel,
                           StudentViewModel studentViewModel) {
        super(application);
        this.scholarshipRepository = scholarshipRepository;
        this.filterViewModel = filterViewModel;
        this.locationViewModel = locationViewModel;
        this.studentViewModel = studentViewModel;
    }

    public LiveData<List<Scholarship>> getFilteredScholarships() {
        return filteredScholarships;
    }

    public LiveData<Student> getCurrentStudent() {
        return studentViewModel.getCurrentStudent();
    }

    public LiveData<String> getLocationHeader() {
        return locationViewModel.getLocationHeader();
    }

    public LiveData<String> getErrorMessage() {
        return locationViewModel.getErrorMessage();
    }

    public LiveData<String> getFilterErrorMessage() {
        return filterViewModel.getErrorMessage();
    }

    public LiveData<Student> loadStudentData() {
        return studentViewModel.loadStudentData();
    }

    public void setSearchQuery(String query) {
        filterViewModel.setSearchQuery(query);
        applyFilters();
    }

    public void setLocationFilter(String location) {
        filterViewModel.setLocationFilter(location);
        applyFilters();
    }

    public void toggleGpaFilter() {
        Student student = studentViewModel.getStudentValue();
        if (student == null) {
            locationViewModel.getErrorMessage().postValue("Please complete your profile first");
            return;
        }
        filterViewModel.toggleGpaFilter();
        applyFilters();
    }

    public void refreshLocation() {
        Student student = studentViewModel.getStudentValue();
        locationViewModel.refreshLocation(student);
    }

    public LiveData<List<Scholarship>> getScholarshipsLiveData() {
        return scholarshipRepository.searchAndFilterScholarships(
            filterViewModel.getCurrentSearchQuery(),
            filterViewModel.getCurrentLocationFilter()
        );
    }

    public void setCurrentStudent(Student student) {
        studentViewModel.setCurrentStudent(student);
        locationViewModel.updateLocationHeader(student);
    }

    public List<Scholarship> applyGpaFilter(List<Scholarship> scholarships) {
        Student student = studentViewModel.getStudentValue();
        if (student != null) {
            return filterViewModel.applyGpaFilter(scholarships, student.getGpa());
        }
        return scholarships;
    }

    private void applyFilters() {
        Student student = studentViewModel.getStudentValue();
        if (student != null) {
            filterViewModel.validateGpaFilter(student.getGpa());
        }
    }

    public void updateScholarshipSaved(Scholarship scholarship) {
        scholarship.setSaved(!scholarship.isSaved());
        scholarshipRepository.update(scholarship);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
