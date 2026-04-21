package com.example.iskolarphh.database.entity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StudentTest {

    private Student student;

    @Before
    public void setUp() {
        student = new Student();
    }

    @Test
    public void testPasswordHashing_validPassword_generatesHash() {
        String password = "TestPassword123";
        student.setPassword(password);

        assertNotNull(student.getPasswordHash());
        assertNotEquals(password, student.getPasswordHash());
        assertEquals(64, student.getPasswordHash().length());
    }

    @Test
    public void testPasswordHashing_samePassword_sameHash() {
        String password = "TestPassword123";
        student.setPassword(password);
        String hash1 = student.getPasswordHash();

        Student student2 = new Student();
        student2.setPassword(password);
        String hash2 = student2.getPasswordHash();

        assertEquals(hash1, hash2);
    }

    @Test
    public void testPasswordHashing_differentPassword_differentHash() {
        String password1 = "Password1";
        String password2 = "Password2";

        student.setPassword(password1);
        String hash1 = student.getPasswordHash();

        student.setPassword(password2);
        String hash2 = student.getPasswordHash();

        assertNotEquals(hash1, hash2);
    }

    @Test
    public void testPasswordHashing_nullPassword_returnsNull() {
        student.setPassword(null);
        assertNull(student.getPasswordHash());
    }

    @Test
    public void testPasswordHashing_emptyPassword_returnsNull() {
        student.setPassword("");
        assertNull(student.getPasswordHash());
    }

    @Test
    public void testConstructor_withAllFields_createsStudent() {
        Student testStudent = new Student(
            "firebase123",
            "John",
            "Doe",
            "A",
            "Manila",
            "john@test.com",
            "password123",
            3.5
        );

        assertEquals("firebase123", testStudent.getFirebaseUid());
        assertEquals("John", testStudent.getFirstName());
        assertEquals("Doe", testStudent.getLastName());
        assertEquals("A", testStudent.getMiddleInitial());
        assertEquals("Manila", testStudent.getLocation());
        assertEquals("john@test.com", testStudent.getEmail());
        assertEquals(3.5, testStudent.getGpa(), 0.001);
        assertNotNull(testStudent.getPasswordHash());
    }

    @Test
    public void testConstructor_withoutPassword_createsStudent() {
        Student testStudent = new Student(
            "firebase123",
            "John",
            "Doe",
            "A",
            "Manila",
            3.5
        );

        assertEquals("firebase123", testStudent.getFirebaseUid());
        assertEquals("John", testStudent.getFirstName());
        assertEquals("Doe", testStudent.getLastName());
        assertEquals("A", testStudent.getMiddleInitial());
        assertEquals("Manila", testStudent.getLocation());
        assertEquals(3.5, testStudent.getGpa(), 0.001);
        assertNull(testStudent.getPasswordHash());
    }

    @Test
    public void testSettersAndGetters() {
        student.setId(1);
        student.setFirebaseUid("uid123");
        student.setFirstName("Jane");
        student.setLastName("Smith");
        student.setMiddleInitial("B");
        student.setLocation("Cebu");
        student.setEmail("jane@test.com");
        student.setGpa(3.8);
        student.setCourse("BSCS");

        assertEquals(1, student.getId());
        assertEquals("uid123", student.getFirebaseUid());
        assertEquals("Jane", student.getFirstName());
        assertEquals("Smith", student.getLastName());
        assertEquals("B", student.getMiddleInitial());
        assertEquals("Cebu", student.getLocation());
        assertEquals("jane@test.com", student.getEmail());
        assertEquals(3.8, student.getGpa(), 0.001);
        assertEquals("BSCS", student.getCourse());
    }
}
