package urms.ui.pages;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Allocation;
import urms.service.AllocationService;
import urms.ui.dialogs.AllocationModalDialog;

import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern University Resource Allocations & Loan Management Page.
 * Displays resource checkouts, loan status, borrower info, and return logs in a themed FlatLaf table.
 * Provides real-time search, status filtering, one-click return workflows, and modal creation.
 */
public class AllocationPanel extends JPanel {

    private JTextField searchField;
    private JComboBox<String> statusFilterCombo;
    private JLabel countBadge;

    private JTable allocationTable;
    private DefaultTableModel tableModel;
    private JButton returnButton;
    private JButton viewButton;

    // Cache of currently loaded allocation records
    private List<Allocation> currentAllocations = new ArrayList<>();

    public AllocationPanel() {
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
     * Top branded header matching URMS design guidelines.
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

        JLabel titleLabel = new JLabel("Allocations");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Track resource checkouts, campus loans, due dates, and inventory returns");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitleLabel);

        // Right button block: "+ Issue Resource"
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton addBtn = new JButton("Issue Resource");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(BRAND_CRIMSON);
        addBtn.setFocusPainted(false);
        addBtn.setContentAreaFilled(false);
        addBtn.setOpaque(true);
        addBtn.setIcon(FontIcon.of(Material.SWAP_HORIZ, 18, Color.WHITE));
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
        addBtn.addActionListener(e -> openAllocationModal(null));

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
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search borrower, ID, or resource...");
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

        // Status Filter Combo
        statusFilterCombo = new JComboBox<>(new String[]{
                "All Status", "ACTIVE Only (On Loan)", "RETURNED", "OVERDUE (Pending)"
        });
        statusFilterCombo.setPreferredSize(new Dimension(190, 36));
        statusFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFilterCombo.setBackground(CARD_BACKGROUND);
        statusFilterCombo.addActionListener(e -> applyFilters());

        // Refresh Button
        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(FontIcon.of(Material.REFRESH, 18, TEXT_SLATE));
        refreshBtn.setToolTipText("Refresh allocations log");
        refreshBtn.setPreferredSize(new Dimension(36, 36));
        refreshBtn.setBackground(CARD_BACKGROUND);
        refreshBtn.setFocusPainted(false);
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setOpaque(true);
        refreshBtn.setBorder(new LineBorder(BORDER_FIELD, 1, true));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            loadData();
        });

        filterControls.add(searchField);
        filterControls.add(statusFilterCombo);
        filterControls.add(refreshBtn);

        // Right side: Count badge & Action Buttons
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        countBadge = new JLabel("0 records");
        countBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        countBadge.setForeground(TEXT_SLATE);
        countBadge.setBorder(new EmptyBorder(8, 4, 8, 8));

        viewButton = new JButton("View Details");
        viewButton.setIcon(FontIcon.of(Material.VISIBILITY, 16, TEXT_SLATE));
        viewButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        viewButton.setBackground(CARD_BACKGROUND);
        viewButton.setFocusPainted(false);
        viewButton.setContentAreaFilled(false);
        viewButton.setOpaque(true);
        viewButton.setEnabled(false);
        viewButton.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(6, 12, 6, 12)));
        viewButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewButton.addActionListener(e -> viewSelectedAllocation());

        returnButton = new JButton("Return Resource");
        returnButton.setIcon(FontIcon.of(Material.KEYBOARD_RETURN, 16, STATUS_AVAILABLE_TEXT));
        returnButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        returnButton.setForeground(STATUS_AVAILABLE_TEXT);
        returnButton.setBackground(STATUS_AVAILABLE_BG);
        returnButton.setFocusPainted(false);
        returnButton.setContentAreaFilled(false);
        returnButton.setOpaque(true);
        returnButton.setEnabled(false);
        returnButton.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(6, 14, 6, 14)));
        returnButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnButton.addActionListener(e -> returnSelectedResource());

        rightControls.add(countBadge);
        rightControls.add(viewButton);
        rightControls.add(returnButton);

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
                "S.No", "Borrower", "Role", "Resource Name", "Qty", "Start Date", "Due Date", "Return Date", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
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

        allocationTable = new JTable(tableModel);
        allocationTable.setRowHeight(42);
        allocationTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        allocationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        allocationTable.setShowGrid(true);
        allocationTable.setGridColor(TABLE_GRID);
        allocationTable.setSelectionBackground(TABLE_ROW_HOVER);
        allocationTable.setSelectionForeground(TEXT_DARK);
        allocationTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader tableHeader = allocationTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SLATE);
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Set column widths
        allocationTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        allocationTable.getColumnModel().getColumn(0).setMaxWidth(65);
        allocationTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        allocationTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        allocationTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        allocationTable.getColumnModel().getColumn(4).setPreferredWidth(55);
        allocationTable.getColumnModel().getColumn(4).setMaxWidth(70);
        allocationTable.getColumnModel().getColumn(5).setPreferredWidth(105);
        allocationTable.getColumnModel().getColumn(6).setPreferredWidth(105);
        allocationTable.getColumnModel().getColumn(7).setPreferredWidth(110);
        allocationTable.getColumnModel().getColumn(8).setPreferredWidth(115);

        // Center numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        allocationTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        allocationTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Custom badges for Role and Status columns
        allocationTable.getColumnModel().getColumn(2).setCellRenderer(new RoleBadgeRenderer());
        allocationTable.getColumnModel().getColumn(8).setCellRenderer(new AllocationStatusBadgeRenderer());

        // Selection listener to enable/disable action buttons
        allocationTable.getSelectionModel().addListSelectionListener(e -> updateButtonStates());

        // Double-click row listener to view allocation details
        allocationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && allocationTable.getSelectedRow() != -1) {
                    viewSelectedAllocation();
                }
            }
        });

        // Popup Context Menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem returnItem = new JMenuItem("Mark as Returned", FontIcon.of(Material.KEYBOARD_RETURN, 16, STATUS_AVAILABLE_TEXT));
        returnItem.addActionListener(e -> returnSelectedResource());

        JMenuItem viewItem = new JMenuItem("View Allocation Details", FontIcon.of(Material.VISIBILITY, 16, TEXT_SLATE));
        viewItem.addActionListener(e -> viewSelectedAllocation());

        contextMenu.add(returnItem);
        contextMenu.addSeparator();
        contextMenu.add(viewItem);

        allocationTable.setComponentPopupMenu(contextMenu);

        JScrollPane scrollPane = new JScrollPane(allocationTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    private void updateButtonStates() {
        Allocation selected = getSelectedAllocation();
        boolean hasSelection = (selected != null);
        viewButton.setEnabled(hasSelection);

        boolean canReturn = hasSelection && "ACTIVE".equalsIgnoreCase(selected.getStatus());
        returnButton.setEnabled(canReturn);
    }

    /**
     * Loads all allocation records from the database and updates the table.
     */
    public void loadData() {
        try {
            currentAllocations = AllocationService.getAllAllocations();
            renderTableData(currentAllocations);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading allocations: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called when this tab becomes active or when external actions require refresh.
     */
    public void refreshData() {
        loadData();
    }

    /**
     * Applies search query and status filters.
     */
    private void applyFilters() {
        String query = searchField.getText().trim();
        int statusIdx = statusFilterCombo.getSelectedIndex();

        String statusParam = "ALL";
        if (statusIdx == 1) {
            statusParam = "ACTIVE";
        } else if (statusIdx == 2) {
            statusParam = "RETURNED";
        }

        try {
            List<Allocation> filtered = AllocationService.searchAndFilter(query, statusParam);

            // Filter for OVERDUE if index 3 is selected
            if (statusIdx == 3) {
                LocalDate today = LocalDate.now();
                filtered = filtered.stream().filter(a -> {
                    if (!"ACTIVE".equalsIgnoreCase(a.getStatus()) || a.getDueDate() == null) {
                        return false;
                    }
                    try {
                        String cleanDate = a.getDueDate().length() >= 10 ? a.getDueDate().substring(0, 10) : a.getDueDate();
                        LocalDate due = LocalDate.parse(cleanDate);
                        return today.isAfter(due);
                    } catch (Exception ignored) {
                        return false;
                    }
                }).toList();
            }

            currentAllocations = filtered;
            renderTableData(filtered);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Filter error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderTableData(List<Allocation> list) {
        tableModel.setRowCount(0);

        int serialNumber = 1;
        for (Allocation a : list) {
            String borrowerText = (a.getUserFullName() != null ? a.getUserFullName() : "Unknown")
                    + " (" + (a.getUserEnrollmentNo() != null ? a.getUserEnrollmentNo() : "—") + ")";

            String startDate = a.getIssueDate() != null && a.getIssueDate().length() >= 10
                    ? a.getIssueDate().substring(0, 10)
                    : (a.getIssueDate() != null ? a.getIssueDate() : "—");

            String dueDate = a.getDueDate() != null && a.getDueDate().length() >= 10
                    ? a.getDueDate().substring(0, 10)
                    : (a.getDueDate() != null ? a.getDueDate() : "—");

            String returnDate = a.getReturnDate() != null && a.getReturnDate().length() >= 10
                    ? a.getReturnDate().substring(0, 10)
                    : "—";

            // Check if overdue
            String displayStatus = a.getStatus();
            if ("ACTIVE".equalsIgnoreCase(a.getStatus()) && a.getDueDate() != null) {
                try {
                    String cleanDue = a.getDueDate().length() >= 10 ? a.getDueDate().substring(0, 10) : a.getDueDate();
                    if (LocalDate.now().isAfter(LocalDate.parse(cleanDue))) {
                        displayStatus = "OVERDUE";
                    }
                } catch (Exception ignored) {}
            }

            tableModel.addRow(new Object[]{
                    serialNumber++,
                    borrowerText,
                    a.getUserRole() != null ? a.getUserRole() : "STUDENT",
                    a.getResourceName() != null ? a.getResourceName() : "Resource #" + a.getResourceId(),
                    a.getQuantity(),
                    startDate,
                    dueDate,
                    returnDate,
                    displayStatus
            });
        }

        countBadge.setText(list.size() + (list.size() == 1 ? " record" : " records"));
        updateButtonStates();
    }

    private Allocation getSelectedAllocation() {
        int selectedRow = allocationTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = allocationTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentAllocations.size()) {
            return currentAllocations.get(modelRow);
        }
        return null;
    }

    private void openAllocationModal(Allocation allocToView) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        AllocationModalDialog dialog = new AllocationModalDialog(parentWindow, allocToView, this::loadData);
        dialog.setVisible(true);
    }

    private void viewSelectedAllocation() {
        Allocation selected = getSelectedAllocation();
        if (selected != null) {
            openAllocationModal(selected);
        }
    }

    private void returnSelectedResource() {
        Allocation selected = getSelectedAllocation();
        if (selected == null) {
            return;
        }

        if (!"ACTIVE".equalsIgnoreCase(selected.getStatus())) {
            JOptionPane.showMessageDialog(this,
                    "This resource has already been returned on: " + selected.getReturnDate(),
                    "Already Returned", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String msg = "Confirm return of the following resource to inventory?\n\n"
                + "• Resource: " + selected.getResourceName() + "\n"
                + "• Quantity: " + selected.getQuantity() + " unit(s)\n"
                + "• Borrower: " + selected.getUserFullName() + " (" + selected.getUserEnrollmentNo() + ")\n"
                + "• Issued On: " + selected.getIssueDate() + "\n\n"
                + "Stock will automatically be incremented back to the available pool.";

        int choice = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Confirm Resource Return",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            try {
                AllocationService.returnResource(selected.getAllocationId());
                loadData();
                JOptionPane.showMessageDialog(
                        this,
                        "Resource '" + selected.getResourceName() + "' returned successfully.\nInventory stock updated.",
                        "Return Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Failed to process return: " + ex.getMessage(),
                        "Return Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Role Badge Renderer (Pill badge: Student vs Faculty).
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
