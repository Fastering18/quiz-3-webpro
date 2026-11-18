package com.zstore.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Sumopod sandbox/production payment settings from payment.properties,
 * optional payment.local.properties, and environment variables.
 */
public final class PaymentConfig {

    private static final String DEFAULT_BASE_URL = "https://api-pay-sandbox.sumopod.com";

    private static String baseUrl = DEFAULT_BASE_URL;
    private static String apiKey = "";
    private static String webhookToken = "";
    private static boolean debugMode = true;
    private static int expireMinutes = 60;

    static {
        load();
    }

    private PaymentConfig() {
    }

    private static void load() {
        Properties props = new Properties();
        loadResource(props, "payment.properties");
        loadResource(props, "payment.local.properties");

        baseUrl = firstNonEmpty(
                System.getenv("SUMOPOD_BASE_URL"),
                props.getProperty("sumopod.base_url"),
                DEFAULT_BASE_URL
        );
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        apiKey = firstNonEmpty(
                System.getenv("SUMOPOD_API_KEY"),
                props.getProperty("sumopod.api_key"),
                ""
        );

        webhookToken = firstNonEmpty(
                System.getenv("SUMOPOD_WEBHOOK_TOKEN"),
                props.getProperty("sumopod.webhook_token"),
                ""
        );

        String debugStr = firstNonEmpty(
                System.getenv("PAYMENT_DEBUG_MODE"),
                props.getProperty("payment.debug_mode"),
                "true"
        );
        debugMode = Boolean.parseBoolean(debugStr.trim());

        String expStr = firstNonEmpty(
                System.getenv("PAYMENT_EXPIRE_MINUTES"),
                props.getProperty("payment.expire_minutes"),
                "60"
        );
        try {
            int parsed = Integer.parseInt(expStr.trim());
            if (parsed > 0) {
                expireMinutes = parsed;
            }
        } catch (NumberFormatException ignored) {
            expireMinutes = 60;
        }
    }

    private static void loadResource(Properties props, String name) {
        try (InputStream in = PaymentConfig.class.getClassLoader().getResourceAsStream(name)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // Ignore missing optional local override.
        }
    }

    private static String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return "";
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getWebhookToken() {
        return webhookToken;
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static int getExpireMinutes() {
        return expireMinutes;
    }

    public static boolean isSumopodConfigured() {
        return !apiKey.isEmpty();
    }

    public static boolean isSandbox() {
        return baseUrl.toLowerCase().contains("sandbox");
    }
}

