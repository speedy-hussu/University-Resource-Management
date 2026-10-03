# 📋 Task: Single-User Persistent Authentication & Auto-Login (Token Expiry)

---

## 📌 Task Summary
* **Task ID:** `AUTH-01`
* **Title:** Implement Persistent Token-Based Auto-Login for Single-User Desktop App
* **Assignee:** Teammate
* **Priority:** High
* **Estimated Effort:** ~1 - 2 hours

---

## 🎯 Objective
Currently, the operator must enter credentials (`admin` / `admin123`) every single time the application is launched. 

Since URMS is a **single-user, single-device desktop application**, we want to improve the operator's UX by adding **session persistence**:
1. When the admin logs in successfully, generate a unique `session_token` and store it in SQLite with an expiration date (`token_expiry` set to `+7 days`).
2. When the application launches (`Main.java`), check if an unexpired token exists:
   * **If valid:** Automatically launch the main dashboard (`DefaultWindow`), bypassing `LoginWindow`.
   * **If missing or expired:** Open `LoginWindow` as usual.
3. Provide a **Logout** action in the sidebar that nullifies the token and returns the user to the login screen.

---

## 📂 Files to Modify

| File | Path | What to Change |
| :--- | :--- | :--- |
| **`schema.sql`** | `src/main/resources/db/schema.sql` | Add `session_token TEXT` and `token_expiry TEXT` to `Users` table. |
| **`DatabaseConnection.java`** | `src/main/java/urms/dao/DatabaseConnection.java` | Add session creation, validation, and logout methods. |
| **`Main.java`** | `src/main/java/urms/Main.java` | Add session check on launch to decide whether to show `DefaultWindow` or `LoginWindow`. |
| **`LoginWindow.java`** | `src/main/java/urms/ui/LoginWindow.java` | Store token upon successful login. |
| **`SidebarPanel.java`** | `src/main/java/urms/ui/SidebarPanel.java` | Add a Logout button that clears the token and opens `LoginWindow`. |

---

## 🛠️ Step-by-Step Implementation Guide

### Step 1: Update Database Schema
In `src/main/resources/db/schema.sql`, add `session_token` and `token_expiry` to the `Users` table:

```sql
CREATE TABLE IF NOT EXISTS Users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT,
    is_active INTEGER NOT NULL DEFAULT 1,
    session_token TEXT,         -- Unique token once logged in
    token_expiry TEXT,          -- ISO timestamp when token expires
    created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))
);
```

> ⚠️ **Important Local DB Note:**
> Because `resourceregister.db` already exists on your machine, `CREATE TABLE IF NOT EXISTS` will **not** modify an existing table. To apply the change locally:
> * Either delete the local file `resourceregister.db` so the app recreates it fresh on next run,
> * Or execute: `ALTER TABLE Users ADD COLUMN session_token TEXT; ALTER TABLE Users ADD COLUMN token_expiry TEXT;`.

---

### Step 2: Add Database Helper Methods
In `src/main/java/urms/dao/DatabaseConnection.java`, add three helper methods:

```java
import java.util.UUID;

// 1. Create a session token upon successful login (valid for 7 days)
public static String createSession(String username) throws SQLException {
    String token = UUID.randomUUID().toString();
    String sql = "UPDATE Users SET session_token = ?, token_expiry = datetime('now', '+7 days') WHERE username = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setString(1, token);
        pstmt.setString(2, username);
        pstmt.executeUpdate();
    }
    return token;
}

// 2. Validate if an active, unexpired session exists on startup
public static String getActiveSessionUser() {
    String sql = "SELECT full_name FROM Users "
               + "WHERE session_token IS NOT NULL "
               + "  AND datetime(token_expiry) > datetime('now', 'localtime') "
               + "  AND is_active = 1 "
               + "LIMIT 1";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        if (rs.next()) {
            return rs.getString("full_name");
        }
    } catch (SQLException e) {
        System.err.println("Session check error: " + e.getMessage());
    }
    return null; // No valid session found
}

// 3. Clear session upon logout
public static void clearSession() {
    String sql = "UPDATE Users SET session_token = NULL, token_expiry = NULL";
    try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
        stmt.executeUpdate(sql);
    } catch (SQLException e) {
        System.err.println("Logout error: " + e.getMessage());
    }
}
```

