package com.psgitech.library.servlet;

import com.google.gson.Gson;
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
 * Servlet handling user logout, session invalidation, and Remember Me token revocation.
 * URL Mapping: /logout
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
    private AuthenticationService authService;
    private Gson gson;

    @Override
    public void init() {
        this.authService = new AuthenticationService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processLogout(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processLogout(req, resp);
    }

    private void processLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Revoke Remember Me token from database and clear cookie
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (AuthenticationService.REMEMBER_ME_COOKIE_NAME.equals(c.getName())) {
                    authService.revokeRememberToken(c.getValue());
                    Cookie clearCookie = new Cookie(AuthenticationService.REMEMBER_ME_COOKIE_NAME, "");
                    clearCookie.setMaxAge(0);
                    clearCookie.setPath("/");
                    clearCookie.setHttpOnly(true);
                    resp.addCookie(clearCookie);
                    break;
                }
            }
        }

        // Invalidate HTTP session
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"));
        if (isAjax) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Logged out successfully.");
            result.put("redirect", req.getContextPath() + "/login.html?logged_out=true");
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(result));
        } else {
            resp.sendRedirect(req.getContextPath() + "/login.html?logged_out=true");
        }
    }
}
