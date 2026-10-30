package com.psgitech.library.filter;

import com.psgitech.library.enums.Role;
import com.psgitech.library.model.Librarian;
import com.psgitech.library.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Filter enforcing Role-Based Access Control (RBAC) and mandatory first-login password change.
 */
@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String path = uri.substring(contextPath.length());

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Check first-login password change requirement
        if (user instanceof Librarian) {
            Librarian lib = (Librarian) user;
            if (lib.isMustChangePassword()) {
                boolean isAllowedPath = path.contains("first-login-change-password")
                        || path.contains("change-password")
                        || path.contains("logout")
                        || path.startsWith("/css")
                        || path.startsWith("/js")
                        || path.startsWith("/images");

                if (!isAllowedPath) {
                    res.sendRedirect(contextPath + "/first-login-change-password.html");
                    return;
                }
            }
        }

        // Admin-only protection
        if (path.startsWith("/admin/") || path.contains("admin-users")) {
            if (user == null) {
                res.sendRedirect(contextPath + "/login.html?redirect=" + path);
                return;
            }
            if (user.getRole() != Role.ADMIN) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Administrator role required.");
                return;
            }
        }

        // Librarian-only protection
        if (path.startsWith("/librarian/") || path.contains("librarian-dashboard")) {
            if (user == null) {
                res.sendRedirect(contextPath + "/login.html?redirect=" + path);
                return;
            }
            if (user.getRole() != Role.LIBRARIAN && user.getRole() != Role.ADMIN) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Librarian role required.");
                return;
            }
        }

        // Student-only protection
        if (path.startsWith("/student/") || path.contains("student-home")) {
            if (user == null) {
                res.sendRedirect(contextPath + "/login.html?redirect=" + path);
                return;
            }
            if (user.getRole() != Role.STUDENT && user.getRole() != Role.ADMIN) {
                res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Student role required.");
                return;
            }
        }

        // Profile / Change Password protection
        if (path.contains("profile.html") || path.equals("/profile")) {
            if (user == null) {
                res.sendRedirect(contextPath + "/login.html?redirect=" + path);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
