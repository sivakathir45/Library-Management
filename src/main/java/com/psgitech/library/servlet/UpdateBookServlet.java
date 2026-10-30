package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.BookNotFoundException;
import com.psgitech.library.exception.InvalidBookOperationException;
import com.psgitech.library.model.Book;
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
 * Servlet handling updating book bibliographic metadata.
 * Restricted to Librarians and Administrators.
 * URL Mapping: /books/update
 */
@WebServlet(name = "UpdateBookServlet", urlPatterns = {"/books/update"})
public class UpdateBookServlet extends HttpServlet {
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
        String title = null;
        String author = null;
        String publisher = null;
        String edition = null;
        Integer publicationYear = null;
        String category = null;
        String department = null;
        String description = null;
        String coverImageUrl = null;

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
                if (json.has("title")) title = json.get("title").getAsString();
                if (json.has("author")) author = json.get("author").getAsString();
                if (json.has("publisher")) publisher = json.get("publisher").getAsString();
                if (json.has("edition")) edition = json.get("edition").getAsString();
                if (json.has("publicationYear")) publicationYear = json.get("publicationYear").getAsInt();
                if (json.has("category")) category = json.get("category").getAsString();
                if (json.has("department")) department = json.get("department").getAsString();
                if (json.has("description")) description = json.get("description").getAsString();
                if (json.has("coverImageUrl")) coverImageUrl = json.get("coverImageUrl").getAsString();
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
            if (idStr == null) idStr = req.getParameter("id");
            if (idStr != null && !idStr.trim().isEmpty()) {
                try {
                    bookId = Integer.parseInt(idStr.trim());
                } catch (NumberFormatException ignored) {}
            }
            title = req.getParameter("title");
            author = req.getParameter("author");
            publisher = req.getParameter("publisher");
            edition = req.getParameter("edition");
            String yearStr = req.getParameter("publicationYear");
            if (yearStr != null && !yearStr.trim().isEmpty()) {
                try {
                    publicationYear = Integer.parseInt(yearStr.trim());
                } catch (NumberFormatException ignored) {}
            }
            category = req.getParameter("category");
            department = req.getParameter("department");
            description = req.getParameter("description");
            coverImageUrl = req.getParameter("coverImageUrl");
        }

        if (bookId == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Book ID is required for updating.");
            resp.getWriter().write(gson.toJson(err));
            return;
        }

        Book book = new Book();
        book.setBookId(bookId);
        book.setTitle(title);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setEdition(edition);
        book.setPublicationYear(publicationYear);
        book.setCategory(category);
        book.setDepartment(department);
        book.setDescription(description);
        book.setCoverImageUrl(coverImageUrl);

        Map<String, Object> result = new HashMap<>();
        try {
            Book updated = bookService.updateBook(book);
            result.put("success", true);
            result.put("message", "Book details updated successfully.");
            result.put("book", updated);
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
            result.put("message", "Internal server error updating book: " + e.getMessage());
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
