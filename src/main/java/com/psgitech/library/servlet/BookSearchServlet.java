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
 * Servlet handling dynamic parameterized catalog search and filtering.
 * Parameters: keyword, searchType, category, department, availability, includeArchived.
 * URL Mapping: /books/search
 */
@WebServlet(name = "BookSearchServlet", urlPatterns = {"/books/search"})
public class BookSearchServlet extends HttpServlet {
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

        String keyword = req.getParameter("keyword");
        if (keyword == null) keyword = req.getParameter("q");

        String searchType = req.getParameter("searchType");
        if (searchType == null) searchType = req.getParameter("type");

        String category = req.getParameter("category");
        String department = req.getParameter("department");
        String availability = req.getParameter("availability");

        User currentUser = getAuthenticatedUser(req);
        boolean isStaff = currentUser != null && (currentUser.getRole() == Role.LIBRARIAN || currentUser.getRole() == Role.ADMIN);

        // Security rule: Only Librarians and Admins can view archived books
        boolean includeArchived = isStaff && "true".equalsIgnoreCase(req.getParameter("includeArchived"));

        List<Book> results = bookSearchService.search(keyword, searchType, category, department, availability, includeArchived);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("count", results.size());
        responseData.put("books", results);
        responseData.put("includeArchived", includeArchived);

        resp.getWriter().write(gson.toJson(responseData));
    }
}
