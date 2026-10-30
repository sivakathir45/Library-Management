package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.UserAlreadyExistsException;
import com.psgitech.library.exception.UserNotFoundException;
import com.psgitech.library.model.Librarian;
import com.psgitech.library.model.User;
import com.psgitech.library.service.UserService;

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
 * Servlet handling Admin User Management operations:
 * - Listing and filtering all users
 * - Searching by Name, Register No, Employee ID, Email
 * - Creating Librarian accounts (with must_change_password=true)
 * - Activating / Deactivating user accounts
 * URL Mapping: /admin/users
 */
@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    private boolean verifyAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied: Administrator privileges required.");
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(err));
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!verifyAdmin(req, resp)) return;

        String query = req.getParameter("q");
        String role = req.getParameter("role");
        String status = req.getParameter("status");

        List<User> users = userService.searchUsers(query, role, status);

        // Sanitize sensitive fields before sending JSON
        for (User u : users) {
            u.setPasswordHash(null);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("success", true);
        responseData.put("count", users.size());
        responseData.put("users", users);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseData));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!verifyAdmin(req, resp)) return;

        String action = req.getParameter("action");
        Map<String, Object> result = new HashMap<>();

        if ("create_librarian".equalsIgnoreCase(action)) {
            handleCreateLibrarian(req, resp, result);
        } else if ("toggle_status".equalsIgnoreCase(action)) {
            handleToggleStatus(req, resp, result);
        } else if ("get_user".equalsIgnoreCase(action)) {
            handleGetUser(req, resp, result);
        } else {
            result.put("success", false);
            result.put("message", "Unknown admin action.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        }
    }

    private void handleCreateLibrarian(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> result) throws IOException {
        String name = req.getParameter("name");
        String employeeId = req.getParameter("employeeId");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String initialPassword = req.getParameter("initialPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        Librarian lib = new Librarian();
        lib.setName(name);
        lib.setEmployeeId(employeeId);
        lib.setEmail(email);
        lib.setPhone(phone);

        try {
            userService.createLibrarian(lib, initialPassword, confirmPassword);
            lib.setPasswordHash(null); // Never expose hash

            result.put("success", true);
            result.put("message", "Librarian account created successfully for " + lib.getName() + " (Employee ID: " + lib.getEmployeeId() + "). First-time password change is required.");
            result.put("librarian", lib);

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));

        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to create librarian account: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        }
    }

    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> result) throws IOException {
        String userIdStr = req.getParameter("userId");
        String activeStr = req.getParameter("active");

        if (userIdStr == null || activeStr == null) {
            result.put("success", false);
            result.put("message", "User ID and active status are required.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr.trim());
            boolean active = Boolean.parseBoolean(activeStr.trim());

            boolean updated = userService.toggleUserStatus(userId, active);
            result.put("success", updated);
            result.put("message", "Account status updated to: " + (active ? "ACTIVE" : "INACTIVE"));

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));

        } catch (UserNotFoundException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Failed to update account status.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        }
    }

    private void handleGetUser(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> result) throws IOException {
        String userIdStr = req.getParameter("userId");
        if (userIdStr == null) {
            result.put("success", false);
            result.put("message", "User ID is required.");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
            return;
        }

        try {
            int userId = Integer.parseInt(userIdStr.trim());
            User u = userService.getUserById(userId);
            if (u != null) {
                u.setPasswordHash(null);
                result.put("success", true);
                result.put("user", u);
            } else {
                result.put("success", false);
                result.put("message", "User not found.");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Error retrieving user details.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        }
    }
}
