package com.psgitech.library;

import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.InvalidCredentialsException;
import com.psgitech.library.exception.UserAlreadyExistsException;
import com.psgitech.library.exception.UserNotFoundException;
import com.psgitech.library.model.Librarian;
import com.psgitech.library.model.Student;
import com.psgitech.library.model.User;
import com.psgitech.library.service.UserService;
import com.psgitech.library.util.DBConnection;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UserServiceTest {
    private UserService userService;

    @Before
    public void setUp() {
        DBConnection.setForceFallback(true);
        DBConnection.initializeDatabase();
        userService = new UserService();
    }

    @Test
    public void testStudentRegistrationSuccess() throws Exception {
        Student student = new Student();
        student.setName("Kavitha M");
        student.setEmail("kavitha." + System.currentTimeMillis() + "@psgitech.ac.in");
        student.setRegisterNumber("REG" + System.currentTimeMillis());
        student.setPhone("9876543210");
        student.setDepartment("CSE");
        student.setYear(2);

        Student registered = userService.registerStudent(student, "Psgitech@2026", "Psgitech@2026");
        assertNotNull(registered);
        assertTrue(registered.getUserId() > 0);
        assertEquals(Role.STUDENT, registered.getRole());
        assertTrue(registered.isActive());
        assertNotNull(registered.getPasswordHash());
    }

    @Test(expected = UserAlreadyExistsException.class)
    public void testDuplicateEmailRejection() throws Exception {
        String email = "dup." + System.currentTimeMillis() + "@psgitech.ac.in";

        Student s1 = new Student();
        s1.setName("User One");
        s1.setEmail(email);
        s1.setRegisterNumber("REG" + (System.currentTimeMillis() + 1));
        s1.setDepartment("IT");
        s1.setYear(1);
        userService.registerStudent(s1, "Psgitech@2026", "Psgitech@2026");

        Student s2 = new Student();
        s2.setName("User Two");
        s2.setEmail(email);
        s2.setRegisterNumber("REG" + (System.currentTimeMillis() + 2));
        s2.setDepartment("IT");
        s2.setYear(1);
        userService.registerStudent(s2, "Psgitech@2026", "Psgitech@2026");
    }

    @Test
    public void testCreateLibrarianByAdmin() throws Exception {
        Librarian lib = new Librarian();
        lib.setName("Dr. S. Sundar");
        lib.setEmail("sundar." + System.currentTimeMillis() + "@psgitech.ac.in");
        lib.setEmployeeId("LIB" + (System.currentTimeMillis() % 10000));
        lib.setPhone("9123456789");

        Librarian created = userService.createLibrarian(lib, "InitialPass@2026", "InitialPass@2026");
        assertNotNull(created);
        assertEquals(Role.LIBRARIAN, created.getRole());
        assertTrue(created.isMustChangePassword()); // Required for first login!
    }

    @Test
    public void testPasswordChangeWorkflow() throws Exception {
        Student s = new Student();
        s.setName("Password Test Student");
        s.setEmail("pwtest." + System.currentTimeMillis() + "@psgitech.ac.in");
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 100000));
        s.setDepartment("ECE");
        s.setYear(3);

        Student registered = userService.registerStudent(s, "OldPassword@123", "OldPassword@123");

        // Change password successfully
        userService.changePassword(registered.getUserId(), "OldPassword@123", "NewSecure@2026", "NewSecure@2026");

        // Verify with refreshed record
        User refreshed = userService.getUserById(registered.getUserId());
        assertNotNull(refreshed);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testPasswordChangeWrongCurrentPassword() throws Exception {
        Student s = new Student();
        s.setName("Wrong Pass Student");
        s.setEmail("wrongpass." + System.currentTimeMillis() + "@psgitech.ac.in");
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 100000));
        s.setDepartment("CIVIL");
        s.setYear(4);

        Student registered = userService.registerStudent(s, "CurrentPass@123", "CurrentPass@123");
        userService.changePassword(registered.getUserId(), "IncorrectPass@123", "NewPass@2026", "NewPass@2026");
    }

    @Test
    public void testToggleUserStatus() throws Exception {
        Student s = new Student();
        s.setName("Status Student");
        s.setEmail("status." + System.currentTimeMillis() + "@psgitech.ac.in");
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 100000));
        s.setDepartment("MECH");
        s.setYear(2);

        Student registered = userService.registerStudent(s, "StatusPass@123", "StatusPass@123");
        assertTrue(registered.isActive());

        // Deactivate
        userService.toggleUserStatus(registered.getUserId(), false);
        User deactivated = userService.getUserById(registered.getUserId());
        assertFalse(deactivated.isActive());

        // Reactivate
        userService.toggleUserStatus(registered.getUserId(), true);
        User reactivated = userService.getUserById(registered.getUserId());
        assertTrue(reactivated.isActive());
    }
}
