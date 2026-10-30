package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.exception.InvalidBookOperationException;
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
import java.util.Map;

/**
 * Servlet handling archival of physical copies or entire book titles.
 * Restricted to Librarians and Administrators.
 * URL Mapping: /books/archive
 */
@WebServlet(name = "ArchiveBookServlet", urlPatterns = {"/books/archive"})
public class ArchiveBookServlet extends HttpServlet {
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

        String target = "book"; // "book" or "copy"
        Integer bookId = null;
        Integer copyId = null;

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
                if (json.has("target")) target = json.get("target").getAsString();
                if (json.has("bookId")) bookId = json.get("bookId").getAsInt();
                if (json.has("copyId")) copyId = json.get("copyId").getAsInt();
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Invalid JSON request format: " + e.getMessage());
                resp.getWriter().write(gson.toJson(err));
                return;
            }
        } else {
            String t = req.getParameter("target");
            if (t != null && !t.trim().isEmpty()) target = t.trim();

            String bId = req.getParameter("bookId");
            if (bId != null && !bId.trim().isEmpty()) {
                try {
                    bookId = Integer.parseInt(bId.trim());
                } catch (NumberFormatException ignored) {}
            }

            String cId = req.getParameter("copyId");
            if (cId != null && !cId.trim().isEmpty()) {
                try {
                    copyId = Integer.parseInt(cId.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        Map<String, Object> result = new HashMap<>();

        try {
            if ("copy".equalsIgnoreCase(target)) {
                if (copyId == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("success", false);
                    result.put("message", "Copy ID is required to archive a physical copy.");
                    resp.getWriter().write(gson.toJson(result));
                    return;
                }

                boolean archived = bookService.archiveCopy(copyId);
                result.put("success", archived);
                result.put("message", "Physical copy #" + copyId + " has been successfully archived.");
                resp.getWriter().write(gson.toJson(result));

            } else {
                // Default: Archive entire book
                if (bookId == null) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    result.put("success", false);
                    result.put("message", "Book ID is required to archive a book.");
                    resp.getWriter().write(gson.toJson(result));
                    return;
                }

                boolean archived = bookService.archiveBook(bookId);
                result.put("success", archived);
                result.put("message", "Book and all available copies have been successfully archived.");
                resp.getWriter().write(gson.toJson(result));
            }

        } catch (BookNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (InvalidBookOperationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "Internal server error during archive operation: " + e.getMessage());
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
