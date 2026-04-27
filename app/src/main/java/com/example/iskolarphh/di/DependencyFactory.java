package com.example.iskolarphh.di;

import android.content.Context;
import com.example.iskolarphh.BuildConfig;
import com.example.iskolarphh.api.ApiClientConfig;
import com.example.iskolarphh.api.RetrofitClient;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.service.GeocoderService;
import com.example.iskolarphh.service.LocationFlowManager;
import com.example.iskolarphh.service.LocationManager;
import com.example.iskolarphh.service.ScholarshipFilterService;
import com.example.iskolarphh.viewmodel.CatalogViewModel;
import com.example.iskolarphh.viewmodel.DashboardViewModel;
import com.example.iskolarphh.viewmodel.FilterViewModel;
import com.example.iskolarphh.viewmodel.LocationViewModel;
import com.example.iskolarphh.viewmodel.LoginViewModel;
import com.example.iskolarphh.viewmodel.ProfileViewModel;
import com.example.iskolarphh.viewmodel.SignupViewModel;
import com.example.iskolarphh.viewmodel.StudentViewModel;

/**
 * Simple dependency factory following SOLID principles.
 * Provides constructor injection without complex DI framework.
 * This is a pragmatic approach following YAGNI principle.
 */
public class DependencyFactory {
    
    private static volatile DependencyFactory INSTANCE;
    private final Context context;
    
    // Lazy-loaded dependencies
    private ScholarshipRepository scholarshipRepository;
    private StudentRepository studentRepository;
    private ScholarshipFilterService scholarshipFilterService;
    private GeocoderService geocoderService;
    private LocationManager locationManager;
    private LocationFlowManager locationFlowManager;
    private RetrofitClient retrofitClient;
    
    private DependencyFactory(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static DependencyFactory getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DependencyFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DependencyFactory(context);
                }
            }
        }
        return INSTANCE;
    }
    
    // Repository dependencies
    public ScholarshipRepository getScholarshipRepository() {
        if (scholarshipRepository == null) {
            scholarshipRepository = new ScholarshipRepository(context);
        }
        return scholarshipRepository;
    }
    
    public StudentRepository getStudentRepository() {
        if (studentRepository == null) {
            studentRepository = new StudentRepository(context);
        }
        return studentRepository;
    }
    
    // Service dependencies
    public ScholarshipFilterService getScholarshipFilterService() {
        if (scholarshipFilterService == null) {
            scholarshipFilterService = new ScholarshipFilterService();
        }
        return scholarshipFilterService;
    }
    
    public GeocoderService getGeocoderService() {
        if (geocoderService == null) {
            geocoderService = new GeocoderService();
        }
        return geocoderService;
    }
    
    public LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = new LocationManager();
        }
        return locationManager;
    }
    
    public LocationFlowManager getLocationFlowManager() {
        if (locationFlowManager == null) {
            locationFlowManager = new LocationFlowManager(
                context,
                getLocationManager(),
                getGeocoderService(),
                getStudentRepository()
            );
        }
        return locationFlowManager;
    }
    
    // API dependencies
    public RetrofitClient getRetrofitClient() {
        if (retrofitClient == null) {
            // Read configuration from BuildConfig (generated from local.properties)
            String baseUrl = BuildConfig.LONGCAT_API_BASE_URL;
            String apiKey = BuildConfig.LONGCAT_API_KEY;
            
            if (apiKey.isEmpty() || "DEFAULT_API_KEY".equals(apiKey)) {
                throw new IllegalStateException("LONGCAT_API_KEY not properly configured. Please check your local.properties file and rebuild the project.");
            }
            
            ApiClientConfig config = new ApiClientConfig(baseUrl, apiKey);
            retrofitClient = new RetrofitClient(config);
        }
        return retrofitClient;
    }
    
    // ViewModel factory methods
    public LoginViewModel createLoginViewModel(android.app.Application application) {
        return new LoginViewModel(application);
    }
    
    public SignupViewModel createSignupViewModel(android.app.Application application) {
        return new SignupViewModel(application);
    }
    
    public CatalogViewModel createCatalogViewModel(android.app.Application application) {
        return new CatalogViewModel(
            application,
            getScholarshipRepository(),
            createFilterViewModel(application),
            createLocationViewModel(application),
            createStudentViewModel(application)
        );
    }
    
    public DashboardViewModel createDashboardViewModel(android.app.Application application) {
        return new DashboardViewModel(
            application,
            getScholarshipRepository(),
            getStudentRepository()
        );
    }
    
    public ProfileViewModel createProfileViewModel(android.app.Application application) {
        return new ProfileViewModel(
            application,
            getStudentRepository(),
            getLocationFlowManager()
        );
    }
    
    public FilterViewModel createFilterViewModel(android.app.Application application) {
        return new FilterViewModel(application, getScholarshipFilterService());
    }
    
    public LocationViewModel createLocationViewModel(android.app.Application application) {
        return new LocationViewModel(application, getStudentRepository(), getLocationFlowManager());
    }
    
    public StudentViewModel createStudentViewModel(android.app.Application application) {
        return new StudentViewModel(application, getStudentRepository());
    }
    
    /**
     * Reset all dependencies - useful for testing
     */
    public void reset() {
        scholarshipRepository = null;
        studentRepository = null;
        scholarshipFilterService = null;
        geocoderService = null;
        locationManager = null;
        locationFlowManager = null;
        retrofitClient = null;
    }
}
