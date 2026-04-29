package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();
    private Observer<Student> studentObserver;
    private LiveData<Student> studentLiveData;

    public StudentViewModel(Application application) {
        this(application, new StudentRepository(application));
    }

    // Constructor with dependency injection
    public StudentViewModel(Application application, StudentRepository studentRepository) {
        super(application);
        this.studentRepository = studentRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<Student> getCurrentStudent() {
        return currentStudent;
    }

    public LiveData<Student> loadStudentData() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Remove existing observer if any
            if (studentObserver != null && studentLiveData != null) {
                studentLiveData.removeObserver(studentObserver);
            }
            
            studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentObserver = student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                }
            };
            studentLiveData.observeForever(studentObserver);
            return studentLiveData;
        }
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up observer to prevent memory leak
        if (studentObserver != null && studentLiveData != null) {
            studentLiveData.removeObserver(studentObserver);
            studentObserver = null;
        }
        studentLiveData = null;
        // Shutdown repository executor
        studentRepository.shutdown();
    }

    public void setCurrentStudent(Student student) {
        currentStudent.setValue(student);
    }

    public Student getStudentValue() {
        return currentStudent.getValue();
    }
}
