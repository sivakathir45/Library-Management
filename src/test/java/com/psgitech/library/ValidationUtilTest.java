package com.psgitech.library;

import com.psgitech.library.util.ValidationUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class ValidationUtilTest {

    @Test
    public void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("student@psgitech.ac.in"));
        assertTrue(ValidationUtil.isValidEmail("john.doe@gmail.com"));
        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    public void testPhoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("9876543210"));
        assertTrue(ValidationUtil.isValidPhone("+919876543210"));
        assertFalse(ValidationUtil.isValidPhone("12345"));
        assertFalse(ValidationUtil.isValidPhone("abcdefghij"));
        assertFalse(ValidationUtil.isValidPhone(null));
    }

    @Test
    public void testPasswordStrength() {
        assertTrue(ValidationUtil.isStrongPassword("Library@2026"));
        assertTrue(ValidationUtil.isStrongPassword("Psgitech#123"));
        assertFalse(ValidationUtil.isStrongPassword("weak"));
        assertFalse(ValidationUtil.isStrongPassword("alllowercase123!"));
        assertFalse(ValidationUtil.isStrongPassword("ALLUPPERCASE123!"));
        assertFalse(ValidationUtil.isStrongPassword("NoSpecial123"));
        assertFalse(ValidationUtil.isStrongPassword(null));
    }

    @Test
    public void testDepartmentValidation() {
        assertTrue(ValidationUtil.isValidDepartment("CSE"));
        assertTrue(ValidationUtil.isValidDepartment("it"));
        assertTrue(ValidationUtil.isValidDepartment("AI&DS"));
        assertTrue(ValidationUtil.isValidDepartment("ECE"));
        assertFalse(ValidationUtil.isValidDepartment("AERONAUTICAL"));
        assertFalse(ValidationUtil.isValidDepartment(null));
    }

    @Test
    public void testYearValidation() {
        assertTrue(ValidationUtil.isValidYear(1));
        assertTrue(ValidationUtil.isValidYear(4));
        assertFalse(ValidationUtil.isValidYear(0));
        assertFalse(ValidationUtil.isValidYear(5));
        assertFalse(ValidationUtil.isValidYear(null));
    }
}
