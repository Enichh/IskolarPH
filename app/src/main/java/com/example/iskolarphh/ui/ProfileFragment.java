package com.example.iskolarphh.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.iskolarphh.R;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.LocationPermissionHandler;
import com.example.iskolarphh.util.LocationConstants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    private StudentRepository studentRepository;
    private FirebaseAuth firebaseAuth;
    private Student currentStudent;

    private TextView tvProfileName;
    private TextView tvGPADisplay;
    private TextView tvLocationDisplay;
    private TextView tvEmailDisplay;
    private TextView tvCourseDisplay;
    private TextView tvCollegeDisplay;
    private TextView tvContactDisplay;
    private EditText etFullName;
    private EditText etGPA;
    private EditText etLocation;
    private EditText etEmail;
    private EditText etCollege;
    private EditText etCourse;
    private EditText etContactNumber;
    private Button btnUpdate;
    private Button btnExit;
    private ImageButton btnUseCurrentLocation;
    private Button btnThemeToggle;
    private ImageButton btnEditToggle;
    private ProgressBar progressBarLocation;

    private boolean isEditMode = false;
    private boolean isDarkTheme = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        studentRepository = new StudentRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadStudentData();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvGPADisplay = view.findViewById(R.id.tvGPADisplay);
        tvLocationDisplay = view.findViewById(R.id.tvLocationDisplay);
        tvEmailDisplay = view.findViewById(R.id.tvEmailDisplay);
        tvCourseDisplay = view.findViewById(R.id.tvCourseDisplay);
        tvCollegeDisplay = view.findViewById(R.id.tvCollegeDisplay);
        tvContactDisplay = view.findViewById(R.id.tvContactDisplay);
        etFullName = view.findViewById(R.id.etFullName);
        etGPA = view.findViewById(R.id.etGPA);
        etLocation = view.findViewById(R.id.etLocation);
        etEmail = view.findViewById(R.id.etEmail);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnExit = view.findViewById(R.id.btnExit);
        btnUseCurrentLocation = view.findViewById(R.id.btnUseCurrentLocation);
        progressBarLocation = view.findViewById(R.id.progressBarLocation);

        etCollege = view.findViewById(R.id.etCollege);
        etCourse = view.findViewById(R.id.etCourse);
        etContactNumber = view.findViewById(R.id.etContactNumber);
        btnThemeToggle = view.findViewById(R.id.btnThemeToggle);
        btnEditToggle = view.findViewById(R.id.btnEditToggle);

        // Initialize in view mode
        setEditMode(false);
    }

    private void loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            LiveData<Student> studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentLiveData.observe(getViewLifecycleOwner(), new Observer<Student>() {
                @Override
                public void onChanged(Student student) {
                    if (student != null) {
                        currentStudent = student;
                        populateDisplayFields();
                        populateEditFields();
                    }
                }
            });
        }
    }

    private void populateDisplayFields() {
        if (currentStudent != null) {
            String fullName = currentStudent.getFirstName();
            if (currentStudent.getMiddleInitial() != null && !currentStudent.getMiddleInitial().isEmpty()) {
                fullName += " " + currentStudent.getMiddleInitial() + ". " + currentStudent.getLastName();
            } else {
                fullName += " " + currentStudent.getLastName();
            }
            tvProfileName.setText(fullName);
            tvGPADisplay.setText("GPA: " + currentStudent.getGpa());
            tvLocationDisplay.setText(currentStudent.getLocation());
            tvEmailDisplay.setText(currentStudent.getEmail());

            tvCourseDisplay.setText(currentStudent.getCourse() != null ? currentStudent.getCourse() : "Course / Program");
            tvCollegeDisplay.setText(currentStudent.getCollege() != null ? currentStudent.getCollege() : "School / University");
            tvContactDisplay.setText(currentStudent.getContactNumber() != null ? currentStudent.getContactNumber() : "Contact Number");

            tvCourseDisplay.setVisibility(View.VISIBLE);
            tvCollegeDisplay.setVisibility(View.VISIBLE);
            tvContactDisplay.setVisibility(View.VISIBLE);
        }
    }

    private void populateEditFields() {
        if (currentStudent != null) {
            String fullName = currentStudent.getFirstName() + " " + currentStudent.getLastName();
            if (currentStudent.getMiddleInitial() != null && !currentStudent.getMiddleInitial().isEmpty()) {
                fullName = currentStudent.getFirstName() + " " + currentStudent.getMiddleInitial() + ". " + currentStudent.getLastName();
            }
            etFullName.setText(fullName);
            etGPA.setText(String.valueOf(currentStudent.getGpa()));
            etLocation.setText(currentStudent.getLocation());
            
            String email = currentStudent.getEmail();
            if (email == null || email.isEmpty()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null && firebaseUser.getEmail() != null) {
                    email = firebaseUser.getEmail();
                }
            }
            etEmail.setText(email);
            
            etCollege.setText(currentStudent.getCollege());
            etCourse.setText(currentStudent.getCourse());
            etContactNumber.setText(currentStudent.getContactNumber());
        }
    }

    private void setupListeners() {
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLocationButtonClick();
            }
        });

        btnThemeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTheme();
            }
        });

        btnEditToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleEditMode();
            }
        });
    }

    private void updateProfile() {
        if (currentStudent != null) {
            String fullName = etFullName.getText().toString().trim();
            String gpaStr = etGPA.getText().toString().trim();
            String location = etLocation.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String college = etCollege.getText().toString().trim();
            String course = etCourse.getText().toString().trim();
            String contactNumber = etContactNumber.getText().toString().trim();

            if (fullName.isEmpty() || gpaStr.isEmpty() || location.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double gpa = Double.parseDouble(gpaStr);
                if (gpa < 0 || gpa > 5.0) {
                    Toast.makeText(requireContext(), "GPA must be between 0 and 5.0", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] nameParts = fullName.split(" ", 3);
                if (nameParts.length < 2) {
                    Toast.makeText(requireContext(), "Please enter a valid full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                currentStudent.setFirstName(nameParts[0]);
                currentStudent.setLastName(nameParts[nameParts.length - 1]);
                if (nameParts.length == 3) {
                    currentStudent.setMiddleInitial(nameParts[1]);
                } else {
                    currentStudent.setMiddleInitial("");
                }
                currentStudent.setGpa(gpa);
                currentStudent.setLocation(location);
                currentStudent.setEmail(email);
                currentStudent.setCollege(college.isEmpty() ? null : college);
                currentStudent.setCourse(course.isEmpty() ? null : course);
                currentStudent.setContactNumber(contactNumber.isEmpty() ? null : contactNumber);

                studentRepository.update(currentStudent, new StudentRepository.UpdateCallback() {
                    @Override
                    public void onUpdateComplete(int rowsAffected) {
                        if (rowsAffected > 0) {
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            populateDisplayFields();
                            setEditMode(false);
                        } else {
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Please enter a valid GPA", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logout() {
        firebaseAuth.signOut();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        requireActivity().finish();
    }

    private void handleLocationButtonClick() {
        if (LocationPermissionHandler.hasLocationPermissions(requireContext())) {
            fetchCurrentLocation();
        } else {
            LocationPermissionHandler.checkAndRequestLocationPermissions(requireActivity(),
                LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showLocationPermissionDialog() {
        LocationPermissionDialog.show(requireContext(), new LocationPermissionDialog.LocationPermissionCallback() {
            @Override
            public void onPermissionAllowed() {
                fetchCurrentLocation();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCurrentLocation() {
        progressBarLocation.setVisibility(View.VISIBLE);
        btnUseCurrentLocation.setEnabled(false);

        LocationManager.getInstance().requestSingleLocationUpdate(requireContext(), new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                GeocoderService.getInstance().getLocationFromCoordinates(requireContext(), latitude, longitude, new LocationCallback() {
                    @Override
                    public void onLocationRetrieved(String locationString, double lat, double lng) {
                        requireActivity().runOnUiThread(() -> {
                            progressBarLocation.setVisibility(View.GONE);
                            btnUseCurrentLocation.setEnabled(true);
                            etLocation.setText(locationString);
                            Toast.makeText(requireContext(), "Location updated: " + locationString, Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onLocationError(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            progressBarLocation.setVisibility(View.GONE);
                            btnUseCurrentLocation.setEnabled(true);
                            etLocation.setText(LocationConstants.DEFAULT_LOCATION);
                            Toast.makeText(requireContext(), "Using default location", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onPermissionDenied() {
                        requireActivity().runOnUiThread(() -> {
                            progressBarLocation.setVisibility(View.GONE);
                            btnUseCurrentLocation.setEnabled(true);
                        });
                    }
                });
            }

            @Override
            public void onLocationError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    progressBarLocation.setVisibility(View.GONE);
                    btnUseCurrentLocation.setEnabled(true);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onPermissionDenied() {
                requireActivity().runOnUiThread(() -> {
                    progressBarLocation.setVisibility(View.GONE);
                    btnUseCurrentLocation.setEnabled(true);
                });
            }
        });
    }

    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        View rootView = getView();
        if (rootView != null) {
            if (isDarkTheme) {
                rootView.setBackgroundColor(android.graphics.Color.parseColor("#001F3F"));
            } else {
                rootView.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            }
        }
    }

    private void toggleEditMode() {
        setEditMode(!isEditMode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LocationConstants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setEditMode(boolean editMode) {
        isEditMode = editMode;
        
        View rootView = getView();
        if (rootView == null) return;

        // Toggle edit section visibility
        LinearLayout editSection = rootView.findViewById(R.id.editSection);
        if (editSection != null) {
            editSection.setVisibility(editMode ? View.VISIBLE : View.GONE);
        }

        // Update button icon
        if (btnEditToggle != null) {
            btnEditToggle.setImageResource(editMode ? 
                android.R.drawable.ic_menu_save : 
                android.R.drawable.ic_menu_edit);
        }

        // Toggle photo change button visibility
        ImageView btnChangePhoto = rootView.findViewById(R.id.btnChangePhoto);
        if (btnChangePhoto != null) {
            btnChangePhoto.setVisibility(editMode ? View.VISIBLE : View.GONE);
        }
    }
}
