package com.zstore.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Neon Object Storage (S3-compatible) settings from OS environment variables,
 * local .env file, or optional s3.local.properties.
 */
public final class S3Config {

    private static final String DEFAULT_REGION = "ap-southeast-1";
    private static final String DEFAULT_BUCKET = "zapstore";

    private static String endpoint = "";
    private static String accessKey = "";
    private static String secretKey = "";
    private static String region = DEFAULT_REGION;
    private static String bucket = DEFAULT_BUCKET;

    static {
        load();
    }

    private S3Config() {
    }

    private static void load() {
        Properties props = new Properties();
        loadResource(props, "s3.properties");
        loadResource(props, "s3.local.properties");

        endpoint = firstNonEmpty(
                EnvLoader.get("AWS_ENDPOINT_URL_S3"),
                props.getProperty("s3.endpoint"),
                ""
        );
        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }

        accessKey = firstNonEmpty(
                EnvLoader.get("AWS_ACCESS_KEY_ID"),
                props.getProperty("s3.access_key_id"),
                ""
        );

        secretKey = firstNonEmpty(
                EnvLoader.get("AWS_SECRET_ACCESS_KEY"),
                props.getProperty("s3.secret_access_key"),
                ""
        );

        region = firstNonEmpty(
                EnvLoader.get("AWS_REGION"),
                props.getProperty("s3.region"),
                DEFAULT_REGION
        );

        bucket = firstNonEmpty(
                EnvLoader.get("AWS_S3_BUCKET"),
                props.getProperty("s3.bucket"),
                DEFAULT_BUCKET
        );
    }

    private static void loadResource(Properties props, String name) {
        try (InputStream in = S3Config.class.getClassLoader().getResourceAsStream(name)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // Optional properties file.
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

    public static String getEndpoint() {
        return endpoint;
    }

    public static String getAccessKey() {
        return accessKey;
    }

    public static String getSecretKey() {
        return secretKey;
    }

    public static String getRegion() {
        return region;
    }

    public static String getBucket() {
        return bucket;
    }

    public static boolean isConfigured() {
        return !endpoint.isEmpty() && !accessKey.isEmpty() && !secretKey.isEmpty() && !bucket.isEmpty();
    }

    public static String getPublicBaseUrl() {
        return endpoint + "/" + bucket;
    }
}

