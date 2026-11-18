package com.zstore.servlet;

import com.zstore.dao.PelangganDAO;
import com.zstore.dao.TransaksiDAO;
import com.zstore.model.Pelanggan;
import com.zstore.model.Transaksi;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DashboardServlet extends HttpServlet {

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Pelanggan sessionUser = session != null ? (Pelanggan) session.getAttribute("user") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            Pelanggan user = pelangganDAO.findById(sessionUser.getIdPelanggan());
            if (user == null) {
                session.invalidate();
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
            session.setAttribute("user", user);

            List<Transaksi> transactions = transaksiDAO.findByPelanggan(user.getIdPelanggan());
            req.setAttribute("transactions", transactions);
            req.setAttribute("pageTitle", "Dashboard | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading dashboard", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Pelanggan sessionUser = session != null ? (Pelanggan) session.getAttribute("user") : null;
        if (sessionUser == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = FormatUtil.trimOrEmpty(req.getParameter("action"));
        try {
            if ("update_profile".equals(action)) {
                String name = FormatUtil.trimOrEmpty(req.getParameter("name"));
                String newPass = req.getParameter("password");
                if (name.isEmpty()) {
                    session.setAttribute("flashError", "Nama tidak boleh kosong.");
                } else if (newPass != null && !newPass.trim().isEmpty() && newPass.trim().length() < 6) {
                    session.setAttribute("flashError", "Password baru minimal 6 karakter.");
                } else {
                    pelangganDAO.updateProfile(sessionUser.getIdPelanggan(), name, newPass);
                    session.setAttribute("flashSuccess", "Profil akun berhasil diperbarui.");
                }
            } else if ("topup".equals(action)) {
                long amount = FormatUtil.parseLongOrDefault(req.getParameter("amount"), 0);
                if (amount < 10000 || amount > 50000000) {
                    session.setAttribute("flashError", "Nominal top-up harus antara Rp 10.000 dan Rp 50.000.000.");
                } else {
                    pelangganDAO.topUpSaldo(sessionUser.getIdPelanggan(), amount);
                    session.setAttribute("flashSuccess", "Saldo berhasil ditambah sebesar " + FormatUtil.formatRupiah(amount) + ".");
                }
            } else if ("cancel_order".equals(action)) {
                long idTrx = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
                Transaksi trx = transaksiDAO.findById(idTrx);
                if (trx != null && trx.getIdPelanggan() != null
                        && trx.getIdPelanggan().equals(sessionUser.getIdPelanggan())
                        && !"Completed".equalsIgnoreCase(trx.getStatus())) {
                    transaksiDAO.deleteTransaction(idTrx);
                    session.setAttribute("flashSuccess", "Pesanan #" + idTrx + " dibatalkan dan stok dikembalikan.");
                } else {
                    session.setAttribute("flashError", "Pesanan tidak dapat dibatalkan.");
                }
            }

            session.setAttribute("user", pelangganDAO.findById(sessionUser.getIdPelanggan()));
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (SQLException e) {
            throw new ServletException("Database error processing dashboard action", e);
        }
    }
}

