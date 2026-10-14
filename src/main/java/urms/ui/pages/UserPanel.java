package urms.ui.pages;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.User;
import urms.service.UserService;
import urms.ui.dialogs.UserModalDialog;


import static urms.util.ColorConstants.*;

<<<<<<< HEAD
import javax.swing.*;
=======
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
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
<<<<<<< HEAD
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
=======

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

>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Campus User Management Page.
 * Displays campus borrowers (students and faculty members) in a themed FlatLaf data table
 * with real-time search, role filters, status badges, and modal registration forms.
 * Mirrors the architecture and UI conventions of ResourcePanel.java.
 */
public class UserPanel extends JPanel {

    private JTextField searchField;
    private JComboBox<String> roleFilterCombo;
    private JComboBox<String> statusFilterCombo;
    private JLabel countBadge;

    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton editButton;
    private JButton deleteButton;

    // Cache of currently loaded campus users
    private List<User> currentUsers = new ArrayList<>();

    public UserPanel() {
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
     * Top branded header matching DefaultWindow / ResourcePanel / CategoryPanel styling.
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

        JLabel titleLabel = new JLabel("Users");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Manage registered students, faculty members, and campus borrowers");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        left.add(titleLabel);
        left.add(Box.createVerticalStrut(4));
        left.add(subtitleLabel);

        // Right button block: "+ Add User"
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JButton addBtn = new JButton("Add User");
        addBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        addBtn.setForeground(Color.WHITE);
        addBtn.setBackground(BRAND_CRIMSON);
        addBtn.setFocusPainted(false);
<<<<<<< HEAD
=======
        addBtn.setContentAreaFilled(false);
        addBtn.setOpaque(true);
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
        addBtn.setIcon(FontIcon.of(Material.PERSON_ADD, 18, Color.WHITE));
        addBtn.setIconTextGap(8);
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        addBtn.addActionListener(e -> openUserModal(null));

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
        searchField.setPreferredSize(new Dimension(250, 36));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search name or enrollment ID...");
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

        // Role Filter Combo
        roleFilterCombo = new JComboBox<>(new String[]{"All Roles", "STUDENT", "FACULTY"});
        roleFilterCombo.setPreferredSize(new Dimension(150, 36));
        roleFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roleFilterCombo.setBackground(CARD_BACKGROUND);
        roleFilterCombo.addActionListener(e -> applyFilters());

        // Status Filter Combo
        statusFilterCombo = new JComboBox<>(new String[]{"All Status", "Active Only", "Inactive"});
        statusFilterCombo.setPreferredSize(new Dimension(140, 36));
        statusFilterCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusFilterCombo.setBackground(CARD_BACKGROUND);
        statusFilterCombo.addActionListener(e -> applyFilters());

        // Reset / Refresh Button
        JButton refreshBtn = new JButton();
        refreshBtn.setIcon(FontIcon.of(Material.REFRESH, 18, TEXT_SLATE));
        refreshBtn.setToolTipText("Refresh directory data");
        refreshBtn.setPreferredSize(new Dimension(36, 36));
        refreshBtn.setBackground(CARD_BACKGROUND);
        refreshBtn.setFocusPainted(false);
<<<<<<< HEAD
=======
        refreshBtn.setContentAreaFilled(false);
        refreshBtn.setOpaque(true);
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
        refreshBtn.setBorder(new LineBorder(BORDER_FIELD, 1, true));
        refreshBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            roleFilterCombo.setSelectedIndex(0);
            statusFilterCombo.setSelectedIndex(0);
            loadData();
        });

        filterControls.add(searchField);
        filterControls.add(roleFilterCombo);
        filterControls.add(statusFilterCombo);
        filterControls.add(refreshBtn);

        // Right side: Count badge & Action Buttons
        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightControls.setOpaque(false);

        countBadge = new JLabel("0 users");
        countBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
        countBadge.setForeground(TEXT_SLATE);
        countBadge.setBorder(new EmptyBorder(8, 4, 8, 8));

        editButton = new JButton("View / Edit");
        editButton.setIcon(FontIcon.of(Material.VISIBILITY, 16, TEXT_SLATE));
        editButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        editButton.setBackground(CARD_BACKGROUND);
        editButton.setFocusPainted(false);
