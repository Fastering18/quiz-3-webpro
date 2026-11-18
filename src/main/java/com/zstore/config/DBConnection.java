package com.zstore.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages MySQL / TiDB JDBC connections using environment variables (.env or OS env),
 * supporting both mysql:// URIs and jdbc:mysql:// URLs, with fallback to db.properties.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/zstore_jsp?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver not found in classpath", e);
        }
        loadConfig();
    }

    private DBConnection() {
    }

    private static void loadConfig() {
        Properties props = new Properties();
        loadProps(props, "db.properties");
        loadProps(props, "db.local.properties");

        String rawUrl = firstNonEmpty(
                EnvLoader.get("DB_URL"),
                EnvLoader.get("DATABASE_URL")
        );
        String envHost = EnvLoader.get("DB_HOST");
        String envPort = EnvLoader.get("DB_PORT");
        String envName = EnvLoader.get("DB_NAME");
        String envSsl = EnvLoader.get("DB_SSL");
        String envUser = EnvLoader.get("DB_USER");
        String envPass = EnvLoader.get("DB_PASSWORD");

        if (!rawUrl.isEmpty() && rawUrl.startsWith("mysql://")) {
            applyMysqlUri(rawUrl, envUser, envPass, envSsl);
            return;
        }

        if (!rawUrl.isEmpty()) {
            url = rawUrl;
        } else if (envHost != null && !envHost.trim().isEmpty()) {
            String host = envHost.trim();
            String port = (envPort != null && !envPort.trim().isEmpty()) ? envPort.trim() : "3306";
            String name = (envName != null && !envName.trim().isEmpty()) ? envName.trim() : "zstore_jsp";
            boolean useSsl = "true".equalsIgnoreCase(envSsl)
                    || "4000".equals(port)
                    || host.toLowerCase().contains("tidbcloud.com");
            url = buildJdbcUrl(host, port, name, useSsl);
        } else {
            url = props.getProperty("db.url", DEFAULT_URL);
        }

        user = (envUser != null && !envUser.isEmpty()) ? envUser : props.getProperty("db.user", DEFAULT_USER);
        password = (envPass != null) ? envPass : props.getProperty("db.password", DEFAULT_PASS);
    }

    private static void applyMysqlUri(String rawUri, String overrideUser, String overridePass, String envSsl) {
        URI uri = URI.create(rawUri);
        String host = uri.getHost();
        int portNum = uri.getPort() > 0 ? uri.getPort() : 3306;
        String port = String.valueOf(portNum);
        String path = uri.getPath();
        String dbName = (path != null && path.length() > 1) ? path.substring(1) : "zstore_jsp";

        String uriUser = "";
        String uriPass = "";
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                uriUser = decode(userInfo.substring(0, colon));
                uriPass = decode(userInfo.substring(colon + 1));
            } else {
                uriUser = decode(userInfo);
            }
        }

        boolean useSsl = !"false".equalsIgnoreCase(envSsl)
                && ("true".equalsIgnoreCase(envSsl)
                || portNum == 4000
                || (host != null && host.toLowerCase().contains("tidbcloud.com")));

        url = buildJdbcUrl(host, port, dbName, useSsl);
        user = (overrideUser != null && !overrideUser.isEmpty()) ? overrideUser : uriUser;
        password = (overridePass != null && !overridePass.isEmpty()) ? overridePass : uriPass;
    }

    private static String buildJdbcUrl(String host, String port, String dbName, boolean useSsl) {
        String sslParams = useSsl
                ? "sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3"
                : "useSSL=false&allowPublicKeyRetrieval=true";
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName
                + "?useUnicode=true&characterEncoding=UTF-8&" + sslParams
                + "&serverTimezone=Asia/Jakarta";
    }

    private static void loadProps(Properties props, String resourceName) {
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(resourceName)) {
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

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
