package com.psgitech.library.model;

import com.psgitech.library.enums.Role;

/**
 * Admin model extending User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Admin extends User {

    public Admin() {
        super();
        setRole(Role.ADMIN);
    }

    public Admin(int userId, String name, String email, String passwordHash, String phone, boolean active) {
        super(userId, name, email, passwordHash, phone, Role.ADMIN, active);
    }

    @Override
    public String getIdentifier() {
        return getEmail();
    }
}
