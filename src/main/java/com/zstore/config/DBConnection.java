package com.zstore.config;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Manages MySQL / TiDB JDBC connections with a lightweight in-process connection pool.
 * Reads environment variables (.env or OS env) supporting mysql:// URIs and jdbc:mysql:// URLs,
 * with fallback to db.properties.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/zstore_jsp?useUnicode=true&characterEncoding=UTF-8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private static final int MAX_POOL_SIZE = 8;
    private static final long MAX_IDLE_MS = 4L * 60L * 1000L;
    private static final BlockingQueue<PooledEntry> POOL = new ArrayBlockingQueue<>(MAX_POOL_SIZE);

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
                + "&serverTimezone=Asia/Jakarta"
                + "&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048"
                + "&tcpKeepAlive=true&connectTimeout=10000&socketTimeout=30000";
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

    /**
     * Borrows a validated physical connection from the pool (or opens a new one if none is idle)
     * and wraps it so calling close() returns it cleanly to the pool.
     */
    public static Connection getConnection() throws SQLException {
        long now = System.currentTimeMillis();
        PooledEntry entry;
        while ((entry = POOL.poll()) != null) {
            if (now - entry.lastUsedMs > MAX_IDLE_MS) {
                closeQuietly(entry.physical);
                continue;
            }
            if (isHealthy(entry.physical)) {
                return wrap(entry.physical);
            }
            closeQuietly(entry.physical);
        }
        Connection fresh = DriverManager.getConnection(url, user, password);
        return wrap(fresh);
    }

    /**
     * Closes all idle pooled connections (called on servlet context shutdown).
     */
    public static void shutdownPool() {
        PooledEntry entry;
        while ((entry = POOL.poll()) != null) {
            closeQuietly(entry.physical);
        }
    }

    private static Connection wrap(Connection physical) {
        return (Connection) Proxy.newProxyInstance(
                DBConnection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(physical)
        );
    }

    private static void release(Connection physical) {
        if (physical == null) {
            return;
        }
        try {
            if (physical.isClosed()) {
                return;
            }
            if (!physical.getAutoCommit()) {
                physical.rollback();
                physical.setAutoCommit(true);
            }
            if (!POOL.offer(new PooledEntry(physical, System.currentTimeMillis()))) {
                closeQuietly(physical);
            }
        } catch (SQLException e) {
            closeQuietly(physical);
        }
    }

    private static boolean isHealthy(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignored) {
                // Best effort cleanup.
            }
        }
    }

    private static final class PooledEntry {
        final Connection physical;
        final long lastUsedMs;

        PooledEntry(Connection physical, long lastUsedMs) {
            this.physical = physical;
            this.lastUsedMs = lastUsedMs;
        }
    }

    private static final class PooledConnectionHandler implements InvocationHandler {
        private final Connection physical;
        private boolean closed = false;

        PooledConnectionHandler(Connection physical) {
            this.physical = physical;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                if (!closed) {
                    closed = true;
                    release(physical);
                }
                return null;
            }
            if ("isClosed".equals(name)) {
                return closed || physical.isClosed();
            }
            if (closed) {
                throw new SQLException("Connection is already closed");
            }
            try {
                return method.invoke(physical, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
