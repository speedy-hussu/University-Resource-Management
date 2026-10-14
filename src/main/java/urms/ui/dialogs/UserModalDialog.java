package urms.ui.dialogs;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.User;
import urms.service.UserService;

<<<<<<< HEAD

import static urms.util.ColorConstants.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
=======
import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4

/**
 * Modern modal dialog for registering campus borrowers (students and faculty)
 * and inspecting user account profiles.
 * Follows the URMS University Crimson design language and FlatLaf styling.
 */
public class UserModalDialog extends JDialog {

    private final User userToEdit;
    private final Runnable onSuccessCallback;

    private JTextField enrollmentField;
    private JTextField nameField;
    private JComboBox<String> roleComboBox;
    private JLabel errorBanner;
    private JButton saveButton;

    public UserModalDialog(Window owner, User userToEdit, Runnable onSuccessCallback) {
        super(owner, userToEdit == null ? "Register Campus User" : "Campus User Details", ModalityType.APPLICATION_MODAL);
        this.userToEdit = userToEdit;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        populateFieldsIfEditing();

        setSize(500, 520);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BACKGROUND);

        // Header Panel
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // Form Body Panel
        root.add(createFormPanel(), BorderLayout.CENTER);

        // Footer Actions Panel
        root.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(MODAL_HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                new EmptyBorder(20, 24, 20, 24)
        ));

        // Crimson circular badge for icon
        JPanel iconBadge = new JPanel(new GridBagLayout());
        iconBadge.setPreferredSize(new Dimension(46, 46));
        iconBadge.setBackground(BRAND_CRIMSON_TINT);
        iconBadge.setBorder(BorderFactory.createLineBorder(BRAND_CRIMSON_BORDER, 1, true));

        Material iconMat = (userToEdit == null) ? Material.PERSON_ADD : Material.PERSON;
        JLabel iconLabel = new JLabel(FontIcon.of(iconMat, 24, BRAND_CRIMSON));
        iconBadge.add(iconLabel);

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        String titleText = (userToEdit == null) ? "Register Campus Member" : "Campus Borrower Details";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(TEXT_PRIMARY);

        String subText = (userToEdit == null)
                ? "Enter student enrollment roll or faculty staff ID to grant borrowing access."
                : "View borrower profile, affiliation role, and directory credentials.";
        JLabel subtitle = new JLabel(subText);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        textGroup.add(title);
        textGroup.add(Box.createVerticalStrut(4));
        textGroup.add(subtitle);

        header.add(iconBadge, BorderLayout.WEST);
        header.add(textGroup, BorderLayout.CENTER);

        return header;
    }

    private JPanel createFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(16, 24, 10, 24));

        // Error message banner
        errorBanner = new JLabel(" ");
        errorBanner.setOpaque(true);
        errorBanner.setBackground(ERROR_BG);
        errorBanner.setForeground(ERROR_RED);
        errorBanner.setFont(new Font("SansSerif", Font.BOLD, 12));
        errorBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ERROR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));
        errorBanner.setVisible(false);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int row = 0;

        // 1. Enrollment Number / Staff ID
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Enrollment / Staff ID *"), gbc);
        gbc.gridy = row++;
        enrollmentField = new JTextField();
        styleTextField(enrollmentField, "e.g. 22BCE104 or FAC-CSE-014");
        fieldsPanel.add(enrollmentField, gbc);

        // 2. Full Name
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Full Name *"), gbc);
        gbc.gridy = row++;
        nameField = new JTextField();
        styleTextField(nameField, "e.g. Aryan Verma or Dr. Rajesh Sharma");
        fieldsPanel.add(nameField, gbc);

        // 3. Role
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Campus Role *"), gbc);
        gbc.gridy = row++;
        roleComboBox = createRoleComboBox();
        fieldsPanel.add(roleComboBox, gbc);

        // 4. Status Indicator (shown when inspecting existing user)
        if (userToEdit != null) {
            gbc.gridy = row++;
            fieldsPanel.add(createFieldLabel("Directory Account Status"), gbc);
            gbc.gridy = row++;

            JPanel statusChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            statusChip.setOpaque(false);

            JLabel chipLabel = new JLabel(userToEdit.isActive() ? "Active Campus Borrower" : "Inactive / Suspended");
            chipLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            chipLabel.setOpaque(true);
            if (userToEdit.isActive()) {
                chipLabel.setBackground(STATUS_AVAILABLE_BG);
                chipLabel.setForeground(STATUS_AVAILABLE_TEXT);
                chipLabel.setBorder(new CompoundBorder(new LineBorder(STATUS_AVAILABLE_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
            } else {
                chipLabel.setBackground(STATUS_OUT_OF_STOCK_BG);
                chipLabel.setForeground(STATUS_OUT_OF_STOCK_TEXT);
                chipLabel.setBorder(new CompoundBorder(new LineBorder(STATUS_OUT_OF_STOCK_BORDER, 1, true), new EmptyBorder(4, 10, 4, 10)));
            }
            statusChip.add(chipLabel);
            fieldsPanel.add(statusChip, gbc);
        }

        container.add(errorBanner, BorderLayout.NORTH);
        container.add(fieldsPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createFooterPanel() {
<<<<<<< HEAD
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));

        JButton cancelButton = new JButton(userToEdit == null ? "Cancel" : "Close");
        cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cancelButton.setForeground(TEXT_SLATE);
        cancelButton.setBackground(CARD_BACKGROUND);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_FIELD, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());

        saveButton = new JButton(userToEdit == null ? "Register User" : "Done");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(BRAND_CRIMSON);
        saveButton.setFocusPainted(false);
        saveButton.setIcon(FontIcon.of(Material.CHECK, 16, Color.WHITE));
        saveButton.setBorder(new EmptyBorder(9, 20, 9, 20));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

=======
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 18));
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));

        // --- Cancel / Close Button (ghost style with hover) ---
        JButton cancelButton = new JButton(userToEdit == null ? "Cancel" : "Close");
        cancelButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        cancelButton.setForeground(TEXT_SLATE);
        cancelButton.setBackground(CARD_BACKGROUND);
        cancelButton.setFocusPainted(false);
        cancelButton.setContentAreaFilled(false);
        cancelButton.setOpaque(true);
        cancelButton.setIcon(FontIcon.of(Material.CLOSE, 15, TEXT_SLATE));
        cancelButton.setIconTextGap(6);
        cancelButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_FIELD, 1, true),
                new EmptyBorder(9, 20, 9, 20)
        ));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setBackground(PAGE_BACKGROUND);
                cancelButton.setForeground(TEXT_DARK);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setBackground(CARD_BACKGROUND);
                cancelButton.setForeground(TEXT_SLATE);
            }
        });
        cancelButton.addActionListener(e -> dispose());

        // --- Primary Action Button (Crimson or Blue accent with hover) ---
        boolean isViewMode = (userToEdit != null);
        Color primaryBg = isViewMode ? BLUE_ACCENT : BRAND_CRIMSON;
        Color primaryHover = isViewMode ? new Color(28, 75, 125) : BRAND_CRIMSON_HOVER;
        Material primaryIcon = isViewMode ? Material.CHECK_CIRCLE : Material.PERSON_ADD;
        String primaryText = isViewMode ? "Done" : "Register User";

        saveButton = new JButton(primaryText);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(primaryBg);
        saveButton.setFocusPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setOpaque(true);
        saveButton.setIcon(FontIcon.of(primaryIcon, 16, Color.WHITE));
        saveButton.setIconTextGap(8);
        saveButton.setBorder(new EmptyBorder(10, 24, 10, 24));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (saveButton.isEnabled()) {
                    saveButton.setBackground(primaryHover);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (saveButton.isEnabled()) {
                    saveButton.setBackground(primaryBg);
                }
            }
        });
