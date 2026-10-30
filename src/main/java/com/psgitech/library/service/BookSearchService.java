package com.psgitech.library.service;

import com.psgitech.library.model.Book;
import com.psgitech.library.repository.BookRepository;

import java.util.List;

/**
 * Service managing book searching, filtering, and role-based archive visibility rules.
 */
public class BookSearchService {
    private final BookRepository bookRepository;

    public BookSearchService() {
        this.bookRepository = new BookRepository();
    }

    public BookSearchService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * Searches and filters catalogue books.
     * Enforces role-based visibility: students never see archived books;
     * librarians/admins can see archived books if explicitly requested.
     */
    public List<Book> search(String keyword, String searchType, String category,
                             String department, String availability, boolean includeArchived) {
        return bookRepository.searchBooks(keyword, searchType, category, department, availability, includeArchived);
    }

    /**
     * Retrieves all active books for the public/student catalogue.
     */
    public List<Book> getCatalogueBooks(boolean includeArchived) {
        return bookRepository.searchBooks(null, "all", null, null, null, includeArchived);
    }

    /**
     * Retrieves featured books for the homepage.
     */
    public List<Book> getFeaturedBooks(int limit) {
        List<Book> books = bookRepository.searchBooks(null, "all", null, null, null, false);
        if (books.size() <= limit) {
            return books;
        }
        return books.subList(0, limit);
    }

    /**
     * Retrieves books belonging to a specific category.
     */
    public List<Book> getBooksByCategory(String category, int limit) {
        List<Book> books = bookRepository.searchBooks(null, "all", category, null, null, false);
        if (limit > 0 && books.size() > limit) {
            return books.subList(0, limit);
        }
        return books;
    }
}
