package urms.service;

import urms.dao.CategoryDAO;
import urms.model.CategorySummary;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer containing business logic, validations, and rules for Categories.
 */
public class CategoryService {

    /**
     * Retrieves all active category summaries with their associated resource counts.
     *
     * @return List of CategorySummary objects (never null)
     */
    public static List<CategorySummary> getCategories() {
        try {
            List<CategorySummary> list = CategoryDAO.selectAllWithCounts();
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve categories: " + e.getMessage(), e);
        }
    }

    /**
     * Validates input fields and creates a new category.
     *
     * @param rawName        The category name entered by the user
     * @param rawDescription The category description entered by the user
     * @throws IllegalArgumentException If validation fails (empty name, name too long, duplicate name)
     * @throws RuntimeException         If a database error occurs
     */
    public static void addCategory(String rawName, String rawDescription) {
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
            boolean success = CategoryDAO.insert(name, description);

            if (!success) {
                throw new RuntimeException("Failed to insert category into database.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while adding category: " + e.getMessage(), e);
        }
    }
}
