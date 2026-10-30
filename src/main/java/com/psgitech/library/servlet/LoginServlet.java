package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.enums.Role;
import com.psgitech.library.exception.AccountInactiveException;
import com.psgitech.library.exception.InvalidCredentialsException;
import com.psgitech.library.model.Librarian;
import com.psgitech.library.model.User;
import com.psgitech.library.service.AuthenticationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet handling user authentication and session establishment.
 * URL Mapping: /login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    private AuthenticationService authService;
    private Gson gson;

    @Override
    public void init() {
        this.authService = new AuthenticationService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            resp.sendRedirect(getRedirectUrlForUser(req.getContextPath(), user));
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || (req.getContentType() != null && req.getContentType().contains("application/json"));

        String loginKey = req.getParameter("loginKey");
        if (loginKey == null || loginKey.trim().isEmpty()) {
            loginKey = req.getParameter("email");
        }
        if (loginKey == null || loginKey.trim().isEmpty()) {
            loginKey = req.getParameter("registerNumber");
        }
        String password = req.getParameter("password");
        String rememberMe = req.getParameter("rememberMe");

        Map<String, Object> result = new HashMap<>();

        try {
            User user = authService.login(loginKey, password);

            // Establish secure session
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userRole", user.getRole().name());
            session.setAttribute("userName", user.getName());

            // Handle Remember Me
            if (rememberMe != null && (rememberMe.equals("true") || rememberMe.equals("on") || rememberMe.equals("1"))) {
                String rawToken = authService.createRememberToken(user.getUserId());
                if (rawToken != null) {
                    Cookie rememberCookie = new Cookie(AuthenticationService.REMEMBER_ME_COOKIE_NAME, rawToken);
                    rememberCookie.setMaxAge(AuthenticationService.REMEMBER_ME_DAYS * 24 * 60 * 60);
                    rememberCookie.setPath("/");
                    rememberCookie.setHttpOnly(true);
                    resp.addCookie(rememberCookie);
                }
            }

            String redirectUrl = getRedirectUrlForUser(req.getContextPath(), user);
            result.put("success", true);
            result.put("message", "Login successful! Welcome, " + user.getName());
            result.put("redirect", redirectUrl);
            result.put("user", user);

            if (isAjax) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.sendRedirect(redirectUrl);
            }

        } catch (InvalidCredentialsException e) {
            result.put("success", false);
            result.put("message", e.getMessage());

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/login.html").forward(req, resp);
            }
        } catch (AccountInactiveException e) {
            result.put("success", false);
            result.put("message", e.getMessage());

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", e.getMessage());
                req.getRequestDispatcher("/login.html").forward(req, resp);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "An unexpected error occurred during login. Please try again.");

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", "An error occurred. Please try again.");
                req.getRequestDispatcher("/login.html").forward(req, resp);
            }
        }
    }

    private String getRedirectUrlForUser(String contextPath, User user) {
        if (user instanceof Librarian) {
            Librarian lib = (Librarian) user;
            if (lib.isMustChangePassword()) {
                return contextPath + "/first-login-change-password.html";
            }
            return contextPath + "/librarian-dashboard.html";
        }
        if (user.getRole() == Role.ADMIN) {
            return contextPath + "/admin-users.html";
        }
        return contextPath + "/student-home.html";
    }
}
