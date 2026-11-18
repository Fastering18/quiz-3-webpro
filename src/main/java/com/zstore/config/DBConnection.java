package com.zstore.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages MySQL JDBC connections using environment variables with fallback to db.properties.
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
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException ignored) {
            // Fall back to defaults and environment variables.
        }

        String envUrl = System.getenv("DB_URL");
        String envHost = System.getenv("DB_HOST");
        String envPort = System.getenv("DB_PORT");
        String envName = System.getenv("DB_NAME");

        if (envUrl != null && !envUrl.isEmpty()) {
            url = envUrl;
        } else if (envHost != null && !envHost.isEmpty()) {
            String port = (envPort != null && !envPort.isEmpty()) ? envPort : "3306";
            String name = (envName != null && !envName.isEmpty()) ? envName : "zstore_jsp";
            url = "jdbc:mysql://" + envHost + ":" + port + "/" + name
                    + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
        } else {
            url = props.getProperty("db.url", DEFAULT_URL);
        }

        String envUser = System.getenv("DB_USER");
        user = (envUser != null && !envUser.isEmpty()) ? envUser : props.getProperty("db.user", DEFAULT_USER);

        String envPass = System.getenv("DB_PASSWORD");
        password = (envPass != null) ? envPass : props.getProperty("db.password", DEFAULT_PASS);
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

