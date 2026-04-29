package com.example.iskolarphh.di;

import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.iskolarphh.BuildConfig;
import com.example.iskolarphh.api.ApiClientConfig;
import com.example.iskolarphh.api.RetrofitClient;
import com.example.iskolarphh.repository.ScholarshipRepository;
import com.example.iskolarphh.repository.StudentRepository;
import com.example.iskolarphh.repository.PrivacyConsentRepository;
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
    private PrivacyConsentRepository privacyConsentRepository;
    private ScholarshipFilterService scholarshipFilterService;
    private GeocoderService geocoderService;
    private LocationManager locationManager;
    private LocationFlowManager locationFlowManager;
    private RetrofitClient retrofitClient;
    private ExecutorService executorService;
    
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

    public PrivacyConsentRepository getPrivacyConsentRepository() {
        if (privacyConsentRepository == null) {
            privacyConsentRepository = new PrivacyConsentRepository(context);
        }
        return privacyConsentRepository;
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
    
    public ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
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
        // Create new repository instances per ViewModel for proper lifecycle management
        StudentRepository studentRepo = new StudentRepository(context);
        PrivacyConsentRepository privacyRepo = new PrivacyConsentRepository(context);
        return new SignupViewModel(application, privacyRepo, studentRepo);
    }
    
    public CatalogViewModel createCatalogViewModel(android.app.Application application) {
        // Create new repository instances per ViewModel for proper lifecycle management
        return new CatalogViewModel(
            application,
            new ScholarshipRepository(context),
            new StudentRepository(context),
            getScholarshipFilterService()  // Stateless, safe to share
        );
    }
    
    public DashboardViewModel createDashboardViewModel(android.app.Application application) {
        // Create new instances per ViewModel - each manages its own executor lifecycle
        return new DashboardViewModel(
            application,
            new ScholarshipRepository(context),
            new StudentRepository(context),
            Executors.newSingleThreadExecutor()
        );
    }
    
    public ProfileViewModel createProfileViewModel(android.app.Application application) {
        // Create new instances per ViewModel for proper executor lifecycle management
        StudentRepository studentRepo = new StudentRepository(context);
        LocationFlowManager locationFlow = new LocationFlowManager(
            context,
            getLocationManager(),
            new GeocoderService(),  // New instance per ViewModel
            studentRepo  // Same repo instance for consistency
        );
        return new ProfileViewModel(
            application,
            studentRepo,
            locationFlow
        );
    }
    
    public FilterViewModel createFilterViewModel(android.app.Application application) {
        return new FilterViewModel(application, getScholarshipFilterService());
    }
    
    public LocationViewModel createLocationViewModel(android.app.Application application) {
        // Create new instances per ViewModel for proper executor lifecycle management
        StudentRepository studentRepo = new StudentRepository(context);
        LocationFlowManager locationFlow = new LocationFlowManager(
            context,
            getLocationManager(),
            new GeocoderService(),
            studentRepo
        );
        return new LocationViewModel(application, studentRepo, locationFlow);
    }
    
    public StudentViewModel createStudentViewModel(android.app.Application application) {
        // Create new repository instance per ViewModel
        return new StudentViewModel(application, new StudentRepository(context));
    }
    
    /**
     * Reset all dependencies - useful for testing
     */
    public void reset() {
        scholarshipRepository = null;
        studentRepository = null;
        privacyConsentRepository = null;
        scholarshipFilterService = null;
        geocoderService = null;
        locationManager = null;
        locationFlowManager = null;
        retrofitClient = null;
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        executorService = null;
    }
}
