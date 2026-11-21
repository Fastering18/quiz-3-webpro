package com.zstore.servlet;

import com.zstore.dao.PelangganDAO;
import com.zstore.dao.TransaksiDAO;
import com.zstore.model.Pelanggan;
import com.zstore.model.Transaksi;
import com.zstore.storage.S3Client;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

public class DashboardServlet extends HttpServlet {

    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final S3Client s3Client = new S3Client();

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
                handleUpdateProfile(req, session, sessionUser.getIdPelanggan());
            } else if ("cancel_order".equals(action)) {
                long idTrx = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
                Transaksi trx = transaksiDAO.findById(idTrx);
                if (trx != null && trx.getIdPelanggan() != null
                        && trx.getIdPelanggan().equals(sessionUser.getIdPelanggan())
                        && !"Completed".equalsIgnoreCase(trx.getStatus())) {
                    String label = (trx.getInvoice() != null && trx.getInvoice().getExternalId() != null)
                            ? trx.getInvoice().getExternalId()
                            : ("#" + idTrx);
                    transaksiDAO.deleteTransaction(idTrx);
                    session.setAttribute("flashSuccess", "Pesanan " + label + " dibatalkan dan stok dikembalikan.");
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

    private void handleUpdateProfile(HttpServletRequest req, HttpSession session, long userId)
            throws SQLException, IOException, ServletException {
        String name = FormatUtil.trimOrEmpty(req.getParameter("name"));
        String newPass = req.getParameter("password");
        if (name.isEmpty()) {
            session.setAttribute("flashError", "Nama tidak boleh kosong.");
            return;
        }
        if (newPass != null && !newPass.trim().isEmpty() && newPass.trim().length() < 6) {
            session.setAttribute("flashError", "Password baru minimal 6 karakter.");
            return;
        }

        Pelanggan current = pelangganDAO.findById(userId);
        String oldAvatar = current != null ? current.getAvatarImg() : null;
        String newAvatar = oldAvatar;
        boolean updateAvatar = false;
        boolean removeAvatar = "1".equals(FormatUtil.trimOrEmpty(req.getParameter("remove_avatar")));

        Part avatarPart = getFilePartIfMultipart(req, "avatar_file");
        if (avatarPart != null && avatarPart.getSize() > 0) {
            try {
                newAvatar = s3Client.uploadImagePart(avatarPart, "avatars");
                updateAvatar = true;
            } catch (IOException e) {
                session.setAttribute("flashError", e.getMessage());
                return;
            }
        } else if (removeAvatar) {
            newAvatar = null;
            updateAvatar = true;
        }

        pelangganDAO.updateProfile(userId, name, newPass, newAvatar, updateAvatar);
        if (updateAvatar && oldAvatar != null && !oldAvatar.isEmpty() && !oldAvatar.equals(newAvatar)) {
            s3Client.deleteByUrl(oldAvatar);
        }
        session.setAttribute("flashSuccess", "Profil akun berhasil diperbarui.");
    }

    private Part getFilePartIfMultipart(HttpServletRequest req, String partName)
            throws IOException, ServletException {
        String ct = req.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        return req.getPart(partName);
    }
}
