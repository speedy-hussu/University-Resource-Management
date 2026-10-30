package urms.model;

/**
 * Domain entity and DTO representing a resource allocation / checkout log record.
 * Maps to the Allocations table joined with Users and Resources for enriched display.
 */
public class Allocation {

    private String allocationId;
    private String resourceId;
    private String userId;
    private int quantity;
    private String issueDate;
    private String dueDate;
    private String returnDate;
    private String status;      // ACTIVE, RETURNED, OVERDUE
    private String issuedBy;
    private String remarks;

    // Joined fields from Resources & Users for direct display without extra queries
    private String resourceName;
    private String resourceLocation;
    private String userFullName;
    private String userEnrollmentNo;
    private String userRole;

    public Allocation() {
        this.status = "ACTIVE";
        this.quantity = 1;
    }

    /**
     * Constructor for issuing a new allocation (before ID and timestamps generated).
     */
    public Allocation(String resourceId, String userId, int quantity, String dueDate, String issuedBy, String remarks) {
        this.resourceId = resourceId;
        this.userId = userId;
        this.quantity = quantity;
        this.dueDate = dueDate;
        this.issuedBy = issuedBy;
        this.remarks = remarks;
        this.status = "ACTIVE";
    }

    /**
     * Full constructor mapping all database and join columns.
     */
    public Allocation(String allocationId, String resourceId, String userId, int quantity,
                      String issueDate, String dueDate, String returnDate,
                      String status, String issuedBy, String remarks,
                      String resourceName, String resourceLocation,
                      String userFullName, String userEnrollmentNo, String userRole) {
        this.allocationId = allocationId;
        this.resourceId = resourceId;
        this.userId = userId;
        this.quantity = quantity;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.issuedBy = issuedBy;
        this.remarks = remarks;
        this.resourceName = resourceName;
        this.resourceLocation = resourceLocation;
        this.userFullName = userFullName;
        this.userEnrollmentNo = userEnrollmentNo;
        this.userRole = userRole;
    }

    // Getters and Setters

    public String getAllocationId() {
        return allocationId;
    }

    public void setAllocationId(String allocationId) {
        this.allocationId = allocationId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation) {
        this.resourceLocation = resourceLocation;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEnrollmentNo() {
        return userEnrollmentNo;
    }

    public void setUserEnrollmentNo(String userEnrollmentNo) {
        this.userEnrollmentNo = userEnrollmentNo;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}
