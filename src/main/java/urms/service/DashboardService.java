package urms.service;

import urms.dao.AllocationDAO;
import urms.dao.DashboardDAO;
import urms.model.Allocation;
import urms.model.DashboardStats;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer coordinating dashboard metrics and recent activity feeds.
 */
public class DashboardService {

    /**
     * Retrieves aggregated statistics for Dashboard KPI cards.
     */
    public static DashboardStats getDashboardStats() {
        try {
            return DashboardDAO.getStats();
        } catch (SQLException e) {
            System.err.println("Failed to fetch dashboard stats: " + e.getMessage());
            return new DashboardStats(0, 0, 0, 0);
        }
    }

    /**
     * Retrieves the most recent allocations up to the specified limit.
     *
     * @param limit Maximum number of recent records (e.g. 10)
     * @return List of recent Allocation entities
     */
    public static List<Allocation> getRecentAllocations(int limit) {
        try {
            List<Allocation> list = AllocationDAO.selectRecent(limit);
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            System.err.println("Failed to fetch recent allocations: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
