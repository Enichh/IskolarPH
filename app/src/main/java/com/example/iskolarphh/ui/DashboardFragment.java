package com.example.iskolarphh.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.ViewModelProvider;

import com.example.iskolarphh.R;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.viewmodel.DashboardViewModel;
import com.example.iskolarphh.adapter.ScholarshipAdapter;
import com.example.iskolarphh.util.PerformanceMonitor;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private ExecutorService executorService;
    private android.os.Handler mainHandler;
    private ScholarshipAdapter scholarshipAdapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        // Initialize modern threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        dashboardViewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        initializeViews(view);
        observeViewModel();
        setupCardListeners(view);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void initializeViews(View view) {
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewScholarships);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        
        // Setup RecyclerView with optimized configuration
        scholarshipAdapter = new ScholarshipAdapter();
        recyclerView.setAdapter(scholarshipAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Optimize RecyclerView performance
        // Note: setHasFixedSize(false) because RecyclerView height is wrap_content
        recyclerView.setHasFixedSize(false);
    }

    private void observeViewModel() {
        dashboardViewModel.loadStudentData();

        dashboardViewModel.getWelcomeMessage().observe(getViewLifecycleOwner(), message -> {
            View view = getView();
            if (view != null) {
                TextView tvWelcome = view.findViewById(R.id.tvWelcome);
                if (tvWelcome != null && message != null) {
                    tvWelcome.setText(message);
                }
            }
        });

        dashboardViewModel.getRecommendedScholarships().observe(getViewLifecycleOwner(), scholarships -> {
            if (scholarships != null) {
                displayRecommendations(scholarships);
            }
        });

        dashboardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCardListeners(View view) {
        CardView cardSaved = view.findViewById(R.id.cardSaved);
        CardView cardCourse = view.findViewById(R.id.cardCourse);
        CardView cardDeadlines = view.findViewById(R.id.cardDeadlines);

        cardSaved.setOnClickListener(v -> {
            loadFilteredScholarships("Saved Scholarships", dashboardViewModel.getSavedScholarships());
        });

        cardCourse.setOnClickListener(v -> {
            LiveData<com.example.iskolarphh.database.entity.Student> studentLiveData = dashboardViewModel.getCurrentStudent();
            studentLiveData.observe(getViewLifecycleOwner(), student -> {
                if (student != null && student.getCourse() != null) {
                    loadFilteredScholarships("For your Course: " + student.getCourse(), 
                        dashboardViewModel.searchByCourse(student.getCourse()));
                } else {
                    Toast.makeText(requireContext(), "Please set your course in Profile first", Toast.LENGTH_SHORT).show();
                }
            });
        });

        cardDeadlines.setOnClickListener(v -> {
            loadFilteredScholarships("Upcoming Deadlines", dashboardViewModel.getScholarshipsByDeadline());
        });
    }

    private void loadFilteredScholarships(String title, LiveData<List<Scholarship>> data) {
        View view = getView();
        if (view == null) return;
        
        TextView tvTitle = view.findViewById(R.id.tvRecommendationsTitle);
        if (tvTitle != null) tvTitle.setText(title);
        
        data.observe(getViewLifecycleOwner(), scholarships -> {
            if (scholarships != null) {
                displayRecommendations(scholarships);
            }
        });
    }



    private void displayRecommendations(List<Scholarship> scholarships) {
        long startTime = System.nanoTime();
        
        // Use RecyclerView for efficient view recycling
        if (scholarships == null || scholarships.isEmpty()) {
            // Show empty state
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            // Show scholarships
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            
            // Update adapter data efficiently with DiffUtil
            scholarshipAdapter.submitList(scholarships);
            
            // Log performance metrics
            PerformanceMonitor.logMethodExecutionTime("DashboardFragment.displayRecommendations()", startTime);
            PerformanceMonitor.logMemoryUsage("After RecyclerView update");
        }
    }

    // Removed old view creation methods - now using RecyclerView for performance
}
