   package urms.dao;

import urms.model.Allocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object (DAO) for the Allocations table.
 * Handles checkout operations, return updates, inventory quantity sync via SQLite transactions,
 * and joined queries for reporting and audit logs.
 */
public class AllocationDAO {

    private static final String BASE_SELECT =
            "SELECT a.allocation_id, a.resource_id, a.user_id, a.quantity, "
            + "a.issue_date, a.due_date, a.return_date, a.status, a.issued_by, a.remarks, "
            + "r.resource_name, r.location AS resource_location, "
            + "u.full_name AS user_full_name, u.enrollment_no AS user_enrollment_no, u.role AS user_role "
            + "FROM Allocations a "
            + "JOIN Resources r ON a.resource_id = r.resource_id "
            + "JOIN Users u ON a.user_id = u.user_id ";

    private static Allocation mapRow(ResultSet rs) throws SQLException {
        return new Allocation(
                rs.getString("allocation_id"),
                rs.getString("resource_id"),
                rs.getString("user_id"),
                rs.getInt("quantity"),
                rs.getString("issue_date"),
                rs.getString("due_date"),
                rs.getString("return_date"),
                rs.getString("status"),
                rs.getString("issued_by"),
                rs.getString("remarks"),
                rs.getString("resource_name"),
                rs.getString("resource_location"),
                rs.getString("user_full_name"),
                rs.getString("user_enrollment_no"),
                rs.getString("user_role")
        );
    }

