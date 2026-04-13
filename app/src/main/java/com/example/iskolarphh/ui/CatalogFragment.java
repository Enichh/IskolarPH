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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.repository.ScholarshipRepository;

import java.util.List;

public class CatalogFragment extends Fragment {

    private ScholarshipRepository repository;
    private ScholarshipAdapter adapter;
    private String currentSearchQuery = "";
    private String currentLocationFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_catalog, container, false);

        repository = new ScholarshipRepository(requireContext());

        setupRecyclerView(view);
        setupSearch(view);
        setupFilters(view);

        observeScholarships();

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
        btnFilterLocation.setOnClickListener(v -> showLocationFilterDialog());
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

    private void observeScholarships() {
        repository.searchAndFilterScholarships(currentSearchQuery, currentLocationFilter)
                .observe(getViewLifecycleOwner(), scholarships -> {
                    if (adapter != null) {
                        adapter.submitList(scholarships);
                    }
                });
    }
}
