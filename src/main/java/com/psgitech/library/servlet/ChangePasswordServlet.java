package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.exception.InvalidCredentialsException;
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
import java.util.Map;

/**
 * Servlet handling password changes for all roles, including mandatory first login updates.
 * URL Mapping: /change-password
 */
@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in to change your password.");
            return;
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || (req.getContentType() != null && req.getContentType().contains("application/json"));

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");

        Map<String, Object> result = new HashMap<>();

        try {
            userService.changePassword(currentUser.getUserId(), currentPassword, newPassword, confirmPassword);

            // If user was a librarian on first login, clear mustChangePassword in session
            if (currentUser instanceof Librarian) {
                ((Librarian) currentUser).setMustChangePassword(false);
                session.setAttribute("user", currentUser);
            }

            result.put("success", true);
            result.put("message", "Password changed successfully! Please keep your new password safe.");
            result.put("redirect", req.getContextPath() + (currentUser instanceof Librarian ? "/librarian-dashboard.html" : "/profile.html?passwordChanged=true"));

            if (isAjax) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.sendRedirect(req.getContextPath() + "/profile.html?passwordChanged=true");
            }

        } catch (InvalidCredentialsException | IllegalArgumentException e) {
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
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "An error occurred while updating the password.");

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", "An error occurred while updating password.");
                req.getRequestDispatcher("/profile.html").forward(req, resp);
            }
        }
    }
}
