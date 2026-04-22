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

public class DashboardFragment extends Fragment {

    private StudentRepository studentRepository;
    private ScholarshipRepository scholarshipRepository;
    private FirebaseAuth firebaseAuth;
    private Student currentStudent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        studentRepository = new StudentRepository(requireContext());
        scholarshipRepository = ScholarshipRepository.getInstance(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadStudentData();
        setupCardListeners(view);

        return view;
    }

    private void initializeViews(View view) {
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
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
                        personalizeWelcome();
                        loadRecommendations();
                    }
                }
            });
        }
    }

    private void personalizeWelcome() {
        View view = getView();
        if (view == null) return;
        
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        if (currentStudent != null && tvWelcome != null) {
            String firstName = currentStudent.getFirstName();
            tvWelcome.setText("Welcome, " + firstName + "!");
        }
    }

    private void setupCardListeners(View view) {
        CardView cardSaved = view.findViewById(R.id.cardSaved);
        CardView cardCourse = view.findViewById(R.id.cardCourse);
        CardView cardDeadlines = view.findViewById(R.id.cardDeadlines);

        cardSaved.setOnClickListener(v -> {
            loadFilteredScholarships("Saved Scholarships", scholarshipRepository.getSavedScholarships());
        });

        cardCourse.setOnClickListener(v -> {
            if (currentStudent != null && currentStudent.getCourse() != null) {
                loadFilteredScholarships("For your Course: " + currentStudent.getCourse(), 
                    scholarshipRepository.searchAndFilterScholarships(currentStudent.getCourse(), null));
            } else {
                Toast.makeText(requireContext(), "Please set your course in Profile first", Toast.LENGTH_SHORT).show();
            }
        });

        cardDeadlines.setOnClickListener(v -> {
            loadFilteredScholarships("Upcoming Deadlines", scholarshipRepository.getScholarshipsByDeadline());
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

    private void loadRecommendations() {
        if (currentStudent == null) {
            return;
        }

        scholarshipRepository.getAllScholarships().observe(getViewLifecycleOwner(), scholarships -> {
            if (scholarships != null) {
                List<Scholarship> recommended = getRecommendedScholarships(scholarships, currentStudent);
                displayRecommendations(recommended);
            }
        });
    }

    private List<Scholarship> getRecommendedScholarships(List<Scholarship> allScholarships, Student student) {
        List<Scholarship> recommended = new ArrayList<>();
        
        for (Scholarship scholarship : allScholarships) {
            Double requiredGpa = parseGpaFromEligibility(scholarship.getEligibilityCriteria());
            
            boolean gpaMatch = (requiredGpa == null || student.getGpa() >= requiredGpa);
            boolean locationMatch = (scholarship.getLocation().equalsIgnoreCase("National") || 
                                   scholarship.getLocation().equalsIgnoreCase(student.getLocation()) ||
                                   student.getLocation() == null);
            
            if (gpaMatch && locationMatch && scholarship.isActive()) {
                recommended.add(scholarship);
            }
        }
        
        if (recommended.size() > 3) {
            return recommended.subList(0, 3);
        }
        
        return recommended;
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

    private void displayRecommendations(List<Scholarship> scholarships) {
        View view = getView();
        if (view == null) {
            return;
        }

        LinearLayout scrollViewContent = view.findViewById(R.id.dashboardContent);
        if (scrollViewContent == null) return;

        // Keep children up to tvRecommendationsTitle
        int titleIndex = -1;
        for (int i = 0; i < scrollViewContent.getChildCount(); i++) {
            if (scrollViewContent.getChildAt(i).getId() == R.id.tvRecommendationsTitle) {
                titleIndex = i;
                break;
            }
        }

        // Remove old scholarship cards below the title
        if (titleIndex != -1 && titleIndex < scrollViewContent.getChildCount() - 1) {
            scrollViewContent.removeViews(titleIndex + 1, scrollViewContent.getChildCount() - (titleIndex + 1));
        }

        if (scholarships.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("No scholarships found for this category.");
            emptyView.setTextColor(0xFFFFFFFF);
            emptyView.setPadding(0, 32, 0, 0);
            emptyView.setGravity(android.view.Gravity.CENTER);
            scrollViewContent.addView(emptyView);
            return;
        }

        for (Scholarship scholarship : scholarships) {
            CardView scholarshipCard = createScholarshipCard(scholarship);
            scrollViewContent.addView(scholarshipCard);
        }
    }

    private CardView createScholarshipCard(Scholarship scholarship) {
        CardView cardView = new CardView(requireContext());
        cardView.setCardBackgroundColor(0xFFFFFFFF);
        cardView.setCardElevation(4);
        cardView.setRadius(16);
        
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 16, 0, 0);
        cardView.setLayoutParams(cardParams);
        
        cardView.setClickable(true);
        cardView.setFocusable(true);
        cardView.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ScholarshipDetailActivity.class);
            intent.putExtra(ScholarshipDetailActivity.EXTRA_SCHOLARSHIP_ID, scholarship.getId());
            startActivity(intent);
        });
        
        LinearLayout innerLayout = new LinearLayout(requireContext());
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        innerLayout.setPadding(16, 16, 16, 16);
        cardView.addView(innerLayout);
        
        android.widget.ImageView imageView = new android.widget.ImageView(requireContext());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(70, 70);
        imageView.setLayoutParams(imageParams);
        imageView.setImageResource(R.drawable.logo_iskolar_ph);
        innerLayout.addView(imageView);
        
        LinearLayout textLayout = new LinearLayout(requireContext());
        textLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.setMargins(16, 0, 0, 0);
        textLayout.setLayoutParams(textLayoutParams);
        innerLayout.addView(textLayout);
        
        TextView titleView = new TextView(requireContext());
        titleView.setText(scholarship.getScholarshipName());
        titleView.setTextColor(0xFF001F3F);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setTextSize(16);
        textLayout.addView(titleView);
        
        TextView deadlineView = new TextView(requireContext());
        deadlineView.setText("Deadline: " + scholarship.getApplicationDeadline());
        deadlineView.setTextColor(0xFFAAAAAA);
        LinearLayout.LayoutParams deadlineParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        deadlineParams.setMargins(0, 4, 0, 0);
        deadlineView.setLayoutParams(deadlineParams);
        textLayout.addView(deadlineView);
        
        return cardView;
    }
}
