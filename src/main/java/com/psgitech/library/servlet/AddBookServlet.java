package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.DuplicateBookException;
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
 * Servlet handling adding new books with initial physical copies.
 * Restricted to Librarians and Administrators.
 * URL Mapping: /books/add
 */
@WebServlet(name = "AddBookServlet", urlPatterns = {"/books/add"})
public class AddBookServlet extends HttpServlet {
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

        String title = null;
        String author = null;
        String isbn = null;
        String publisher = null;
        String edition = null;
        Integer publicationYear = null;
        String category = null;
        String department = null;
        String description = null;
        String coverImageUrl = null;
        int initialCopies = 1;

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
                if (json.has("title")) title = json.get("title").getAsString();
                if (json.has("author")) author = json.get("author").getAsString();
                if (json.has("isbn")) isbn = json.get("isbn").getAsString();
                if (json.has("publisher")) publisher = json.get("publisher").getAsString();
                if (json.has("edition")) edition = json.get("edition").getAsString();
                if (json.has("publicationYear")) publicationYear = json.get("publicationYear").getAsInt();
                if (json.has("category")) category = json.get("category").getAsString();
                if (json.has("department")) department = json.get("department").getAsString();
                if (json.has("description")) description = json.get("description").getAsString();
                if (json.has("coverImageUrl")) coverImageUrl = json.get("coverImageUrl").getAsString();
                if (json.has("initialCopies")) initialCopies = json.get("initialCopies").getAsInt();
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Invalid JSON request format: " + e.getMessage());
                resp.getWriter().write(gson.toJson(err));
                return;
            }
        } else {
            title = req.getParameter("title");
            author = req.getParameter("author");
            isbn = req.getParameter("isbn");
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
            String copiesStr = req.getParameter("initialCopies");
            if (copiesStr != null && !copiesStr.trim().isEmpty()) {
                try {
                    initialCopies = Integer.parseInt(copiesStr.trim());
                } catch (NumberFormatException ignored) {}
            }
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setEdition(edition);
        book.setPublicationYear(publicationYear);
        book.setCategory(category);
        book.setDepartment(department);
        book.setDescription(description);
        book.setCoverImageUrl(coverImageUrl);

        Map<String, Object> result = new HashMap<>();
        try {
            Book created = bookService.addBook(book, initialCopies);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            result.put("success", true);
            result.put("message", "Book added successfully with " + initialCopies + " physical copies.");
            result.put("book", created);
            Map<String, Object> data = new HashMap<>();
            data.put("bookId", created.getBookId());
            data.put("isbn", created.getIsbn());
            result.put("data", data);
            resp.getWriter().write(gson.toJson(result));

        } catch (DuplicateBookException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(result));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.put("success", false);
            result.put("message", "An unexpected error occurred while adding the book: " + e.getMessage());
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
