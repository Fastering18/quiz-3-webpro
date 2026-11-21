package com.zstore.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Formatting and string helpers for JSP views and servlets.
 */
public final class FormatUtil {

    private static final Locale ID_LOCALE = new Locale("id", "ID");

    private FormatUtil() {
    }

    public static String formatRupiah(long amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(ID_LOCALE);
        nf.setMaximumFractionDigits(0);
        return "Rp " + nf.format(amount);
    }

    public static String toSlug(String name) {
        if (name == null) {
            return "";
        }
        return name.trim().replace(" ", "-");
    }

    public static long parseLongOrDefault(String value, long defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static int parseIntOrDefault(String value, int defaultVal) {
        if (value == null || value.trim().isEmpty()) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static String resolveImageUrl(String contextPath, String path, String fallbackPath) {
        String ctx = contextPath == null ? "" : contextPath;
        String trimmed = trimOrEmpty(path);
        if (trimmed.isEmpty()) {
            return fallbackPath == null || fallbackPath.isEmpty() ? "" : ctx + fallbackPath;
        }
        String low = trimmed.toLowerCase(Locale.ROOT);
        if (low.startsWith("http://") || low.startsWith("https://") || low.startsWith("data:")) {
            return trimmed;
        }
        return ctx + (trimmed.startsWith("/") ? trimmed : "/" + trimmed);
    }

    public static String extractFileName(String pathOrUrl) {
        String trimmed = trimOrEmpty(pathOrUrl);
        if (trimmed.isEmpty()) {
            return "";
        }
        int q = trimmed.indexOf('?');
        if (q >= 0) {
            trimmed = trimmed.substring(0, q);
        }
        int slash = Math.max(trimmed.lastIndexOf('/'), trimmed.lastIndexOf('\\'));
        String name = slash >= 0 ? trimmed.substring(slash + 1) : trimmed;
        try {
            name = URLDecoder.decode(name, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            // Keep raw filename if not percent-encoded.
        }
        if (name.matches("^[0-9a-f]{8}-.+")) {
            name = name.substring(9);
        }
        return name;
    }

    public static List<String> splitCommaTags(String raw) {
        List<String> result = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return result;
        }
        Map<String, String> unique = new LinkedHashMap<>();
        String[] parts = raw.split(",");
        for (String part : parts) {
            String cleaned = part.trim().replaceAll("\\s+", " ");
            if (!cleaned.isEmpty()) {
                if (cleaned.length() > 200) {
                    cleaned = cleaned.substring(0, 200);
                }
                String key = cleaned.toLowerCase(Locale.ROOT);
                unique.putIfAbsent(key, cleaned);
            }
        }
        result.addAll(unique.values());
        return result;
    }
}
