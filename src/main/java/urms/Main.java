package urms;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import urms.dao.DatabaseConnection;
import urms.ui.DefaultWindow;

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

        // 3. Launch login UI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            DefaultWindow.showWindow("Admin");
            System.out.println("Login UI launched successfully.");
        });
    }
}
