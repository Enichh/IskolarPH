package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.callback.UpdateCallback;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.LocationFlowManager;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.GeocoderService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final LocationFlowManager locationFlowManager;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> locationUpdateStatus = new MutableLiveData<>();

    public ProfileViewModel(Application application) {
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
    public ProfileViewModel(Application application,
                             StudentRepository studentRepository,
                             LocationFlowManager locationFlowManager) {
        super(application);
        this.studentRepository = studentRepository;
        this.locationFlowManager = locationFlowManager;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Student> getCurrentStudent() {
        return currentStudent;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getLocationUpdateStatus() {
        return locationUpdateStatus;
    }

    public void loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            LiveData<Student> studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentLiveData.observeForever(student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                }
            });
        }
    }

    public void updateProfile(String fullName, String gpaStr, String location, String email, 
                              String college, String course, String contactNumber) {
        Student student = currentStudent.getValue();
        if (student == null) {
            errorMessage.postValue("Student data not loaded");
            return;
        }

        if (fullName.isEmpty() || gpaStr.isEmpty() || location.isEmpty() || email.isEmpty()) {
            errorMessage.postValue("Please fill in required fields");
            return;
        }

        try {
            double gpa = Double.parseDouble(gpaStr);
            if (gpa < 0 || gpa > 5.0) {
                errorMessage.postValue("GPA must be between 0 and 5.0");
                return;
            }

            String[] nameParts = fullName.split(" ", 3);
            if (nameParts.length < 2) {
                errorMessage.postValue("Please enter a valid full name");
                return;
            }

            student.setFirstName(nameParts[0]);
            student.setLastName(nameParts[nameParts.length - 1]);
            if (nameParts.length == 3) {
                student.setMiddleInitial(nameParts[1]);
            } else {
                student.setMiddleInitial("");
            }
            student.setGpa(gpa);
            student.setLocation(location);
            student.setEmail(email);
            student.setCollege(college.isEmpty() ? null : college);
            student.setCourse(course.isEmpty() ? null : course);
            student.setContactNumber(contactNumber.isEmpty() ? null : contactNumber);

            studentRepository.update(student, new UpdateCallback() {
                @Override
                public void onUpdateComplete(int rowsAffected) {
                    if (rowsAffected > 0) {
                        updateSuccess.postValue(true);
                        errorMessage.postValue("Profile updated successfully");
                    } else {
                        updateSuccess.postValue(false);
                        errorMessage.postValue("Failed to update profile");
                    }
                }
            });
        } catch (NumberFormatException e) {
            errorMessage.postValue("Please enter a valid GPA");
        }
    }

    public void fetchCurrentLocation() {
        locationFlowManager.fetchAndSaveLocation(new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                locationUpdateStatus.postValue(location);
            }

            @Override
            public void onLocationError(String errorMessage) {
                ProfileViewModel.this.errorMessage.postValue(errorMessage);
            }

            @Override
            public void onPermissionDenied() {
                ProfileViewModel.this.errorMessage.postValue("Location permission denied");
            }
        });
    }

    public boolean hasLocationPermissions() {
        return locationFlowManager.hasLocationPermissions();
    }

    public void requestLocationPermissions(android.app.Activity activity, int requestCode) {
        locationFlowManager.requestLocationPermissions(activity, requestCode);
    }

    public void handlePermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        locationFlowManager.handlePermissionResult(requestCode, permissions, grantResults, new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                locationUpdateStatus.postValue(location);
            }

            @Override
            public void onLocationError(String errorMessage) {
                ProfileViewModel.this.errorMessage.postValue(errorMessage);
            }

            @Override
            public void onPermissionDenied() {
                ProfileViewModel.this.errorMessage.postValue("Location permission denied");
            }
        });
    }

    public void logout() {
        firebaseAuth.signOut();
    }
}
