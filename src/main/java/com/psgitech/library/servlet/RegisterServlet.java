package com.psgitech.library.servlet;

import com.google.gson.Gson;
import com.psgitech.library.exception.UserAlreadyExistsException;
import com.psgitech.library.model.Student;
import com.psgitech.library.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet handling student registration.
 * URL Mapping: /register
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    private UserService userService;
    private Gson gson;

    @Override
    public void init() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/register.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With"))
                || (req.getContentType() != null && req.getContentType().contains("application/json"));

        String name = req.getParameter("name");
        String registerNumber = req.getParameter("registerNumber");
        if (registerNumber == null) {
            registerNumber = req.getParameter("regNo"); // fallback compatibility
        }
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String department = req.getParameter("department");
        String yearStr = req.getParameter("year");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");

        Integer year = null;
        if (yearStr != null && !yearStr.trim().isEmpty()) {
            try {
                year = Integer.parseInt(yearStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        Student student = new Student();
        student.setName(name);
        student.setRegisterNumber(registerNumber);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDepartment(department);
        student.setYear(year);

        Map<String, Object> result = new HashMap<>();

        try {
            userService.registerStudent(student, password, confirmPassword);
            result.put("success", true);
            result.put("message", "Registration successful! You can now log in with your credentials.");
            result.put("redirect", req.getContextPath() + "/login.html?registered=true");

            if (isAjax) {
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                resp.sendRedirect(req.getContextPath() + "/login.html?registered=true");
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
                req.getRequestDispatcher("/register.html").forward(req, resp);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "An unexpected error occurred during registration. Please try again.");

            if (isAjax) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(result));
            } else {
                req.setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
                req.getRequestDispatcher("/register.html").forward(req, resp);
            }
        }
    }
}
