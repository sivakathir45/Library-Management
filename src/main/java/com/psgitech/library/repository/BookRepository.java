package com.psgitech.library.repository;

import com.psgitech.library.enums.CopyStatus;
import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.exception.DuplicateBookException;
import com.psgitech.library.exception.InvalidBookOperationException;
import com.psgitech.library.model.Book;
import com.psgitech.library.model.BookCopy;
import com.psgitech.library.model.BookInventory;
import com.psgitech.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Repository managing database persistence for Books and Physical Book Copies.
 * Uses JDBC PreparedStatements, transactions, and aggregate joins to prevent N+1 queries.
 */
public class BookRepository {
    private static final Logger LOGGER = Logger.getLogger(BookRepository.class.getName());

    private static final Pattern COPY_CODE_PATTERN = Pattern.compile("PSG-BK(\\d+)-C(\\d+)");

    /**
     * Checks if an ISBN is already assigned to a book.
     */
    public boolean existsByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking ISBN existence: " + isbn, e);
            throw new RuntimeException("Database error verifying ISBN uniqueness.", e);
        }
    }

    /**
     * Checks if an ISBN is assigned to another book (excluding the target book ID).
     */
    public boolean existsByIsbnExcluding(String isbn, int excludeBookId) {
        if (isbn == null || isbn.trim().isEmpty()) return false;
        String sql = "SELECT 1 FROM books WHERE isbn = ? AND book_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn.trim());
            ps.setInt(2, excludeBookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking ISBN collision: " + isbn, e);
            throw new RuntimeException("Database error checking ISBN collision.", e);
        }
    }

    /**
     * Creates a new book and its initial physical copies in a single atomic database transaction.
     * Guardrail: The book must never be created without its initial copies.
     */
    public Book createBookWithCopies(Book book, int initialCopies) throws DuplicateBookException {
        if (existsByIsbn(book.getIsbn())) {
            throw new DuplicateBookException("A book with ISBN '" + book.getIsbn() + "' already exists in the catalog.");
        }

        String insertBookSql = "INSERT INTO books (isbn, title, author, publisher, edition, publication_year, " +
                "category, department, description, cover_image_url, is_archived) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE)";

        String insertCopySql = "INSERT INTO book_copies (copy_code, book_id, status) VALUES (?, ?, 'AVAILABLE')";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int generatedBookId;
            try (PreparedStatement psBook = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                psBook.setString(1, book.getIsbn().trim());
                psBook.setString(2, book.getTitle().trim());
                psBook.setString(3, book.getAuthor().trim());
                psBook.setString(4, book.getPublisher().trim());
                psBook.setString(5, book.getEdition() != null ? book.getEdition().trim() : null);
                psBook.setInt(6, book.getPublicationYear());
                psBook.setString(7, book.getCategory().trim());
                psBook.setString(8, book.getDepartment().trim());
                psBook.setString(9, book.getDescription() != null ? book.getDescription().trim() : null);
                psBook.setString(10, book.getCoverImageUrl() != null ? book.getCoverImageUrl().trim() : null);

                int rows = psBook.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Failed to insert book record; no rows affected.");
                }

                try (ResultSet generatedKeys = psBook.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedBookId = generatedKeys.getInt(1);
                        book.setBookId(generatedBookId);
                    } else {
                        throw new SQLException("Failed to obtain generated book_id.");
                    }
                }
            }

            // Generate and insert physical copies
            List<BookCopy> createdCopies = new ArrayList<>();
            try (PreparedStatement psCopy = conn.prepareStatement(insertCopySql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= initialCopies; i++) {
                    String copyCode = formatCopyCode(generatedBookId, i);
                    psCopy.setString(1, copyCode);
                    psCopy.setInt(2, generatedBookId);
                    psCopy.executeUpdate();

                    int copyId = 0;
                    try (ResultSet gk = psCopy.getGeneratedKeys()) {
                        if (gk.next()) {
                            copyId = gk.getInt(1);
                        }
                    }

                    BookCopy copy = new BookCopy(copyId, copyCode, generatedBookId, CopyStatus.AVAILABLE);
                    createdCopies.add(copy);
                }
            }

            conn.commit();

            // Attach initialized inventory
            BookInventory inventory = new BookInventory(initialCopies, initialCopies);
            inventory.setCopies(createdCopies);
            book.setInventory(inventory);

            return book;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to rollback transaction", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Failed to create book and copies transactionally", e);
            throw new RuntimeException("Failed to add book due to database error: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Adds N new physical copies to an existing book in an atomic transaction.
     * Guarantees unique copy codes format PSG-BK{bookId}-C{copyNumber}.
     */
    public List<BookCopy> addCopies(int bookId, int numberOfCopies) {
        if (numberOfCopies <= 0) {
            throw new IllegalArgumentException("Number of copies to add must be at least 1.");
        }

        String countSql = "SELECT copy_code FROM book_copies WHERE book_id = ?";
        String insertCopySql = "INSERT INTO book_copies (copy_code, book_id, status) VALUES (?, ?, 'AVAILABLE')";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Determine highest copy index
            int maxCopyNumber = 0;
            try (PreparedStatement psCount = conn.prepareStatement(countSql)) {
                psCount.setInt(1, bookId);
                try (ResultSet rs = psCount.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("copy_code");
                        Matcher matcher = COPY_CODE_PATTERN.matcher(code);
                        if (matcher.matches()) {
                            try {
                                int num = Integer.parseInt(matcher.group(2));
                                if (num > maxCopyNumber) {
                                    maxCopyNumber = num;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }

            List<BookCopy> added = new ArrayList<>();
            try (PreparedStatement psInsert = conn.prepareStatement(insertCopySql, Statement.RETURN_GENERATED_KEYS)) {
                for (int i = 1; i <= numberOfCopies; i++) {
                    int nextNum = maxCopyNumber + i;
                    String code = formatCopyCode(bookId, nextNum);
                    psInsert.setString(1, code);
                    psInsert.setInt(2, bookId);
                    psInsert.executeUpdate();

                    int copyId = 0;
                    try (ResultSet gk = psInsert.getGeneratedKeys()) {
                        if (gk.next()) {
                            copyId = gk.getInt(1);
                        }
                    }

                    BookCopy copy = new BookCopy(copyId, code, bookId, CopyStatus.AVAILABLE);
                    added.add(copy);
                }
            }

            conn.commit();
            return added;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed in addCopies", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Failed to add physical copies", e);
            throw new RuntimeException("Database error adding book copies: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Updates editable bibliographic metadata of an existing book.
     * ISBN is preserved and immutable.
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, publisher = ?, edition = ?, " +
                "publication_year = ?, category = ?, department = ?, description = ?, " +
                "cover_image_url = ? WHERE book_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, book.getTitle().trim());
            ps.setString(2, book.getAuthor().trim());
            ps.setString(3, book.getPublisher().trim());
            ps.setString(4, book.getEdition() != null ? book.getEdition().trim() : null);
            ps.setInt(5, book.getPublicationYear());
            ps.setString(6, book.getCategory().trim());
            ps.setString(7, book.getDepartment().trim());
            ps.setString(8, book.getDescription() != null ? book.getDescription().trim() : null);
            ps.setString(9, book.getCoverImageUrl() != null ? book.getCoverImageUrl().trim() : null);
            ps.setInt(10, book.getBookId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating book: " + book.getBookId(), e);
            throw new RuntimeException("Database error updating book details.", e);
        }
    }

    /**
     * Archives an individual physical copy only if its status is currently AVAILABLE.
     */
    public boolean archiveCopy(int copyId) {
        String checkSql = "SELECT status FROM book_copies WHERE copy_id = ?";
        String updateSql = "UPDATE book_copies SET status = 'ARCHIVED' WHERE copy_id = ? AND status = 'AVAILABLE'";

        try (Connection conn = DBConnection.getConnection()) {
            CopyStatus currentStatus = null;
            try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                psCheck.setInt(1, copyId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        currentStatus = CopyStatus.fromString(rs.getString("status"));
                    } else {
                        throw new InvalidBookOperationException("Physical copy with ID " + copyId + " does not exist.");
                    }
                }
            }

            if (currentStatus != CopyStatus.AVAILABLE) {
                throw new InvalidBookOperationException("Cannot archive copy: current status is " + currentStatus +
                        ". Only AVAILABLE copies can be archived.");
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setInt(1, copyId);
                return psUpdate.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error archiving copy " + copyId, e);
            throw new RuntimeException("Database error archiving physical copy.", e);
        }
    }

    /**
     * Archives an entire book and all its AVAILABLE copies in an atomic transaction.
     * Guardrail: The book may be archived ONLY if none of its copies are BORROWED, ALLOCATED, or TRANSFER_PENDING.
     */
    public boolean archiveBook(int bookId) {
        String checkCopiesSql = "SELECT status FROM book_copies WHERE book_id = ?";
        String archiveBookSql = "UPDATE books SET is_archived = TRUE WHERE book_id = ?";
        String archiveCopiesSql = "UPDATE book_copies SET status = 'ARCHIVED' WHERE book_id = ? AND status = 'AVAILABLE'";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Verify circulation statuses
            try (PreparedStatement psCheck = conn.prepareStatement(checkCopiesSql)) {
                psCheck.setInt(1, bookId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    while (rs.next()) {
                        CopyStatus status = CopyStatus.fromString(rs.getString("status"));
                        if (status == CopyStatus.BORROWED || status == CopyStatus.ALLOCATED || status == CopyStatus.TRANSFER_PENDING) {
                            throw new InvalidBookOperationException(
                                    "Cannot archive book: One or more physical copies are currently " + status +
                                    ". All issued, allocated, or in-transfer copies must be returned first."
                            );
                        }
                    }
                }
            }

            // Mark book as archived
            int bookUpdated;
            try (PreparedStatement psBook = conn.prepareStatement(archiveBookSql)) {
                psBook.setInt(1, bookId);
                bookUpdated = psBook.executeUpdate();
            }

            if (bookUpdated == 0) {
                throw new BookNotFoundException("Book with ID " + bookId + " does not exist.");
            }

            // Mark all AVAILABLE copies as ARCHIVED
            try (PreparedStatement psCopies = conn.prepareStatement(archiveCopiesSql)) {
                psCopies.setInt(1, bookId);
                psCopies.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Rollback failed during book archival", ex);
                }
            }
            LOGGER.log(Level.SEVERE, "Failed to archive book " + bookId, e);
            throw new RuntimeException("Database error archiving book: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error closing connection", e);
                }
            }
        }
    }

    /**
     * Retrieves book details by ID with derived inventory counts.
     * Optionally loads all individual physical copies (for librarians/admins).
     */
    public Book findById(int bookId, boolean includeCopies) {
        String sql = "SELECT b.*, " +
                "COUNT(CASE WHEN c.status != 'ARCHIVED' THEN 1 END) AS total_active_copies, " +
                "COUNT(CASE WHEN c.status = 'AVAILABLE' THEN 1 END) AS available_copies, " +
                "COUNT(CASE WHEN c.status = 'BORROWED' THEN 1 END) AS borrowed_copies, " +
                "COUNT(CASE WHEN c.status = 'ALLOCATED' THEN 1 END) AS allocated_copies, " +
                "COUNT(CASE WHEN c.status = 'TRANSFER_PENDING' THEN 1 END) AS transfer_pending_copies, " +
                "COUNT(CASE WHEN c.status = 'ARCHIVED' THEN 1 END) AS archived_copies " +
                "FROM books b " +
                "LEFT JOIN book_copies c ON b.book_id = c.book_id " +
                "WHERE b.book_id = ? " +
                "GROUP BY b.book_id";

        Book book = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    book = mapBookFromResultSet(rs);
                }
            }

            if (book != null && includeCopies) {
                book.getInventory().setCopies(findCopiesByBookId(conn, bookId));
            }

            return book;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching book by ID: " + bookId, e);
            throw new RuntimeException("Database error retrieving book details.", e);
        }
    }

    /**
     * Retrieves book by ISBN.
     */
    public Book findByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) return null;
        String sql = "SELECT b.*, " +
                "COUNT(CASE WHEN c.status != 'ARCHIVED' THEN 1 END) AS total_active_copies, " +
                "COUNT(CASE WHEN c.status = 'AVAILABLE' THEN 1 END) AS available_copies, " +
                "COUNT(CASE WHEN c.status = 'BORROWED' THEN 1 END) AS borrowed_copies, " +
                "COUNT(CASE WHEN c.status = 'ALLOCATED' THEN 1 END) AS allocated_copies, " +
                "COUNT(CASE WHEN c.status = 'TRANSFER_PENDING' THEN 1 END) AS transfer_pending_copies, " +
                "COUNT(CASE WHEN c.status = 'ARCHIVED' THEN 1 END) AS archived_copies " +
                "FROM books b " +
                "LEFT JOIN book_copies c ON b.book_id = c.book_id " +
                "WHERE b.isbn = ? " +
                "GROUP BY b.book_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, isbn.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBookFromResultSet(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching book by ISBN: " + isbn, e);
            throw new RuntimeException("Database error retrieving book by ISBN.", e);
        }
    }

    /**
     * Retrieves all physical copies of a book.
     */
    public List<BookCopy> findCopiesByBookId(int bookId) {
        try (Connection conn = DBConnection.getConnection()) {
            return findCopiesByBookId(conn, bookId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching copies for book: " + bookId, e);
            throw new RuntimeException("Database error retrieving physical copies.", e);
        }
    }

    private List<BookCopy> findCopiesByBookId(Connection conn, int bookId) throws SQLException {
        String sql = "SELECT * FROM book_copies WHERE book_id = ? ORDER BY copy_id ASC";
        List<BookCopy> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BookCopy copy = new BookCopy();
                    copy.setCopyId(rs.getInt("copy_id"));
                    copy.setCopyCode(rs.getString("copy_code"));
                    copy.setBookId(rs.getInt("book_id"));
                    copy.setStatus(CopyStatus.fromString(rs.getString("status")));
                    copy.setCreatedAt(rs.getTimestamp("created_at"));
                    copy.setUpdatedAt(rs.getTimestamp("updated_at"));
                    list.add(copy);
                }
            }
        }
        return list;
    }

    /**
     * Searches and filters the catalog with dynamic parameterized JDBC statements.
     * Protects completely against SQL injection by binding all inputs as parameters.
     */
    public List<Book> searchBooks(String keyword, String searchType, String category,
                                  String department, String availability, boolean includeArchived) {

        StringBuilder sql = new StringBuilder(
                "SELECT b.*, " +
                "COUNT(CASE WHEN c.status != 'ARCHIVED' THEN 1 END) AS total_active_copies, " +
                "COUNT(CASE WHEN c.status = 'AVAILABLE' THEN 1 END) AS available_copies, " +
                "COUNT(CASE WHEN c.status = 'BORROWED' THEN 1 END) AS borrowed_copies, " +
                "COUNT(CASE WHEN c.status = 'ALLOCATED' THEN 1 END) AS allocated_copies, " +
                "COUNT(CASE WHEN c.status = 'TRANSFER_PENDING' THEN 1 END) AS transfer_pending_copies, " +
                "COUNT(CASE WHEN c.status = 'ARCHIVED' THEN 1 END) AS archived_copies " +
                "FROM books b " +
                "LEFT JOIN book_copies c ON b.book_id = c.book_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        // Archive visibility filter
        if (!includeArchived) {
            sql.append("AND b.is_archived = FALSE ");
        }

        // Keyword filter
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            String type = (searchType != null) ? searchType.trim().toLowerCase() : "all";

            switch (type) {
                case "title":
                    sql.append("AND LOWER(b.title) LIKE ? ");
                    params.add(kw);
                    break;
                case "author":
                    sql.append("AND LOWER(b.author) LIKE ? ");
                    params.add(kw);
                    break;
                case "isbn":
                    sql.append("AND LOWER(b.isbn) LIKE ? ");
                    params.add(kw);
                    break;
                case "category":
                    sql.append("AND LOWER(b.category) LIKE ? ");
                    params.add(kw);
                    break;
                case "all":
                default:
                    sql.append("AND (LOWER(b.title) LIKE ? OR LOWER(b.author) LIKE ? OR LOWER(b.isbn) LIKE ? OR LOWER(b.category) LIKE ?) ");
                    params.add(kw);
                    params.add(kw);
                    params.add(kw);
                    params.add(kw);
                    break;
            }
        }

        // Category filter
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim())) {
            sql.append("AND LOWER(b.category) = LOWER(?) ");
            params.add(category.trim());
        }

        // Department filter
        if (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department.trim())) {
            sql.append("AND LOWER(b.department) = LOWER(?) ");
            params.add(department.trim());
        }

        sql.append("GROUP BY b.book_id ");

        // Availability filter (applied via HAVING on calculated count)
        if ("AVAILABLE".equalsIgnoreCase(availability) || "AVAILABLE_NOW".equalsIgnoreCase(availability)) {
            sql.append("HAVING available_copies > 0 ");
        } else if ("UNAVAILABLE".equalsIgnoreCase(availability) || "BORROWED".equalsIgnoreCase(availability)) {
            sql.append("HAVING available_copies = 0 ");
        }

        sql.append("ORDER BY b.is_archived ASC, b.title ASC");

        List<Book> results = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapBookFromResultSet(rs));
                }
            }

            return results;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching books", e);
            throw new RuntimeException("Database error searching catalog.", e);
        }
    }

    /**
     * Maps a ResultSet row into a populated Book instance with its BookInventory.
     */
    private Book mapBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setPublisher(rs.getString("publisher"));
        book.setEdition(rs.getString("edition"));
        book.setPublicationYear(rs.getInt("publication_year"));
        book.setCategory(rs.getString("category"));
        book.setDepartment(rs.getString("department"));
        book.setDescription(rs.getString("description"));
        book.setCoverImageUrl(rs.getString("cover_image_url"));
        book.setArchived(rs.getBoolean("is_archived"));
        book.setCreatedAt(rs.getTimestamp("created_at"));
        book.setUpdatedAt(rs.getTimestamp("updated_at"));

        // Build composite BookInventory from aggregate query columns
        BookInventory inventory = new BookInventory();
        inventory.setTotalActiveCopies(rs.getInt("total_active_copies"));
        inventory.setAvailableCopies(rs.getInt("available_copies"));
        inventory.setBorrowedCopies(rs.getInt("borrowed_copies"));
        inventory.setAllocatedCopies(rs.getInt("allocated_copies"));
        inventory.setTransferPendingCopies(rs.getInt("transfer_pending_copies"));
        inventory.setArchivedCopies(rs.getInt("archived_copies"));
        book.setInventory(inventory);

        return book;
    }

    /**
     * Formats copy code as PSG-BK{bookId}-C{copyNumber padded to 2 digits}.
     * Example: PSG-BK1-C01
     */
    public static String formatCopyCode(int bookId, int copyNumber) {
        return String.format("PSG-BK%d-C%02d", bookId, copyNumber);
    }
}
