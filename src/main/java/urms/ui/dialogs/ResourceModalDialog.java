package urms.ui.dialogs;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Category;
import urms.model.Resource;
import urms.service.CategoryService;
import urms.service.ResourceService;
import urms.util.ColorConstants;

import static urms.util.ColorConstants.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modern modal dialog for creating and editing university resources.
 * Matches URMS University Crimson branding and FlatLaf styling.
 */
public class ResourceModalDialog extends JDialog {

    private final Resource resourceToEdit;
    private final Runnable onSuccessCallback;

    private JTextField nameField;
    private JComboBox<Category> categoryComboBox;
    private JTextField locationField;
    private JSpinner totalQtySpinner;
    private JSpinner availableQtySpinner;
    private JLabel errorBanner;
    private JButton saveButton;

    public ResourceModalDialog(Window owner, Resource resourceToEdit, Runnable onSuccessCallback) {
        super(owner, resourceToEdit == null ? "Add New Resource" : "Edit Resource", ModalityType.APPLICATION_MODAL);
        this.resourceToEdit = resourceToEdit;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        populateFieldsIfEditing();

        setSize(520, 620);
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

        Material iconMat = (resourceToEdit == null) ? Material.ADD_BOX : Material.EDIT;
        JLabel iconLabel = new JLabel(FontIcon.of(iconMat, 24, BRAND_CRIMSON));
        iconBadge.add(iconLabel);

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        String titleText = (resourceToEdit == null) ? "Register New Resource" : "Edit Resource Details";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(TEXT_PRIMARY);

        String subText = (resourceToEdit == null)
                ? "Enter equipment, venue, or consumable details for campus tracking."
                : "Update inventory attributes, allocation quotas, and location info.";
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

        // 1. Resource Name
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Resource Name *"), gbc);
        gbc.gridy = row++;
        nameField = new JTextField();
        styleTextField(nameField, "e.g. Rigol Digital Oscilloscope (50MHz)");
        fieldsPanel.add(nameField, gbc);

        // 2. Category
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Category *"), gbc);
        gbc.gridy = row++;
        categoryComboBox = createCategoryComboBox();
        fieldsPanel.add(categoryComboBox, gbc);

        // 3. Location
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Location / Storage Room"), gbc);
        gbc.gridy = row++;
        locationField = new JTextField();
        styleTextField(locationField, "e.g. Electronics Lab Bench 2, Media Locker A1");
        fieldsPanel.add(locationField, gbc);

        // 4. Quantities in 2-column layout
        gbc.gridy = row++;
        JPanel qtyRow = new JPanel(new GridLayout(1, 2, 14, 0));
        qtyRow.setOpaque(false);

        JPanel totalGroup = new JPanel(new BorderLayout(0, 6));
        totalGroup.setOpaque(false);
        totalGroup.add(createFieldLabel("Total Quantity *"), BorderLayout.NORTH);
        totalQtySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100000, 1));
        styleSpinner(totalQtySpinner);
        totalGroup.add(totalQtySpinner, BorderLayout.CENTER);

        JPanel availGroup = new JPanel(new BorderLayout(0, 6));
        availGroup.setOpaque(false);
        availGroup.add(createFieldLabel("Available Quantity *"), BorderLayout.NORTH);
        availableQtySpinner = new JSpinner(new SpinnerNumberModel(1, 0, 100000, 1));
        styleSpinner(availableQtySpinner);
        availGroup.add(availableQtySpinner, BorderLayout.CENTER);

        // Auto-sync available quantity with total when creating a new resource
        if (resourceToEdit == null) {
            totalQtySpinner.addChangeListener(e -> {
                int total = (Integer) totalQtySpinner.getValue();
                availableQtySpinner.setValue(total);
            });
        }

        qtyRow.add(totalGroup);
        qtyRow.add(availGroup);
        fieldsPanel.add(qtyRow, gbc);

        container.add(errorBanner, BorderLayout.NORTH);
        container.add(fieldsPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));

        JButton cancelButton = new JButton("Cancel");
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

        saveButton = new JButton(resourceToEdit == null ? "Add Resource" : "Save Changes");
        saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(BRAND_CRIMSON);
        saveButton.setFocusPainted(false);
        saveButton.setIcon(FontIcon.of(Material.CHECK, 16, Color.WHITE));
        saveButton.setBorder(new EmptyBorder(9, 20, 9, 20));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        saveButton.addActionListener(e -> onSave());

        footer.add(cancelButton);
        footer.add(saveButton);

        return footer;
    }

    private JComboBox<Category> createCategoryComboBox() {
        JComboBox<Category> combo = new JComboBox<>();
        styleComboBox(combo);

        try {
            List<Category> categories = CategoryService.getActiveCategories();
            for (Category cat : categories) {
                combo.addItem(cat);
            }
        } catch (Exception e) {
            showError("Failed to load categories: " + e.getMessage());
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Category cat) {
                    setText(cat.getCategoryName());
                } else if (value == null) {
                    setText("-- Select Category --");
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });

        return combo;
    }

    private void populateFieldsIfEditing() {
        if (resourceToEdit == null) {
            return;
        }

        nameField.setText(resourceToEdit.getResourceName());
        locationField.setText(resourceToEdit.getLocation());
        totalQtySpinner.setValue(resourceToEdit.getTotalQuantity());
        availableQtySpinner.setValue(resourceToEdit.getAvailableQuantity());

        // Match category
        for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
            Category cat = categoryComboBox.getItemAt(i);
            if (cat.getCategoryId() != null && cat.getCategoryId().equals(resourceToEdit.getCategoryId())) {
                categoryComboBox.setSelectedIndex(i);
                break;
            }
        }

    }

    private void onSave() {
        hideError();

        String name = nameField.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        String location = locationField.getText().trim();
        int totalQty = (Integer) totalQtySpinner.getValue();
        int availQty = (Integer) availableQtySpinner.getValue();

        if (name.isEmpty()) {
            showError("Resource name is required.");
            nameField.requestFocusInWindow();
            return;
        }

        if (selectedCategory == null) {
            showError("Please select a valid category.");
            categoryComboBox.requestFocusInWindow();
            return;
        }

        if (availQty > totalQty) {
            showError("Available quantity (" + availQty + ") cannot exceed total quantity (" + totalQty + ").");
            availableQtySpinner.requestFocusInWindow();
            return;
        }

        saveButton.setEnabled(false);

        try {
            if (resourceToEdit == null) {
                // Add new resource
                Resource newResource = new Resource(
                        name,
                        selectedCategory.getCategoryId(),
                        location,
                        totalQty,
                        availQty
                );
                ResourceService.addResource(newResource);
            } else {
                // Update existing
                resourceToEdit.setResourceName(name);
                resourceToEdit.setCategoryId(selectedCategory.getCategoryId());
                resourceToEdit.setCategoryName(selectedCategory.getCategoryName());
                resourceToEdit.setLocation(location);
                resourceToEdit.setTotalQuantity(totalQty);
                resourceToEdit.setAvailableQuantity(availQty);
                ResourceService.updateResource(resourceToEdit);
            }

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();

        } catch (IllegalArgumentException | IllegalStateException ex) {
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

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.PLAIN, 13));
        spinner.setPreferredSize(new Dimension(0, 38));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_FIELD, 1, true));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setFont(new Font("SansSerif", Font.PLAIN, 13));
            defaultEditor.getTextField().setBorder(new EmptyBorder(4, 8, 4, 8));
        }
    }
}
