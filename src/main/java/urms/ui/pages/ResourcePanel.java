package urms.ui.pages;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Category;
import urms.model.Resource;
import urms.service.CategoryService;
import urms.service.ResourceService;
import urms.ui.dialogs.ResourceModalDialog;
import urms.util.ColorConstants;

import static urms.util.ColorConstants.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Resource Management Page.
 * Displays campus equipment, venues, and consumables in a themed FlatLaf data table
 * with real-time search, category filters, and modal input forms.
 */
public class ResourcePanel extends JPanel {

    private JTextField searchField;
    private JComboBox<Object> categoryFilterCombo;
    private JComboBox<String> stockFilterCombo;
    private JLabel countBadge;

    private JTable resourceTable;
    private DefaultTableModel tableModel;
    private JButton editButton;
    private JButton deleteButton;

    // Cache of currently loaded resources
    private List<Resource> currentResources = new ArrayList<>();

    public ResourcePanel() {
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

    /**
     * Top branded header matching DefaultWindow / CategoryPanel styling.
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

        JLabel titleLabel = new JLabel("Resources");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage university rooms, lab equipment, audiovisual inventory, and consumables");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitleLabel);

        // Right button block: "+ Add Resource"
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton addBtn = new JButton("Add Resource");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(BRAND_CRIMSON);
        addBtn.setFocusPainted(false);
        addBtn.setIcon(FontIcon.of(Material.ADD, 18, Color.WHITE));
        addBtn.setIconTextGap(8);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        addBtn.addActionListener(e -> openResourceModal(null));

        right.add(addBtn);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Search and Filter toolbar.
     */
    private JPanel createToolbar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        // Left side: Search field and filters
        JPanel filterControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterControls.setOpaque(false);

        // Search Field
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(240, 36));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search name or location...");
        searchField.putClientProperty("JTextField.showClearButton", true);
        searchField.putClientProperty("JTextField.leadingIcon", FontIcon.of(Material.SEARCH, 16, ICON_MUTED));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_FIELD, 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        // Category Filter Combo
        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.setPreferredSize(new Dimension(170, 36));
        categoryFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        categoryFilterCombo.setBackground(CARD_BACKGROUND);
        populateCategoryFilter();
        categoryFilterCombo.addActionListener(e -> applyFilters());

        // Stock Status Filter Combo
        stockFilterCombo = new JComboBox<>(new String[]{"All Stock", "In Stock Only", "Out of Stock"});
        stockFilterCombo.setPreferredSize(new Dimension(140, 36));
        stockFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stockFilterCombo.setBackground(CARD_BACKGROUND);
        stockFilterCombo.addActionListener(e -> applyFilters());

