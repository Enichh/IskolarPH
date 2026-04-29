package com.example.iskolarphh.di;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.iskolarphh.viewmodel.CatalogViewModel;
import com.example.iskolarphh.viewmodel.DashboardViewModel;
import com.example.iskolarphh.viewmodel.LocationViewModel;
import com.example.iskolarphh.viewmodel.LoginViewModel;
import com.example.iskolarphh.viewmodel.ProfileViewModel;
import com.example.iskolarphh.viewmodel.SignupViewModel;
import com.example.iskolarphh.viewmodel.StudentViewModel;

/**
 * Custom ViewModelProvider.Factory for dependency injection.
 * Follows SOLID principles by enabling constructor injection without complex DI framework.
 */
public class ViewModelFactory implements ViewModelProvider.Factory {
    
    private final Application application;
    private final DependencyFactory dependencyFactory;
    
    public ViewModelFactory(Application application) {
        this.application = application;
        this.dependencyFactory = DependencyFactory.getInstance(application);
    }
    
    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) dependencyFactory.createLoginViewModel(application);
        } else if (modelClass.isAssignableFrom(SignupViewModel.class)) {
            return (T) dependencyFactory.createSignupViewModel(application);
        } else if (modelClass.isAssignableFrom(CatalogViewModel.class)) {
            return (T) dependencyFactory.createCatalogViewModel(application);
        } else if (modelClass.isAssignableFrom(DashboardViewModel.class)) {
            return (T) dependencyFactory.createDashboardViewModel(application);
        } else if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) dependencyFactory.createProfileViewModel(application);
        } else if (modelClass.isAssignableFrom(LocationViewModel.class)) {
            return (T) dependencyFactory.createLocationViewModel(application);
        } else if (modelClass.isAssignableFrom(StudentViewModel.class)) {
            return (T) dependencyFactory.createStudentViewModel(application);
        }
        
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
