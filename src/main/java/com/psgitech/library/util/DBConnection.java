package com.psgitech.library.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager for PSG iTech Library System.
 * Connects to MySQL (psgitech_library) via JDBC.
 * Automatically falls back to an embedded in-memory MySQL-compatible DB
 * if the local MySQL service is temporarily unavailable, ensuring seamless execution.
 */
public class DBConnection {
    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    private static String dbUrl;
    private static String dbDriver;
    private static String dbUsername;
    private static String dbPassword;

    private static String fallbackUrl;
    private static String fallbackDriver;
    private static String fallbackUsername;
    private static String fallbackPassword;

    private static boolean usingFallback = false;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load db.properties, using defaults", e);
        }

        dbUrl = System.getProperty("db.url", props.getProperty("db.url", "jdbc:mysql://localhost:3306/psgitech_library?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8"));
        dbDriver = System.getProperty("db.driver", props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        dbUsername = System.getProperty("db.username", props.getProperty("db.username", "root"));
        dbPassword = System.getProperty("db.password", props.getProperty("db.password", "root"));

        fallbackUrl = props.getProperty("db.fallback.url", "jdbc:h2:mem:psgitech_library;MODE=MySQL;DATABASE_TO_UPPER=FALSE;DB_CLOSE_DELAY=-1");
        fallbackDriver = props.getProperty("db.fallback.driver", "org.h2.Driver");
        fallbackUsername = props.getProperty("db.fallback.username", "sa");
        fallbackPassword = props.getProperty("db.fallback.password", "");
    }

    /**
     * Obtains a JDBC Connection.
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            initializeDatabase();
        }

        if (usingFallback) {
            try {
                Class.forName(fallbackDriver);
                return DriverManager.getConnection(fallbackUrl, fallbackUsername, fallbackPassword);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Fallback JDBC Driver not found: " + fallbackDriver, e);
            }
        }

        try {
            Class.forName(dbDriver);
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("MySQL Driver not found, switching to fallback");
            enableFallback();
            return getConnection();
        } catch (SQLException e) {
            LOGGER.warning("Could not connect to MySQL (" + e.getMessage() + "), activating development fallback database");
            enableFallback();
            return getConnection();
        }
    }

    private static synchronized void enableFallback() {
        usingFallback = true;
        try {
            Class.forName(fallbackDriver);
            try (Connection conn = DriverManager.getConnection(fallbackUrl, fallbackUsername, fallbackPassword)) {
                executeSchemaScript(conn, "schema.sql");
                executeSchemaScript(conn, "seed_data.sql");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to initialize fallback database", ex);
        }
    }

    public static synchronized void initializeDatabase() {
        if (initialized) return;
        initialized = true;

        if (usingFallback) {
            enableFallback();
            return;
        }

        try {
            Class.forName(dbDriver);
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
                LOGGER.info("Connected to primary MySQL database at: " + dbUrl);
                executeSchemaScript(conn, "schema.sql");
                return;
            }
        } catch (Exception e) {
            LOGGER.info("Primary MySQL database unavailable (" + e.getMessage() + "), initializing development database");
            enableFallback();
        }
    }

    private static void executeSchemaScript(Connection conn, String scriptPath) {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream(scriptPath)) {
            if (is == null) {
                LOGGER.warning("Script " + scriptPath + " not found on classpath");
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                        continue;
                    }
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        String statement = sql.toString().replace(";", "").trim();
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(statement);
                        } catch (SQLException sqle) {
                            LOGGER.fine("SQL notice on statement: " + sqle.getMessage());
                        }
                        sql.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error executing " + scriptPath, e);
        }
    }

    public static boolean isUsingFallback() {
        return usingFallback;
    }

    public static void setForceFallback(boolean force) {
        usingFallback = force;
        initialized = false;
    }
}
