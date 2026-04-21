package com.example.iskolarphh.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iskolarphh.R;
import com.example.iskolarphh.callback.LocationCallback;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.LocationPermissionHandler;
import com.example.iskolarphh.util.LocationConstants;
import com.example.iskolarphh.util.LocationPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CatalogFragment extends Fragment {

    private ScholarshipRepository scholarshipRepository;
    private StudentRepository studentRepository;
    private ScholarshipAdapter adapter;
    private FirebaseAuth firebaseAuth;
    private Student currentStudent;
    private String currentSearchQuery = "";
    private String currentLocationFilter = null;
    private boolean gpaFilterEnabled = false;

    private TextView tvLocationHeader;
    private ImageButton btnRefreshLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_catalog, container, false);

        scholarshipRepository = ScholarshipRepository.getInstance(requireContext());
        studentRepository = new StudentRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        setupRecyclerView(view);
        setupSearch(view);
        setupFilters(view);
        setupLocationHeader(view);
        loadStudentData();

        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_scholarships);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScholarshipAdapter(new ScholarshipAdapter.OnScholarshipClickListener() {
            @Override
            public void onScholarshipClick(Scholarship scholarship) {
                Intent intent = new Intent(getActivity(), ScholarshipDetailActivity.class);
                intent.putExtra(ScholarshipDetailActivity.EXTRA_SCHOLARSHIP_ID, scholarship.getId());
                startActivity(intent);
            }

            @Override
            public void onSaveClick(Scholarship scholarship) {
                scholarship.setSaved(!scholarship.isSaved());
                scholarshipRepository.update(scholarship);
                adapter.notifyItemChanged(adapter.getCurrentList().indexOf(scholarship));
                String message = scholarship.isSaved() ? "Scholarship saved" : "Scholarship removed from saved";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupSearch(View view) {
        EditText etSearch = view.findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                observeScholarships();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters(View view) {
        ImageView btnFilterLocation = view.findViewById(R.id.btn_filter_location);
        ImageView btnFilterGpa = view.findViewById(R.id.btn_filter_gpa);

        btnFilterLocation.setOnClickListener(v -> showLocationFilterDialog());
        btnFilterGpa.setOnClickListener(v -> toggleGpaFilter());
    }

    private void setupLocationHeader(View view) {
        tvLocationHeader = view.findViewById(R.id.tvLocationHeader);
        btnRefreshLocation = view.findViewById(R.id.btnRefreshLocation);

        btnRefreshLocation.setOnClickListener(v -> refreshLocation());
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
                        updateLocationHeader();
                        observeScholarships();
                    }
                }
            });
        }
    }

    private void showLocationFilterDialog() {
        String[] locations = {"All", "Luzon", "Visayas", "Mindanao", "National"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Location")
                .setItems(locations, (dialog, which) -> {
                    if (which == 0) {
                        currentLocationFilter = null;
                    } else {
                        currentLocationFilter = locations[which];
                    }
                    observeScholarships();
                })
                .show();
    }

    private void toggleGpaFilter() {
        if (currentStudent == null) {
            Toast.makeText(requireContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show();
            return;
        }
        gpaFilterEnabled = !gpaFilterEnabled;
        String status = gpaFilterEnabled ? "enabled" : "disabled";
        Toast.makeText(requireContext(), "GPA filter " + status, Toast.LENGTH_SHORT).show();
        observeScholarships();
    }

    private void observeScholarships() {
        String location = currentLocationFilter != null ? currentLocationFilter : LocationPreferences.getLastLocation(requireContext());
        
        scholarshipRepository.searchAndFilterScholarships(currentSearchQuery, location)
                .observe(getViewLifecycleOwner(), scholarships -> {
                    if (adapter != null) {
                        List<Scholarship> filteredScholarships = scholarships;
                        if (gpaFilterEnabled && currentStudent != null) {
                            filteredScholarships = filterByGpa(scholarships, currentStudent.getGpa());
                        }
                        adapter.submitList(filteredScholarships);
                    }
                });
    }

    private List<Scholarship> filterByGpa(List<Scholarship> scholarships, double studentGpa) {
        List<Scholarship> filtered = new ArrayList<>();
        for (Scholarship scholarship : scholarships) {
            Double requiredGpa = parseGpaFromEligibility(scholarship.getEligibilityCriteria());
            if (requiredGpa == null || studentGpa >= requiredGpa) {
                filtered.add(scholarship);
            }
        }
        return filtered;
    }

    private Double parseGpaFromEligibility(String eligibilityCriteria) {
        if (eligibilityCriteria == null) {
            return null;
        }
        
        Pattern pattern = Pattern.compile("(\\d+\\.?\\d*)%");
        Matcher matcher = pattern.matcher(eligibilityCriteria);
        
        if (matcher.find()) {
            try {
                double percentage = Double.parseDouble(matcher.group(1));
                return percentage / 100.0 * 4.0;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        Pattern gpaPattern = Pattern.compile("(\\d+\\.?\\d*)");
        Matcher gpaMatcher = gpaPattern.matcher(eligibilityCriteria);
        
        if (gpaMatcher.find()) {
            try {
                return Double.parseDouble(gpaMatcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }

    private void updateLocationHeader() {
        if (currentStudent != null && currentStudent.getLocation() != null && !currentStudent.getLocation().isEmpty()) {
            tvLocationHeader.setText("Scholarships near " + currentStudent.getLocation());
        } else {
            tvLocationHeader.setText("All Scholarships");
        }
    }

    private void refreshLocation() {
        if (currentStudent == null) {
            Toast.makeText(requireContext(), "Please complete your profile first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (LocationPermissionHandler.hasLocationPermissions(requireContext())) {
            fetchLocationForHeader();
        } else {
            LocationPermissionHandler.checkAndRequestLocationPermissions(requireActivity(),
                LocationConstants.LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void fetchLocationForHeader() {
        LocationManager.getInstance().requestSingleLocationUpdate(requireContext(), new LocationCallback() {
            @Override
            public void onLocationRetrieved(String location, double latitude, double longitude) {
                GeocoderService.getInstance().getLocationFromCoordinates(requireContext(), latitude, longitude, new LocationCallback() {
                    @Override
                    public void onLocationRetrieved(String locationString, double lat, double lng) {
                        requireActivity().runOnUiThread(() -> {
                            if (currentStudent != null) {
                                currentStudent.setLocation(locationString);
                                LocationPreferences.saveLocation(requireContext(), locationString, LocationConstants.LOCATION_SOURCE_GPS);
                                studentRepository.update(currentStudent, null);
                                updateLocationHeader();
                                Toast.makeText(requireContext(), "Location updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onLocationError(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Geocoder error: " + errorMessage + ". Using last known location.", Toast.LENGTH_SHORT).show();
                            String lastLocation = LocationPreferences.getLastLocation(requireContext());
                            if (currentStudent != null) {
                                currentStudent.setLocation(lastLocation);
                                updateLocationHeader();
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied() {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Location permission denied. Using last known location.", Toast.LENGTH_SHORT).show();
                            String lastLocation = LocationPreferences.getLastLocation(requireContext());
                            if (currentStudent != null) {
                                currentStudent.setLocation(lastLocation);
                                updateLocationHeader();
                            }
                        });
                    }
                });
            }

            @Override
            public void onLocationError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "GPS error: " + errorMessage + ". Using last known location.", Toast.LENGTH_SHORT).show();
                    String lastLocation = LocationPreferences.getLastLocation(requireContext());
                    if (currentStudent != null) {
                        currentStudent.setLocation(lastLocation);
                        updateLocationHeader();
                    }
                });
            }

            @Override
            public void onPermissionDenied() {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Location permission denied. Using last known location.", Toast.LENGTH_SHORT).show();
                    String lastLocation = LocationPreferences.getLastLocation(requireContext());
                    if (currentStudent != null) {
                        currentStudent.setLocation(lastLocation);
                        updateLocationHeader();
                    }
                });
            }
        });
    }
}
