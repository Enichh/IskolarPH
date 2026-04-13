package com.example.iskolarphh.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_catalog, container, false);

        scholarshipRepository = new ScholarshipRepository(requireContext());
        studentRepository = new StudentRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        setupRecyclerView(view);
        setupSearch(view);
        setupFilters(view);
        loadStudentData();

        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.rv_scholarships);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ScholarshipAdapter(scholarship -> {
            Intent intent = new Intent(getActivity(), ScholarshipDetailActivity.class);
            intent.putExtra(ScholarshipDetailActivity.EXTRA_SCHOLARSHIP_ID, scholarship.getId());
            startActivity(intent);
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

    private void loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            LiveData<Student> studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentLiveData.observe(getViewLifecycleOwner(), new Observer<Student>() {
                @Override
                public void onChanged(Student student) {
                    if (student != null) {
                        currentStudent = student;
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
        scholarshipRepository.searchAndFilterScholarships(currentSearchQuery, currentLocationFilter)
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
}
