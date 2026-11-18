package com.zstore.payment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zstore.config.PaymentConfig;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
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
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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

    public PaymentResult createPayment(String orderId, long amount, int expiresInHours)
            throws IOException, InterruptedException {
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

        String jsonBody = GSON.toJson(payload);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(PaymentConfig.getBaseUrl() + "/api/v1/payments"))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/json")
                .header("X-Api-Key", PaymentConfig.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (res.statusCode() < 200 || res.statusCode() > 299) {
            throw new IOException("Sumopod API error (" + res.statusCode() + "): " + res.body());
        }

        JsonObject root = JsonParser.parseString(res.body()).getAsJsonObject();
        JsonObject target = pickPaymentObject(root);
        if (target == null) {
            throw new IOException("Sumopod response missing payment id or payment_link_url: " + res.body());
        }

        String id = firstNonBlank(getString(target, "payment_id"), getString(target, "id"));
        String link = getString(target, "payment_link_url");
        long respAmount = getLong(target, "amount", amount);
        int fee = (int) getLong(target, "fee", 0);
        long netAmount = getLong(target, "net_amount", 0);
        String status = getString(target, "status");
        Timestamp expiresAt = parseIsoTimestamp(getString(target, "expires_at"));

        return new PaymentResult(id, link, respAmount, fee, netAmount, status, expiresAt);
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
