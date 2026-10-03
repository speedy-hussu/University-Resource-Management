package urms.util;

import java.awt.Color;

/**
 * Application-wide centralized color palette and UI theme constants.
 * Reflects Navrachana University Crimson branding (#B32025) and modern FlatLaf styling.
 */
public final class ColorConstants {

    private ColorConstants() {
        // Prevent instantiation of utility constants class
    }

    // ==========================================
    // 1. BRAND COLORS (Navrachana Crimson #B32025)
    // ==========================================
    public static final Color BRAND_CRIMSON        = new Color(179, 32, 37);  // Primary University Crimson
    public static final Color BRAND_CRIMSON_HOVER  = new Color(153, 24, 29);  // Darker shade for button hover/active
    public static final Color BRAND_CRIMSON_TINT   = new Color(254, 241, 242); // Soft light crimson highlight (#FEF1F2)
    public static final Color BRAND_CRIMSON_BORDER = new Color(252, 218, 220); // Delicate crimson badge outline

    // ==========================================
    // 2. TEXT & TYPOGRAPHY COLORS
    // ==========================================
    public static final Color TEXT_PRIMARY         = new Color(28, 78, 111);  // Primary heading dark navy/slate
    public static final Color TEXT_DARK            = new Color(30, 41, 59);   // Deep slate for table cells & primary labels
    public static final Color TEXT_MUTED           = new Color(110, 125, 136); // Subtitle and placeholder text
    public static final Color TEXT_SLATE           = new Color(71, 85, 105);  // Neutral label and icon text
    public static final Color TEXT_NAV_ACTIVE      = new Color(168, 28, 33);  // Selected sidebar navigation text
    public static final Color TEXT_NAV_INACTIVE    = new Color(95, 105, 115); // Unselected sidebar navigation text

    // ==========================================
    // 3. BACKGROUNDS & SURFACES
    // ==========================================
    public static final Color PAGE_BACKGROUND      = new Color(242, 246, 249); // Main application canvas background
    public static final Color CARD_BACKGROUND      = Color.WHITE;             // White cards, panels & dialog surfaces
    public static final Color SIDEBAR_BACKGROUND   = new Color(250, 250, 251); // Clean warm sidebar background
    public static final Color MODAL_HEADER_BG      = new Color(254, 248, 248); // Subtle crimson-tinted modal dialog header
    public static final Color TABLE_HEADER_BG      = new Color(248, 250, 252); // Flat table header surface
    public static final Color TABLE_ROW_ALT        = new Color(250, 252, 255); // Alternating table row background
    public static final Color TABLE_ROW_HOVER      = new Color(254, 241, 242); // Table row selection/hover tint
    public static final Color TABLE_GRID           = new Color(241, 245, 249); // Subtle table inner grid lines

    // ==========================================
    // 4. BORDERS & DIVIDERS
    // ==========================================
    public static final Color BORDER_SUBTLE        = new Color(221, 228, 232); // Main container dividing line
    public static final Color BORDER_DIVIDER       = new Color(232, 227, 227); // Sidebar divider
    public static final Color BORDER_CARD          = new Color(226, 232, 240); // Table card outer border
    public static final Color BORDER_FIELD         = new Color(203, 213, 225); // Text fields and combo boxes border

    // ==========================================
    // 5. STATUS BADGES & ALERTS
    // ==========================================
    // In Stock / Available (Green)
    public static final Color STATUS_AVAILABLE_BG     = new Color(220, 252, 231);
    public static final Color STATUS_AVAILABLE_TEXT   = new Color(22, 101, 52);
    public static final Color STATUS_AVAILABLE_BORDER = new Color(187, 247, 208);

    // Low Stock / Warning (Amber)
    public static final Color STATUS_LOW_STOCK_BG     = new Color(254, 243, 199);
    public static final Color STATUS_LOW_STOCK_TEXT   = new Color(180, 83, 9);
    public static final Color STATUS_LOW_STOCK_BORDER = new Color(253, 230, 138);

    // Out of Stock / Danger / Errors (Red)
    public static final Color STATUS_OUT_OF_STOCK_BG     = new Color(254, 226, 226);
    public static final Color STATUS_OUT_OF_STOCK_TEXT   = new Color(185, 28, 28);
    public static final Color STATUS_OUT_OF_STOCK_BORDER = new Color(254, 202, 202);

    public static final Color ERROR_RED            = new Color(185, 28, 28);
    public static final Color ERROR_BG             = new Color(254, 242, 242);
    public static final Color ERROR_BORDER         = new Color(254, 202, 202);

    // ==========================================
    // 6. SECONDARY ACCENTS
    // ==========================================
    public static final Color BLUE_ACCENT          = new Color(35, 93, 153);  // Secondary action blue
    public static final Color ICON_MUTED           = new Color(148, 163, 184); // Search & input leading icon color
}
