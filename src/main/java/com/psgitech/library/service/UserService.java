package com.psgitech.library.service;

import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.InvalidCredentialsException;
import com.psgitech.library.exception.UserAlreadyExistsException;
import com.psgitech.library.exception.UserNotFoundException;
import com.psgitech.library.model.Librarian;
import com.psgitech.library.model.Student;
import com.psgitech.library.model.User;
import com.psgitech.library.repository.UserRepository;
import com.psgitech.library.util.PasswordUtil;
import com.psgitech.library.util.ValidationUtil;

import java.util.List;

/**
 * Service managing user registration, profile updates, password change,
 * librarian account creation, and admin user administration.
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new student account with validations and BCrypt password hashing.
     */
    public Student registerStudent(Student student, String password, String confirmPassword)
            throws UserAlreadyExistsException, IllegalArgumentException {

        // 1. Mandatory Fields Validation
        if (student == null || student.getName() == null || student.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name is required.");
        }
        if (student.getRegisterNumber() == null || student.getRegisterNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Register Number is required.");
        }
        if (student.getEmail() == null || student.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email Address is required.");
        }
        if (student.getDepartment() == null || student.getDepartment().trim().isEmpty()) {
            throw new IllegalArgumentException("Department is required.");
        }
        if (student.getYear() == null) {
            throw new IllegalArgumentException("Year of Study is required.");
        }

        // 2. Format Validations
        if (!ValidationUtil.isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (student.getPhone() != null && !student.getPhone().trim().isEmpty() && !ValidationUtil.isValidPhone(student.getPhone())) {
            throw new IllegalArgumentException("Please enter a valid 10-digit mobile number.");
        }
        if (!ValidationUtil.isValidRegisterNumber(student.getRegisterNumber())) {
            throw new IllegalArgumentException("Register Number should be 6-20 alphanumeric characters.");
        }
        if (!ValidationUtil.isValidDepartment(student.getDepartment())) {
            throw new IllegalArgumentException("Invalid Department selected.");
        }
        if (!ValidationUtil.isValidYear(student.getYear())) {
            throw new IllegalArgumentException("Year of Study must be between 1 and 4.");
        }

        // 3. Password Validations
        if (password == null || confirmPassword == null || !password.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character.");
        }

        // 4. Uniqueness Checks
        if (userRepository.findByEmail(student.getEmail()) != null) {
            throw new UserAlreadyExistsException("An account with email " + student.getEmail() + " already exists.");
        }
        if (userRepository.findByRegisterNumber(student.getRegisterNumber()) != null) {
            throw new UserAlreadyExistsException("Register Number " + student.getRegisterNumber() + " is already registered.");
        }

        // 5. BCrypt Hashing and Role Enforcement
        student.setRole(Role.STUDENT);
        student.setPasswordHash(PasswordUtil.hashPassword(password));
        student.setActive(true);

        boolean saved = userRepository.saveStudent(student);
        if (!saved) {
            throw new RuntimeException("Failed to register student in database.");
        }
        return student;
    }

    /**
     * Creates a new Librarian account (Admin only).
     * Sets must_change_password to true for first login enforcement.
     */
    public Librarian createLibrarian(Librarian librarian, String initialPassword, String confirmPassword)
            throws UserAlreadyExistsException, IllegalArgumentException {

        if (librarian == null || librarian.getName() == null || librarian.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Librarian Name is required.");
        }
        if (librarian.getEmployeeId() == null || librarian.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }
        if (librarian.getEmail() == null || librarian.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (!ValidationUtil.isValidEmail(librarian.getEmail())) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (librarian.getPhone() != null && !librarian.getPhone().trim().isEmpty() && !ValidationUtil.isValidPhone(librarian.getPhone())) {
            throw new IllegalArgumentException("Please enter a valid 10-digit phone number.");
        }
        if (!ValidationUtil.isValidEmployeeId(librarian.getEmployeeId())) {
            throw new IllegalArgumentException("Invalid Employee ID format.");
        }

        if (initialPassword == null || confirmPassword == null || !initialPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (!ValidationUtil.isStrongPassword(initialPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character.");
        }

        if (userRepository.findByEmail(librarian.getEmail()) != null) {
            throw new UserAlreadyExistsException("Email " + librarian.getEmail() + " is already registered.");
        }
        if (userRepository.findByEmployeeId(librarian.getEmployeeId()) != null) {
            throw new UserAlreadyExistsException("Employee ID " + librarian.getEmployeeId() + " is already registered.");
        }

        librarian.setRole(Role.LIBRARIAN);
        librarian.setPasswordHash(PasswordUtil.hashPassword(initialPassword));
        librarian.setActive(true);
        librarian.setMustChangePassword(true);

        boolean saved = userRepository.saveLibrarian(librarian);
        if (!saved) {
            throw new RuntimeException("Failed to save librarian account.");
        }
        return librarian;
    }

    /**
     * Changes a user's password with current password verification and token revocation.
     */
    public void changePassword(int userId, String currentPassword, String newPassword, String confirmPassword)
            throws UserNotFoundException, InvalidCredentialsException, IllegalArgumentException {

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        if (!PasswordUtil.verifyPassword(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password does not match.");
        }

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New passwords do not match.");
        }

        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password cannot be identical to your current password.");
        }

        if (!ValidationUtil.isStrongPassword(newPassword)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character.");
        }

        String newPasswordHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = userRepository.updatePassword(userId, newPasswordHash);
        if (!updated) {
            throw new RuntimeException("Failed to update password in database.");
        }

        // Revoke active Remember Me tokens to require re-authentication with new password
        userRepository.revokeAllUserTokens(userId);
    }

    /**
     * Updates permitted profile information.
     */
    public void updateProfile(int userId, String name, String email, String phone, String department, Integer year)
            throws UserNotFoundException, UserAlreadyExistsException, IllegalArgumentException {

        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (email == null || !ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Please provide a valid email address.");
        }
        if (phone != null && !phone.trim().isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Please provide a valid 10-digit phone number.");
        }

        // Check if email changed and if new email is taken
        if (!user.getEmail().equalsIgnoreCase(email.trim())) {
            User existing = userRepository.findByEmail(email.trim());
            if (existing != null && existing.getUserId() != userId) {
                throw new UserAlreadyExistsException("Email " + email + " is already in use by another account.");
            }
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPhone(phone != null ? phone.trim() : null);

        if (user instanceof Student) {
            Student s = (Student) user;
            if (department != null && ValidationUtil.isValidDepartment(department)) {
                s.setDepartment(department.trim().toUpperCase());
            }
            if (year != null && ValidationUtil.isValidYear(year)) {
                s.setYear(year);
            }
        }

        boolean updated = userRepository.updateUser(user);
        if (!updated) {
            throw new RuntimeException("Failed to update profile.");
        }
    }

    /**
     * Activates or deactivates a user account (preserves historical records).
     */
    public boolean toggleUserStatus(int userId, boolean active) throws UserNotFoundException {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("User with ID " + userId + " not found.");
        }
        return userRepository.setActiveStatus(userId, active);
    }

    public User getUserById(int userId) {
        return userRepository.findById(userId);
    }

    public List<User> searchUsers(String query, String role, String status) {
        return userRepository.searchAndFilterUsers(query, role, status);
    }
}
