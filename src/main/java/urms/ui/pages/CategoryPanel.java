package urms.ui.pages;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Category;
import urms.model.CategorySummary;
import urms.service.CategoryService;
import urms.dao.CategoryDAO;
import urms.ui.dialogs.CategoryModalDialog;

import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing resource categories.
 * Displays categories in a themed FlatLaf data table with real-time search,
 * resource count badges, and modal input forms for add/edit operations.
 * Follows the same architecture and Navrachana Crimson branding as ResourcePanel.
 */
public class CategoryPanel extends JPanel {

    private JTextField searchField;
    private JLabel countBadge;

    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JButton editButton;
    private JButton deleteButton;

    // Empty state panel shown when no categories exist
    private JPanel emptyStatePanel;
    private JScrollPane tableScrollPane;

    // Cache of currently displayed category summaries (with resource counts)
    private List<CategorySummary> currentSummaries = new ArrayList<>();

    public CategoryPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 12));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(16, 24, 20, 24));

        mainContent.add(createToolbar(), BorderLayout.NORTH);
        mainContent.add(createTableCard(), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Load data on initialization
        loadData();
    }

    // ============================================================
    // HEADER — Title, subtitle, and "+ Add Category" crimson button
    // ============================================================

    /**
     * Top branded header with title, subtitle, and "Add Category" button.
     * Matches ResourcePanel header: white card bg, bottom border, 22pt bold title.
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(18, 28, 18, 28)
        ));

        // Left text block
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel("Categories");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Organize and manage campus resource categories");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitleLabel);

        // Right button block: "+ Add Category" — crimson with hover darkening
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton addBtn = new JButton("Add Category");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(BRAND_CRIMSON);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setOpaque(true);
        addBtn.setIcon(FontIcon.of(Material.ADD, 18, Color.WHITE));
        addBtn.setIconTextGap(8);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addBtn.setBackground(BRAND_CRIMSON_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                addBtn.setBackground(BRAND_CRIMSON);
            }
        });
        addBtn.addActionListener(e -> openCategoryModal(null));

        right.add(addBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ============================================================
    // TOOLBAR — Search, refresh, count badge, edit/delete buttons
    // ============================================================

    /**
     * Search and action toolbar matching ResourcePanel toolbar styling.
     */
    private JPanel createToolbar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        // Left side: Search field and refresh
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterControls.setOpaque(false);

        // Search Field — FlatLaf styled with leading search icon
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(260, 36));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search categories...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.leadingIcon", FontIcon.of(Material.SEARCH, 16, ICON_MUTED));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_FIELD, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Refresh Button — icon-only outlined button
        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(FontIcon.of(Material.REFRESH, 18, TEXT_SLATE));
        refreshBtn.setToolTipText("Refresh categories");
        refreshBtn.setPreferredSize(new Dimension(36, 36));
        refreshBtn.setBackground(CARD_BACKGROUND);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setOpaque(true);
        refreshBtn.setBorder(new LineBorder(BORDER_FIELD, 1, true));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                refreshBtn.setBackground(PAGE_BACKGROUND);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                refreshBtn.setBackground(CARD_BACKGROUND);
            }
        });
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadData();
        });

        filterControls.add(searchField);
        filterControls.add(refreshBtn);

        // Right side: Count badge & Action Buttons
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        countBadge = new JLabel("0 categories");
        countBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        countBadge.setForeground(TEXT_SLATE);
        countBadge.setBorder(new EmptyBorder(8, 4, 8, 8));

        editButton = new JButton("Edit");
        editButton.setIcon(FontIcon.of(Material.EDIT, 16, TEXT_SLATE));
        editButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        editButton.setBackground(CARD_BACKGROUND);
        editButton.setFocusPainted(false);
        editButton.setContentAreaFilled(false);
        editButton.setOpaque(true);
        editButton.setEnabled(false);
        editButton.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(6, 12, 6, 12)));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> editSelectedCategory());

        deleteButton = new JButton("Delete");
        deleteButton.setIcon(FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deleteButton.setForeground(ERROR_RED);
        deleteButton.setBackground(CARD_BACKGROUND);
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setOpaque(true);
        deleteButton.setEnabled(false);
        deleteButton.setBorder(new CompoundBorder(new LineBorder(ERROR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteSelectedCategory());

        rightControls.add(countBadge);
        rightControls.add(editButton);
        rightControls.add(deleteButton);

        bar.add(filterControls, BorderLayout.WEST);
        bar.add(rightControls, BorderLayout.EAST);
        return bar;
    }

    // ============================================================
    // TABLE CARD — Styled JTable with pill-badge renderers
    // ============================================================

    /**
     * Card container hosting the themed JTable.
     * Includes alternating row colors, status/count badge renderers,
     * context menu, double-click-to-edit, and an empty state overlay.
     */
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new LineBorder(BORDER_CARD, 1, true));

        String[] columnNames = {
                "S.No", "Category Name", "Description", "Resources", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(44);
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.setShowGrid(true);
        categoryTable.setGridColor(TABLE_GRID);
        categoryTable.setSelectionBackground(TABLE_ROW_HOVER);
        categoryTable.setSelectionForeground(TEXT_DARK);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setIntercellSpacing(new Dimension(0, 0));

        // Header Styling — flat muted header matching ResourcePanel
        JTableHeader tableHeader = categoryTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SLATE);
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Set column widths
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        categoryTable.getColumnModel().getColumn(0).setMaxWidth(75);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        categoryTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        categoryTable.getColumnModel().getColumn(4).setPreferredWidth(120);

        // S.No — centered with alternating row stripe
        categoryTable.getColumnModel().getColumn(0).setCellRenderer(new StripedCenterRenderer());

        // Category Name — bold left-aligned with alternating row stripe
        categoryTable.getColumnModel().getColumn(1).setCellRenderer(new CategoryNameRenderer());

        // Description — muted text with alternating row stripe
        categoryTable.getColumnModel().getColumn(2).setCellRenderer(new DescriptionRenderer());

        // Resources — pill badge renderer (crimson tint when > 0, muted when 0)
        categoryTable.getColumnModel().getColumn(3).setCellRenderer(new ResourceCountBadgeRenderer());

        // Status — pill badge renderer (green "IN USE" / amber "EMPTY")
        categoryTable.getColumnModel().getColumn(4).setCellRenderer(new CategoryStatusRenderer());

        // Selection listener to enable/disable Edit and Delete buttons
        categoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = categoryTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        // Double-click row listener to edit
        categoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && categoryTable.getSelectedRow() != -1) {
                    editSelectedCategory();
                }
            }
        });

        // Popup Context Menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Category", FontIcon.of(Material.EDIT, 16, TEXT_SLATE));
        editItem.setFont(new Font("SansSerif", Font.PLAIN, 13));
        editItem.addActionListener(e -> editSelectedCategory());

        JMenuItem deleteItem = new JMenuItem("Delete Category", FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteItem.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deleteItem.setForeground(ERROR_RED);
        deleteItem.addActionListener(e -> deleteSelectedCategory());

        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        categoryTable.setComponentPopupMenu(contextMenu);

        tableScrollPane = new JScrollPane(categoryTable);
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        tableScrollPane.getViewport().setBackground(CARD_BACKGROUND);

        // Empty state panel — shown when no categories exist
        emptyStatePanel = createEmptyState();

        card.add(tableScrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a visually rich empty state panel shown when no categories are loaded.
     */
    private JPanel createEmptyState() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(CARD_BACKGROUND);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Muted folder icon
        JLabel iconLabel = new JLabel(FontIcon.of(Material.FOLDER_OPEN, 48, ICON_MUTED));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("No Categories Found");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("Create your first category to start organizing resources.");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setForeground(TEXT_MUTED);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // "Add Category" ghost button in the empty state
        JButton addBtn = new JButton("Add Category");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(BRAND_CRIMSON);
        addBtn.setBackground(BRAND_CRIMSON_TINT);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setOpaque(true);
        addBtn.setIcon(FontIcon.of(Material.ADD, 16, BRAND_CRIMSON));
        addBtn.setIconTextGap(6);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BRAND_CRIMSON_BORDER, 1, true),
                new EmptyBorder(8, 20, 8, 20)
        ));
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                addBtn.setBackground(BRAND_CRIMSON);
                addBtn.setForeground(Color.WHITE);
                addBtn.setIcon(FontIcon.of(Material.ADD, 16, Color.WHITE));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                addBtn.setBackground(BRAND_CRIMSON_TINT);
                addBtn.setForeground(BRAND_CRIMSON);
                addBtn.setIcon(FontIcon.of(Material.ADD, 16, BRAND_CRIMSON));
            }
        });
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(e -> openCategoryModal(null));

        content.add(iconLabel);
        content.add(Box.createVerticalStrut(12));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(descLabel);
        content.add(Box.createVerticalStrut(18));
        content.add(addBtn);

        panel.add(content);
        return panel;
    }

    // ============================================================
    // DATA LOADING, FILTERING, AND TABLE RENDERING
    // ============================================================

    /**
     * Loads all active categories with their resource counts and updates the table.
     */
    private void loadData() {
        try {
            List<Category> categories = CategoryService.getActiveCategories();
            currentSummaries = new ArrayList<>();

            for (Category cat : categories) {
                int resourceCount = 0;
                try {
                    resourceCount = CategoryDAO.countActiveResources(cat.getCategoryId());
                } catch (Exception ignored) {
                    // If resource count fails for one category, continue with 0
                }
                currentSummaries.add(new CategorySummary(
                        cat.getCategoryId(),
                        cat.getCategoryName(),
                        cat.getDescription(),
                        resourceCount,
                        cat.isActive()
                ));
            }

            renderTableData(currentSummaries);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading categories: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Applies the search text filter on the loaded category summaries.
     */
    private void applyFilter() {
        String query = searchField.getText().trim().toLowerCase();

        if (query.isEmpty()) {
            renderTableData(currentSummaries);
            return;
        }

        List<CategorySummary> filtered = currentSummaries.stream()
                .filter(cs -> cs.categoryName().toLowerCase().contains(query)
                        || (cs.description() != null && cs.description().toLowerCase().contains(query)))
                .toList();

        renderTableData(filtered);
    }

    /**
     * Renders the category data into the table model and toggles empty state visibility.
     */
    private void renderTableData(List<CategorySummary> summaries) {
        tableModel.setRowCount(0);

        int serialNumber = 1;
        for (CategorySummary cs : summaries) {
            String status = cs.resourceCount() > 0 ? "IN USE" : "EMPTY";

            tableModel.addRow(new Object[]{
                    serialNumber++,
                    cs.categoryName(),
                    (cs.description() != null && !cs.description().isEmpty()) ? cs.description() : "—",
                    cs.resourceCount(),
                    status
            });
        }

        countBadge.setText(summaries.size() + (summaries.size() == 1 ? " category" : " categories"));
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);

        // Toggle empty state vs table
        JPanel card = (JPanel) tableScrollPane.getParent();
        if (summaries.isEmpty()) {
            card.remove(tableScrollPane);
            card.add(emptyStatePanel, BorderLayout.CENTER);
        } else {
            card.remove(emptyStatePanel);
            card.add(tableScrollPane, BorderLayout.CENTER);
        }
        card.revalidate();
        card.repaint();
    }

    // ============================================================
    // SELECTION HELPERS AND MODAL INTEGRATION
    // ============================================================

    /**
     * Returns the CategorySummary for the currently selected table row,
     * accounting for any active search filter that changes the visible list.
     */
    private CategorySummary getSelectedCategorySummary() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = categoryTable.convertRowIndexToModel(selectedRow);

        // Build the currently visible list (same logic as applyFilter)
        String query = searchField.getText().trim().toLowerCase();
        List<CategorySummary> visibleList;
        if (query.isEmpty()) {
            visibleList = currentSummaries;
        } else {
            visibleList = currentSummaries.stream()
                    .filter(cs -> cs.categoryName().toLowerCase().contains(query)
                            || (cs.description() != null && cs.description().toLowerCase().contains(query)))
                    .toList();
        }

        if (modelRow >= 0 && modelRow < visibleList.size()) {
            return visibleList.get(modelRow);
        }
        return null;
    }

    /**
     * Opens the CategoryModalDialog for add (null) or edit (existing Category).
     * On success, refreshes the table via loadData callback.
     */
    private void openCategoryModal(Category categoryToEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        CategoryModalDialog dialog = new CategoryModalDialog(parentWindow, categoryToEdit, this::loadData);
        dialog.setVisible(true);
    }

    /**
     * Converts the selected summary back to a Category and opens the edit modal.
     */
    private void editSelectedCategory() {
        CategorySummary selected = getSelectedCategorySummary();
        if (selected == null) {
            return;
        }

        // Convert CategorySummary back to Category for the modal
        Category cat = new Category(
                selected.categoryId(),
                selected.categoryName(),
                selected.description(),
                selected.isActive()
        );
        openCategoryModal(cat);
    }

    /**
     * Prompts the user and soft-deletes the selected category.
     * Shows a warning if active resources are linked.
     */
    private void deleteSelectedCategory() {
        CategorySummary selected = getSelectedCategorySummary();
        if (selected == null) {
            return;
        }

        if (selected.resourceCount() > 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete '" + selected.categoryName() + "' because it has "
                            + selected.resourceCount() + " active resource(s) associated with it.\n"
                            + "Please reassign or remove those resources first.",
                    "Cannot Delete Category",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete '" + selected.categoryName() + "'?\n"
                        + "This will deactivate the category from the system.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                CategoryService.deleteCategory(selected.categoryId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Cannot Delete Category",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Called when this tab becomes active, or when data needs refreshing.
     */
    public void refreshData() {
        loadData();
    }

    // ============================================================
    // CUSTOM TABLE CELL RENDERERS
    // Provide alternating row striping, themed text, and pill badges
    // matching the ResourcePanel's StatusBadgeRenderer pattern.
    // ============================================================

    /**
     * Centered cell renderer with alternating row stripe colors.
     * Used for the S.No column.
     */
    private static class StripedCenterRenderer extends DefaultTableCellRenderer {
        public StripedCenterRenderer() {
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("SansSerif", Font.PLAIN, 13));
            setForeground(isSelected ? TEXT_DARK : TEXT_MUTED);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT);
            }
            setBorder(new EmptyBorder(0, 8, 0, 8));
            return this;
        }
    }

    /**
     * Bold category name renderer with alternating row stripe colors.
     * Makes the name column stand out as the primary identifier.
     */
    private static class CategoryNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("SansSerif", Font.BOLD, 13));
            setForeground(isSelected ? TEXT_DARK : TEXT_PRIMARY);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT);
            }
            setBorder(new EmptyBorder(0, 10, 0, 8));
            return this;
        }
    }

    /**
     * Muted description renderer with alternating row stripe colors.
     * Uses TEXT_SLATE for a softer secondary appearance.
     */
    private static class DescriptionRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setFont(new Font("SansSerif", Font.PLAIN, 12));
            setForeground(isSelected ? TEXT_DARK : TEXT_SLATE);
            if (!isSelected) {
                setBackground(row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT);
            }
            setBorder(new EmptyBorder(0, 10, 0, 8));
            return this;
        }
    }

    /**
     * Renders the resource count as a small colored pill badge.
     * Crimson tint when count > 0, muted gray when 0.
     */
    private static class ResourceCountBadgeRenderer extends DefaultTableCellRenderer {
        private final JLabel badge = new JLabel();
        private final JPanel panel = new JPanel(new GridBagLayout());

        public ResourceCountBadgeRenderer() {
            badge.setOpaque(true);
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(new EmptyBorder(3, 10, 3, 10));
            panel.setOpaque(true);
            panel.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT));

            int count = (value instanceof Integer) ? (Integer) value : 0;
            badge.setText(String.valueOf(count));

            if (count > 0) {
                badge.setBackground(BRAND_CRIMSON_TINT);
                badge.setForeground(BRAND_CRIMSON);
                badge.setBorder(new CompoundBorder(new LineBorder(BRAND_CRIMSON_BORDER, 1, true), new EmptyBorder(3, 10, 3, 10)));
            } else {
                badge.setBackground(TABLE_HEADER_BG);
                badge.setForeground(TEXT_MUTED);
                badge.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(3, 10, 3, 10)));
            }

            return panel;
        }
    }

    /**
     * Renders the category status ("IN USE" / "EMPTY") as a colored pill badge.
     * Green for IN USE, amber for EMPTY — matching ResourcePanel's StatusBadgeRenderer.
     */
    private static class CategoryStatusRenderer extends DefaultTableCellRenderer {
        private final JLabel badge = new JLabel();
        private final JPanel panel = new JPanel(new GridBagLayout());

        public CategoryStatusRenderer() {
            badge.setOpaque(true);
            badge.setFont(new Font("SansSerif", Font.BOLD, 11));
            badge.setHorizontalAlignment(SwingConstants.CENTER);
            badge.setBorder(new EmptyBorder(3, 8, 3, 8));
            panel.setOpaque(true);
            panel.add(badge);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? CARD_BACKGROUND : TABLE_ROW_ALT));

            String status = (value != null) ? value.toString() : "";
            badge.setText(status);

            if ("IN USE".equals(status)) {
                badge.setBackground(STATUS_AVAILABLE_BG);
                badge.setForeground(STATUS_AVAILABLE_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else {
                badge.setBackground(STATUS_LOW_STOCK_BG);
                badge.setForeground(STATUS_LOW_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_LOW_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            }

            return panel;
        }
    }
}
