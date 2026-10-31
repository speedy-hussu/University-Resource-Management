package urms;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import urms.dao.DatabaseConnection;
import urms.model.Allocation;
import urms.model.DashboardStats;
import urms.service.DashboardService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardTest {

    @BeforeAll
    static void setup() {
        DatabaseConnection.initializeDatabase();
    }

    @Test
    void testDashboardStats() {
        DashboardStats stats = DashboardService.getDashboardStats();
        assertNotNull(stats, "Stats should not be null");
        assertTrue(stats.getTotalUsers() >= 0, "Total users should be >= 0");
        assertTrue(stats.getTotalResources() >= 0, "Total resources should be >= 0");
        assertTrue(stats.getTotalCategories() >= 0, "Total categories should be >= 0");
        assertTrue(stats.getTotalAllocatedResources() >= 0, "Allocated resources should be >= 0");
    }

    @Test
    void testRecentAllocations() {
        List<Allocation> recent = DashboardService.getRecentAllocations(10);
        assertNotNull(recent, "Recent allocations list should not be null");
        assertTrue(recent.size() <= 10, "Should return at most 10 allocations");
    }
}
