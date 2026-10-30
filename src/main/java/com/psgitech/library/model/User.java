package com.psgitech.library.model;

import com.psgitech.library.enums.Role;
import java.sql.Timestamp;

/**
 * Abstract base class representing a User in the PSG iTech Library System.
 * Demonstrates OOP principles: Encapsulation, Abstraction, and Polymorphism.
 */
public abstract class User {
    private int userId;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private Role role;
    private boolean active;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public User() {
        this.active = true;
    }

    public User(int userId, String name, String email, String passwordHash, String phone, Role role, boolean active) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    /**
     * Polymorphic method to get the primary college identifier:
     * - Register Number for Students
     * - Employee ID for Librarians
     * - Email or ID for Admins
     */
    public abstract String getIdentifier();

    // Getters and Setters (Encapsulation)

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