    /**
     * Retrieves all allocation records ordered chronologically (newest first).
     */
    public static List<Allocation> selectAll() throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY a.issue_date DESC;";

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
     * Retrieves all currently active allocations (items currently checked out / in loan).
     */
    public static List<Allocation> selectActive() throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = BASE_SELECT + "WHERE a.status = 'ACTIVE' ORDER BY a.issue_date DESC;";

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
     * Retrieves the most recent allocation records ordered chronologically (newest first).
     *
     * @param limit Maximum number of records to return
     */
    public static List<Allocation> selectRecent(int limit) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        String sql = BASE_SELECT + "ORDER BY a.issue_date DESC, a.rowid DESC LIMIT ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Finds an allocation record by its UUID.
     */
    public static Allocation findById(String allocationId) throws SQLException {
        String sql = BASE_SELECT + "WHERE a.allocation_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, allocationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    /**
     * Searches and filters allocations by query text and status.
     * Searches across user full_name, enrollment_no, and resource_name.
     */
    public static List<Allocation> searchAndFilter(String query, String status) throws SQLException {
        List<Allocation> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<String> params = new ArrayList<>();

        List<String> conditions = new ArrayList<>();

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            conditions.add("a.status = ?");
            params.add(status.trim().toUpperCase());
        }

        if (query != null && !query.trim().isEmpty()) {
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            conditions.add("(LOWER(u.full_name) LIKE ? OR LOWER(u.enrollment_no) LIKE ? OR LOWER(r.resource_name) LIKE ?)");
            params.add(wildcard);
            params.add(wildcard);
            params.add(wildcard);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        sql.append(" ORDER BY a.issue_date DESC;");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i));
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
     * Atomically issues an allocation and decrements the resource available_quantity.
     * Executes inside a SQLite transaction to prevent race conditions or inconsistent inventory.
     *
     * @param allocation The allocation details to create
     * @return Generated allocation_id UUID
     * @throws SQLException If available stock is insufficient or database failure occurs
     */
    public static String insertAllocation(Allocation allocation) throws SQLException {
        String allocationId = UUID.randomUUID().toString();
        allocation.setAllocationId(allocationId);

        String checkStockSql = "SELECT available_quantity FROM Resources WHERE resource_id = ? AND is_active = 1;";
        String decrementStockSql = "UPDATE Resources SET available_quantity = available_quantity - ? WHERE resource_id = ?;";
        String insertAllocSql = "INSERT INTO Allocations (allocation_id, resource_id, user_id, quantity, "
                + "issue_date, due_date, status, issued_by, remarks) "
                + "VALUES (?, ?, ?, ?, COALESCE(?, datetime('now', 'localtime')), ?, 'ACTIVE', ?, ?);";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Verify stock is sufficient
                int availableQty = 0;
                try (PreparedStatement checkStmt = conn.prepareStatement(checkStockSql)) {
                    checkStmt.setString(1, allocation.getResourceId());
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Resource not found or is inactive.");
                        }
                        availableQty = rs.getInt("available_quantity");
                    }
                }

                if (availableQty < allocation.getQuantity()) {
                    throw new SQLException("Insufficient stock available: " + availableQty + " available, requested " + allocation.getQuantity());
                }

                // 2. Decrement available quantity
                try (PreparedStatement decStmt = conn.prepareStatement(decrementStockSql)) {
                    decStmt.setInt(1, allocation.getQuantity());
                    decStmt.setString(2, allocation.getResourceId());
                    int rowsUpdated = decStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new SQLException("Failed to update resource inventory stock.");
                    }
                }

                // 3. Insert allocation record
                try (PreparedStatement insertStmt = conn.prepareStatement(insertAllocSql)) {
                    insertStmt.setString(1, allocationId);
                    insertStmt.setString(2, allocation.getResourceId());
                    insertStmt.setString(3, allocation.getUserId());
                    insertStmt.setInt(4, allocation.getQuantity());
                    insertStmt.setString(5, allocation.getIssueDate() != null && !allocation.getIssueDate().trim().isEmpty() ? allocation.getIssueDate().trim() : null);
                    insertStmt.setString(6, allocation.getDueDate());
                    insertStmt.setString(7, allocation.getIssuedBy() != null ? allocation.getIssuedBy().trim() : "admin");
                    insertStmt.setString(8, allocation.getRemarks() != null ? allocation.getRemarks().trim() : "");
                    insertStmt.executeUpdate();
                }

                conn.commit();
                return allocationId;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Atomically marks an allocation as RETURNED, records the return timestamp,
     * and increments the resource's available_quantity back.
     *
     * @param allocationId The UUID of the allocation to return
     * @throws SQLException If record not found, already returned, or DB error occurs
     */
    public static void returnAllocation(String allocationId) throws SQLException {
        String findAllocSql = "SELECT resource_id, quantity, status FROM Allocations WHERE allocation_id = ?;";
        String updateAllocSql = "UPDATE Allocations SET status = 'RETURNED', return_date = datetime('now', 'localtime') WHERE allocation_id = ?;";
        String incrementStockSql = "UPDATE Resources SET available_quantity = available_quantity + ? WHERE resource_id = ?;";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String resourceId = null;
                int quantity = 0;
                String currentStatus = null;

                // 1. Fetch current allocation status
                try (PreparedStatement findStmt = conn.prepareStatement(findAllocSql)) {
                    findStmt.setString(1, allocationId);
                    try (ResultSet rs = findStmt.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Allocation record not found.");
                        }
                        resourceId = rs.getString("resource_id");
                        quantity = rs.getInt("quantity");
                        currentStatus = rs.getString("status");
                    }
                }

                if ("RETURNED".equalsIgnoreCase(currentStatus)) {
                    throw new SQLException("Resource has already been returned.");
                }

                // 2. Mark allocation as returned
                try (PreparedStatement updateStmt = conn.prepareStatement(updateAllocSql)) {
                    updateStmt.setString(1, allocationId);
                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated == 0) {
                        throw new SQLException("Failed to update allocation status.");
                    }
                }

                // 3. Increment resource inventory available_quantity
                try (PreparedStatement incStmt = conn.prepareStatement(incrementStockSql)) {
                    incStmt.setInt(1, quantity);
                    incStmt.setString(2, resourceId);
                    incStmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Checks if there are active loans for a specific user.
     */
    public static int countActiveByUser(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Allocations WHERE user_id = ? AND status = 'ACTIVE';";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Checks if there are active loans for a specific resource.
     */
    public static int countActiveByResource(String resourceId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Allocations WHERE resource_id = ? AND status = 'ACTIVE';";
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
