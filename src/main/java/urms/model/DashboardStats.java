package urms.model;

/**
 * Domain DTO holding aggregate metric counts for the Dashboard view.
 */
public class DashboardStats {

    private final int totalUsers;
    private final int totalResources;
    private final int totalCategories;
    private final int totalAllocatedResources;

    public DashboardStats(int totalUsers, int totalResources, int totalCategories, int totalAllocatedResources) {
        this.totalUsers = totalUsers;
        this.totalResources = totalResources;
        this.totalCategories = totalCategories;
        this.totalAllocatedResources = totalAllocatedResources;
    }

    public int getTotalUsers() {
        return totalUsers;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public int getTotalCategories() {
        return totalCategories;
    }

    public int getTotalAllocatedResources() {
        return totalAllocatedResources;
    }
}
