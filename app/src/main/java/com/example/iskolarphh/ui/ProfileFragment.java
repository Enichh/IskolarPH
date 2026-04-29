package com.example.iskolarphh.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.iskolarphh.service.LocationFlowManager;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.repository.StudentRepository;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import com.example.iskolarphh.R;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.ui.LogoutConfirmationDialog;
import com.example.iskolarphh.viewmodel.ProfileViewModel;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.util.LocationConstants;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private FirebaseAuth firebaseAuth;
    private LocationFlowManager locationFlowManager;
    private Student currentStudent;

    private TextView tvProfileName;
    private TextView tvGPADisplay;
    private TextView tvLocationDisplay;
    private TextView tvEmailDisplay;
    private TextView tvCourseDisplay;
    private TextView tvCollegeDisplay;
    private TextView tvContactDisplay;
    private ImageView ivProfilePicture;
    private ImageView btnChangePhoto;
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
    private ProgressBar progressBarLocation;

    private boolean isEditMode = false;
    private boolean isDarkTheme = false;
    private boolean isStudentLoaded = false;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private Bitmap capturedPhotoBitmap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Register Camera Permission Launcher
        cameraPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    DialogManager.showErrorDialog(requireContext(),
                            "Permission Needed",
                            "Camera access is required to update your profile photo. Please enable it in your device settings.");
                }
            }
        );

        // Register Camera Launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (ivProfilePicture != null && imageBitmap != null) {
                            ivProfilePicture.setImageBitmap(imageBitmap);
                            capturedPhotoBitmap = imageBitmap;
                        }
                    }
                }
            }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_profile, container, false);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        firebaseAuth = FirebaseAuth.getInstance();
        locationFlowManager = new LocationFlowManager(requireContext(),
            new LocationManager(),
            new GeocoderService(),
            new StudentRepository(requireContext()));

        initializeViews(view);
        observeViewModel();
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
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture);
        btnChangePhoto = view.findViewById(R.id.btnChangePhoto);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnExit = view.findViewById(R.id.btnExit);
        btnUseCurrentLocation = view.findViewById(R.id.btnUseCurrentLocation);
        progressBarLocation = view.findViewById(R.id.progressBarLocation);

        etCollege = view.findViewById(R.id.etCollege);
        etCourse = view.findViewById(R.id.etCourse);
        etContactNumber = view.findViewById(R.id.etContactNumber);
        btnThemeToggle = view.findViewById(R.id.btnThemeToggle);

        // Initialize in view mode, disable edit until student data loads
        setEditMode(false);
        btnUpdate.setEnabled(false);
    }

    private void observeViewModel() {
        profileViewModel.loadStudentData();

        profileViewModel.getCurrentStudent().observe(getViewLifecycleOwner(), new Observer<Student>() {
            @Override
            public void onChanged(Student student) {
                if (student != null) {
                    currentStudent = student;
                    isStudentLoaded = true;
                    populateDisplayFields();
                    populateEditFields();
                    // Enable Edit button now that data is loaded
                    if (btnUpdate != null) {
                        btnUpdate.setEnabled(true);
                    }
                } else {
                    isStudentLoaded = false;
                    if (btnUpdate != null) {
                        btnUpdate.setEnabled(false);
                    }
                }
            }
        });

        profileViewModel.getUpdateSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                populateDisplayFields();
                setEditMode(false);
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                DialogManager.showErrorDialog(requireContext(), "Something went wrong", error);
            }
        });

        profileViewModel.getLocationUpdateStatus().observe(getViewLifecycleOwner(), location -> {
            if (location != null && !location.isEmpty()) {
                etLocation.setText(location);
                if (getView() != null) {
                    DialogManager.showSuccessSnackbar(getView(), "Location updated to " + location);
                }
            }
        });
    }

    private void populateDisplayFields() {
        if (currentStudent != null) {
            // Load profile photo from internal storage if available
            loadProfilePhoto();
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
                if (isEditMode) {
                    updateProfile();
                } else {
                    setEditMode(true);
                }
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

        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    private void updateProfile() {
        // Guard against saving when student data isn't loaded yet
        if (!isStudentLoaded || currentStudent == null) {
            DialogManager.showErrorDialog(requireContext(),
                    "Please wait",
                    "Your profile is still loading. Please try again in a moment.");
            return;
        }

        String fullName = etFullName.getText().toString().trim();
        String gpaStr = etGPA.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String college = etCollege.getText().toString().trim();
        String course = etCourse.getText().toString().trim();
        String contactNumber = etContactNumber.getText().toString().trim();

        // Save captured photo to internal storage if available
        String photoPath = null;
        if (capturedPhotoBitmap != null) {
            photoPath = savePhotoToInternalStorage(capturedPhotoBitmap, currentStudent.getFirebaseUid());
        }

        profileViewModel.updateProfile(fullName, gpaStr, location, email, college, course, contactNumber, photoPath);
    }

    private String savePhotoToInternalStorage(Bitmap bitmap, String firebaseUid) {
        try {
            File directory = new File(requireContext().getFilesDir(), "profile_photos");
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File photoFile = new File(directory, firebaseUid + "_profile.jpg");
            FileOutputStream fos = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            return photoFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            DialogManager.showErrorDialog(requireContext(),
                    "Photo Save Failed",
                    "We couldn't save your profile photo. Please try again or check your device storage.");
            return null;
        }
    }

    private void loadProfilePhoto() {
        if (currentStudent != null && currentStudent.getProfilePhotoPath() != null) {
            File photoFile = new File(currentStudent.getProfilePhotoPath());
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                if (bitmap != null && ivProfilePicture != null) {
                    ivProfilePicture.setImageBitmap(bitmap);
                }
            }
        }
    }

    private void logout() {
        // Show logout confirmation dialog
        LogoutConfirmationDialog logoutDialog = new LogoutConfirmationDialog();
        logoutDialog.setLogoutConfirmationListener(new LogoutConfirmationDialog.LogoutConfirmationListener() {
            @Override
            public void onLogoutConfirmed() {
                // User confirmed logout
                profileViewModel.logout();
                if (getView() != null) {
                    DialogManager.showSuccessSnackbar(getView(), "You've been signed out");
                }
                requireActivity().finish();
            }

            @Override
            public void onLogoutCancelled() {
                // User cancelled logout - no action needed
                // Dialog will be dismissed automatically
            }
        });
        
        logoutDialog.show(getParentFragmentManager(), "LogoutConfirmationDialog");
    }

    private void handleLocationButtonClick() {
        if (profileViewModel.hasLocationPermissions()) {
            fetchCurrentLocation();
        } else {
            profileViewModel.requestLocationPermissions(requireActivity(),
                LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void showLocationPermissionDialog() {
        locationFlowManager.checkAndRequestLocationPermission(requireActivity(), new LocationPermissionDialog.LocationPermissionCallback() {
            @Override
            public void onPermissionAllowed() {
                fetchCurrentLocation();
            }

            @Override
            public void onPermissionDenied() {
                DialogManager.showErrorDialog(requireContext(),
                        "Location Access Denied",
                        "Without location permission, we can't automatically update your location. You can still enter it manually in your profile.");
            }
        });
    }

    private void fetchCurrentLocation() {
        progressBarLocation.setVisibility(View.VISIBLE);
        btnUseCurrentLocation.setEnabled(false);

        profileViewModel.fetchCurrentLocation();

        profileViewModel.getLocationUpdateStatus().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                progressBarLocation.setVisibility(View.GONE);
                btnUseCurrentLocation.setEnabled(true);
            }
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                progressBarLocation.setVisibility(View.GONE);
                btnUseCurrentLocation.setEnabled(true);
                etLocation.setText(LocationConstants.DEFAULT_LOCATION);
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

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            cameraLauncher.launch(takePictureIntent);
        } catch (Exception e) {
            DialogManager.showErrorDialog(requireContext(),
                    "Camera Not Available",
                    "We couldn't open your camera app. Please make sure you have a camera app installed.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        profileViewModel.handlePermissionResult(requestCode, permissions, grantResults);
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

        // Update button text
        if (btnUpdate != null) {
            btnUpdate.setText(editMode ? "Save Profile" : "Edit Profile");
        }

        // Toggle photo change button visibility
        if (btnChangePhoto != null) {
            btnChangePhoto.setVisibility(editMode ? View.VISIBLE : View.GONE);
        }
    }
}
