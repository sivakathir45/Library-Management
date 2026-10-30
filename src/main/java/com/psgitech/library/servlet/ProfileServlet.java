package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.exception.UserAlreadyExistsException;
import com.psgitech.library.exception.UserNotFoundException;
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
import java.util.Map;

/**
 * Servlet handling My Profile views and updates for permitted fields.
 * URL Mapping: /profile
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        // Fetch refreshed user record
        User refreshed = userService.getUserById(currentUser.getUserId());
        if (refreshed != null) {
            session.setAttribute("user", refreshed);
            currentUser = refreshed;
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || "application/json".equalsIgnoreCase(req.getHeader("Accept"));

        if (isAjax) {
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(currentUser));
        } else {
            req.setAttribute("user", currentUser);
            req.getRequestDispatcher("/profile.html").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in first.");
            return;
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || (req.getContentType() != null && req.getContentType().contains("application/json"));

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String department = req.getParameter("department");
        String yearStr = req.getParameter("year");

        Integer year = null;
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        Map<String, Object> result = new HashMap<>();

        try {
            userService.updateProfile(currentUser.getUserId(), name, email, phone, department, year);

            // Refresh user in session
            User refreshed = userService.getUserById(currentUser.getUserId());
            session.setAttribute("user", refreshed);
            session.setAttribute("userName", refreshed.getName());

            result.put("success", true);
            result.put("message", "Profile updated successfully!");
            result.put("user", refreshed);

            if (isAjax) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.sendRedirect(req.getContextPath() + "/profile.html?updated=true");
            }

        } catch (UserAlreadyExistsException | IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/profile.html").forward(req, resp);
            }
        } catch (UserNotFoundException e) {
            result.put("success", false);
            result.put("message", e.getMessage());

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.html");
            }
        }
    }
}
