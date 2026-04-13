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
    private ScholarshipAdapter recommendationAdapter;
    private RecyclerView rvRecommendations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_dashboard, container, false);

        studentRepository = new StudentRepository(requireContext());
        scholarshipRepository = new ScholarshipRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews(view);
        loadStudentData();
        setupCardListeners(view);

        return view;
    }

    private void initializeViews(View view) {
        TextView tvWelcome = view.findViewById(R.id.tvWelcome);
        rvRecommendations = new RecyclerView(requireContext());
        rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        LinearLayout recommendationsContainer = view.findViewById(R.id.rootLayout).findViewById(android.R.id.content);
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
        TextView tvWelcome = getView().findViewById(R.id.tvWelcome);
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
            Toast.makeText(requireContext(), "Saved scholarships feature coming soon", Toast.LENGTH_SHORT).show();
        });

        cardCourse.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "My course feature coming soon", Toast.LENGTH_SHORT).show();
        });

        cardDeadlines.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Deadlines feature coming soon", Toast.LENGTH_SHORT).show();
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
        if (view == null || scholarships.isEmpty()) {
            return;
        }

        android.widget.ScrollView scrollView = view.findViewById(R.id.scrollView);
        LinearLayout scrollViewContent = view.findViewById(R.id.dashboardContent);
        TextView recommendationsTitle = view.findViewById(R.id.tvRecommendationsTitle);

        if (scrollViewContent != null) {
            for (Scholarship scholarship : scholarships) {
                CardView scholarshipCard = createScholarshipCard(scholarship);
                scrollViewContent.addView(scholarshipCard);
            }
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
