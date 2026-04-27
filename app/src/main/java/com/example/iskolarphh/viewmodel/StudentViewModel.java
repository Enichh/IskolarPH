package com.example.iskolarphh.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.iskolarphh.database.entity.Student;
import com.example.iskolarphh.repository.StudentRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentViewModel extends AndroidViewModel {

    private final StudentRepository studentRepository;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<Student> currentStudent = new MutableLiveData<>();

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
            LiveData<Student> studentLiveData = studentRepository.getStudentByFirebaseUid(firebaseUser.getUid());
            studentLiveData.observeForever(student -> {
                if (student != null) {
                    currentStudent.postValue(student);
                }
            });
            return studentLiveData;
        }
        return null;
    }

    public void setCurrentStudent(Student student) {
        currentStudent.setValue(student);
    }

    public Student getStudentValue() {
        return currentStudent.getValue();
    }
}
