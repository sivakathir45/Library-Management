package com.psgitech.library.enums;

/**
 * Enumeration representing the circulation status of an individual physical book copy.
 * Strict status definitions:
 * - AVAILABLE: The physical copy is on the shelf and ready to be borrowed.
 * - BORROWED: The copy is currently issued to a student.
 * - ALLOCATED: The copy has been allocated to a student for collection.
 * - TRANSFER_PENDING: A student-to-student transfer is currently in progress.
 * - ARCHIVED: The copy is retired and no longer available for circulation.
 */
public enum CopyStatus {
    AVAILABLE,
    BORROWED,
    ALLOCATED,
    TRANSFER_PENDING,
    ARCHIVED;

    /**
     * Helper to safely parse string values into CopyStatus.
     */
    public static CopyStatus fromString(String text) {
        if (text == null) return AVAILABLE;
        for (CopyStatus status : CopyStatus.values()) {
            if (status.name().equalsIgnoreCase(text.trim())) {
                return status;
            }
        }
        return AVAILABLE;
    }
}
