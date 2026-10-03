package urms.service;

import urms.dao.CategoryDAO;
import urms.model.Category;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer containing business logic, validations, and rules for Categories using UUID keys.
 */
public class CategoryService {

    /**
     * Retrieves all active categories.
     *
     * @return List of Category objects (never null)
     */
    public static List<Category> getCategories() {
        return getActiveCategories();
    }

    /**
     * Validates input fields and creates a new category with a generated UUID.
     *
     * @param rawName        The category name entered by the user
     * @param rawDescription The category description entered by the user
     * @return The generated category UUID
     * @throws IllegalArgumentException If validation fails (empty name, name too long, duplicate name)
     * @throws RuntimeException         If a database error occurs
     */
    public static String addCategory(String rawName, String rawDescription) {
        // 1. Validation: Name cannot be empty or blank
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }

        String name = rawName.trim();

        // 2. Validation: Length constraints
        if (name.length() > 50) {
            throw new IllegalArgumentException("Category name cannot exceed 50 characters.");
        }

        // 3. Validation: Duplicate name check (Business Rule)
        try {
            if (CategoryDAO.existsByName(name)) {
                throw new IllegalArgumentException("Category '" + name + "' already exists.");
            }

            // 4. Sanitize description and execute insert via DAO
            String description = (rawDescription == null) ? "" : rawDescription.trim();
            String generatedId = CategoryDAO.insert(name, description);

            if (generatedId == null) {
                throw new RuntimeException("Failed to insert category into database.");
            }
            return generatedId;
        } catch (SQLException e) {
            throw new RuntimeException("Database error while adding category: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all active Category domain objects (ordered by name).
     * Useful for UI comboboxes and entity pickers.
     */
    public static List<Category> getActiveCategories() {
        try {
            List<Category> list = CategoryDAO.selectAllActive();
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve active categories: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a single active category by UUID.
     */
    public static Category getCategoryById(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }

        try {
            return CategoryDAO.findById(categoryId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve category by ID " + categoryId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Validates and updates an existing category.
     */
    public static void updateCategory(String categoryId, String rawName, String rawDescription) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Valid category ID is required.");
        }
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String name = rawName.trim();
        if (name.length() > 50) {
            throw new IllegalArgumentException("Category name cannot exceed 50 characters.");
        }

        try {
            Category existing = CategoryDAO.findById(categoryId);
            if (existing == null) {
                throw new IllegalArgumentException("Category with ID " + categoryId + " not found.");
            }

            // If name is changing, check duplicate
            if (!existing.getCategoryName().equalsIgnoreCase(name) && CategoryDAO.existsByName(name)) {
                throw new IllegalArgumentException("Category '" + name + "' already exists.");
            }

            String description = (rawDescription == null) ? "" : rawDescription.trim();
            boolean success = CategoryDAO.update(categoryId, name, description);
            if (!success) {
                throw new RuntimeException("Failed to update category.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while updating category: " + e.getMessage(), e);
        }
    }

    /**
     * Soft-deactivates a category if no active resources are linked to it.
     */
    public static void deleteCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("Valid category ID is required.");
        }

        try {
            int activeResourceCount = CategoryDAO.countActiveResources(categoryId);
            if (activeResourceCount > 0) {
                throw new IllegalArgumentException("Cannot delete category: " + activeResourceCount
                        + " active resource(s) are currently associated with it.");
            }

            boolean success = CategoryDAO.deactivate(categoryId);
            if (!success) {
                throw new RuntimeException("Failed to deactivate category with ID " + categoryId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while deleting category: " + e.getMessage(), e);
        }
    }
}
