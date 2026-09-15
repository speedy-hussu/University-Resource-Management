package urms.dao;

import urms.model.CategorySummary;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for Category database operations using SQLite.
 */
public class CategoryDAO {

    /**
     * Fetches all active categories along with the count of active resources in each category.
     */
    public static List<CategorySummary> selectAllWithCounts() throws SQLException {
        List<CategorySummary> categories = new ArrayList<>();

        String sql = "SELECT c.category_id, c.category_name, c.description, c.is_active, "
                + "COUNT(r.resource_id) AS resource_count "
                + "FROM Categories c "
                + "LEFT JOIN Resources r ON c.category_id = r.category_id AND r.is_active = 1 "
                + "WHERE c.is_active = 1 "
                + "GROUP BY c.category_id "
                + "ORDER BY c.category_name ASC;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                String description = rs.getString("description");
                boolean isActive = rs.getInt("is_active") == 1;
                int resourceCount = rs.getInt("resource_count");

                CategorySummary summary = new CategorySummary(id, name, description, resourceCount, isActive);
                categories.add(summary);
            }
        }

        return categories;
    }

    /**
     * Inserts a new category into the Categories table.
     */
    public static boolean insert(String name, String description) throws SQLException {
        String sql = "INSERT INTO Categories (category_name, description, is_active) VALUES (?, ?, 1);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);

            return pstmt.executeUpdate() > 0;
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
}
