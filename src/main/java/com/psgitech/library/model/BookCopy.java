package com.psgitech.library.model;

import com.psgitech.library.enums.CopyStatus;

import java.sql.Timestamp;

/**
 * Model representing an individual physical copy of a book title.
 * Unique copy code format: PSG-BK{bookId}-C{copyNumber}
 */
public class BookCopy {
    private Integer copyId;
    private String copyCode;
    private int bookId;
    private CopyStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public BookCopy() {
        this.status = CopyStatus.AVAILABLE;
    }

    public BookCopy(String copyCode, int bookId) {
        this.copyCode = copyCode;
        this.bookId = bookId;
        this.status = CopyStatus.AVAILABLE;
    }

    public BookCopy(Integer copyId, String copyCode, int bookId, CopyStatus status) {
        this.copyId = copyId;
        this.copyCode = copyCode;
        this.bookId = bookId;
        this.status = status;
    }

    public Integer getCopyId() {
        return copyId;
    }

    public void setCopyId(Integer copyId) {
        this.copyId = copyId;
    }

    public String getCopyCode() {
        return copyCode;
    }

    public void setCopyCode(String copyCode) {
        this.copyCode = copyCode;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
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

    /**
     * Checks if this physical copy is currently available on the shelf.
     */
    public boolean isAvailable() {
        return this.status == CopyStatus.AVAILABLE;
    }

    /**
     * Checks if this copy can be archived.
     * Guardrail: An individual copy may be archived ONLY when status is AVAILABLE.
     */
    public boolean canBeArchived() {
        return this.status == CopyStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "BookCopy{" +
                "copyId=" + copyId +
                ", copyCode='" + copyCode + '\'' +
                ", bookId=" + bookId +
                ", status=" + status +
                '}';
    }
}
