package urms.util;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Application-wide configuration and environment path resolution.
 */
public final class AppConfig {

    private static final String DB_FILENAME = "resourceregister.db";

    private AppConfig() {
        // Utility class - prevent instantiation
    }

    /**
     * Resolves the SQLite database file path reliably.
     * Uses the current working directory to ensure consistent behavior across teammates' machines.
     */
    public static String getDatabaseUrl() {
        Path dbPath = Paths.get(System.getProperty("user.dir"), DB_FILENAME).toAbsolutePath().normalize();
        return "jdbc:sqlite:" + dbPath.toString().replace("\\", "/");
    }

    public static String getDbFileName() {
        return DB_FILENAME;
    }
}
