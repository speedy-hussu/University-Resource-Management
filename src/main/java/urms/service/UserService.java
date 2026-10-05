package urms.service;

import urms.dao.UserDAO;
import urms.model.User;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer: business rules, input validation, and operations for Campus Users (Directory).
 *
 * Errors:
 *   IllegalArgumentException -> user input mistakes (shown as warning to operator)
 *   RuntimeException         -> system/database failures (shown as error dialog)
 */
public class UserService {

    private static final int MAX_ENROLLMENT_LENGTH = 50;
    private static final int MAX_NAME_LENGTH = 100;

    // ==========================================
    // Read operations
    // ==========================================

    /**
     * Retrieves all active campus borrowers (students and faculty).
     */
    public static List<User> getAllUsers() {
        try {
            List<User> list = UserDAO.selectAllActiveUsers();
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw dbError("retrieving campus users", e);
        }
    }

    /**
     * Searches and filters campus users by keyword and optional role filter.
     */
    public static List<User> searchUsers(String query, String role) {
        try {
            List<User> list = UserDAO.searchAndFilter(query, role);
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw dbError("searching campus users", e);
        }
    }

    /**
     * Finds an active user by their enrollment number or staff ID.
     */
    public static User getUserByEnrollment(String enrollmentNo) {
        if (enrollmentNo == null || enrollmentNo.trim().isEmpty()) {
            return null;
        }
        try {
            return UserDAO.findByEnrollment(enrollmentNo.trim());
        } catch (SQLException e) {
            throw dbError("looking up user by enrollment", e);
        }
    }

    // ==========================================
    // Write operations
    // ==========================================

    /**
     * Validates input fields and registers a new campus borrower.
     *
     * @param rawEnrollmentNo Student roll number or faculty staff ID
     * @param rawFullName     Full name of the user
     * @param rawRole         Role: "STUDENT" or "FACULTY"
     * @return Generated UUID for the new user
     * @throws IllegalArgumentException If validation fails
     * @throws RuntimeException         If a database error occurs
     */
    public static String registerUser(String rawEnrollmentNo, String rawFullName, String rawRole) {
        // 1. Validate Enrollment Number
        if (rawEnrollmentNo == null || rawEnrollmentNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Enrollment / Staff ID cannot be empty.");
        }
        String enrollmentNo = rawEnrollmentNo.trim();
        if (enrollmentNo.length() > MAX_ENROLLMENT_LENGTH) {
            throw new IllegalArgumentException("Enrollment / Staff ID cannot exceed " + MAX_ENROLLMENT_LENGTH + " characters.");
        }

        // 2. Validate Full Name
        if (rawFullName == null || rawFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        String fullName = rawFullName.trim();
        if (fullName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Full name cannot exceed " + MAX_NAME_LENGTH + " characters.");
        }

        // 3. Validate Role
        if (rawRole == null || rawRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }
        String role = rawRole.trim().toUpperCase();
        if (!"STUDENT".equals(role) && !"FACULTY".equals(role)) {
            throw new IllegalArgumentException("Role must be either 'STUDENT' or 'FACULTY'.");
        }

        // 4. Check for duplicate enrollment number
        try {
            if (UserDAO.existsByEnrollment(enrollmentNo)) {
                throw new IllegalArgumentException("A campus member with Enrollment / Staff ID '" + enrollmentNo + "' already exists.");
            }

            String userId = UserDAO.insertUser(enrollmentNo, fullName, role);
            if (userId == null) {
                throw new RuntimeException("Failed to register campus user into database.");
            }
            return userId;
        } catch (SQLException e) {
            throw dbError("registering new campus user", e);
        }
    }

    private static RuntimeException dbError(String action, SQLException cause) {
        return new RuntimeException("Database error while " + action + ": " + cause.getMessage(), cause);
    }
}
