package com.zstore.servlet;

import com.google.gson.Gson;
import com.zstore.dao.TransaksiDAO;
import com.zstore.payment.SumopodClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Receives Sumopod payment settlement webhooks at POST /webhooks/sumopod.
 */
public class SumopodWebhookServlet extends HttpServlet {

    private static final int MAX_BODY_BYTES = 1 << 20;
    private static final Gson GSON = new Gson();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String token = req.getHeader("X-Webhook-Token");
        if (!SumopodClient.verifyWebhookToken(token)) {
            writeJson(resp, HttpServletResponse.SC_UNAUTHORIZED, "error", "Token webhook tidak valid");
            return;
        }

        String body;
        try {
            body = readLimitedBody(req.getInputStream(), MAX_BODY_BYTES);
        } catch (IllegalStateException e) {
            writeJson(resp, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "error", "Payload terlalu besar");
            return;
        }

        SumopodClient.PaymentEvent event;
        try {
            event = SumopodClient.parseWebhook(body);
        } catch (Exception e) {
            writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "Format webhook tidak valid");
            return;
        }

        if (event.isSettled()) {
            if (event.getOrderId() == null || event.getOrderId().isEmpty()) {
                writeJson(resp, HttpServletResponse.SC_BAD_REQUEST, "error", "order_id wajib diisi");
                return;
            }
            try {
                transaksiDAO.applyPaymentEvent(event);
            } catch (SQLException e) {
                writeJson(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "error", "Database error");
                return;
            }
        }

        writeJson(resp, HttpServletResponse.SC_OK, "success", "ok");
    }

    private static String readLimitedBody(InputStream in, int maxBytes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > maxBytes) {
                throw new IllegalStateException("Body exceeds limit");
            }
            out.write(buf, 0, n);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private static void writeJson(HttpServletResponse resp, int status, String statusVal, String msg)
            throws IOException {
        resp.setStatus(status);
        Map<String, String> payload = new HashMap<>();
        payload.put("status", statusVal);
        payload.put("message", msg);
        resp.getWriter().write(GSON.toJson(payload));
    }
}

