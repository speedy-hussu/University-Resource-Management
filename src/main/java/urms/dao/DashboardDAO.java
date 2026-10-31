package urms.dao;

import urms.model.DashboardStats;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Data Access Object (DAO) for Dashboard summary metrics.
 * Runs aggregation queries across Users, Resources, Categories, and Allocations.
 */
public class DashboardDAO {

    /**
     * Retrieves key metric totals for the Dashboard overview cards.
     *
     * @return DashboardStats containing user, resource, category, and active loan counts.
     * @throws SQLException If any aggregation query fails
     */
    public static DashboardStats getStats() throws SQLException {
        int totalUsers = 0;
        int totalResources = 0;
        int totalCategories = 0;
        int totalAllocated = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Total active users in the directory
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users WHERE is_active = 1;")) {
                if (rs.next()) {
                    totalUsers = rs.getInt(1);
                }
            }

            // 2. Total active registered resources
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Resources WHERE is_active = 1;")) {
                if (rs.next()) {
                    totalResources = rs.getInt(1);
                }
            }

            // 3. Total active categories
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Categories WHERE is_active = 1;")) {
                if (rs.next()) {
                    totalCategories = rs.getInt(1);
                }
            }

            // 4. Total units of resources currently allocated (ACTIVE or OVERDUE)
            try (ResultSet rs = stmt.executeQuery("SELECT COALESCE(SUM(quantity), 0) FROM Allocations WHERE status IN ('ACTIVE', 'OVERDUE');")) {
                if (rs.next()) {
                    totalAllocated = rs.getInt(1);
                }
            }
        }

        return new DashboardStats(totalUsers, totalResources, totalCategories, totalAllocated);
    }
}
