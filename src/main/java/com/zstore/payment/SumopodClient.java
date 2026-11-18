package com.zstore.payment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zstore.config.PaymentConfig;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Minimal client and webhook parser for Sumopod Managed Payment (QRIS).
 * Ported from ilits27-backend/pkg/sumopod.
 */
public class SumopodClient {

    public static final String PROVIDER_NAME = "SUMOPOD";
    public static final String PAYMENT_METHOD_QRIS = "QRIS";

    private static final Gson GSON = new Gson();
    private static final int TIMEOUT_MS = 10000;

    public static class PaymentResult {
        private final String paymentId;
        private final String paymentLinkUrl;
        private final long amount;
        private final int fee;
        private final long netAmount;
        private final String status;
        private final Timestamp expiresAt;

        public PaymentResult(String paymentId, String paymentLinkUrl, long amount,
                             int fee, long netAmount, String status, Timestamp expiresAt) {
            this.paymentId = paymentId;
            this.paymentLinkUrl = paymentLinkUrl;
            this.amount = amount;
            this.fee = fee;
            this.netAmount = netAmount;
            this.status = status;
            this.expiresAt = expiresAt;
        }

        public String getPaymentId() { return paymentId; }
        public String getPaymentLinkUrl() { return paymentLinkUrl; }
        public long getAmount() { return amount; }
        public int getFee() { return fee; }
        public long getNetAmount() { return netAmount; }
        public String getStatus() { return status; }
        public Timestamp getExpiresAt() { return expiresAt; }
    }

    public static class PaymentEvent {
        private final String orderId;
        private final String status;
        private final Long netAmount;
        private final Timestamp paidAt;
        private final boolean settled;

        public PaymentEvent(String orderId, String status, Long netAmount, Timestamp paidAt, boolean settled) {
            this.orderId = orderId;
            this.status = status;
            this.netAmount = netAmount;
            this.paidAt = paidAt;
            this.settled = settled;
        }

        public String getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public Long getNetAmount() { return netAmount; }
        public Timestamp getPaidAt() { return paidAt; }
        public boolean isSettled() { return settled; }
    }

    public boolean isConfigured() {
        return PaymentConfig.isSumopodConfigured();
    }

    public PaymentResult createPayment(String orderId, long amount, int expiresInHours) throws IOException {
        if (!PaymentConfig.isSumopodConfigured()) {
            throw new IllegalStateException("SUMOPOD_API_KEY belum dikonfigurasi");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", orderId);
        payload.put("amount", amount);
        payload.put("currency", "IDR");
        payload.put("payment_method_type_code", PAYMENT_METHOD_QRIS);
        if (expiresInHours > 0) {
            payload.put("expires_in_hours", Math.min(24, expiresInHours));
        }

        byte[] bodyBytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        URL url = new URL(PaymentConfig.getBaseUrl() + "/api/v1/payments");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-Api-Key", PaymentConfig.getApiKey());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyBytes);
            }

            int status = conn.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            String respBody = readAll(stream);
            if (status < 200 || status > 299) {
                throw new IOException("Sumopod API error (" + status + "): " + respBody);
            }

            JsonObject root = JsonParser.parseString(respBody).getAsJsonObject();
            JsonObject target = pickPaymentObject(root);
            if (target == null) {
                throw new IOException("Sumopod response missing payment id or payment_link_url: " + respBody);
            }

            String id = firstNonBlank(getString(target, "payment_id"), getString(target, "id"));
            String link = getString(target, "payment_link_url");
            long respAmount = getLong(target, "amount", amount);
            int fee = (int) getLong(target, "fee", 0);
            long netAmount = getLong(target, "net_amount", 0);
            String respStatus = getString(target, "status");
            Timestamp expiresAt = parseIsoTimestamp(getString(target, "expires_at"));

            return new PaymentResult(id, link, respAmount, fee, netAmount, respStatus, expiresAt);
        } finally {
            conn.disconnect();
        }
    }

    public static boolean verifyWebhookToken(String headerToken) {
        String expected = PaymentConfig.getWebhookToken();
        if (expected == null || expected.isEmpty() || headerToken == null) {
            return false;
        }
        return MessageDigest.isEqual(
                headerToken.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)
        );
    }

    public static PaymentEvent parseWebhook(String rawBody) {
        JsonObject root = JsonParser.parseString(rawBody).getAsJsonObject();
        JsonObject fields = root;
        if (root.has("data") && root.get("data").isJsonObject()) {
            JsonObject dataObj = root.getAsJsonObject("data");
            if (!getString(dataObj, "order_id").isEmpty()) {
                fields = dataObj;
            }
        }

        String orderId = getString(fields, "order_id");
        String event = firstNonBlank(getString(fields, "event"), getString(root, "event"));
        String eventType = firstNonBlank(getString(fields, "event_type"), getString(root, "event_type"));
        String type = firstNonBlank(getString(fields, "type"), getString(root, "type"));
        String status = getString(fields, "status");

        String settled = resolveSettledStatus(event, eventType, type, status);
        if (settled == null) {
            return new PaymentEvent(orderId, "", null, null, false);
        }

        Long netAmount = null;
        if (fields.has("net_amount") && !fields.get("net_amount").isJsonNull()) {
            netAmount = fields.get("net_amount").getAsLong();
        }
        Timestamp paidAt = parseIsoTimestamp(getString(fields, "paid_at"));
        return new PaymentEvent(orderId, settled, netAmount, paidAt, true);
    }

    private static String readAll(InputStream stream) throws IOException {
        if (stream == null) {
            return "";
        }
        try (InputStream in = stream) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            int total = 0;
            while ((n = in.read(buf)) != -1) {
                total += n;
                if (total > (1 << 20)) {
                    break;
                }
                out.write(buf, 0, n);
            }
            return out.toString(StandardCharsets.UTF_8);
        }
    }

    private static String resolveSettledStatus(String... labels) {
        for (String raw : labels) {
            if (raw == null || raw.isEmpty()) {
                continue;
            }
            String label = raw.toLowerCase(Locale.ROOT);
            if (label.contains("complet") || label.contains("paid") || label.contains("success")) {
                return "PAID";
            }
            if (label.contains("fail") || label.contains("cancel")) {
                return "FAILED";
            }
            if (label.contains("expire")) {
                return "EXPIRED";
            }
        }
        return null;
    }

    private static JsonObject pickPaymentObject(JsonObject root) {
        if (root.has("data") && root.get("data").isJsonObject()) {
            JsonObject data = root.getAsJsonObject("data");
            if (hasIdAndLink(data)) {
                return data;
            }
        }
        if (hasIdAndLink(root)) {
            return root;
        }
        return null;
    }

    private static boolean hasIdAndLink(JsonObject obj) {
        String id = firstNonBlank(getString(obj, "payment_id"), getString(obj, "id"));
        String link = getString(obj, "payment_link_url");
        return !id.isEmpty() && !link.isEmpty();
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isEmpty()) {
            return a;
        }
        return b == null ? "" : b;
    }

    private static String getString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return "";
        }
        JsonElement el = obj.get(key);
        return el.isJsonPrimitive() ? el.getAsString() : "";
    }

    private static long getLong(JsonObject obj, String key, long fallback) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return obj.get(key).getAsLong();
        } catch (Exception e) {
            return fallback;
        }
    }

    private static Timestamp parseIsoTimestamp(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Timestamp.from(Instant.parse(raw));
        } catch (Exception ignored) {
            try {
                return Timestamp.from(OffsetDateTime.parse(raw).toInstant());
            } catch (Exception ignored2) {
                return null;
            }
        }
    }
}
