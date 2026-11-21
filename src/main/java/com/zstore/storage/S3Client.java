package com.zstore.storage;

import com.zstore.config.S3Config;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Part;

/**
 * Lightweight pure-Java AWS SigV4 path-style S3 client for Neon Object Storage.
 * Uses HttpURLConnection so Tomcat reloads cleanly without leaking selector threads.
 */
public class S3Client {

    private static final long MAX_IMAGE_BYTES = 5L * 1024L * 1024L;
    private static final int TIMEOUT_MS = 20000;
    private static final DateTimeFormatter AMZ_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final DateTimeFormatter DATE_STAMP_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("png", "jpg", "jpeg", "webp", "gif")
    );

    public boolean isConfigured() {
        return S3Config.isConfigured();
    }

    /**
     * Validates an uploaded image Part and stores it in Neon S3 under the specified folder.
     */
    public String uploadImagePart(Part part, String folder) throws IOException {
        if (part == null || part.getSize() <= 0) {
            return null;
        }
        if (!isConfigured()) {
            throw new IOException("Neon S3 belum dikonfigurasi. Periksa variabel AWS_ENDPOINT_URL_S3 dan kredensial S3.");
        }
        if (part.getSize() > MAX_IMAGE_BYTES) {
            throw new IOException("Ukuran gambar maksimal 5 MB.");
        }

        String submitted = extractSubmittedFileName(part);
        String ext = extractExtension(submitted);
        String contentType = part.getContentType();

        if (ext.isEmpty()) {
            ext = extensionFromContentType(contentType);
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IOException("Format gambar harus PNG, JPG, JPEG, WEBP, atau GIF.");
        }
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            contentType = contentTypeForExtension(ext);
        }

        byte[] data = readPartBytes(part);
        if (data.length == 0) {
            return null;
        }
        if (data.length > MAX_IMAGE_BYTES) {
            throw new IOException("Ukuran gambar maksimal 5 MB.");
        }

        String safeBase = sanitizeBaseName(submitted, ext);
        String uniquePrefix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String cleanFolder = folder == null ? "uploads" : folder.replaceAll("^/+|/+$", "");
        String key = cleanFolder + "/" + uniquePrefix + "-" + safeBase + "." + ext;

        return uploadBytes(key, data, contentType);
    }

    /**
     * Uploads raw bytes to the configured bucket and returns the public path-style URL.
     */
    public String uploadBytes(String objectKey, byte[] data, String contentType) throws IOException {
        if (!isConfigured()) {
            throw new IOException("Neon S3 tidak dikonfigurasi.");
        }
        String normalizedKey = objectKey.replace('\\', '/').replaceAll("^/+", "");
        String encodedPath = "/" + encodeSegment(S3Config.getBucket()) + "/" + encodeKey(normalizedKey);
        String ct = (contentType == null || contentType.trim().isEmpty())
                ? "application/octet-stream"
                : contentType.trim();

        HttpResult res = sendSignedRequest("PUT", encodedPath, data, ct);
        if (res.statusCode < 200 || res.statusCode >= 300) {
            throw new IOException("Upload ke Neon S3 gagal (HTTP " + res.statusCode + "): " + res.body);
        }
        return S3Config.getEndpoint() + encodedPath;
    }

    /**
     * Deletes an object if the URL points to our configured Neon S3 bucket.
     * Best-effort: does not throw if object is missing or URL is external/local.
     */
    public void deleteByUrl(String url) {
        if (url == null || url.trim().isEmpty() || !isConfigured()) {
            return;
        }
        String basePrefix = S3Config.getPublicBaseUrl() + "/";
        if (!url.startsWith(basePrefix)) {
            return;
        }
        // Keep seeded shared catalog backup images unless explicitly replaced in uploads/avatars/products/providers
        String relativeEncodedPath = "/" + encodeSegment(S3Config.getBucket()) + "/"
                + url.substring(basePrefix.length());
        try {
            sendSignedRequest("DELETE", relativeEncodedPath, new byte[0], null);
        } catch (Exception ignored) {
            // Best effort cleanup only.
        }
    }

    private HttpResult sendSignedRequest(String method, String canonicalPath, byte[] body, String contentType)
            throws IOException {
        try {
            URL url = new URL(S3Config.getEndpoint() + canonicalPath);
            String host = url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String amzDate = AMZ_DATE_FMT.format(now);
            String dateStamp = DATE_STAMP_FMT.format(now);
            String payloadHash = sha256Hex(body);

            String canonicalHeaders = "host:" + host + "\n"
                    + "x-amz-content-sha256:" + payloadHash + "\n"
                    + "x-amz-date:" + amzDate + "\n";
            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";
            String canonicalRequest = method + "\n"
                    + canonicalPath + "\n"
                    + "\n"
                    + canonicalHeaders + "\n"
                    + signedHeaders + "\n"
                    + payloadHash;

            String region = S3Config.getRegion();
            String credentialScope = dateStamp + "/" + region + "/s3/aws4_request";
            String stringToSign = "AWS4-HMAC-SHA256\n"
                    + amzDate + "\n"
                    + credentialScope + "\n"
                    + sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            byte[] signingKey = getSignatureKey(S3Config.getSecretKey(), dateStamp, region, "s3");
            String signature = toHex(hmacSha256(signingKey, stringToSign));
            String authorization = "AWS4-HMAC-SHA256 Credential="
                    + S3Config.getAccessKey() + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders
                    + ", Signature=" + signature;

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestProperty("Host", host);
            conn.setRequestProperty("x-amz-date", amzDate);
            conn.setRequestProperty("x-amz-content-sha256", payloadHash);
            conn.setRequestProperty("Authorization", authorization);
            if (contentType != null && !contentType.isEmpty()) {
                conn.setRequestProperty("Content-Type", contentType);
            }
            if ("PUT".equals(method)) {
                conn.setRequestProperty("Cache-Control", "public, max-age=31536000");
                conn.setDoOutput(true);
                conn.setFixedLengthStreamingMode(body.length);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                }
            }

            int code = conn.getResponseCode();
            InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String respBody = readStreamToString(stream);
            conn.disconnect();
            return new HttpResult(code, respBody);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Gagal menandatangani permintaan S3: " + e.getMessage(), e);
        }
    }

    private static byte[] readPartBytes(Part part) throws IOException {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            long total = 0;
            while ((n = is.read(buf)) != -1) {
                total += n;
                if (total > MAX_IMAGE_BYTES) {
                    throw new IOException("Ukuran gambar maksimal 5 MB.");
                }
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }

    private static String extractSubmittedFileName(Part part) {
        String name = part.getSubmittedFileName();
        if (name == null) {
            return "";
        }
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return slash >= 0 ? name.substring(slash + 1).trim() : name.trim();
    }

    private static String extractExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String sanitizeBaseName(String filename, String ext) {
        String base = filename;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        base = base.replaceAll("[^A-Za-z0-9._-]+", "_").replaceAll("^_+|_+$", "");
        if (base.isEmpty()) {
            base = "image";
        }
        if (base.length() > 60) {
            base = base.substring(0, 60);
        }
        return base;
    }

    private static String extensionFromContentType(String ct) {
        if (ct == null) {
            return "";
        }
        String low = ct.toLowerCase(Locale.ROOT);
        if (low.contains("png")) return "png";
        if (low.contains("jpeg") || low.contains("jpg")) return "jpg";
        if (low.contains("webp")) return "webp";
        if (low.contains("gif")) return "gif";
        return "";
    }

    public static String contentTypeForExtension(String ext) {
        if (ext == null) {
            return "application/octet-stream";
        }
        switch (ext.toLowerCase(Locale.ROOT)) {
            case "png": return "image/png";
            case "jpg":
            case "jpeg": return "image/jpeg";
            case "webp": return "image/webp";
            case "gif": return "image/gif";
            case "svg": return "image/svg+xml";
            default: return "application/octet-stream";
        }
    }

    public static String encodeKey(String key) {
        String[] parts = key.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append('/');
            }
            sb.append(encodeSegment(parts[i]));
        }
        return sb.toString();
    }

    private static String encodeSegment(String segment) {
        byte[] bytes = segment.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int c = b & 0xFF;
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append((char) c);
            } else {
                sb.append(String.format(Locale.ROOT, "%%%02X", c));
            }
        }
        return sb.toString();
    }

    private static String readStreamToString(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return toHex(md.digest(data));
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] getSignatureKey(String secretKey, String dateStamp, String region, String service)
            throws Exception {
        byte[] kSecret = ("AWS4" + secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, dateStamp);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, service);
        return hmacSha256(kService, "aws4_request");
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format(Locale.ROOT, "%02x", b));
        }
        return sb.toString();
    }

    private static final class HttpResult {
        final int statusCode;
        final String body;

        HttpResult(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }
    }
}

