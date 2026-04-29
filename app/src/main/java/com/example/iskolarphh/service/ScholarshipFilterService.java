package com.example.iskolarphh.service;

import com.example.iskolarphh.database.entity.Scholarship;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipFilterService {

    private static final double MIN_GPA = 0.0;
    private static final double MAX_GPA = 5.0;

    public List<Scholarship> filterByGpa(List<Scholarship> scholarships, double studentGpa) {
        if (scholarships == null) {
            return new ArrayList<>();
        }
        if (studentGpa < MIN_GPA || studentGpa > MAX_GPA) {
            throw new IllegalArgumentException("GPA must be between " + MIN_GPA + " and " + MAX_GPA);
        }
        List<Scholarship> filtered = new ArrayList<>();
        for (Scholarship scholarship : scholarships) {
            Double requiredGpa = parseGpaFromEligibility(scholarship.getEligibilityCriteria());
            if (requiredGpa == null || studentGpa >= requiredGpa) {
                filtered.add(scholarship);
            }
        }
        return filtered;
    }

    public Double parseGpaFromEligibility(String eligibilityCriteria) {
        if (eligibilityCriteria == null) {
            return null;
        }
        // First try to find explicit GPA mentions (e.g., "GPA of 3.0", "3.5 GPA")
        java.util.regex.Pattern gpaPattern = java.util.regex.Pattern.compile("(?i)(?:gpa\\s*(?:of|:)?\\s*)?(\\d+\\.?\\d*)");
        java.util.regex.Matcher gpaMatcher = gpaPattern.matcher(eligibilityCriteria);
        if (gpaMatcher.find()) {
            try {
                double gpa = Double.parseDouble(gpaMatcher.group(1));
                // Only return if it's a valid GPA range (typically 0-5.0 for Philippines)
                if (gpa >= 0 && gpa <= 5.0) {
                    return gpa;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
