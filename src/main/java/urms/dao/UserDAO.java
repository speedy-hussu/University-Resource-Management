package urms.dao;

import urms.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) for the Users table (campus directory).
 */
public class UserDAO {

    private static User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getString("user_id"),
                rs.getString("enrollment_no"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getInt("is_active") == 1
        );
    }

    /**
     * Retrieves all active users sorted by role, full_name.
     */
    public static List<User> selectAllActiveUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, enrollment_no, full_name, role, is_active FROM Users "
                + "WHERE is_active = 1 ORDER BY role ASC, full_name ASC;";

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
     * Inserts a new user record with a generated UUID.
     */
    public static String insertUser(String enrollmentNo, String fullName, String role) throws SQLException {
        String userId = UUID.randomUUID().toString();
        String sql = "INSERT INTO Users (user_id, enrollment_no, full_name, role, is_active) VALUES (?, ?, ?, ?, 1);";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId);
            pstmt.setString(2, enrollmentNo.trim());
            pstmt.setString(3, fullName.trim());
            pstmt.setString(4, role.trim().toUpperCase());

            int affected = pstmt.executeUpdate();
            return affected > 0 ? userId : null;
        }
    }

    /**
     * Looks up an active user by their enrollment number / ID.
     */
    public static User findByEnrollment(String enrollmentNo) throws SQLException {
        if (enrollmentNo == null || enrollmentNo.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT user_id, enrollment_no, full_name, role, is_active FROM Users "
                + "WHERE LOWER(enrollment_no) = LOWER(?) AND is_active = 1 LIMIT 1;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, enrollmentNo.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Checks if an enrollment number already exists.
     */
    public static boolean existsByEnrollment(String enrollmentNo) throws SQLException {
        return findByEnrollment(enrollmentNo) != null;
    }

    /**
     * Dynamically searches users by keyword and/or role filter.
     */
    public static List<User> searchAndFilter(String query, String role) throws SQLException {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT user_id, enrollment_no, full_name, role, is_active FROM Users WHERE is_active = 1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            sql.append("AND (LOWER(full_name) LIKE ? OR LOWER(enrollment_no) LIKE ?) ");
            params.add(wildcard);
            params.add(wildcard);
        }

        if (role != null && !role.trim().isEmpty() && !role.equalsIgnoreCase("ALL")) {
            sql.append("AND role = ? ");
            params.add(role.trim().toUpperCase());
        }

        sql.append("ORDER BY role ASC, full_name ASC;");

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
}