<<<<<<< HEAD
=======
        editButton.setContentAreaFilled(false);
        editButton.setOpaque(true);
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
        editButton.setEnabled(false);
        editButton.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(6, 12, 6, 12)));
        editButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> editSelectedUser());

        deleteButton = new JButton("Deactivate");
        deleteButton.setIcon(FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        deleteButton.setForeground(ERROR_RED);
        deleteButton.setBackground(CARD_BACKGROUND);
        deleteButton.setFocusPainted(false);
<<<<<<< HEAD
=======
        deleteButton.setContentAreaFilled(false);
        deleteButton.setOpaque(true);
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
        deleteButton.setEnabled(false);
        deleteButton.setBorder(new CompoundBorder(new LineBorder(ERROR_BORDER, 1, true), new EmptyBorder(6, 12, 6, 12)));
        deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> deleteSelectedUser());

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
                "S.No", "Enrollment / Staff ID", "Full Name", "Role", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Integer.class;
                }
                return String.class;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setRowHeight(42);
        userTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setShowGrid(true);
        userTable.setGridColor(TABLE_GRID);
        userTable.setSelectionBackground(TABLE_ROW_HOVER);
        userTable.setSelectionForeground(TEXT_DARK);
        userTable.setFillsViewportHeight(true);

        // Header Styling
        JTableHeader tableHeader = userTable.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(0, 40));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));
        tableHeader.setBackground(TABLE_HEADER_BG);
        tableHeader.setForeground(TEXT_SLATE);
        tableHeader.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tableHeader.getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        userTable.getColumnModel().getColumn(0).setMaxWidth(70);
        userTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(320);
        userTable.getColumnModel().getColumn(3).setPreferredWidth(140);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(130);

        // Center numeric serial number column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        userTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Custom badges for Role and Status columns
        userTable.getColumnModel().getColumn(3).setCellRenderer(new RoleBadgeRenderer());
        userTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());

        // Selection listener to enable/disable Edit and Delete buttons
        userTable.getSelectionModel().addListSelectionListener(e -> {
            boolean hasSelection = userTable.getSelectedRow() != -1;
            editButton.setEnabled(hasSelection);
            deleteButton.setEnabled(hasSelection);
        });

        // Double-click row listener to view/edit user
        userTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && userTable.getSelectedRow() != -1) {
                    editSelectedUser();
                }
            }
        });

        // Popup Context Menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("View User Details", FontIcon.of(Material.VISIBILITY, 16, TEXT_SLATE));
        editItem.addActionListener(e -> editSelectedUser());

        JMenuItem deleteItem = new JMenuItem("Deactivate Member", FontIcon.of(Material.DELETE_OUTLINE, 16, ERROR_RED));
        deleteItem.addActionListener(e -> deleteSelectedUser());

        contextMenu.add(editItem);
        contextMenu.addSeparator();
        contextMenu.add(deleteItem);

        userTable.setComponentPopupMenu(contextMenu);

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(CARD_BACKGROUND);

        card.add(scrollPane, BorderLayout.CENTER);
        return card;
    }

    /**
     * Loads all active campus users from the database and updates the table.
     */
    public void loadData() {
        try {
            currentUsers = UserService.getAllUsers();
            renderTableData(currentUsers);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading campus users: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Called when this tab becomes active, or when data needs refreshing.
     */
    public void refreshData() {
        loadData();
    }

    /**
     * Applies search query and role/status combo filters.
     */
    private void applyFilters() {
        String query = searchField.getText().trim();

        String selectedRole = (String) roleFilterCombo.getSelectedItem();
        if ("All Roles".equals(selectedRole)) {
            selectedRole = null;
        }

        int statusIdx = statusFilterCombo.getSelectedIndex();

        try {
            List<User> filtered = UserService.searchUsers(query, selectedRole);

            // Filter status
            if (statusIdx == 1) { // Active Only
                filtered = filtered.stream().filter(User::isActive).toList();
            } else if (statusIdx == 2) { // Inactive
                filtered = filtered.stream().filter(u -> !u.isActive()).toList();
            }

            currentUsers = filtered;
            renderTableData(filtered);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Filter error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderTableData(List<User> users) {
        tableModel.setRowCount(0);

        int serialNumber = 1;
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    serialNumber++,
                    u.getEnrollmentNo(),
                    u.getFullName(),
                    u.getRole(),
                    u.isActive() ? "ACTIVE" : "INACTIVE"
            });
        }

        countBadge.setText(users.size() + (users.size() == 1 ? " user" : " users"));
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private User getSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        if (modelRow >= 0 && modelRow < currentUsers.size()) {
            return currentUsers.get(modelRow);
        }
        return null;
    }

    private void openUserModal(User userToEdit) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        UserModalDialog dialog = new UserModalDialog(parentWindow, userToEdit, this::loadData);
        dialog.setVisible(true);
    }

    private void editSelectedUser() {
        User selected = getSelectedUser();
        if (selected != null) {
            openUserModal(selected);
        }
    }

    private void deleteSelectedUser() {
        User selected = getSelectedUser();
        if (selected == null) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to request deactivation for member:\n'"
                        + selected.getFullName() + "' (" + selected.getEnrollmentNo() + ")?",
                "Confirm Deactivation Request",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(
                    this,
                    "Campus member records are permanently preserved to safeguard allocation audits and loan histories.\n"
                            + "To mark an account as inactive or archive credentials, please submit a request to the University Registrar.",
                    "Audit Protected Record",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    /**
     * Custom renderer for rendering colorful pill badges for Roles (STUDENT vs FACULTY).
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
                // Purple / Plum badge for Faculty
                badge.setBackground(new Color(243, 232, 255));
                badge.setForeground(new Color(126, 34, 206));
                badge.setBorder(new CompoundBorder(new LineBorder(new Color(216, 180, 254), 1, true), new EmptyBorder(3, 10, 3, 10)));
            } else {
                // Blue badge for Student
                badge.setBackground(new Color(239, 246, 255));
                badge.setForeground(new Color(29, 78, 216));
                badge.setBorder(new CompoundBorder(new LineBorder(new Color(191, 219, 254), 1, true), new EmptyBorder(3, 10, 3, 10)));
            }

            return panel;
        }
    }

    /**
     * Custom renderer for rendering modern pill badges in the Status column.
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

            if ("ACTIVE".equalsIgnoreCase(status)) {
                badge.setBackground(STATUS_AVAILABLE_BG);
                badge.setForeground(STATUS_AVAILABLE_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            } else {
                badge.setBackground(STATUS_OUT_OF_STOCK_BG);
                badge.setForeground(STATUS_OUT_OF_STOCK_TEXT);
                badge.setBorder(new CompoundBorder(new LineBorder(STATUS_OUT_OF_STOCK_BORDER, 1, true), new EmptyBorder(3, 8, 3, 8)));
            }

            return panel;
        }
    }
}