        // Reset / Refresh Button
        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(FontIcon.of(Material.REFRESH, 18, TEXT_SLATE));
        refreshBtn.setToolTipText("Refresh data and reload categories");
        refreshBtn.setPreferredSize(new Dimension(36, 36));
        refreshBtn.setBackground(CARD_BACKGROUND);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBorder(new LineBorder(BORDER_FIELD, 1, true));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            populateCategoryFilter();
            stockFilterCombo.setSelectedIndex(0);
            loadData();
        });

        filterControls.add(searchField);
        filterControls.add(categoryFilterCombo);
        filterControls.add(stockFilterCombo);
        filterControls.add(refreshBtn);

        // Right side: Count badge & Action Buttons
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        countBadge = new JLabel("0 items");
        countBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        countBadge.setForeground(TEXT_SLATE);
        countBadge.setBorder(new EmptyBorder(8, 4, 8, 8));

        editButton = new JButton("Edit");
        editButton.setIcon(FontIcon.of(Material.EDIT, 16, TEXT_SLATE));
        editButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        editButton.setBackground(CARD_BACKGROUND);
        editButton.setFocusPainted(false);
        editButton.setEnabled(false);
        editButton.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(6, 12, 6, 12)));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> editSelectedResource());

        deleteButton = new JButton("Delete");
        deleteButton.setIcon(FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deleteButton.setForeground(ERROR_RED);
        deleteButton.setBackground(CARD_BACKGROUND);
        deleteButton.setFocusPainted(false);
        deleteButton.setEnabled(false);
        deleteButton.setBorder(new CompoundBorder(new LineBorder(ERROR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteSelectedResource());

        rightControls.add(countBadge);
        rightControls.add(editButton);
        rightControls.add(deleteButton);

        bar.add(filterControls, BorderLayout.WEST);
        bar.add(rightControls, BorderLayout.EAST);
        return bar;
    }

    /**
     * Card container hosting the FlatLaf JTable.
     */
    private JPanel createTableCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BACKGROUND);
        card.setBorder(new LineBorder(BORDER_CARD, 1, true));

        String[] columnNames = {
                "S.No", "Resource Name", "Category", "Location",
                "Total Qty", "Available", "In Use", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 4 || columnIndex == 5 || columnIndex == 6) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        resourceTable = new JTable(tableModel);
        resourceTable.setRowHeight(42);
        resourceTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resourceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resourceTable.setShowGrid(true);
        resourceTable.setGridColor(TABLE_GRID);
        resourceTable.setSelectionBackground(TABLE_ROW_HOVER);
        resourceTable.setSelectionForeground(TEXT_DARK);
        resourceTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader tableHeader = resourceTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SLATE);
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Set column widths
        resourceTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        resourceTable.getColumnModel().getColumn(0).setMaxWidth(70);
        resourceTable.getColumnModel().getColumn(1).setPreferredWidth(280);
        resourceTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        resourceTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        resourceTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        resourceTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        resourceTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        resourceTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        // Center numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        resourceTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        resourceTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        resourceTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        resourceTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Custom status badge renderer
        resourceTable.getColumnModel().getColumn(7).setCellRenderer(new StatusBadgeRenderer());

        // Selection listener to enable/disable Edit and Delete buttons
        resourceTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = resourceTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        // Double-click row listener to edit
        resourceTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && resourceTable.getSelectedRow() != -1) {
                    editSelectedResource();
                }
            }
        });

        // Popup Context Menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("Edit Resource", FontIcon.of(Material.EDIT, 16, TEXT_SLATE));
        editItem.addActionListener(e -> editSelectedResource());

        JMenuItem deleteItem = new JMenuItem("Delete Resource", FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteItem.addActionListener(e -> deleteSelectedResource());

        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        resourceTable.setComponentPopupMenu(contextMenu);

        JScrollPane scrollPane = new JScrollPane(resourceTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void populateCategoryFilter() {
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All Categories");
        try {
            List<Category> categories = CategoryService.getActiveCategories();
            for (Category cat : categories) {
                categoryFilterCombo.addItem(cat);
            }
        } catch (Exception e) {
            // Category service fallback
        }

        categoryFilterCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category cat) {
                    setText(cat.getCategoryName());
                } else if (value != null) {
                    setText(value.toString());
                }
                return this;
            }
        });
    }

    /**
     * Loads all active resources from the database and updates the table.
     */
    public void loadData() {
        try {
            currentResources = ResourceService.getAllResources();
            renderTableData(currentResources);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading resources: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Applies search text and combo filters in memory or via database query.
     */
    private void applyFilters() {
        String query = searchField.getText().trim();

        String selectedCategoryId = null;
        Object selectedCat = categoryFilterCombo.getSelectedItem();
        if (selectedCat instanceof Category cat) {
            selectedCategoryId = cat.getCategoryId();
        }

        int stockIdx = stockFilterCombo.getSelectedIndex();

        try {
            List<Resource> filtered = ResourceService.searchAndFilter(query, selectedCategoryId);

            // Filter stock status
            if (stockIdx == 1) { // In Stock Only
                filtered = filtered.stream().filter(r -> r.getAvailableQuantity() > 0).toList();
            } else if (stockIdx == 2) { // Out of Stock
                filtered = filtered.stream().filter(r -> r.getAvailableQuantity() == 0).toList();
            }

            currentResources = filtered;
            renderTableData(filtered);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Filter error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderTableData(List<Resource> resources) {
        tableModel.setRowCount(0);

        int serialNumber = 1;
        for (Resource r : resources) {
            String status = "AVAILABLE";
            if (r.getAvailableQuantity() == 0) {
                status = "OUT OF STOCK";
            } else if (r.getAvailableQuantity() <= 2 && r.getTotalQuantity() > 2) {
                status = "LOW STOCK";
            }

            tableModel.addRow(new Object[]{
                    serialNumber++,
                    r.getResourceName(),
                    r.getCategoryName(),
                    (r.getLocation() != null && !r.getLocation().isEmpty()) ? r.getLocation() : "—",
                    r.getTotalQuantity(),
                    r.getAvailableQuantity(),
                    r.getCheckedOutQuantity(),
                    status
            });
        }

        countBadge.setText(resources.size() + (resources.size() == 1 ? " resource" : " resources"));
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private Resource getSelectedResource() {
        int selectedRow = resourceTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = resourceTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentResources.size()) {
            return currentResources.get(modelRow);
        }
        return null;
    }

    private void openResourceModal(Resource resourceToEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        ResourceModalDialog dialog = new ResourceModalDialog(parentWindow, resourceToEdit, () -> {
            loadData();
            populateCategoryFilter();
        });
        dialog.setVisible(true);
    }

    private void editSelectedResource() {
        Resource selected = getSelectedResource();
        if (selected != null) {
            openResourceModal(selected);
        }
    }

    private void deleteSelectedResource() {
        Resource selected = getSelectedResource();
        if (selected == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to deactivate '" + selected.getResourceName() + "'?\n"
                        + "This will hide it from the active inventory.",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                ResourceService.deleteResource(selected.getResourceId());
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Cannot Delete Resource",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Custom renderer for rendering colorful modern pill badges in the Status column.
     */
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private final JLabel badge = new JLabel();
        private final JPanel panel = new JPanel(new GridBagLayout());

        public StatusBadgeRenderer() {
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

            if ("AVAILABLE".equals(status)) {
                badge.setBackground(STATUS_AVAILABLE_BG);
                badge.setForeground(STATUS_AVAILABLE_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else if ("LOW STOCK".equals(status)) {
                badge.setBackground(STATUS_LOW_STOCK_BG);
                badge.setForeground(STATUS_LOW_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_LOW_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else {
                badge.setBackground(STATUS_OUT_OF_STOCK_BG);
                badge.setForeground(STATUS_OUT_OF_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_OUT_OF_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            }

            return panel;
        }
    }
}
