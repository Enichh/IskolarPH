package com.example.iskolarphh.viewmodel;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.service.ScholarshipFilterService;
import java.util.List;

public class FilterViewModel extends AndroidViewModel {

    private static final String GPA_FILTER_STATUS_PREFIX = "GPA filter ";

    private final ScholarshipFilterService filterService;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private String currentSearchQuery = "";
    private String currentLocationFilter = null;
    private boolean gpaFilterEnabled = false;

    public FilterViewModel(android.app.Application application) {
        this(application, new ScholarshipFilterService());
    }

    // Constructor with dependency injection
    public FilterViewModel(android.app.Application application, ScholarshipFilterService filterService) {
        super(application);
        this.filterService = filterService;
    }

    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public String getCurrentLocationFilter() {
        return currentLocationFilter;
    }

    public boolean isGpaFilterEnabled() {
        return gpaFilterEnabled;
    }

    public void setSearchQuery(String query) {
        currentSearchQuery = query;
    }

    public void setLocationFilter(String location) {
        currentLocationFilter = location;
    }

    public void toggleGpaFilter() {
        gpaFilterEnabled = !gpaFilterEnabled;
        String status = gpaFilterEnabled ? "enabled" : "disabled";
        errorMessage.postValue(GPA_FILTER_STATUS_PREFIX + status);
    }

    public List<Scholarship> applyGpaFilter(List<Scholarship> scholarships, double studentGpa) {
        if (gpaFilterEnabled) {
            return filterService.filterByGpa(scholarships, studentGpa);
        }
        return scholarships;
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void validateGpaFilter(double studentGpa) {
        if (gpaFilterEnabled && studentGpa < 0) {
            errorMessage.postValue("Invalid GPA value");
            gpaFilterEnabled = false;
        }
    }
}
