package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.LocationFlowManager;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.GeocoderService;

public class LocationViewModel extends AndroidViewModel {

    private final LocationFlowManager locationFlowManager;
    private final StudentRepository studentRepository;
    private final MutableLiveData<String> locationHeader = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final GeocoderService geocoderService;

    public LocationViewModel(Application application) {
        this(application, 
             new StudentRepository(application),
             new LocationFlowManager(
                 application,
                 new LocationManager(),
                 new GeocoderService(),
                 new StudentRepository(application)
             ));
    }

    // Constructor with dependency injection
    public LocationViewModel(Application application, 
                             StudentRepository studentRepository,
                             LocationFlowManager locationFlowManager) {
        super(application);
        this.studentRepository = studentRepository;
        this.locationFlowManager = locationFlowManager;
        this.geocoderService = new GeocoderService();
    }

    public MutableLiveData<String> getLocationHeader() {
        return locationHeader;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public boolean hasLocationPermissions() {
        return locationFlowManager.hasLocationPermissions();
    }

    public void refreshLocation(Student student) {
        if (student == null) {
            errorMessage.postValue("Please complete your profile first");
            return;
        }

        if (locationFlowManager.hasLocationPermissions()) {
            fetchLocationForHeader(student);
        } else {
            errorMessage.postValue("Location permission required");
        }
    }

    public void updateLocationHeader(Student student) {
        if (student != null && student.getLocation() != null && !student.getLocation().isEmpty()) {
            locationHeader.postValue("Scholarships near " + student.getLocation());
        } else {
            locationHeader.postValue("All Scholarships");
        }
    }

    private void fetchLocationForHeader(Student student) {
        locationFlowManager.fetchAndSaveLocation(new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                student.setLocation(location);
                studentRepository.update(student, null);
                updateLocationHeader(student);
                errorMessage.postValue("Location updated");
            }

            @Override
            public void onLocationError(String errorMessage) {
                LocationViewModel.this.errorMessage.postValue("Location error: " + errorMessage);
            }

            @Override
            public void onPermissionDenied() {
                LocationViewModel.this.errorMessage.postValue("Location permission denied");
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        geocoderService.close();
    }
}
