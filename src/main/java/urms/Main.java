package urms;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import urms.dao.DatabaseConnection;
import urms.ui.LoginWindow;

/**
 * Application Entry Point (Orchestrator).
 * Initializes theme, database bootstrap, and launches the UI.
 */
public class Main {

    public static void main(String[] args) {
        // 1. Setup System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not load system look and feel: " + e.getMessage());
        }

        // 2. Initialize Database & Tables (Runs schema.sql and seed.sql, fails loudly on error)
        System.out.println("Starting application & initializing database...");
        DatabaseConnection.initializeDatabase();

        // 3. Launch login UI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginWindow.showWindow();
            System.out.println("Login UI launched successfully.");
        });
    }
}
