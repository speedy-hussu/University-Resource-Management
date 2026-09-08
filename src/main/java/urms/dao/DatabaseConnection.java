package urms.dao;

import urms.util.AppConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * SQLite Database Connection and Schema Initialization Manager.
 */
public class DatabaseConnection {

    /**
     * Obtains a connection to the SQLite database.
     * Enforces foreign key support.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(AppConfig.getDatabaseUrl());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    /**
     * Initializes database tables and demo seed data.
     * Throws an unchecked RuntimeException on failure to prevent running against an invalid database.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            System.out.println("Connecting to database: " + AppConfig.getDatabaseUrl());

            // 1. Execute schema.sql
            executeSqlScript(conn, "/db/schema.sql");
            System.out.println("Database schema verified successfully.");

            // 2. Execute seed.sql
            executeSqlScript(conn, "/db/seed.sql");
            System.out.println("Database demo seed data verified successfully.");

        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Database initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Helper to read and execute a SQL script from resources.
     */
    private static void executeSqlScript(Connection conn, String resourcePath) throws Exception {
        try (InputStream is = DatabaseConnection.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Required SQL resource not found on classpath: " + resourcePath);
            }

            String content = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // Strip full-line comments and execute statement by statement
            StringBuilder currentStatement = new StringBuilder();
            try (Statement stmt = conn.createStatement()) {
                for (String line : content.split("\n")) {
                    String trimmed = line.trim();
                    if (trimmed.startsWith("--") || trimmed.isEmpty()) {
                        continue;
                    }
                    currentStatement.append(line).append("\n");
                    if (trimmed.endsWith(";")) {
                        String query = currentStatement.toString().trim();
                        if (!query.isEmpty()) {
                            stmt.execute(query);
                        }
                        currentStatement.setLength(0);
                    }
                }
            }
        }
    }
}