---

### Step 3: Check Session on Startup in `Main.java`
Update `src/main/java/urms/Main.java` so it checks the active session before deciding which window to open:

```java
// 3. Launch UI on the Event Dispatch Thread (EDT)
SwingUtilities.invokeLater(() -> {
    String activeUser = DatabaseConnection.getActiveSessionUser();
    if (activeUser != null) {
        // Valid token found! Bypass login
        System.out.println("Active session found for: " + activeUser + ". Auto-logging in...");
        DefaultWindow.showWindow(activeUser);
    } else {
        // No token or expired -> show login screen
        System.out.println("No active session. Showing login screen...");
        LoginWindow.showWindow();
    }
});
```

---

### Step 4: Call `createSession()` in `LoginWindow.java`
In `src/main/java/urms/ui/LoginWindow.java`, right after `DatabaseConnection.authenticate(...)` succeeds:

```java
String fullName = DatabaseConnection.authenticate(username, password);
if (fullName != null) {
    // Generate and persist the session token
    DatabaseConnection.createSession(username);

    frame.dispose();
    DefaultWindow.showWindow(fullName);
}
```

---

### Step 5: Add Logout Button in `SidebarPanel.java`
In `src/main/java/urms/ui/SidebarPanel.java`, add a **Logout** action at the bottom of the sidebar (`BorderLayout.SOUTH`):
* Calls `DatabaseConnection.clearSession()`.
* Disposes the current `DefaultWindow` frame.
* Opens `LoginWindow.showWindow()`.

---

## ✅ Acceptance Criteria (Definition of Done)
1. **First-time launch (or after logout):** `LoginWindow` appears. Entering `admin` / `admin123` signs in and opens `DefaultWindow`.
2. **Close and Re-open:** Closing the app and running `mvn compile exec:java` opens `DefaultWindow` **directly without asking for credentials**.
3. **Logout:** Clicking "Logout" in the sidebar clears the database token, closes `DefaultWindow`, and returns to `LoginWindow`.
4. **Expiry test:** If `token_expiry` in the DB is set to a past date, the app automatically reverts to `LoginWindow`.

---

## 🤖 AI Reference Prompt (Copy-Paste for Teammate)

If you use an AI assistant or IDE coding agent, copy and paste this exact prompt:

```text
Please implement persistent session authentication (auto-login) for our Java Swing single-user desktop application.

Context & Rules:
- The app is single-user and runs locally against SQLite (resourceregister.db).
- We want to avoid asking the admin for credentials on every app launch.

Please make the following changes:
1. In `src/main/resources/db/schema.sql`, add `session_token TEXT` and `token_expiry TEXT` to the `Users` table.
2. In `src/main/java/urms/dao/DatabaseConnection.java`, implement:
   - `createSession(String username)`: generates a UUID token and updates `session_token` and `token_expiry = datetime('now', '+7 days')`.
   - `getActiveSessionUser()`: returns the `full_name` if `session_token IS NOT NULL` and `token_expiry > now`, otherwise returns null.
   - `clearSession()`: sets `session_token = NULL` and `token_expiry = NULL`.
3. In `src/main/java/urms/ui/LoginWindow.java`, call `DatabaseConnection.createSession(username)` when credentials verify successfully.
4. In `src/main/java/urms/Main.java`, check `DatabaseConnection.getActiveSessionUser()`. If non-null, directly open `DefaultWindow.showWindow(user)`; otherwise open `LoginWindow.showWindow()`.
5. In `src/main/java/urms/ui/SidebarPanel.java`, add a clean "Logout" button at the bottom (BorderLayout.SOUTH) that clears the session and transitions back to `LoginWindow`.

Ensure all code follows our existing FlatLaf styling and compiles cleanly.
```
