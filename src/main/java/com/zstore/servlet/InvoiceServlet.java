package com.zstore.servlet;

import com.google.gson.Gson;
import com.zstore.config.PaymentConfig;
import com.zstore.dao.PelangganDAO;
import com.zstore.dao.TransaksiDAO;
import com.zstore.model.Pelanggan;
import com.zstore.model.Transaksi;
import com.zstore.payment.SumopodClient;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class InvoiceServlet extends HttpServlet {

    private static final Gson GSON = new Gson();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long trxId = FormatUtil.parseLongOrDefault(req.getParameter("id"), 0);
        String format = FormatUtil.trimOrEmpty(req.getParameter("format"));

        if (trxId <= 0) {
            req.setAttribute("pageTitle", "Cari Invoice | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/invoice.jsp").forward(req, resp);
            return;
        }

        try {
            Transaksi trx = transaksiDAO.findById(trxId);
            if ("json".equalsIgnoreCase(format)) {
                resp.setContentType("application/json; charset=UTF-8");
                Map<String, Object> out = new HashMap<>();
                if (trx == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.put("status", "not_found");
                } else {
                    out.put("id_trx", trx.getIdTrx());
                    out.put("status", trx.getStatus());
                    out.put("invoice_status", trx.getInvoice() != null ? trx.getInvoice().getStatus() : "");
                }
                resp.getWriter().write(GSON.toJson(out));
                return;
            }

            if (trx == null) {
                req.setAttribute("error", "Invoice dengan ID #" + trxId + " tidak ditemukan.");
            } else {
                req.setAttribute("trx", trx);
            }
            req.setAttribute("debugMode", PaymentConfig.isDebugMode());
            req.setAttribute("sumopodConfigured", PaymentConfig.isSumopodConfigured());
            req.setAttribute("sumopodSandbox", PaymentConfig.isSandbox());
            req.setAttribute("pageTitle", "Invoice #" + trxId + " | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/invoice.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading invoice", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long trxId = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
        String action = FormatUtil.trimOrEmpty(req.getParameter("action"));

        if (trxId <= 0) {
            resp.sendRedirect(req.getContextPath() + "/invoice");
            return;
        }

        try {
            Transaksi existing = transaksiDAO.findById(trxId);
            if (existing == null) {
                resp.sendRedirect(req.getContextPath() + "/invoice?id=" + trxId);
                return;
            }

            if ("confirm".equals(action)) {
                transaksiDAO.confirmPayment(trxId);
                refreshSessionUser(req.getSession(false));
                resp.sendRedirect(req.getContextPath() + "/invoice?id=" + trxId + "&paid=1");
                return;
            } else if ("cancel".equals(action) && !"Completed".equalsIgnoreCase(existing.getStatus())) {
                transaksiDAO.deleteTransaction(trxId);
                resp.sendRedirect(req.getContextPath() + "/invoice?cancelled=1");
                return;
            } else if (PaymentConfig.isDebugMode() && action.startsWith("simulate_")
                    && existing.getInvoice() != null) {
                String orderId = existing.getInvoice().getExternalId();
                String simulatedJson = null;
                if ("simulate_paid".equals(action)) {
                    simulatedJson = "{\"event\":\"payment.completed\",\"order_id\":\"" + orderId + "\"}";
                } else if ("simulate_failed".equals(action)) {
                    simulatedJson = "{\"event\":\"payment.failed\",\"order_id\":\"" + orderId + "\"}";
                } else if ("simulate_expired".equals(action)) {
                    simulatedJson = "{\"event\":\"payment.expired\",\"order_id\":\"" + orderId + "\"}";
                }
                if (simulatedJson != null) {
                    SumopodClient.PaymentEvent ev = SumopodClient.parseWebhook(simulatedJson);
                    transaksiDAO.applyPaymentEvent(ev);
                    refreshSessionUser(req.getSession(false));
                    resp.sendRedirect(req.getContextPath() + "/invoice?id=" + trxId + "&simulated=" + ev.getStatus());
                    return;
                }
            }
            resp.sendRedirect(req.getContextPath() + "/invoice?id=" + trxId);
        } catch (SQLException e) {
            throw new ServletException("Database error updating invoice", e);
        }
    }

    private void refreshSessionUser(HttpSession session) throws SQLException {
        if (session == null) {
            return;
        }
        Object obj = session.getAttribute("user");
        if (obj instanceof Pelanggan) {
            Pelanggan fresh = pelangganDAO.findById(((Pelanggan) obj).getIdPelanggan());
            if (fresh != null) {
                session.setAttribute("user", fresh);
            }
        }
    }
}
