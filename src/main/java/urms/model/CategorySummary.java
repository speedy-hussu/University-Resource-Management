package urms.model;

/**
 * Immutable data model representing a category row along with its live resource count.
 */
public record CategorySummary(
    int categoryId,
    String categoryName,
    String description,
    int resourceCount,
    boolean isActive
) {}
