package com.psgitech.library.model;

import java.sql.Timestamp;

/**
 * Model representing a book title in the catalog.
 * Uses composition: A Book has a BookInventory.
 */
public class Book {
    private Integer bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String edition;
    private Integer publicationYear;
    private String category;
    private String department;
    private String description;
    private String coverImageUrl;
    private boolean archived;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Composition: A Book has a BookInventory
    private BookInventory inventory;

    public Book() {
        this.archived = false;
        this.inventory = new BookInventory();
    }

    public Book(String isbn, String title, String author, String publisher, Integer publicationYear, String category, String department) {
        this();
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.publicationYear = publicationYear;
        this.category = category;
        this.department = department;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public Integer getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
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

    public BookInventory getInventory() {
        if (this.inventory == null) {
            this.inventory = new BookInventory();
        }
        return inventory;
    }

    public void setInventory(BookInventory inventory) {
        this.inventory = inventory != null ? inventory : new BookInventory();
    }

    /**
     * Convenience helper returning available copy count.
     */
    public int getAvailableCopies() {
        return getInventory().getAvailableCopies();
    }

    /**
     * Convenience helper returning total active copies count.
     */
    public int getTotalActiveCopies() {
        return getInventory().getTotalActiveCopies();
    }

    /**
     * Checks if this book currently has at least one copy available on shelf.
     */
    public boolean isAvailable() {
        return !this.archived && getAvailableCopies() > 0;
    }

    /**
     * Checks whether this book can be safely archived.
     */
    public boolean canBeArchived() {
        return !this.archived && getInventory().canArchiveBook();
    }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", department='" + department + '\'' +
                ", archived=" + archived +
                ", inventory=" + inventory +
                '}';
    }
}
