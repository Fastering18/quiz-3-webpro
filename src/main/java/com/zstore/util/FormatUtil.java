package com.zstore.util;

import java.text.NumberFormat;
import java.util.Locale;

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
}

