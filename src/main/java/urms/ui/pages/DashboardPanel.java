package urms.ui.pages;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Allocation;
import urms.model.DashboardStats;
import urms.service.DashboardService;
import urms.ui.SidebarPanel;

import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern University Resource Dashboard.
 * Displays aggregate KPI statistics and the 10 most recent allocation records.
 */
public class DashboardPanel extends JPanel {

    private final Consumer<String> onNavigate;

    // Stat metric value labels
    private JLabel usersValueLabel;
    private JLabel resourcesValueLabel;
    private JLabel categoriesValueLabel;
    private JLabel allocatedValueLabel;

    // Recent allocations table
    private JTable recentTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(16, 24, 20, 24));

        mainContent.add(createKpiGrid(), BorderLayout.NORTH);
        mainContent.add(createRecentAllocationsCard(), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Initial data load
        loadData();
    }

    /**
     * Branded top header matching URMS design system.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(18, 28, 18, 28)
        ));

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Overview of campus inventory, directory members, and real-time loans");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitleLabel);

        // Header right-side actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setIcon(FontIcon.of(Material.REFRESH, 16, TEXT_SLATE));
        refreshBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        refreshBtn.setBackground(CARD_BACKGROUND);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setOpaque(true);
        refreshBtn.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(6, 14, 6, 14)));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> loadData());

        actions.add(refreshBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    /**
     * Top 4 KPI Stat Cards arranged in a row.
     */
    private JPanel createKpiGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 4, 16, 0));
        grid.setOpaque(false);

        // 1. Total Users Card
        usersValueLabel = new JLabel("0");
        grid.add(createStatCard(
                "TOTAL USERS",
                usersValueLabel,
                "Active students & faculty",
                Material.PEOPLE,
                new Color(37, 99, 235),      // Blue accent
                new Color(239, 246, 255),    // Soft blue background
                new Color(191, 219, 254),    // Soft blue border
                "Users"
        ));

        // 2. Total Resources Card
        resourcesValueLabel = new JLabel("0");
        grid.add(createStatCard(
                "TOTAL RESOURCES",
                resourcesValueLabel,
                "Campus catalog items",
                Material.ALL_INBOX,
                BRAND_CRIMSON,               // Crimson accent
                BRAND_CRIMSON_TINT,          // Soft crimson background
                BRAND_CRIMSON_BORDER,        // Crimson border
                "Resources"
        ));

        // 3. Total Categories Card
        categoriesValueLabel = new JLabel("0");
        grid.add(createStatCard(
                "CATEGORIES",
                categoriesValueLabel,
                "Departments & groupings",
                Material.TOC,
                new Color(124, 58, 237),     // Purple accent
                new Color(245, 243, 255),    // Soft purple background
                new Color(221, 214, 254),    // Soft purple border
                "Categories"
        ));

        // 4. Resources Allocated Card
        allocatedValueLabel = new JLabel("0");
        grid.add(createStatCard(
                "RESOURCES ALLOCATED",
                allocatedValueLabel,
                "Currently on loan",
                Material.SWAP_HORIZ,
                new Color(5, 150, 105),      // Emerald accent
                new Color(236, 253, 245),    // Soft emerald background
                new Color(167, 243, 208),    // Soft emerald border
                "Allocations"
        ));

        return grid;
    }

    /**
     * Individual KPI Stat Card component.
     */
    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle,
                                 Material icon, Color accentColor, Color iconBg, Color iconBorder,
                                 String targetNav) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CARD, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top Row: Label + Icon Badge
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_MUTED);

        JPanel iconBadge = new JPanel(new GridBagLayout());
        iconBadge.setPreferredSize(new Dimension(36, 36));
        iconBadge.setBackground(iconBg);
        iconBadge.setBorder(new LineBorder(iconBorder, 1, true));
        JLabel iconLabel = new JLabel(FontIcon.of(icon, 18, accentColor));
        iconBadge.add(iconLabel);

        topRow.add(titleLabel, BorderLayout.WEST);
        topRow.add(iconBadge, BorderLayout.EAST);

        // Center: Bold Number
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_DARK);

        // Bottom: Subtitle
        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_SLATE);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.add(valueLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(subLabel);

        card.add(topRow, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        // Click to navigate directly to the matching section
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (targetNav != null) {
                    SidebarPanel.selectTab(targetNav);
                    if (onNavigate != null) {
                        onNavigate.accept(targetNav);
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(253, 254, 255));
                card.setBorder(new CompoundBorder(
                        new LineBorder(new Color(203, 213, 225), 1, true),
                        new EmptyBorder(16, 18, 16, 18)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD_BACKGROUND);
                card.setBorder(new CompoundBorder(
                        new LineBorder(BORDER_CARD, 1, true),
                        new EmptyBorder(16, 18, 16, 18)
                ));
            }
        });

        return card;
    }

    /**
     * Card container hosting the Last 10 Allocations Table.
     */
    private JPanel createRecentAllocationsCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_CARD, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Card Header Bar
        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setOpaque(false);

        JPanel titleArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleArea.setOpaque(false);

        JLabel historyIcon = new JLabel(FontIcon.of(Material.HISTORY, 20, BRAND_CRIMSON));
        JLabel tableTitle = new JLabel("Last 10 Allocations");
        tableTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_PRIMARY);

        JLabel tableSubtitle = new JLabel("•  Recent loan & checkout transactions");
        tableSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tableSubtitle.setForeground(TEXT_MUTED);

        titleArea.add(historyIcon);
        titleArea.add(tableTitle);
        titleArea.add(tableSubtitle);

        JButton viewAllBtn = new JButton("View All Allocations");
        viewAllBtn.setIcon(FontIcon.of(Material.ARROW_FORWARD, 14, BLUE_ACCENT));
        viewAllBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        viewAllBtn.setForeground(BLUE_ACCENT);
        viewAllBtn.setBackground(CARD_BACKGROUND);
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setOpaque(true);
        viewAllBtn.setBorder(new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1, true), new EmptyBorder(6, 12, 6, 12)));
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setHorizontalTextPosition(SwingConstants.LEFT);
        viewAllBtn.addActionListener(e -> {
            SidebarPanel.selectTab("Allocations");
            if (onNavigate != null) {
                onNavigate.accept("Allocations");
            }
        });

        cardHeader.add(titleArea, BorderLayout.WEST);
        cardHeader.add(viewAllBtn, BorderLayout.EAST);

        // Table Setup
        String[] columns = {
                "S.No", "Borrower", "Role", "Resource Name", "Qty", "Issue Date", "Due Date", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        recentTable = new JTable(tableModel);
        recentTable.setRowHeight(38);
        recentTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTable.setShowGrid(true);
        recentTable.setGridColor(TABLE_GRID);
        recentTable.setSelectionBackground(TABLE_ROW_HOVER);
        recentTable.setSelectionForeground(TEXT_DARK);
        recentTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader tableHeader = recentTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 38));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SLATE);
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Column widths
        recentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        recentTable.getColumnModel().getColumn(0).setMaxWidth(60);
        recentTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        recentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        recentTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        recentTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        recentTable.getColumnModel().getColumn(4).setMaxWidth(65);
        recentTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        recentTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        recentTable.getColumnModel().getColumn(7).setPreferredWidth(110);

        // Center S.No and Qty
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        recentTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        recentTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Custom badges
        recentTable.getColumnModel().getColumn(2).setCellRenderer(new RoleBadgeRenderer());
        recentTable.getColumnModel().getColumn(7).setCellRenderer(new AllocationStatusBadgeRenderer());

        // Double-click row jumps to Allocations view
        recentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && recentTable.getSelectedRow() != -1) {
                    SidebarPanel.selectTab("Allocations");
                    if (onNavigate != null) {
                        onNavigate.accept("Allocations");
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(TABLE_GRID, 1));
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * Refreshes the KPI stats and recent allocations table.
     */
    public void loadData() {
        // 1. Fetch & update KPI totals
        DashboardStats stats = DashboardService.getDashboardStats();
        usersValueLabel.setText(String.valueOf(stats.getTotalUsers()));
        resourcesValueLabel.setText(String.valueOf(stats.getTotalResources()));
        categoriesValueLabel.setText(String.valueOf(stats.getTotalCategories()));
        allocatedValueLabel.setText(String.valueOf(stats.getTotalAllocatedResources()));

        // 2. Fetch & update Last 10 allocations
        List<Allocation> recent = DashboardService.getRecentAllocations(10);
        tableModel.setRowCount(0);

        LocalDate today = LocalDate.now();
        int sNo = 1;
        for (Allocation a : recent) {
            String status = a.getStatus();
            if ("ACTIVE".equalsIgnoreCase(status) && a.getDueDate() != null) {
                try {
                    LocalDate dueDate = LocalDate.parse(a.getDueDate().substring(0, 10));
                    if (dueDate.isBefore(today)) {
                        status = "OVERDUE";
                    }
                } catch (Exception ignored) {
                }
            }

            tableModel.addRow(new Object[]{
                    sNo++,
                    a.getUserFullName() != null ? a.getUserFullName() : "N/A",
                    a.getUserRole() != null ? a.getUserRole() : "STUDENT",
                    a.getResourceName() != null ? a.getResourceName() : "N/A",
                    a.getQuantity(),
                    a.getIssueDate() != null ? a.getIssueDate().substring(0, Math.min(10, a.getIssueDate().length())) : "N/A",
                    a.getDueDate() != null ? a.getDueDate().substring(0, Math.min(10, a.getDueDate().length())) : "N/A",
                    status
            });
        }
    }

    /**
     * Called whenever switching back to Dashboard.
     */
    public void refreshData() {
        loadData();
    }

    /**
     * Role Badge Renderer (STUDENT: Blue, FACULTY: Purple).
     */
    private static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        private final JLabel badge = new JLabel();
        private final JPanel panel = new JPanel(new GridBagLayout());

        public RoleBadgeRenderer() {
            badge.setOpaque(true);
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            panel.setOpaque(true);
            panel.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT));

            String role = (value != null) ? value.toString() : "";
            badge.setText(role);

            if ("FACULTY".equalsIgnoreCase(role)) {
                badge.setBackground(new Color(243, 232, 255));
                badge.setForeground(new Color(126, 34, 206));
                badge.setBorder(new CompoundBorder(new LineBorder(new Color(216, 180, 254), 1, true), new EmptyBorder(3, 10, 3, 10)));
            } else {
                badge.setBackground(new Color(239, 246, 255));
                badge.setForeground(new Color(29, 78, 216));
                badge.setBorder(new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1, true), new EmptyBorder(3, 10, 3, 10)));
            }

            return panel;
        }
    }

    /**
     * Allocation Status Badge Renderer:
     * - RETURNED: Green
     * - ACTIVE: Amber
     * - OVERDUE: Red
     */
    private static class AllocationStatusBadgeRenderer extends DefaultTableCellRenderer {
        private final JLabel badge = new JLabel();
        private final JPanel panel = new JPanel(new GridBagLayout());

        public AllocationStatusBadgeRenderer() {
            badge.setOpaque(true);
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));
            panel.setOpaque(true);
            panel.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT));

            String status = (value != null) ? value.toString() : "";
            badge.setText(status);

            if ("RETURNED".equalsIgnoreCase(status)) {
                badge.setBackground(STATUS_AVAILABLE_BG);
                badge.setForeground(STATUS_AVAILABLE_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else if ("OVERDUE".equalsIgnoreCase(status)) {
                badge.setBackground(STATUS_OUT_OF_STOCK_BG);
                badge.setForeground(STATUS_OUT_OF_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_OUT_OF_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else {
                badge.setBackground(STATUS_LOW_STOCK_BG);
                badge.setForeground(STATUS_LOW_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_LOW_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            }

            return panel;
        }
    }
}
