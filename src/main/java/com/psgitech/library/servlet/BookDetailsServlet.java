package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.model.Book;
import com.psgitech.library.model.User;
import com.psgitech.library.service.BookService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet returning complete book metadata and inventory counts.
 * Physical copy details are restricted to Librarians and Administrators.
 * URL Mapping: /books/details
 */
@WebServlet(name = "BookDetailsServlet", urlPatterns = {"/books/details"})
public class BookDetailsServlet extends HttpServlet {
    private BookService bookService;
    private Gson gson;

    @Override
    public void init() {
        this.bookService = new BookService();
        this.gson = new Gson();
    }

    private User getAuthenticatedUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return (session != null) ? (User) session.getAttribute("user") : null;
    }

    private boolean isStaff(User user) {
        return user != null && (user.getRole() == Role.LIBRARIAN || user.getRole() == Role.ADMIN);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String idStr = req.getParameter("id");
        String isbn = req.getParameter("isbn");

        User currentUser = getAuthenticatedUser(req);
        boolean isStaffUser = isStaff(currentUser);

        Map<String, Object> result = new HashMap<>();

        try {
            Book book;
            if (idStr != null && !idStr.trim().isEmpty()) {
                int bookId = Integer.parseInt(idStr.trim());
                // Include physical copy details ONLY for Librarians and Admins
                book = bookService.getBookDetails(bookId, isStaffUser);
            } else if (isbn != null && !isbn.trim().isEmpty()) {
                book = bookService.getBookByIsbn(isbn.trim());
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("success", false);
                result.put("message", "Book ID (parameter 'id') or ISBN is required.");
                resp.getWriter().write(gson.toJson(result));
                return;
            }

            // If the book is archived and user is a student or unauthenticated, hide it
            if (book.isArchived() && !isStaffUser) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result.put("success", false);
                result.put("message", "This book is no longer available in the active library catalog.");
                resp.getWriter().write(gson.toJson(result));
                return;
            }

            result.put("success", true);
            result.put("book", book);
            result.put("isStaff", isStaffUser);

            resp.getWriter().write(gson.toJson(result));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Invalid Book ID format.");
            resp.getWriter().write(gson.toJson(result));

        } catch (BookNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Internal error retrieving book details: " + e.getMessage());
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
