package com.example.iskolarphh.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.util.PerformanceMonitor;
import com.example.iskolarphh.viewmodel.CatalogViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavedFragment extends Fragment {

    private ScholarshipRepository scholarshipRepository;
    private CatalogViewModel catalogViewModel;
    private ExecutorService executorService;
    private ScholarshipAdapter scholarshipAdapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        executorService = Executors.newSingleThreadExecutor();
        scholarshipRepository = new ScholarshipRepository(requireContext());
        
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        catalogViewModel = new ViewModelProvider(this, factory).get(CatalogViewModel.class);

        initializeViews(view);
        loadSavedScholarships();

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
        recyclerView = view.findViewById(R.id.recyclerViewSaved);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        scholarshipAdapter = new ScholarshipAdapter(new ScholarshipAdapter.OnScholarshipClickListener() {
            @Override
            public void onScholarshipClick(Scholarship scholarship) {
                Intent intent = new Intent(getActivity(), ScholarshipDetailActivity.class);
                intent.putExtra(ScholarshipDetailActivity.EXTRA_SCHOLARSHIP_ID, scholarship.getId());
                startActivity(intent);
            }

            @Override
            public void onSaveClick(Scholarship scholarship) {
                String scholarshipName = scholarship.getScholarshipName();

                // Always showing confirmation when unsaving from SavedFragment
                DialogManager.showSaveScholarshipConfirmation(
                        requireContext(),
                        scholarshipName,
                        false, // Always removing in SavedFragment
                        () -> {
                            catalogViewModel.updateScholarshipSaved(scholarship);
                            if (getView() != null) {
                                DialogManager.showSuccessSnackbar(getView(), "Scholarship removed from saved");
                            }
                            // Refresh the list to remove unsaved items
                            loadSavedScholarships();
                        },
                        null
                );
            }
        });
        recyclerView.setAdapter(scholarshipAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
    }

    private void loadSavedScholarships() {
        long startTime = System.nanoTime();

        LiveData<List<Scholarship>> savedScholarships = scholarshipRepository.getSavedScholarships();
        savedScholarships.observe(getViewLifecycleOwner(), scholarships -> {
            if (scholarships != null) {
                displaySavedScholarships(scholarships);
                PerformanceMonitor.logMethodExecutionTime("SavedFragment.loadSavedScholarships()", startTime);
            }
        });
    }

    private void displaySavedScholarships(List<Scholarship> scholarships) {
        if (scholarships == null || scholarships.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);
            scholarshipAdapter.submitList(scholarships);
        }
    }
}
