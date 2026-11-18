package com.zstore.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves configuration values from OS environment variables first,
 * with fallback to a local .env file when running locally.
 */
public final class EnvLoader {

    private static final Map<String, String> DOTENV = loadDotEnv();

    private EnvLoader() {
    }

    public static String get(String key) {
        String sys = System.getenv(key);
        if (sys != null) {
            return sys;
        }
        return DOTENV.get(key);
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        File envFile = findDotEnv();
        if (envFile == null) {
            return map;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(envFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (trimmed.startsWith("export ")) {
                    trimmed = trimmed.substring(7).trim();
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String k = trimmed.substring(0, eq).trim();
                String v = trimmed.substring(eq + 1).trim();
                if ((v.startsWith("\"") && v.endsWith("\"") && v.length() >= 2)
                        || (v.startsWith("'") && v.endsWith("'") && v.length() >= 2)) {
                    v = v.substring(1, v.length() - 1);
                }
                map.put(k, v);
            }
        } catch (IOException ignored) {
            // Optional local .env file.
        }
        return map;
    }

    private static File findDotEnv() {
        String[] candidates = {
            ".env",
            "../.env",
            System.getProperty("catalina.base", "") + File.separator + ".env"
        };
        for (String path : candidates) {
            if (path == null || path.isEmpty()) {
                continue;
            }
            File f = new File(path);
            if (f.isFile()) {
                return f;
            }
        }
        return null;
    }
}

