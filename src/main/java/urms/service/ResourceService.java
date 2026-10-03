package urms.service;

import java.sql.SQLException;
import java.util.List;

import urms.dao.CategoryDAO;
import urms.dao.ResourceDAO;
import urms.model.Resource;

/**
 * Service layer: business rules and validation for Resources (UUID keys).
 *
 * Errors:
 *   IllegalArgumentException -> user mistake   (show as a warning)
 *   RuntimeException         -> system failure (show a generic error, log the cause)
 */
public class ResourceService {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_LOCATION_LENGTH = 150;

    // ==========================================
    // Read operations
    // ==========================================

    /** Retrieves all active resources joined with category information. */
    public static List<Resource> getAllResources() {
        try {
            return ResourceDAO.selectAll();
        } catch (SQLException e) {
            throw dbError("retrieving resources", e);
        }
    }

    /** Searches and filters resources by query and category UUID. */
    public static List<Resource> searchAndFilter(String query, String categoryId) {
        try {
            return ResourceDAO.searchAndFilter(query, categoryId);
        } catch (SQLException e) {
            throw dbError("searching resources", e);
        }
    }

    /** Retrieves a single resource by its UUID, or null if not found. */
    public static Resource getResourceById(String resourceId) {
        try {
            return ResourceDAO.findById(resourceId);
        } catch (SQLException e) {
            throw dbError("retrieving resource #" + resourceId, e);
        }
    }

    // ==========================================
    // Write operations
    // ==========================================

    /** Validates and creates a new resource. Returns the generated UUID. */
    public static String addResource(Resource resource) {
        requireResource(resource);

        // 1. Check
        checkFields(resource);

        // 2. Rules
        checkCategoryExists(resource.getCategoryId());
        checkDuplicateName(resource.getResourceName(), resource.getCategoryId(), null);

        // 3. Save
        return saveNewResource(resource);
    }

    /** Convenience overload that builds the Resource from individual fields. */
    public static String addResource(String name, String categoryId, String location,
                                     int totalQuantity, int availableQuantity) {
        return addResource(new Resource(name, categoryId, location, totalQuantity, availableQuantity));
    }

    /** Validates and updates an existing resource. */
    public static void updateResource(Resource resource) {
        requireResource(resource);

        // 1. Check
        checkFields(resource);

        // 2. Rules
        Resource existing = requireExistingResource(resource.getResourceId());

        // Compute both change flags once and reuse them throughout.
        boolean categoryChanged = !resource.getCategoryId().equals(existing.getCategoryId());
        boolean nameChanged     = !resource.getResourceName().equalsIgnoreCase(existing.getResourceName());

        if (categoryChanged) {
            checkCategoryExists(resource.getCategoryId());
        }

        // Only query duplicate check if name or category changed (saves 1 DB round-trip).
        if (nameChanged || categoryChanged) {
            checkDuplicateName(resource.getResourceName(), resource.getCategoryId(), resource.getResourceId());
        }

        checkTotalNotBelowCheckedOut(resource.getTotalQuantity(), existing.getCheckedOutQuantity());

        // 3. Save
        saveResourceUpdate(resource);
    }

    /** Soft deletes (deactivates) a resource that has no active allocations. */
    public static void deleteResource(String resourceId) {
        checkNoActiveAllocations(resourceId);
        saveResourceDeactivation(resourceId);
    }

    /** Adjusts stock. quantityDelta is positive to add stock, negative to remove it. */
    public static void adjustStock(String resourceId, int quantityDelta) {
        Resource existing = requireExistingResource(resourceId);
        checkStockAdjustment(existing, quantityDelta);
        saveStockAdjustment(existing, quantityDelta);
    }

    // ==========================================
    // Check helpers (in memory)
    // ==========================================

    // The resource object must be provided.
    private static void requireResource(Resource resource) {
        if (resource == null) {
            throw new IllegalArgumentException("Resource details cannot be null.");
        }
    }

    // The essential field rules.
    private static void checkFields(Resource r) {
        if (r.getResourceName() == null || r.getResourceName().isBlank()) {
            throw new IllegalArgumentException("Resource name cannot be empty.");
        }
        if (r.getResourceName().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("Resource name cannot exceed " + MAX_NAME_LENGTH + " characters.");
        }
        if (r.getCategoryId() == null || r.getCategoryId().isBlank()) {
            throw new IllegalArgumentException("A valid Category must be selected.");
        }

        if (r.getLocation() != null && r.getLocation().length() > MAX_LOCATION_LENGTH) {
            throw new IllegalArgumentException("Location description cannot exceed " + MAX_LOCATION_LENGTH + " characters.");
        }
        if (r.getTotalQuantity() < 0 || r.getAvailableQuantity() < 0) {
            throw new IllegalArgumentException("Quantities cannot be negative.");
        }
        if (r.getAvailableQuantity() > r.getTotalQuantity()) {
            throw new IllegalArgumentException("Available quantity cannot exceed total quantity.");
        }
    }

