package urms.service;

import urms.dao.AllocationDAO;
import urms.dao.ResourceDAO;
import urms.dao.UserDAO;
import urms.model.Allocation;
import urms.model.Resource;
import urms.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;

/**
 * Service layer: business rules, validation, and workflows for Allocations (Checkouts/Returns).
 *
 * Error Handling Convention:
 *   IllegalArgumentException -> Validation / User mistake (displays friendly UI dialog/toast)
 *   RuntimeException         -> System or SQL error (logs cause and shows error message)
 */
public class AllocationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==========================================
    // Read Operations
    // ==========================================

    /**
     * Retrieves all allocation records in descending order of issue date.
     */
    public static List<Allocation> getAllAllocations() {
        try {
            List<Allocation> list = AllocationDAO.selectAll();
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw dbError("retrieving all allocations", e);
        }
    }

    /**
     * Retrieves currently active allocations (checked out / in-use resources).
     */
    public static List<Allocation> getActiveAllocations() {
        try {
            List<Allocation> list = AllocationDAO.selectActive();
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw dbError("retrieving active allocations", e);
        }
    }

    /**
     * Searches and filters allocations by query text and status ('ALL', 'ACTIVE', 'RETURNED').
     */
    public static List<Allocation> searchAndFilter(String query, String status) {
        try {
            List<Allocation> list = AllocationDAO.searchAndFilter(query, status);
            return list != null ? list : Collections.emptyList();
        } catch (SQLException e) {
            throw dbError("searching allocations", e);
        }
    }

    /**
     * Retrieves an allocation by its unique UUID.
     */
    public static Allocation getAllocationById(String allocationId) {
        if (allocationId == null || allocationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation ID cannot be empty.");
        }
        try {
            return AllocationDAO.findById(allocationId.trim());
        } catch (SQLException e) {
            throw dbError("retrieving allocation #" + allocationId, e);
        }
    }

    // ==========================================
    // Write Operations (Checkout & Return)
    // ==========================================

    /**
     * Validates and issues a new allocation.
     * Decrements the resource available quantity and records checkout log atomically.
     *
     * @param allocation Allocation payload containing resourceId, userId, quantity, dueDate, remarks, issuedBy
     * @return Generated UUID of the created allocation
     */
    public static String issueResource(Allocation allocation) {
        if (allocation == null) {
            throw new IllegalArgumentException("Allocation details cannot be null.");
        }

        // 1. Validate Resource selection
        if (allocation.getResourceId() == null || allocation.getResourceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a resource to allocate.");
        }
        Resource resource;
        try {
            resource = ResourceDAO.findById(allocation.getResourceId().trim());
        } catch (SQLException e) {
            throw dbError("verifying resource", e);
        }
        if (resource == null || !resource.isActive()) {
            throw new IllegalArgumentException("The selected resource does not exist or is inactive.");
        }

        // 2. Validate User selection
        if (allocation.getUserId() == null || allocation.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("Please select a valid campus user / borrower.");
        }
        User user;
        try {
            user = UserDAO.findById(allocation.getUserId().trim());
        } catch (SQLException e) {
            throw dbError("verifying user", e);
        }
        if (user == null || !user.isActive()) {
            throw new IllegalArgumentException("The selected user does not exist or is inactive.");
        }

        // 3. Validate Quantity
        if (allocation.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        if (allocation.getQuantity() > resource.getAvailableQuantity()) {
            throw new IllegalArgumentException("Requested quantity (" + allocation.getQuantity()
                    + ") exceeds available stock (" + resource.getAvailableQuantity() + ") for '"
                    + resource.getResourceName() + "'.");
        }

        // 4. Validate Start Date & Due Date
        if (allocation.getDueDate() == null || allocation.getDueDate().trim().isEmpty()) {
            throw new IllegalArgumentException("Due date is required.");
        }
        String cleanDueDate = allocation.getDueDate().trim();
        LocalDate dueDate = parseDate(cleanDueDate, "Due date");
        if (dueDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Due date cannot be in the past.");
        }

        String cleanStartDate = allocation.getIssueDate() != null ? allocation.getIssueDate().trim() : "";
        if (!cleanStartDate.isEmpty()) {
            LocalDate startDate = parseDate(cleanStartDate, "Start / Issue date");
            if (dueDate.isBefore(startDate)) {
                throw new IllegalArgumentException("Due date cannot be earlier than start date (" + cleanStartDate + ").");
            }
            allocation.setIssueDate(cleanStartDate);
        } else {
            allocation.setIssueDate(LocalDate.now().format(DATE_FORMATTER));
        }

        // Standardize clean values
        allocation.setResourceId(allocation.getResourceId().trim());
        allocation.setUserId(allocation.getUserId().trim());
        allocation.setDueDate(cleanDueDate);
        if (allocation.getIssuedBy() == null || allocation.getIssuedBy().trim().isEmpty()) {
            allocation.setIssuedBy("admin");
        } else {
            allocation.setIssuedBy(allocation.getIssuedBy().trim());
        }

        if (allocation.getRemarks() != null) {
            allocation.setRemarks(allocation.getRemarks().trim());
        }

        // 5. Persist through DAO transaction
        try {
            return AllocationDAO.insertAllocation(allocation);
        } catch (SQLException e) {
            throw dbError("processing resource allocation", e);
        }
    }

    /**
     * Marks a checked out allocation as returned and increments available quantity back into inventory.
     *
     * @param allocationId Unique UUID of the allocation
     */
    public static void returnResource(String allocationId) {
        if (allocationId == null || allocationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation ID is required for return.");
        }

        try {
            Allocation current = AllocationDAO.findById(allocationId.trim());
            if (current == null) {
                throw new IllegalArgumentException("Allocation record not found.");
            }
            if ("RETURNED".equalsIgnoreCase(current.getStatus())) {
                throw new IllegalArgumentException("This resource has already been returned.");
            }

            AllocationDAO.returnAllocation(allocationId.trim());
        } catch (SQLException e) {
            throw dbError("processing resource return", e);
        }
    }

    // ==========================================
    // Helpers & Validation
    // ==========================================

    private static LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            if (dateStr.length() == 10) {
                return LocalDate.parse(dateStr, DATE_FORMATTER);
            } else {
                return LocalDateTime.parse(dateStr, DATETIME_FORMATTER).toLocalDate();
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid format for " + fieldName + ". Expected 'YYYY-MM-DD' (e.g. 2026-11-05).");
        }
    }

    private static RuntimeException dbError(String action, SQLException e) {
        System.err.println("Database error while " + action + ": " + e.getMessage());
        return new RuntimeException("Database error while " + action + ". Please try again.", e);
    }
}
