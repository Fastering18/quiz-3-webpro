package com.zstore.servlet;

import com.zstore.config.PaymentConfig;
import com.zstore.dao.PelangganDAO;
import com.zstore.dao.ProdukDAO;
import com.zstore.dao.TransaksiDAO;
import com.zstore.model.Pelanggan;
import com.zstore.model.Produk;
import com.zstore.model.Transaksi;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CheckoutServlet extends HttpServlet {

    private final ProdukDAO produkDAO = new ProdukDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id"), 0);
        if (id <= 0) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            req.getRequestDispatcher("/WEB-INF/views/404.jsp").forward(req, resp);
            return;
        }

        try {
            Produk product = produkDAO.findById(id);
            if (product == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                req.getRequestDispatcher("/WEB-INF/views/404.jsp").forward(req, resp);
                return;
            }

            populateCheckoutAttributes(req, product);
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading checkout", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long idProduk = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        int qty = FormatUtil.parseIntOrDefault(req.getParameter("qty"), 1);
        // Only Sumopod QRIS is supported.
        String method = "qris";

        HttpSession session = req.getSession(false);
        Pelanggan currentUser;
        try {
            currentUser = refreshSessionUser(session);
        } catch (SQLException e) {
            throw new ServletException("Failed to refresh user", e);
        }

        Long idPelanggan = currentUser != null ? currentUser.getIdPelanggan() : null;
        String buyerName = currentUser != null
                ? currentUser.getNamaPelanggan()
                : FormatUtil.trimOrEmpty(req.getParameter("buyer_name"));
        String buyerEmail = currentUser != null
                ? currentUser.getGmailPelanggan()
                : FormatUtil.trimOrEmpty(req.getParameter("buyer_email"));
        int discountPercent = currentUser != null ? currentUser.getDiskonLevel() : 0;

        if (currentUser == null) {
            qty = 1;
        }

        if (idProduk <= 0 || buyerName.isEmpty() || buyerEmail.isEmpty()) {
            req.setAttribute("error", "Data pembeli dan produk wajib diisi.");
            doGetWithId(req, resp, idProduk);
            return;
        }

        try {
            long idTrx = transaksiDAO.createCheckout(
                    idPelanggan, buyerName, buyerEmail, idProduk, qty, method, discountPercent);
            if (session != null && idPelanggan != null) {
                session.setAttribute("user", pelangganDAO.findById(idPelanggan));
            }
            Transaksi created = transaksiDAO.findById(idTrx);
            if (created != null && created.getInvoice() != null
                    && created.getInvoice().getExternalId() != null
                    && !created.getInvoice().getExternalId().isEmpty()) {
                String encodedOrderId = URLEncoder.encode(created.getInvoice().getExternalId(), StandardCharsets.UTF_8);
                resp.sendRedirect(req.getContextPath() + "/invoice?order_id=" + encodedOrderId + "&created=1");
            } else {
                resp.sendRedirect(req.getContextPath() + "/invoice?id=" + idTrx + "&created=1");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            req.setAttribute("error", e.getMessage());
            doGetWithId(req, resp, idProduk);
        } catch (SQLException e) {
            throw new ServletException("Database error creating checkout", e);
        }
    }

    private void doGetWithId(HttpServletRequest req, HttpServletResponse resp, long idProduk)
            throws ServletException, IOException {
        try {
            Produk product = produkDAO.findById(idProduk);
            if (product == null) {
                resp.sendRedirect(req.getContextPath() + "/home");
                return;
            }
            populateCheckoutAttributes(req, product);
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void populateCheckoutAttributes(HttpServletRequest req, Produk product) throws SQLException {
        Pelanggan currentUser = refreshSessionUser(req.getSession(false));
        int discountPercent = currentUser != null ? currentUser.getDiskonLevel() : 0;
        long effectivePrice = product.getHargaProduk();
        if (discountPercent > 0 && discountPercent < 100) {
            effectivePrice = Math.round(product.getHargaProduk() * (100.0 - discountPercent) / 100.0);
        }

        req.setAttribute("product", product);
        req.setAttribute("discountPercent", discountPercent);
        req.setAttribute("effectivePrice", effectivePrice);
        req.setAttribute("sumopodConfigured", PaymentConfig.isSumopodConfigured());
        req.setAttribute("sumopodSandbox", PaymentConfig.isSandbox());
        req.setAttribute("debugMode", PaymentConfig.isDebugMode());
        req.setAttribute("pageTitle", product.getNamaProduk() + " | Checkout");
    }

    private Pelanggan refreshSessionUser(HttpSession session) throws SQLException {
        if (session == null) {
            return null;
        }
        Object obj = session.getAttribute("user");
        if (!(obj instanceof Pelanggan)) {
            return null;
        }
        Pelanggan fresh = pelangganDAO.findById(((Pelanggan) obj).getIdPelanggan());
        if (fresh != null) {
            session.setAttribute("user", fresh);
        }
        return fresh;
    }
}