    // ==========================================
    // Rules helpers (database checks)
    // ==========================================

    // Loads the resource or throws if it does not exist.
    private static Resource requireExistingResource(String resourceId) {
        try {
            Resource existing = ResourceDAO.findById(resourceId);
            if (existing == null) {
                throw new IllegalArgumentException("Resource #" + resourceId + " not found.");
            }
            return existing;
        } catch (SQLException e) {
            throw dbError("retrieving resource #" + resourceId, e);
        }
    }

    // The category must exist and be active.
    private static void checkCategoryExists(String categoryId) {
        try {
            if (CategoryDAO.findById(categoryId) == null) {
                throw new IllegalArgumentException("Selected category does not exist or is inactive.");
            }
        } catch (SQLException e) {
            throw dbError("verifying category", e);
        }
    }

    // No other resource in the category may share the name. Pass null as excludeId when adding.
    private static void checkDuplicateName(String name, String categoryId, String excludeId) {
        try {
            if (ResourceDAO.existsByNameAndCategory(name, categoryId, excludeId)) {
                throw new IllegalArgumentException("A resource named '" + name + "' already exists in this category.");
            }
        } catch (SQLException e) {
            throw dbError("checking for duplicate resource", e);
        }
    }

    // Total stock cannot drop below the units currently checked out.
    private static void checkTotalNotBelowCheckedOut(int total, int checkedOut) {
        if (total < checkedOut) {
            throw new IllegalArgumentException("Total quantity cannot be less than checked-out quantity (" + checkedOut + ").");
        }
    }

    // A resource with units on loan cannot be deleted.
    private static void checkNoActiveAllocations(String resourceId) {
        try {
            int activeQty = ResourceDAO.getActiveAllocationQuantity(resourceId);
            if (activeQty > 0) {
                throw new IllegalArgumentException("Cannot delete resource: " + activeQty
                        + " unit(s) are currently checked out.");
            }
        } catch (SQLException e) {
            throw dbError("checking resource allocations", e);
        }
    }

    // A stock change cannot make available negative or drop total below checked-out units.
    private static void checkStockAdjustment(Resource existing, int quantityDelta) {
        if (existing.getAvailableQuantity() + quantityDelta < 0) {
            throw new IllegalArgumentException("Stock adjustment would result in negative quantity.");
        }
        checkTotalNotBelowCheckedOut(existing.getTotalQuantity() + quantityDelta, existing.getCheckedOutQuantity());
    }

    // ==========================================
    // Save helpers
    // ==========================================

    // Inserts the resource and returns the generated UUID.
    private static String saveNewResource(Resource resource) {
        try {
            String generatedId = ResourceDAO.insert(resource);
            if (generatedId == null) {
                throw new RuntimeException("Failed to create resource record in database.");
            }
            return generatedId;
        } catch (SQLException e) {
            throw dbError("adding resource", e);
        }
    }

    // Updates the resource record.
    private static void saveResourceUpdate(Resource resource) {
        try {
            if (!ResourceDAO.update(resource)) {
                throw new RuntimeException("Failed to update resource #" + resource.getResourceId());
            }
        } catch (SQLException e) {
            throw dbError("updating resource", e);
        }
    }

    // Marks the resource inactive (soft delete).
    private static void saveResourceDeactivation(String resourceId) {
        try {
            if (!ResourceDAO.deactivate(resourceId)) {
                throw new IllegalArgumentException("Resource #" + resourceId + " not found or already deleted.");
            }
        } catch (SQLException e) {
            throw dbError("deleting resource", e);
        }
    }

    // Applies the stock change and saves the resource.
    private static void saveStockAdjustment(Resource existing, int quantityDelta) {
        existing.setTotalQuantity(existing.getTotalQuantity() + quantityDelta);
        existing.setAvailableQuantity(existing.getAvailableQuantity() + quantityDelta);
        saveResourceUpdate(existing);
    }

    // Builds the one standard message for any database failure.
    private static RuntimeException dbError(String action, SQLException cause) {
        return new RuntimeException("Database error while " + action + ": " + cause.getMessage(), cause);
    }
}