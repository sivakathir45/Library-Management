package com.psgitech.library.model;

import com.psgitech.library.enums.CopyStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the inventory and circulation state of physical copies for a book.
 * Employs composition inside Book.java.
 * Responsibilities:
 * - Maintain the physical copy list when detailed copy management is requested.
 * - Calculate/store total active copies (excluding ARCHIVED).
 * - Calculate/store available copies on the shelf.
 * - Validate whether an individual copy can be archived (must be AVAILABLE).
 * - Validate whether an entire book can be safely archived (no active circulation).
 */
public class BookInventory {
    private List<BookCopy> copies;
    private int totalActiveCopies;
    private int availableCopies;
    private int borrowedCopies;
    private int allocatedCopies;
    private int transferPendingCopies;
    private int archivedCopies;

    public BookInventory() {
        this.copies = new ArrayList<>();
        this.totalActiveCopies = 0;
        this.availableCopies = 0;
        this.borrowedCopies = 0;
        this.allocatedCopies = 0;
        this.transferPendingCopies = 0;
        this.archivedCopies = 0;
    }

    public BookInventory(int totalActiveCopies, int availableCopies) {
        this();
        this.totalActiveCopies = totalActiveCopies;
        this.availableCopies = availableCopies;
    }

    public List<BookCopy> getCopies() {
        return copies != null ? Collections.unmodifiableList(copies) : Collections.emptyList();
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies != null ? new ArrayList<>(copies) : new ArrayList<>();
        recalculateFromCopies();
    }

    public void addCopy(BookCopy copy) {
        if (copy != null) {
            if (this.copies == null) {
                this.copies = new ArrayList<>();
            }
            this.copies.add(copy);
            recalculateFromCopies();
        }
    }

    /**
     * Recomputes all summary counts directly from the attached copy collection.
     */
    public void recalculateFromCopies() {
        if (this.copies == null) return;

        int active = 0;
        int avail = 0;
        int borrowed = 0;
        int allocated = 0;
        int transfer = 0;
        int archived = 0;

        for (BookCopy c : this.copies) {
            if (c.getStatus() == CopyStatus.AVAILABLE) {
                avail++;
                active++;
            } else if (c.getStatus() == CopyStatus.BORROWED) {
                borrowed++;
                active++;
            } else if (c.getStatus() == CopyStatus.ALLOCATED) {
                allocated++;
                active++;
            } else if (c.getStatus() == CopyStatus.TRANSFER_PENDING) {
                transfer++;
                active++;
            } else if (c.getStatus() == CopyStatus.ARCHIVED) {
                archived++;
            }
        }

        this.totalActiveCopies = active;
        this.availableCopies = avail;
        this.borrowedCopies = borrowed;
        this.allocatedCopies = allocated;
        this.transferPendingCopies = transfer;
        this.archivedCopies = archived;
    }

    public int getTotalActiveCopies() {
        return totalActiveCopies;
    }

    public void setTotalActiveCopies(int totalActiveCopies) {
        this.totalActiveCopies = totalActiveCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getBorrowedCopies() {
        return borrowedCopies;
    }

    public void setBorrowedCopies(int borrowedCopies) {
        this.borrowedCopies = borrowedCopies;
    }

    public int getAllocatedCopies() {
        return allocatedCopies;
    }

    public void setAllocatedCopies(int allocatedCopies) {
        this.allocatedCopies = allocatedCopies;
    }

    public int getTransferPendingCopies() {
        return transferPendingCopies;
    }

    public void setTransferPendingCopies(int transferPendingCopies) {
        this.transferPendingCopies = transferPendingCopies;
    }

    public int getArchivedCopies() {
        return archivedCopies;
    }

    public void setArchivedCopies(int archivedCopies) {
        this.archivedCopies = archivedCopies;
    }

    public int getTotalCopies() {
        return totalActiveCopies + archivedCopies;
    }

    /**
     * Checks whether an individual copy can be archived.
     * Guardrail: An individual copy may be archived ONLY when status is AVAILABLE.
     */
    public boolean canArchiveCopy(BookCopy copy) {
        return copy != null && copy.getStatus() == CopyStatus.AVAILABLE;
    }

    /**
     * Checks whether the entire book can be archived.
     * Guardrail: A book may be archived ONLY if none of its physical copies are currently:
     * - BORROWED
     * - ALLOCATED
     * - TRANSFER_PENDING
     */
    public boolean canArchiveBook() {
        if (this.copies != null && !this.copies.isEmpty()) {
            for (BookCopy c : this.copies) {
                if (c.getStatus() == CopyStatus.BORROWED ||
                    c.getStatus() == CopyStatus.ALLOCATED ||
                    c.getStatus() == CopyStatus.TRANSFER_PENDING) {
                    return false;
                }
            }
            return true;
        }
        // If copies list not fully loaded in memory, check aggregated counters
        return this.borrowedCopies == 0 && this.allocatedCopies == 0 && this.transferPendingCopies == 0;
    }

    @Override
    public String toString() {
        return "BookInventory{" +
                "totalActive=" + totalActiveCopies +
                ", available=" + availableCopies +
                ", borrowed=" + borrowedCopies +
                ", archived=" + archivedCopies +
                '}';
    }
}
