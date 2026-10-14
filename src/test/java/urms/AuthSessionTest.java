package urms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import urms.dao.DatabaseConnection;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class AuthSessionTest {

    @BeforeEach
    public void setup() {
        DatabaseConnection.initializeDatabase();
        DatabaseConnection.clearSession();
    }

    @Test
    public void testSessionLifecycle() throws SQLException {
        // 1. Initial state: No active session
        assertNull(DatabaseConnection.getActiveSessionUser(), "Should be null when no active session");

        // 2. Authenticate admin credentials
        String authenticatedName = DatabaseConnection.authenticate("admin", "admin123");
        assertNotNull(authenticatedName, "Authentication should succeed for default admin credentials");
        assertEquals("System Administrator", authenticatedName);

        // 3. Create persistent session (7-day token)
        String token = DatabaseConnection.createSession("admin");
        assertNotNull(token, "Session token should not be null");
        assertFalse(token.isBlank(), "Session token should not be blank");

        // 4. Auto-login check: active unexpired session should return user name
        String activeUser = DatabaseConnection.getActiveSessionUser();
        assertNotNull(activeUser, "Active session user should be found");
        assertEquals("System Administrator", activeUser);

        // 5. Logout / clear session
        DatabaseConnection.clearSession();
        assertNull(DatabaseConnection.getActiveSessionUser(), "Session should be null after clearSession()");
    }
}