>>>>>>> 3223ef6f407c193dbc17b8d9fa3ebfd74b819eb4
        saveButton.addActionListener(e -> onSave());

        footer.add(cancelButton);
        footer.add(saveButton);

        return footer;
    }

    private JComboBox<String> createRoleComboBox() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"STUDENT", "FACULTY"});
        styleComboBox(combo);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if ("STUDENT".equals(value)) {
                    setText("STUDENT  (Undergraduate / Postgraduate)");
                } else if ("FACULTY".equals(value)) {
                    setText("FACULTY  (Academic / Research Staff)");
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });

        return combo;
    }

    private void populateFieldsIfEditing() {
        if (userToEdit == null) {
            return;
        }

        enrollmentField.setText(userToEdit.getEnrollmentNo());
        enrollmentField.setEditable(false);
        enrollmentField.setBackground(new Color(248, 250, 252));

        nameField.setText(userToEdit.getFullName());
        nameField.setEditable(false);
        nameField.setBackground(new Color(248, 250, 252));

        roleComboBox.setSelectedItem(userToEdit.getRole());
        roleComboBox.setEnabled(false);
    }

    private void onSave() {
        hideError();

        if (userToEdit != null) {
            // Viewing mode -> simply close
            dispose();
            return;
        }

        String enrollment = enrollmentField.getText().trim();
        String name = nameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (enrollment.isEmpty()) {
            showError("Enrollment / Staff ID cannot be empty.");
            enrollmentField.requestFocusInWindow();
            return;
        }

        if (name.isEmpty()) {
            showError("Full name is required.");
            nameField.requestFocusInWindow();
            return;
        }

        if (role == null || role.isEmpty()) {
            showError("Please select a campus role.");
            roleComboBox.requestFocusInWindow();
            return;
        }

        saveButton.setEnabled(false);

        try {
            UserService.registerUser(enrollment, name, role);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            saveButton.setEnabled(true);
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
            saveButton.setEnabled(true);
        }
    }

    private void showError(String msg) {
        errorBanner.setText(" " + msg);
        errorBanner.setIcon(FontIcon.of(Material.ERROR_OUTLINE, 16, ERROR_RED));
        errorBanner.setVisible(true);
    }

    private void hideError() {
        errorBanner.setVisible(false);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(TEXT_DARK);
        return label;
    }

    private void styleTextField(JTextField field, String placeholder) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setPreferredSize(new Dimension(0, 38));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_FIELD, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        combo.setPreferredSize(new Dimension(0, 38));
        combo.setBackground(CARD_BACKGROUND);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_FIELD, 1, true));
    }
}
