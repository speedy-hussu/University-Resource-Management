package urms.ui.dialogs;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Category;
import urms.service.CategoryService;

import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.BorderLayout;
import java.awt.Color;
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

/**
 * Modern modal dialog for creating and editing resource categories.
 * Matches URMS University Crimson branding and FlatLaf styling.
 * Follows the same architecture as ResourceModalDialog:
 * Header (icon badge + title) → Form body (error banner + fields) → Footer (Cancel + Save).
 */
public class CategoryModalDialog extends JDialog {

    private static final int MAX_NAME_LENGTH = 50;

    private final Category categoryToEdit;
    private final Runnable onSuccessCallback;

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JLabel charCountLabel;
    private JLabel errorBanner;
    private JButton saveButton;

    public CategoryModalDialog(Window owner, Category categoryToEdit, Runnable onSuccessCallback) {
        super(owner, categoryToEdit == null ? "Add New Category" : "Edit Category", ModalityType.APPLICATION_MODAL);
        this.categoryToEdit = categoryToEdit;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        populateFieldsIfEditing();

        setSize(480, 460);
        setResizable(false);
        setLocationRelativeTo(owner);

        // Focus the name field when dialog opens
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CARD_BACKGROUND);

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createFormPanel(), BorderLayout.CENTER);
        root.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    // ============================================================
    // HEADER — Crimson icon badge + title + subtitle
    // ============================================================

    /**
     * Branded header with crimson icon badge, title, and subtitle.
     * Matches ResourceModalDialog header exactly.
     */
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

        Material iconMat = (categoryToEdit == null) ? Material.CREATE_NEW_FOLDER : Material.EDIT;
        JLabel iconLabel = new JLabel(FontIcon.of(iconMat, 24, BRAND_CRIMSON));
        iconBadge.add(iconLabel);

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        String titleText = (categoryToEdit == null) ? "Create New Category" : "Edit Category Details";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(TEXT_PRIMARY);

        String subText = (categoryToEdit == null)
                ? "Define a new category for organizing campus resources."
                : "Update the category name or description.";
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

    // ============================================================
    // FORM BODY — Error banner + name field + description textarea
    // ============================================================

    /**
     * Form body with error banner, styled name field with live character counter,
     * and a multi-line description textarea.
     */
    private JPanel createFormPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(16, 24, 10, 24));

        // Error message banner — hidden by default, shown on validation failure
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

        // ── 1. Category Name ──
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Category Name *"), gbc);

        gbc.gridy = row++;
        nameField = new JTextField();
        styleTextField(nameField, "e.g. Lab Equipment, Classroom Furniture");
        fieldsPanel.add(nameField, gbc);

        // Live character counter (updates as user types)
        gbc.gridy = row++;
        charCountLabel = new JLabel("0 / " + MAX_NAME_LENGTH + " characters");
        charCountLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        charCountLabel.setForeground(ICON_MUTED);
        fieldsPanel.add(charCountLabel, gbc);

        // Wire up the live counter
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateCharCount(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateCharCount(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateCharCount(); }
        });

        // ── 2. Description ──
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Description"), gbc);

        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(BorderFactory.createLineBorder(BORDER_FIELD, 1, true));
        fieldsPanel.add(descScroll, gbc);

        // Optional description hint
        gbc.gridy = row++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        JLabel descHint = new JLabel("Brief description of what resources this category covers");
        descHint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        descHint.setForeground(ICON_MUTED);
        fieldsPanel.add(descHint, gbc);

        container.add(errorBanner, BorderLayout.NORTH);
        container.add(fieldsPanel, BorderLayout.CENTER);

        return container;
    }

    // ============================================================
    // FOOTER — Cancel (ghost) + Save/Add (crimson) buttons
    // ============================================================

    /**
     * Footer with Cancel and Save/Add buttons.
     * Cancel is a ghost button with hover; Save is crimson with hover darkening.
     * Matches ResourceModalDialog footer exactly.
     */
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 18));
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));

        // ── Cancel Button (ghost style with hover) ──
        JButton cancelButton = new JButton("Cancel");
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

        // ── Primary Action Button (Crimson with hover darkening) ──
        boolean isEditing = (categoryToEdit != null);
        Material primaryIcon = isEditing ? Material.SAVE : Material.CREATE_NEW_FOLDER;
        String primaryText = isEditing ? "Save Changes" : "Add Category";

        saveButton = new JButton(primaryText);
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(BRAND_CRIMSON);
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
                    saveButton.setBackground(BRAND_CRIMSON_HOVER);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (saveButton.isEnabled()) {
                    saveButton.setBackground(BRAND_CRIMSON);
                }
            }
        });
        saveButton.addActionListener(e -> onSave());

        footer.add(cancelButton);
        footer.add(saveButton);

        return footer;
    }

    // ============================================================
    // FORM POPULATION AND SAVE LOGIC
    // ============================================================

    /**
     * Pre-fills form fields when editing an existing category.
     */
    private void populateFieldsIfEditing() {
        if (categoryToEdit == null) {
            return;
        }
        nameField.setText(categoryToEdit.getCategoryName());
        descriptionArea.setText(categoryToEdit.getDescription());
        updateCharCount();
    }

    /**
     * Updates the live character counter label.
     * Turns red when the name exceeds the maximum length.
     */
    private void updateCharCount() {
        int length = nameField.getText().length();
        charCountLabel.setText(length + " / " + MAX_NAME_LENGTH + " characters");
        if (length > MAX_NAME_LENGTH) {
            charCountLabel.setForeground(ERROR_RED);
        } else {
            charCountLabel.setForeground(ICON_MUTED);
        }
    }

    /**
     * Validates the form and delegates to CategoryService for add/update.
     * Shows inline error banner on validation failure.
     */
    private void onSave() {
        hideError();

        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();

        // Client-side validation
        if (name.isEmpty()) {
            showError("Category name is required.");
            nameField.requestFocusInWindow();
            return;
        }

        if (name.length() > MAX_NAME_LENGTH) {
            showError("Category name cannot exceed " + MAX_NAME_LENGTH + " characters.");
            nameField.requestFocusInWindow();
            return;
        }

        saveButton.setEnabled(false);

        try {
            if (categoryToEdit == null) {
                // Add new category via service (validates duplicates, length, etc.)
                CategoryService.addCategory(name, description);
            } else {
                // Update existing category via service
                CategoryService.updateCategory(categoryToEdit.getCategoryId(), name, description);
            }

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

    // ============================================================
    // ERROR BANNER HELPERS
    // ============================================================

    private void showError(String msg) {
        errorBanner.setText(" " + msg);
        errorBanner.setIcon(FontIcon.of(Material.ERROR_OUTLINE, 16, ERROR_RED));
        errorBanner.setVisible(true);
        // Revalidate to account for the banner appearing
        errorBanner.getParent().revalidate();
    }

    private void hideError() {
        errorBanner.setVisible(false);
    }

    // ============================================================
    // FIELD STYLING HELPERS
    // ============================================================

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
}
