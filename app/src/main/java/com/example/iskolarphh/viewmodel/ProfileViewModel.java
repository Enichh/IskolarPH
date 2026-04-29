package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.callback.StudentCallback;
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
            String uid = firebaseUser.getUid();
            String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
            
            // Use async method to load student data
            studentRepository.getStudentByFirebaseUidAsync(uid, student -> {
                if (student != null) {
                    // Student found in database
                    currentStudent.postValue(student);
                } else {
                    // No student record found (old account or data loss)
                    // Create a default student record for backwards compatibility
                    Student newStudent = createDefaultStudent(uid, email);
                    studentRepository.insert(newStudent, id -> {
                        if (id > 0) {
                            newStudent.setId((int) id);
                            currentStudent.postValue(newStudent);
                        } else {
                            errorMessage.postValue("Failed to create profile. Please try again.");
                        }
                    });
                }
            });
        }
    }

    private Student createDefaultStudent(String firebaseUid, String email) {
        Student student = new Student(
                firebaseUid,
                "New",          // firstName
                "User",         // lastName
                "",             // middleInitial
                "",             // location
                0.0             // gpa
        );
        student.setEmail(email);
        student.setCollege("");
        student.setCourse("");
        student.setContactNumber("");
        return student;
    }

    public void updateProfile(String fullName, String gpaStr, String location, String email,
                              String college, String course, String contactNumber, String photoPath) {
        Student student = currentStudent.getValue();
        if (student == null) {
            // Try to reload student data asynchronously
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if (firebaseUser != null) {
                studentRepository.getStudentByFirebaseUidAsync(firebaseUser.getUid(), loadedStudent -> {
                    if (loadedStudent != null) {
                        currentStudent.postValue(loadedStudent);
                        // Proceed with update using loaded student
                        performProfileUpdate(loadedStudent, fullName, gpaStr, location, email,
                                college, course, contactNumber, photoPath);
                    } else {
                        errorMessage.postValue("We couldn't load your profile data. Please close the app and try again.");
                    }
                });
                return;
            } else {
                errorMessage.postValue("Your session has expired. Please sign in again.");
                return;
            }
        }

        // Student already loaded, proceed with update
        performProfileUpdate(student, fullName, gpaStr, location, email,
                college, course, contactNumber, photoPath);
    }

    private void performProfileUpdate(Student student, String fullName, String gpaStr, String location,
                                      String email, String college, String course, String contactNumber,
                                      String photoPath) {
        if (fullName.isEmpty() || gpaStr.isEmpty() || location.isEmpty() || email.isEmpty()) {
            errorMessage.postValue("Please fill in all required fields (Name, GPA, Location, and Email) to update your profile.");
            return;
        }

        try {
            double gpa = Double.parseDouble(gpaStr);
            if (gpa < 0 || gpa > 5.0) {
                errorMessage.postValue("Please enter a valid GPA between 0 and 5.0. If you're using a different scale, please convert it.");
                return;
            }

            String[] nameParts = fullName.split(" ", 3);
            if (nameParts.length < 2) {
                errorMessage.postValue("Please enter your full name including both first and last name (e.g., Juan dela Cruz).");
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
            if (photoPath != null) {
                student.setProfilePhotoPath(photoPath);
            }

            studentRepository.update(student, new UpdateCallback() {
                @Override
                public void onUpdateComplete(int rowsAffected) {
                    if (rowsAffected > 0) {
                        updateSuccess.postValue(true);
                        errorMessage.postValue("Your profile has been updated successfully!");
                    } else {
                        updateSuccess.postValue(false);
                        errorMessage.postValue("We couldn't save your changes. Please try again or check your connection.");
                    }
                }
            });
        } catch (NumberFormatException e) {
            errorMessage.postValue("The GPA you entered doesn't look like a number. Please use a format like 3.5 or 4.0.");
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

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown executors to prevent thread leaks
        if (locationFlowManager != null) {
            locationFlowManager.shutdown();
        }
        if (studentRepository != null) {
            studentRepository.shutdown();
        }
    }
}
