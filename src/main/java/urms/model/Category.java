package urms.model;

/**
 * Domain entity representing a category in the Categories table using a UUID primary key.
 */
public class Category {
    private String categoryId;
    private String categoryName;
    private String description;
    private boolean isActive;

    public Category() {
        this.isActive = true;
    }

    public Category(String categoryId, String categoryName, String description, boolean isActive) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.isActive = isActive;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return categoryName;
    }
}
