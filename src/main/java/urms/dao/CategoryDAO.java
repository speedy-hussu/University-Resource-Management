package src.main.java.urms.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:sqlite:urms.db";
        return DriverManager.getConnection(dbUrl);
    }

    public static List<CategorySummary> selectAllWithCounts() {
        List<CategorySummary> categories = new ArrayList<>();
        
        String sql = "SELECT c.category_id, c.category_name, c.description, c.is_active, " +
                     "COUNT(r.resource_id) AS resource_count " +
                     "FROM Categories c " +
                     "LEFT JOIN Resources r ON c.category_id = r.category_id AND r.is_active = 1 " +
                     "WHERE c.is_active = 1 " +
                     "GROUP BY c.category_id " +
                     "ORDER BY c.category_name ASC;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                String description = rs.getString("description");
                boolean isActive = rs.getInt("is_active") == 1; 
                int resourceCount = rs.getInt("resource_count");

                CategorySummary summary = new CategorySummary(id, name, description, isActive, resourceCount);
                categories.add(summary);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching categories with counts: " + e.getMessage());
        }

        return categories;
    }

    public static boolean insert(String name, String description) {
        String sql = "INSERT INTO Categories (category_name, description, is_active) VALUES (?, ?, 1);";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting category: " + e.getMessage());
            return false;
        }
    }

    public static boolean existsByName(String name) {
        String sql = "SELECT 1 FROM Categories WHERE LOWER(category_name) = LOWER(?) AND is_active = 1;";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.err.println("Error checking if category exists: " + e.getMessage());
            return false;
        }
    }
}