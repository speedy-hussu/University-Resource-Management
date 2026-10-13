package urms;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import urms.dao.DatabaseConnection;
import urms.ui.DefaultWindow;
import urms.ui.LoginWindow;

/**
 * Application Entry Point (Orchestrator).
 * Initializes theme, database bootstrap, and launches the UI.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Setup Modern Flat Look and Feel
        FlatLightLaf.setup();

        // 2. Initialize Database & Tables (Runs schema.sql and seed.sql, fails loudly on error)
        System.out.println("Starting application & initializing database...");
        DatabaseConnection.initializeDatabase();

        // 3. Launch UI on the Event Dispatch Thread (EDT) based on session persistence
        SwingUtilities.invokeLater(() -> {
            String activeUser = DatabaseConnection.getActiveSessionUser();
            if (activeUser != null) {
                System.out.println("Active session found for: " + activeUser + ". Auto-logging in...");
                DefaultWindow.showWindow(activeUser);
            } else {
                System.out.println("No active session or session expired. Showing login screen...");
                LoginWindow.showWindow();
            }
        });
    }
}
