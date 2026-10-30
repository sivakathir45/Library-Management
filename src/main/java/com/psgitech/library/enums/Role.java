package com.psgitech.library.enums;

/**
 * User Roles supported in the PSG iTech Library System.
 */
public enum Role {
    STUDENT("Student"),
    LIBRARIAN("Librarian"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String text) {
        if (text == null) {
            return null;
        }
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(text.trim()) || role.displayName.equalsIgnoreCase(text.trim())) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }
}
