package com.psgitech.library;

import com.psgitech.library.exception.AccountInactiveException;
import com.psgitech.library.exception.InvalidCredentialsException;
import com.psgitech.library.model.Student;
import com.psgitech.library.model.User;
import com.psgitech.library.service.AuthenticationService;
import com.psgitech.library.service.UserService;
import com.psgitech.library.util.DBConnection;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AuthenticationServiceTest {
    private AuthenticationService authService;
    private UserService userService;

    @Before
    public void setUp() {
        DBConnection.setForceFallback(true);
        DBConnection.initializeDatabase();
        authService = new AuthenticationService();
        userService = new UserService();
    }

    @Test
    public void testSuccessfulLoginWithEmailAndRegisterNumber() throws Exception {
        String email = "auth." + System.currentTimeMillis() + "@psgitech.ac.in";
        String regNo = "REG" + (System.currentTimeMillis() % 1000000);
        String pass = "SecurePass@2026";

        Student s = new Student();
        s.setName("Auth Student");
        s.setEmail(email);
        s.setRegisterNumber(regNo);
        s.setDepartment("CSE");
        s.setYear(3);

        userService.registerStudent(s, pass, pass);

        // Login using email
        User loggedInEmail = authService.login(email, pass);
        assertNotNull(loggedInEmail);
        assertEquals(email.toLowerCase(), loggedInEmail.getEmail().toLowerCase());

        // Login using register number
        User loggedInRegNo = authService.login(regNo, pass);
        assertNotNull(loggedInRegNo);
        assertEquals(loggedInEmail.getUserId(), loggedInRegNo.getUserId());
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testLoginWithInvalidPassword() throws Exception {
        String email = "invalid." + System.currentTimeMillis() + "@psgitech.ac.in";
        Student s = new Student();
        s.setName("Test Student");
        s.setEmail(email);
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 1000000));
        s.setDepartment("IT");
        s.setYear(1);

        userService.registerStudent(s, "RightPass@123", "RightPass@123");
        authService.login(email, "WrongPassword@123");
    }

    @Test(expected = AccountInactiveException.class)
    public void testLoginWithDeactivatedAccount() throws Exception {
        String email = "deact." + System.currentTimeMillis() + "@psgitech.ac.in";
        Student s = new Student();
        s.setName("Deactivated Student");
        s.setEmail(email);
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 1000000));
        s.setDepartment("EEE");
        s.setYear(4);

        Student registered = userService.registerStudent(s, "ActivePass@123", "ActivePass@123");
        userService.toggleUserStatus(registered.getUserId(), false); // Deactivate account

        authService.login(email, "ActivePass@123");
    }

    @Test
    public void testRememberMeTokenLifecycle() throws Exception {
        String email = "remember." + System.currentTimeMillis() + "@psgitech.ac.in";
        Student s = new Student();
        s.setName("Remember Student");
        s.setEmail(email);
        s.setRegisterNumber("REG" + (System.currentTimeMillis() % 1000000));
        s.setDepartment("AI&DS");
        s.setYear(2);

        Student registered = userService.registerStudent(s, "Remember@2026", "Remember@2026");

        // 1. Create Remember Token
        String rawToken = authService.createRememberToken(registered.getUserId());
        assertNotNull(rawToken);

        // 2. Validate and Restore User
        User restored = authService.validateAndRestoreUserFromToken(rawToken);
        assertNotNull(restored);
        assertEquals(registered.getUserId(), restored.getUserId());

        // 3. Revoke Token (Logout simulation)
        authService.revokeRememberToken(rawToken);
        User restoredAfterRevoke = authService.validateAndRestoreUserFromToken(rawToken);
        assertNull(restoredAfterRevoke);
    }
}
