package urms.dao;

import urms.model.Resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) for Resources table operations in SQLite using UUID keys.
 * Handles inventory queries, joins with Categories, and atomic quantity updates.
 */
public class ResourceDAO {

    private static final String BASE_SELECT =
            "SELECT r.resource_id, r.resource_name, r.category_id, c.category_name, "
            + "r.location, r.total_quantity, r.available_quantity, "
            + "r.is_active, r.created_at "
            + "FROM Resources r "
            + "JOIN Categories c ON r.category_id = c.category_id ";

    /**
     * Maps a ResultSet cursor row to a Resource domain object.
     */
    private static Resource mapRow(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getString("resource_id"),
                rs.getString("resource_name"),
                rs.getString("category_id"),
                rs.getString("category_name"),
                rs.getString("location"),
                rs.getInt("total_quantity"),
                rs.getInt("available_quantity"),
                rs.getInt("is_active") == 1,
                rs.getString("created_at")
        );
    }

    /**
     * Retrieves all active resources joined with category details.
     */
    public static List<Resource> selectAll() throws SQLException {
        List<Resource> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE r.is_active = 1 ORDER BY r.resource_name ASC;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Finds an active resource by its primary key UUID.
     */
    public static Resource findById(String resourceId) throws SQLException {
        if (resourceId == null || resourceId.trim().isEmpty()) {
            return null;
        }

        String sql = BASE_SELECT + "WHERE r.resource_id = ? AND r.is_active = 1 LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Dynamically searches and filters active resources by keyword and/or category.
     *
     * @param searchQuery Optional keyword to search in resource_name or location
     * @param categoryId  Optional category UUID filter (null or empty to ignore)
     */
    public static List<Resource> searchAndFilter(String searchQuery, String categoryId) throws SQLException {
        List<Resource> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT).append("WHERE r.is_active = 1 ");
        List<Object> params = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String wildcard = "%" + searchQuery.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(r.resource_name) LIKE ? OR LOWER(r.location) LIKE ?) ");
            params.add(wildcard);
            params.add(wildcard);
        }

        if (categoryId != null && !categoryId.trim().isEmpty() && !categoryId.equalsIgnoreCase("ALL")) {
            sql.append("AND r.category_id = ? ");
            params.add(categoryId.trim());
        }

        sql.append("ORDER BY r.resource_name ASC;");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Inserts a new resource record with a generated UUID and returns the ID.
     */
    public static String insert(Resource resource) throws SQLException {
        String uuid = resource.getResourceId();
        if (uuid == null || uuid.trim().isEmpty()) {
            uuid = UUID.randomUUID().toString();
            resource.setResourceId(uuid);
        }

        String sql = "INSERT INTO Resources (resource_id, resource_name, category_id, location, "
                + "total_quantity, available_quantity, is_active) VALUES (?, ?, ?, ?, ?, ?, 1);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, uuid);
            pstmt.setString(2, resource.getResourceName());
            pstmt.setString(3, resource.getCategoryId());
            pstmt.setString(4, resource.getLocation());
            pstmt.setInt(5, resource.getTotalQuantity());
            pstmt.setInt(6, resource.getAvailableQuantity());

            int affected = pstmt.executeUpdate();
            return (affected > 0) ? uuid : null;
        }
    }

    /**
     * Updates an existing resource's information.
     */
    public static boolean update(Resource resource) throws SQLException {
        String sql = "UPDATE Resources SET resource_name = ?, category_id = ?, "
                + "location = ?, total_quantity = ?, available_quantity = ? "
                + "WHERE resource_id = ? AND is_active = 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resource.getResourceName());
            pstmt.setString(2, resource.getCategoryId());
            pstmt.setString(3, resource.getLocation());
            pstmt.setInt(4, resource.getTotalQuantity());
            pstmt.setInt(5, resource.getAvailableQuantity());
            pstmt.setString(6, resource.getResourceId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Updates only the available quantity for a resource (used during checkout/return).
     */
    public static boolean updateAvailableQuantity(String resourceId, int newAvailableQuantity) throws SQLException {
        String sql = "UPDATE Resources SET available_quantity = ? WHERE resource_id = ? AND is_active = 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newAvailableQuantity);
            pstmt.setString(2, resourceId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Soft deletes (deactivates) a resource by UUID.
     */
    public static boolean deactivate(String resourceId) throws SQLException {
        String sql = "UPDATE Resources SET is_active = 0 WHERE resource_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a resource with the given name already exists in the same category.
     * Optionally excludes an ID (used during update validation).
     */
    public static boolean existsByNameAndCategory(String name, String categoryId, String excludeResourceId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT 1 FROM Resources WHERE LOWER(resource_name) = LOWER(?) AND category_id = ? AND is_active = 1 "
        );
        if (excludeResourceId != null && !excludeResourceId.trim().isEmpty()) {
            sql.append("AND resource_id != ? ");
        }
        sql.append("LIMIT 1;");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, name.trim());
            pstmt.setString(2, categoryId);
            if (excludeResourceId != null && !excludeResourceId.trim().isEmpty()) {
                pstmt.setString(3, excludeResourceId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Checks if an active resource with the given name already exists in the same category.
     */
    public static boolean existsByNameAndCategory(String name, String categoryId) throws SQLException {
        return existsByNameAndCategory(name, categoryId, null);
    }

    /**
     * Checks if there are active allocations currently out on loan for this resource.
     */
    public static boolean hasActiveAllocations(String resourceId) throws SQLException {
        String sql = "SELECT 1 FROM Allocations WHERE resource_id = ? AND UPPER(status) = 'ACTIVE' LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Returns the total count of active allocations associated with this resource.
     */
    public static int getActiveAllocationQuantity(String resourceId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM Allocations WHERE resource_id = ? AND UPPER(status) = 'ACTIVE';";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
