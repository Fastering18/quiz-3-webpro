package com.zstore.servlet;

import com.zstore.dao.PelangganDAO;
import com.zstore.model.Pelanggan;
import com.zstore.util.FormatUtil;
import com.zstore.util.PasswordUtil;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AuthServlet extends HttpServlet {

    private final PelangganDAO pelangganDAO = new PelangganDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            Pelanggan user = (Pelanggan) session.getAttribute("user");
            resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/dashboard"));
            return;
        }

        if ("/register".equals(path)) {
            req.setAttribute("pageTitle", "Daftar Akun | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
        } else {
            req.setAttribute("pageTitle", "Login | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else {
            handleLogin(req, resp);
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String email = FormatUtil.trimOrEmpty(req.getParameter("email"));
        String password = req.getParameter("password");

        try {
            Pelanggan user = pelangganDAO.findByEmail(email);
            if (user == null || !PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                req.setAttribute("error", "Email atau password salah.");
                req.setAttribute("pageTitle", "Login | Z-Store");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }
            if (!"Active".equalsIgnoreCase(user.getStatusAkun())) {
                req.setAttribute("error", "Akun Anda sedang dinonaktifkan (" + user.getStatusAkun() + ").");
                req.setAttribute("pageTitle", "Login | Z-Store");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            resp.sendRedirect(req.getContextPath() + (user.isAdmin() ? "/admin" : "/dashboard"));
        } catch (SQLException e) {
            throw new ServletException("Database error during login", e);
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = FormatUtil.trimOrEmpty(req.getParameter("name"));
        String email = FormatUtil.trimOrEmpty(req.getParameter("email"));
        String password = req.getParameter("password");

        if (name.isEmpty() || email.isEmpty() || password == null || password.length() < 6) {
            req.setAttribute("error", "Nama, email, dan password minimal 6 karakter wajib diisi.");
            req.setAttribute("pageTitle", "Daftar Akun | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        try {
            if (pelangganDAO.findByEmail(email) != null) {
                req.setAttribute("error", "Email sudah terdaftar.");
                req.setAttribute("pageTitle", "Daftar Akun | Z-Store");
                req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
                return;
            }
            pelangganDAO.register(name, email, password);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (SQLException e) {
            throw new ServletException("Database error during registration", e);
        }
    }
}

