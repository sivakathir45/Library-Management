package com.psgitech.library.service;

import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.exception.DuplicateBookException;
import com.psgitech.library.exception.InvalidBookOperationException;
import com.psgitech.library.model.Book;
import com.psgitech.library.model.BookCopy;
import com.psgitech.library.repository.BookRepository;

import java.time.Year;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Service managing book catalog operations, physical copies, and archival guardrails.
 */
public class BookService {
    private final BookRepository bookRepository;

    private static final Set<String> VALID_CATEGORIES = new HashSet<>(Arrays.asList(
            "Computer Science",
            "Programming",
            "Data Structures",
            "Artificial Intelligence",
            "Mathematics",
            "Electronics",
            "General Knowledge",
            "Fiction"
    ));

    private static final Set<String> VALID_DEPARTMENTS = new HashSet<>(Arrays.asList(
            "CSE", "IT", "ECE", "EEE", "MECH", "AI&DS", "CIVIL", "GENERAL"
    ));

    private static final Pattern ISBN_CLEAN_PATTERN = Pattern.compile("[^0-9X]", Pattern.CASE_INSENSITIVE);

    public BookService() {
        this.bookRepository = new BookRepository();
    }

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Validates and registers a new book title along with its initial physical copies in one transaction.
     */
    public Book addBook(Book book, int initialCopies) throws DuplicateBookException, IllegalArgumentException {
        validateBookFields(book, true);

        if (initialCopies < 1) {
            throw new IllegalArgumentException("Initial number of physical copies must be at least 1.");
        }

        // Automatic Open Library Cover fallback if blank
        if (book.getCoverImageUrl() == null || book.getCoverImageUrl().trim().isEmpty()) {
            String cleanIsbn = cleanIsbn(book.getIsbn());
            book.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-L.jpg");
        }

        return bookRepository.createBookWithCopies(book, initialCopies);
    }

    /**
     * Updates editable bibliographic metadata of an existing book.
     * Guardrail: ISBN remains immutable to prevent dangling references.
     */
    public Book updateBook(Book updatedBook) {
        if (updatedBook == null || updatedBook.getBookId() == null) {
            throw new IllegalArgumentException("Book ID is required for update.");
        }

        Book existing = bookRepository.findById(updatedBook.getBookId(), false);
        if (existing == null) {
            throw new BookNotFoundException("Book with ID " + updatedBook.getBookId() + " was not found.");
        }

        if (existing.isArchived()) {
            throw new InvalidBookOperationException("Cannot update an archived book. The book must be active.");
        }

        validateBookFields(updatedBook, false);

        // Retain original ISBN
        updatedBook.setIsbn(existing.getIsbn());

        // Cover image fallback
        if (updatedBook.getCoverImageUrl() == null || updatedBook.getCoverImageUrl().trim().isEmpty()) {
            String cleanIsbn = cleanIsbn(existing.getIsbn());
            updatedBook.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/" + cleanIsbn + "-L.jpg");
        }

        boolean updated = bookRepository.updateBook(updatedBook);
        if (!updated) {
            throw new RuntimeException("Failed to update book details in database.");
        }

        return bookRepository.findById(updatedBook.getBookId(), true);
    }

    /**
     * Adds N new physical copies to an active book.
     */
    public List<BookCopy> addCopies(int bookId, int numberOfCopies) {
        Book book = bookRepository.findById(bookId, false);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + bookId + " does not exist.");
        }

        if (book.isArchived()) {
            throw new InvalidBookOperationException("Cannot add physical copies to an archived book.");
        }

        if (numberOfCopies < 1) {
            throw new IllegalArgumentException("Number of physical copies to add must be at least 1.");
        }

        return bookRepository.addCopies(bookId, numberOfCopies);
    }

    /**
     * Archives an individual physical copy only if its status is AVAILABLE.
     */
    public boolean archiveCopy(int copyId) {
        return bookRepository.archiveCopy(copyId);
    }

    /**
     * Archives an entire book and its available copies if no copies are in circulation.
     */
    public boolean archiveBook(int bookId) {
        return bookRepository.archiveBook(bookId);
    }

    /**
     * Retrieves book details with copy inventory counts.
     */
    public Book getBookDetails(int bookId, boolean includeCopies) {
        Book book = bookRepository.findById(bookId, includeCopies);
        if (book == null) {
            throw new BookNotFoundException("Book with ID " + bookId + " was not found in the catalog.");
        }
        return book;
    }

    /**
     * Retrieves book by ISBN.
     */
    public Book getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn);
        if (book == null) {
            throw new BookNotFoundException("Book with ISBN " + isbn + " was not found in the catalog.");
        }
        return book;
    }

    /**
     * Validates all mandatory and optional fields for book creation and update.
     */
    public void validateBookFields(Book book, boolean isNew) {
        if (book == null) {
            throw new IllegalArgumentException("Book details cannot be empty.");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book Title is mandatory.");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Author name is mandatory.");
        }

        if (book.getPublisher() == null || book.getPublisher().trim().isEmpty()) {
            throw new IllegalArgumentException("Publisher is mandatory.");
        }

        if (isNew) {
            if (book.getIsbn() == null || book.getIsbn().trim().isEmpty()) {
                throw new IllegalArgumentException("ISBN is mandatory.");
            }
            if (!isValidIsbn(book.getIsbn())) {
                throw new IllegalArgumentException("Please enter a valid ISBN-10 or ISBN-13 format.");
            }
        }

        int currentYear = Year.now().getValue();
        if (book.getPublicationYear() == null) {
            throw new IllegalArgumentException("Publication Year is mandatory.");
        }
        if (book.getPublicationYear() < 1000 || book.getPublicationYear() > currentYear) {
            throw new IllegalArgumentException("Publication Year must be a valid 4-digit year up to " + currentYear + ".");
        }

        if (book.getCategory() == null || !isValidCategory(book.getCategory())) {
            throw new IllegalArgumentException("Invalid Category. Allowed: " + String.join(", ", VALID_CATEGORIES));
        }

        if (book.getDepartment() == null || !isValidDepartment(book.getDepartment())) {
            throw new IllegalArgumentException("Invalid Department. Allowed: " + String.join(", ", VALID_DEPARTMENTS));
        }
    }

    /**
     * Validates reasonable ISBN-10 and ISBN-13 formats.
     */
    /**
     * Validates reasonable ISBN-10 and ISBN-13 formats.
     */
    public static boolean isValidIsbn(String isbn) {
        if (isbn == null) return false;
        String clean = cleanIsbn(isbn);
        if (clean.length() != 10 && clean.length() != 13) {
            return false;
        }
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (!Character.isDigit(c) && (i != 9 || (c != 'X' && c != 'x'))) {
                return false;
            }
        }
        return true;
    }

    public static String cleanIsbn(String isbn) {
        if (isbn == null) return "";
        return ISBN_CLEAN_PATTERN.matcher(isbn).replaceAll("");
    }

    public static boolean isValidCategory(String category) {
        if (category == null) return false;
        for (String c : VALID_CATEGORIES) {
            if (c.equalsIgnoreCase(category.trim())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidDepartment(String department) {
        if (department == null) return false;
        for (String d : VALID_DEPARTMENTS) {
            if (d.equalsIgnoreCase(department.trim())) {
                return true;
            }
        }
        return false;
    }
}
