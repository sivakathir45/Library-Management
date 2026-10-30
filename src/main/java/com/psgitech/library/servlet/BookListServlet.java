package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.enums.Role;
import com.psgitech.library.model.Book;
import com.psgitech.library.model.User;
import com.psgitech.library.service.BookSearchService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling catalog listing and homepage book requests.
 * URL Mapping: /books
 */
@WebServlet(name = "BookListServlet", urlPatterns = {"/books"})
public class BookListServlet extends HttpServlet {
    private BookSearchService bookSearchService;
    private Gson gson;

    @Override
    public void init() {
        this.bookSearchService = new BookSearchService();
        this.gson = new Gson();
    }

    private User getAuthenticatedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("user") : null;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = getAuthenticatedUser(req);
        boolean isStaff = currentUser != null && (currentUser.getRole() == Role.LIBRARIAN || currentUser.getRole() == Role.ADMIN);

        // Archived visibility rule: Only staff can explicitly view archived books
        boolean includeArchived = isStaff && "true".equalsIgnoreCase(req.getParameter("includeArchived"));

        String featured = req.getParameter("featured");
        String category = req.getParameter("category");
        String limitStr = req.getParameter("limit");
        int limit = 0;
        if (limitStr != null && !limitStr.trim().isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        List<Book> books;
        if ("true".equalsIgnoreCase(featured)) {
            books = bookSearchService.getFeaturedBooks(limit > 0 ? limit : 6);
        } else if (category != null && !category.trim().isEmpty()) {
            books = bookSearchService.getBooksByCategory(category.trim(), limit);
        } else {
            books = bookSearchService.getCatalogueBooks(includeArchived);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("count", books.size());
        responseData.put("books", books);

        resp.getWriter().write(gson.toJson(responseData));
    }
}
