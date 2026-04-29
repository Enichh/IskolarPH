package com.example.iskolarphh.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.iskolarphh.database.entity.Scholarship;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.ViewModelProvider;

import com.example.iskolarphh.R;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.viewmodel.DashboardViewModel;
import com.example.iskolarphh.adapter.ScholarshipAdapter;
import com.example.iskolarphh.ui.DialogManager;
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
    private View loadingIndicator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize modern threading
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        dashboardViewModel = new ViewModelProvider(this, factory).get(DashboardViewModel.class);

        initializeViews(view);
        observeViewModel();

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
        
        // Create loading indicator if not present in layout
        if (view.findViewById(R.id.loadingIndicator) == null) {
            loadingIndicator = createLoadingIndicator(view);
        } else {
            loadingIndicator = view.findViewById(R.id.loadingIndicator);
        }
        
        // Setup RecyclerView with optimized configuration
        scholarshipAdapter = new ScholarshipAdapter();
        recyclerView.setAdapter(scholarshipAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Optimize RecyclerView performance
        // Note: setHasFixedSize(false) because RecyclerView height is wrap_content
        recyclerView.setHasFixedSize(false);
    }

    private View createLoadingIndicator(View parent) {
        // Create a simple loading indicator programmatically if not in layout
        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(getContext());
        progressBar.setVisibility(View.GONE);
        
        // Add to parent layout (assuming ConstraintLayout)
        if (parent instanceof androidx.constraintlayout.widget.ConstraintLayout) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
                new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.WRAP_CONTENT);
            params.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            progressBar.setLayoutParams(params);
            ((androidx.constraintlayout.widget.ConstraintLayout) parent).addView(progressBar);
        }
        
        return progressBar;
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

        dashboardViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                setLoadingState(isLoading);
            }
        });

        dashboardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                DialogManager.showErrorDialog(requireContext(), "Oops", error);
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        // Optionally dim the RecyclerView during loading
        if (recyclerView != null) {
            recyclerView.setAlpha(isLoading ? 0.5f : 1.0f);
        }
    }

    private void displayRecommendations(List<Scholarship> scholarships) {
        long startTime = System.nanoTime();
        
        // Use RecyclerView for efficient view recycling
        if (scholarships == null || scholarships.isEmpty()) {
            // Show empty state
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
            tvEmptyState.setText("No scholarships found for your profile. " +
                "Try updating your location and GPA in your profile for better recommendations.");
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
