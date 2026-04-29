package com.example.iskolarphh.ui;

import android.content.Intent;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.di.ViewModelFactory;
import com.example.iskolarphh.ui.DialogManager;
import com.example.iskolarphh.viewmodel.CatalogViewModel;

import java.util.List;

public class CatalogFragment extends Fragment {

    private CatalogViewModel catalogViewModel;
    private ScholarshipAdapter adapter;

    private TextView tvLocationHeader;
    private ImageButton btnRefreshLocation;
    private TextView tvFilterStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_catalog, container, false);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        catalogViewModel = new ViewModelProvider(this, factory).get(CatalogViewModel.class);

        setupRecyclerView(view);
        setupSearch(view);
        setupFilters(view);
        setupFilterStatus(view);
        setupLocationHeader(view);
        observeViewModel();

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
                boolean wasSaved = scholarship.isSaved();
                String scholarshipName = scholarship.getScholarshipName();

                DialogManager.showSaveScholarshipConfirmation(
                        requireContext(),
                        scholarshipName,
                        !wasSaved,
                        () -> {
                            catalogViewModel.updateScholarshipSaved(scholarship);
                            adapter.notifyItemChanged(adapter.getCurrentList().indexOf(scholarship));
                            String message = !wasSaved ? "Scholarship saved successfully" : "Scholarship removed from saved";
                            if (getView() != null) {
                                DialogManager.showSuccessSnackbar(getView(), message);
                            }
                        },
                        null
                );
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
                catalogViewModel.setSearchQuery(s.toString());
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

    private void setupFilterStatus(View view) {
        tvFilterStatus = view.findViewById(R.id.tvFilterStatus);
    }

    private void observeViewModel() {
        catalogViewModel.loadStudentData().observe(getViewLifecycleOwner(), student -> {
            if (student != null) {
                catalogViewModel.setCurrentStudent(student);
            }
        });

        catalogViewModel.getScholarshipsLiveData().observe(getViewLifecycleOwner(), scholarships -> {
            if (adapter != null) {
                adapter.submitList(scholarships);
            }
        });

        catalogViewModel.getLocationHeader().observe(getViewLifecycleOwner(), header -> {
            tvLocationHeader.setText(header);
        });

        catalogViewModel.getFilterStatus().observe(getViewLifecycleOwner(), status -> {
            if (tvFilterStatus != null && status != null) {
                tvFilterStatus.setText(status);
            }
        });

        catalogViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                DialogManager.showErrorDialog(requireContext(), "Oops", error);
            }
        });
    }

    private void showLocationFilterDialog() {
        String[] locations = {"All", "Luzon", "Visayas", "Mindanao", "National"};
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Location")
                .setItems(locations, (dialog, which) -> {
                    if (which == 0) {
                        catalogViewModel.setLocationFilter(null);
                    } else {
                        catalogViewModel.setLocationFilter(locations[which]);
                    }
                })
                .show();
    }

    private void toggleGpaFilter() {
        catalogViewModel.toggleGpaFilter();
    }



    private void refreshLocation() {
        catalogViewModel.refreshLocation();
    }


}
