package urms.dao;

import urms.model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) for Category database operations using SQLite with UUID primary keys.
 */
public class CategoryDAO {


    /**
     * Inserts a new category into the Categories table with a generated UUID.
     *
     * @return Generated category UUID if successful, null otherwise
     */
    public static String insert(String name, String description) throws SQLException {
        String categoryId = UUID.randomUUID().toString();
        String sql = "INSERT INTO Categories (category_id, category_name, description, is_active) VALUES (?, ?, ?, 1);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);
            pstmt.setString(2, name);
            pstmt.setString(3, description);

            boolean success = pstmt.executeUpdate() > 0;
            return success ? categoryId : null;
        }
    }

    /**
     * Checks whether an active category with the given name already exists (case-insensitive).
     */
    public static boolean existsByName(String name) throws SQLException {
        String sql = "SELECT 1 FROM Categories WHERE LOWER(category_name) = LOWER(?) AND is_active = 1 LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }


    /**
     * Retrieves all active categories as Category domain objects (ordered by name).
     * Ideal for populating category dropdowns and select pickers.
     */
    public static List<Category> selectAllActive() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT category_id, category_name, description, is_active FROM Categories "
                + "WHERE is_active = 1 ORDER BY category_name ASC;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getString("category_id"),
                        rs.getString("category_name"),
                        rs.getString("description"),
                        rs.getInt("is_active") == 1
                ));
            }
        }
        return categories;
    }

    /**
     * Finds an active category by its primary key UUID.
     */
    public static Category findById(String categoryId) throws SQLException {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT category_id, category_name, description, is_active FROM Categories "
                + "WHERE category_id = ? AND is_active = 1 LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Category(
                            rs.getString("category_id"),
                            rs.getString("category_name"),
                            rs.getString("description"),
                            rs.getInt("is_active") == 1
                    );
                }
            }
        }
        return null;
    }

    /**
     * Updates an existing category's name and description.
     */
    public static boolean update(String categoryId, String name, String description) throws SQLException {
        String sql = "UPDATE Categories SET category_name = ?, description = ? WHERE category_id = ? AND is_active = 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setString(3, categoryId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Soft deletes (deactivates) a category by UUID.
     */
    public static boolean deactivate(String categoryId) throws SQLException {
        String sql = "UPDATE Categories SET is_active = 0 WHERE category_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Counts how many active resources are linked to this category.
     */
    public static int countActiveResources(String categoryId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Resources WHERE category_id = ? AND is_active = 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, categoryId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
