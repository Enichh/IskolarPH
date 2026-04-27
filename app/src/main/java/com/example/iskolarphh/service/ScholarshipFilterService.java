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
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)%");
        java.util.regex.Matcher matcher = pattern.matcher(eligibilityCriteria);
        if (matcher.find()) {
            try {
                double percentage = Double.parseDouble(matcher.group(1));
                return percentage / 100.0 * 4.0;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        java.util.regex.Pattern gpaPattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)");
        java.util.regex.Matcher gpaMatcher = gpaPattern.matcher(eligibilityCriteria);
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
