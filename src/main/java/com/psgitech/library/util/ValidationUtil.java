package com.psgitech.library.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Validation utility for user registration, input formats, and password strength.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$"
    );

    private static final Pattern REGISTER_NUMBER_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]{6,20}$"
    );

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile(
            "^[A-Za-z0-9]{3,20}$"
    );

    private static final Set<String> VALID_DEPARTMENTS = new HashSet<>(Arrays.asList(
            "CSE", "IT", "ECE", "EEE", "MECH", "AI&DS", "CIVIL"
    ));

    /**
     * Validates whether an email format is valid.
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates whether a phone number is a valid 10-digit mobile number.
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim().replaceAll("\\s", "")).matches();
    }

    /**
     * Validates student register number format.
     */
    public static boolean isValidRegisterNumber(String registerNumber) {
        return registerNumber != null && REGISTER_NUMBER_PATTERN.matcher(registerNumber.trim()).matches();
    }

    /**
     * Validates employee ID format.
     */
    public static boolean isValidEmployeeId(String employeeId) {
        return employeeId != null && EMPLOYEE_ID_PATTERN.matcher(employeeId.trim()).matches();
    }

    /**
     * Validates department against allowed college engineering departments:
     * CSE, IT, ECE, EEE, MECH, AI&DS, CIVIL
     */
    public static boolean isValidDepartment(String department) {
        if (department == null) return false;
        return VALID_DEPARTMENTS.contains(department.trim().toUpperCase());
    }

    /**
     * Validates year of study: 1, 2, 3, or 4.
     */
    public static boolean isValidYear(Integer year) {
        return year != null && year >= 1 && year <= 4;
    }

    /**
     * Password strength rules:
     * - At least 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     * - At least one special character
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
