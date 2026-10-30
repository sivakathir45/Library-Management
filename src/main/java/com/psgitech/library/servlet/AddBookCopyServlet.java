package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.exception.InvalidBookOperationException;
import com.psgitech.library.model.BookCopy;
import com.psgitech.library.model.User;
import com.psgitech.library.service.BookService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet handling addition of physical copies to an existing book title.
 * Restricted to Librarians and Administrators.
 * URL Mapping: /books/copies/add
 */
@WebServlet(name = "AddBookCopyServlet", urlPatterns = {"/books/copies/add"})
public class AddBookCopyServlet extends HttpServlet {
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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User currentUser = getAuthenticatedUser(req);
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Authentication required. Please log in.");
            resp.getWriter().write(gson.toJson(err));
            return;
        }

        if (!isStaff(currentUser)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. Librarian or Administrator privileges required.");
            resp.getWriter().write(gson.toJson(err));
            return;
        }

        Integer bookId = null;
        int numberOfCopies = 1;

        String contentType = req.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = req.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            try {
                JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
                if (json.has("bookId")) bookId = json.get("bookId").getAsInt();
                if (json.has("numberOfCopies")) numberOfCopies = json.get("numberOfCopies").getAsInt();
                else if (json.has("copies")) numberOfCopies = json.get("copies").getAsInt();
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Invalid JSON format: " + e.getMessage());
                resp.getWriter().write(gson.toJson(err));
                return;
            }
        } else {
            String idStr = req.getParameter("bookId");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    bookId = Integer.parseInt(idStr.trim());
                } catch (NumberFormatException ignored) {}
            }
            String countStr = req.getParameter("numberOfCopies");
            if (countStr == null) countStr = req.getParameter("copies");
            if (countStr != null && !countStr.trim().isEmpty()) {
                try {
                    numberOfCopies = Integer.parseInt(countStr.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        Map<String, Object> result = new HashMap<>();
        if (bookId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", "Book ID is required.");
            resp.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            List<BookCopy> added = bookService.addCopies(bookId, numberOfCopies);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            result.put("success", true);
            result.put("message", "Successfully added " + added.size() + " new physical copies.");
            result.put("copies", added);
            resp.getWriter().write(gson.toJson(result));

        } catch (BookNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (InvalidBookOperationException | IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Internal server error adding physical copies: " + e.getMessage());
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
