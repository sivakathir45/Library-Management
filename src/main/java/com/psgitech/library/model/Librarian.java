package com.psgitech.library.model;

import com.psgitech.library.enums.Role;

/**
 * Librarian model extending User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Librarian extends User {
    private String employeeId;
    private boolean mustChangePassword;

    public Librarian() {
        super();
        setRole(Role.LIBRARIAN);
        this.mustChangePassword = false;
    }

    public Librarian(int userId, String name, String email, String passwordHash, String phone,
                     boolean active, String employeeId, boolean mustChangePassword) {
        super(userId, name, email, passwordHash, phone, Role.LIBRARIAN, active);
        this.employeeId = employeeId;
        this.mustChangePassword = mustChangePassword;
    }

    @Override
    public String getIdentifier() {
        return employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
