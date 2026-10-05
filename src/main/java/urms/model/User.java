package urms.model;

/**
 * Domain entity representing a campus borrower (student or faculty) in the Users table.
 */
public class User {
    private String userId;
    private String enrollmentNo;
    private String fullName;
    private String role; // "STUDENT" or "FACULTY"
    private boolean isActive;

    public User() {
        this.isActive = true;
    }

    public User(String userId, String enrollmentNo, String fullName, String role, boolean isActive) {
        this.userId = userId;
        this.enrollmentNo = enrollmentNo;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEnrollmentNo() {
        return enrollmentNo;
    }

    public void setEnrollmentNo(String enrollmentNo) {
        this.enrollmentNo = enrollmentNo;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return fullName + " (" + enrollmentNo + " - " + role + ")";
    }
}
