package urms.ui.pages;

import urms.model.CategorySummary;
import urms.service.CategoryService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing resource categories.
 */
public class CategoryPanel extends JPanel {

    private final DefaultTableModel tableModel;
    private final JTable categoryTable;
    private final JLabel statusLabel;

    public CategoryPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(242, 246, 249));

        tableModel = new DefaultTableModel(new Object[]{"Category", "Description", "Resources"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(32);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        categoryTable.setShowGrid(false);
        categoryTable.setIntercellSpacing(new Dimension(0, 0));
        categoryTable.setFont(new Font("SansSerif", Font.PLAIN, 13));

        statusLabel = new JLabel(" ");

        add(createHeader(), BorderLayout.NORTH);
        add(createContentBody(), BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 228, 232)),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));

        JLabel titleLabel = new JLabel("Categories");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(28, 78, 111));

        JLabel subtitleLabel = new JLabel("Organize and manage campus resource categories");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(110, 125, 136));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);

        return header;
    }

    private JPanel createContentBody() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 20, 18, 20));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);

        JButton addButton = new JButton("Add category");
        addButton.setBackground(new Color(35, 93, 153));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        addButton.addActionListener(event -> openAddCategoryDialog());
        toolbar.add(addButton);

        content.add(toolbar, BorderLayout.NORTH);
        content.add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        statusLabel.setForeground(new Color(140, 153, 163));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        content.add(statusLabel, BorderLayout.SOUTH);

        return content;
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<CategorySummary> categories = CategoryService.getCategories();
            for (CategorySummary category : categories) {
                tableModel.addRow(new Object[]{
                        category.categoryName(),
                        category.description(),
                        category.resourceCount()
                });
            }
            statusLabel.setText(categories.isEmpty() ? "No active categories found." : "Loaded " + categories.size() + " category record(s).");
            statusLabel.setForeground(new Color(110, 125, 136));
        } catch (RuntimeException exception) {
            statusLabel.setText("Unable to load categories: " + exception.getMessage());
            statusLabel.setForeground(new Color(180, 55, 55));
        }
    }

    private void openAddCategoryDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add category", true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(18, 18, 10, 18));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 8, 4);

        JTextField categoryNameField = new JTextField(26);
        JTextArea descriptionArea = new JTextArea(4, 26);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(new Color(180, 55, 55));
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        form.add(new JLabel("Category name:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(categoryNameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.weightx = 0;
        form.add(new JLabel("Description:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        form.add(new JScrollPane(descriptionArea), constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        form.add(errorLabel, constraints);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(event -> {
            String name = categoryNameField.getText() == null ? "" : categoryNameField.getText().trim();
            String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();

            try {
                CategoryService.addCategory(name, description);
                dialog.dispose();
                refreshData();
            } catch (RuntimeException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        cancelButton.addActionListener(event -> dialog.dispose());
        actions.add(saveButton);
        actions.add(cancelButton);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(420, 220));
        dialog.setVisible(true);
    }
}
