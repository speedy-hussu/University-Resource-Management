package urms.model;

/**
 * Domain entity representing an inventory resource item in the Resources table with UUID keys.
 */
public class Resource {
    private String resourceId;
    private String resourceName;
    private String categoryId;
    private String categoryName; // Populated via join query with Categories
    private String location;
    private int totalQuantity;
    private int availableQuantity;
    private boolean isActive;
    private String createdAt;

    public Resource() {
        this.isActive = true;
    }

    /**
     * Constructor for creating a new resource (before database ID assignment).
     */
    public Resource(String resourceName, String categoryId, String location,
                    int totalQuantity, int availableQuantity) {
        this.resourceName = resourceName;
        this.categoryId = categoryId;
        this.location = location;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.isActive = true;
    }

    /**
     * Full constructor including database metadata.
     */
    public Resource(String resourceId, String resourceName, String categoryId, String categoryName,
                    String location, int totalQuantity, int availableQuantity,
                    boolean isActive, String createdAt) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.location = location;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Calculates the quantity of this resource currently in use or checked out.
     */
    public int getCheckedOutQuantity() {
        return Math.max(0, totalQuantity - availableQuantity);
    }

    /**
     * Checks if at least one unit of this resource is available for checkout.
     */
    public boolean isAvailable() {
        return availableQuantity > 0;
    }

    @Override
    public String toString() {
        return resourceName + " (" + (categoryName != null ? categoryName : "Cat #" + categoryId) + ")";
    }
}

