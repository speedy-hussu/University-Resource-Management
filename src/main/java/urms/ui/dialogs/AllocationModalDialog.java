package urms.ui.dialogs;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.model.Allocation;
import urms.model.Resource;
import urms.model.User;
import urms.service.AllocationService;
import urms.service.ResourceService;
import urms.service.UserService;

import static urms.util.ColorConstants.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Modern modal dialog for issuing resource allocations and inspecting loan details.
 * Uses dedicated Date Element controls (JSpinner with SpinnerDateModel + Interactive Calendar Picker)
 * for selecting Start Date and Due Date.
 */
public class AllocationModalDialog extends JDialog {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final Allocation allocationToView;
    private final Runnable onSuccessCallback;

    private JComboBox<User> userComboBox;
    private JComboBox<Resource> resourceComboBox;
    private JLabel stockHintLabel;
    private JSpinner quantitySpinner;
    private JSpinner startDateSpinner;
    private JSpinner dueDateSpinner;
    private JButton startCalendarBtn;
    private JButton dueCalendarBtn;
    private JTextField remarksField;
    private JLabel errorBanner;
    private JButton saveButton;

    public AllocationModalDialog(Window owner, Allocation allocationToView, Runnable onSuccessCallback) {
        super(owner, allocationToView == null ? "Issue Resource Allocation" : "Allocation Details", ModalityType.APPLICATION_MODAL);
        this.allocationToView = allocationToView;
        this.onSuccessCallback = onSuccessCallback;

        initComponents();
        if (allocationToView != null) {
            populateFieldsForView();
        }

        setSize(580, 690);
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
                new EmptyBorder(18, 24, 18, 24)
        ));

        // Crimson circular badge for icon
        JPanel iconBadge = new JPanel(new GridBagLayout());
        iconBadge.setPreferredSize(new Dimension(46, 46));
        iconBadge.setBackground(BRAND_CRIMSON_TINT);
        iconBadge.setBorder(BorderFactory.createLineBorder(BRAND_CRIMSON_BORDER, 1, true));

        Material iconMat = (allocationToView == null) ? Material.SWAP_HORIZ : Material.ASSIGNMENT;
        JLabel iconLabel = new JLabel(FontIcon.of(iconMat, 24, BRAND_CRIMSON));
        iconBadge.add(iconLabel);

        JPanel textGroup = new JPanel();
        textGroup.setOpaque(false);
        textGroup.setLayout(new BoxLayout(textGroup, BoxLayout.Y_AXIS));

        String titleText = (allocationToView == null) ? "Issue Resource Allocation" : "Allocation Record Details";
        JLabel title = new JLabel(titleText);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));
        title.setForeground(TEXT_PRIMARY);

        String subText = (allocationToView == null)
                ? "Assign campus equipment or assets to a registered student or faculty member."
                : "View loan record information, return status, and audit timestamps.";
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
        container.setBorder(new EmptyBorder(14, 24, 10, 24));

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
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        int row = 0;

        // 1. Resource Selection
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Resource to Allocate *"), gbc);
        gbc.gridy = row++;
        resourceComboBox = createResourceComboBox();
        fieldsPanel.add(resourceComboBox, gbc);

        // Stock availability hint label
        gbc.gridy = row++;
        stockHintLabel = new JLabel(" ");
        stockHintLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        stockHintLabel.setForeground(TEXT_MUTED);
        fieldsPanel.add(stockHintLabel, gbc);

        // 2. Borrower / Campus User Selection
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Borrower / Campus User *"), gbc);
        gbc.gridy = row++;
        userComboBox = createUserComboBox();
        fieldsPanel.add(userComboBox, gbc);

        // 3. Quantity & Date Elements Row (Start Date & Due Date)
        gbc.gridy = row++;
        JPanel qtyAndDates = new JPanel(new GridLayout(1, 3, 10, 0));
        qtyAndDates.setOpaque(false);

        // Quantity Spinner
        JPanel qtyGroup = new JPanel(new BorderLayout(0, 4));
        qtyGroup.setOpaque(false);
        qtyGroup.add(createFieldLabel("Quantity *"), BorderLayout.NORTH);
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        styleSpinner(quantitySpinner);
        qtyGroup.add(quantitySpinner, BorderLayout.CENTER);

        // Start Date Element (JSpinner + Calendar Picker)
        JPanel startGroup = new JPanel(new BorderLayout(0, 4));
        startGroup.setOpaque(false);
        startGroup.add(createFieldLabel("Start Date *"), BorderLayout.NORTH);
        Date today = new Date();
        startDateSpinner = createDateSpinner(today);
        startCalendarBtn = createCalendarButton(startDateSpinner);
        startGroup.add(createDateControlWrapper(startDateSpinner, startCalendarBtn), BorderLayout.CENTER);

        // Due Date Element (JSpinner + Calendar Picker)
        JPanel dueGroup = new JPanel(new BorderLayout(0, 4));
        dueGroup.setOpaque(false);
        dueGroup.add(createFieldLabel("Due Date *"), BorderLayout.NORTH);
        Calendar dueCal = Calendar.getInstance();
        dueCal.add(Calendar.DAY_OF_MONTH, 7);
        dueDateSpinner = createDateSpinner(dueCal.getTime());
        dueCalendarBtn = createCalendarButton(dueDateSpinner);
        dueGroup.add(createDateControlWrapper(dueDateSpinner, dueCalendarBtn), BorderLayout.CENTER);

        qtyAndDates.add(qtyGroup);
        qtyAndDates.add(startGroup);
        qtyAndDates.add(dueGroup);
        fieldsPanel.add(qtyAndDates, gbc);

        // Date duration quick preset chips (Only in creation mode)
        if (allocationToView == null) {
            gbc.gridy = row++;
            JPanel presetsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
            presetsPanel.setOpaque(false);

            JLabel presetLabel = new JLabel("Quick Due Date:");
            presetLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
            presetLabel.setForeground(TEXT_MUTED);
            presetsPanel.add(presetLabel);

            presetsPanel.add(createPresetButton("+7 Days", 7));
            presetsPanel.add(createPresetButton("+14 Days", 14));
            presetsPanel.add(createPresetButton("+30 Days", 30));
            fieldsPanel.add(presetsPanel, gbc);
        }

        // 4. Purpose / Remarks Field
        gbc.gridy = row++;
        fieldsPanel.add(createFieldLabel("Remarks / Purpose"), gbc);
        gbc.gridy = row++;
        remarksField = new JTextField();
        styleTextField(remarksField, "e.g. Lab experiment, Final year project, AV seminar setup");
        fieldsPanel.add(remarksField, gbc);

        // Update initial stock hint & max quantity based on selected resource
        updateStockHint();
        resourceComboBox.addActionListener(e -> updateStockHint());

        container.add(errorBanner, BorderLayout.NORTH);
        container.add(fieldsPanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 16));
        footer.setBackground(TABLE_HEADER_BG);
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));

        // --- Cancel / Close Button ---
        JButton cancelButton = new JButton(allocationToView == null ? "Cancel" : "Close");
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

        // --- Primary Button ---
        boolean isViewMode = (allocationToView != null);
        Color primaryBg = isViewMode ? BLUE_ACCENT : BRAND_CRIMSON;
        Color primaryHover = isViewMode ? new Color(28, 75, 125) : BRAND_CRIMSON_HOVER;
        Material primaryIcon = isViewMode ? Material.CHECK_CIRCLE : Material.SWAP_HORIZ;
        String primaryText = isViewMode ? "Done" : "Issue Allocation";

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
        saveButton.addActionListener(e -> onSave());

        footer.add(cancelButton);
        footer.add(saveButton);

        return footer;
    }

    private JSpinner createDateSpinner(Date initialDate) {
        SpinnerDateModel model = new SpinnerDateModel(initialDate, null, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        styleSpinner(spinner);
        return spinner;
    }

    private JButton createCalendarButton(JSpinner associatedSpinner) {
        JButton btn = new JButton();
        btn.setIcon(FontIcon.of(Material.EVENT, 18, BRAND_CRIMSON));
        btn.setToolTipText("Open interactive calendar");
        btn.setPreferredSize(new Dimension(36, 38));
        btn.setBackground(CARD_BACKGROUND);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_FIELD, 1, true), new EmptyBorder(4, 4, 4, 4)));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BRAND_CRIMSON_TINT);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CARD_BACKGROUND);
            }
        });

        btn.addActionListener(e -> showCalendarPopup(btn, associatedSpinner));
        return btn;
    }

    private JPanel createDateControlWrapper(JSpinner spinner, JButton calendarBtn) {
        JPanel wrapper = new JPanel(new BorderLayout(4, 0));
        wrapper.setOpaque(false);
        wrapper.add(spinner, BorderLayout.CENTER);
        wrapper.add(calendarBtn, BorderLayout.EAST);
        return wrapper;
    }

    /**
     * Renders a lightweight, interactive calendar popup menu attached to the date button.
     */
    private void showCalendarPopup(Component invoker, JSpinner targetSpinner) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(BORDER_CARD, 1, true),
                new EmptyBorder(8, 8, 8, 8)
        ));
        popup.setBackground(CARD_BACKGROUND);

        Calendar cal = Calendar.getInstance();
        if (targetSpinner.getValue() instanceof Date d) {
            cal.setTime(d);
        }

        buildCalendarGrid(popup, targetSpinner, cal);
        popup.show(invoker, -180, invoker.getHeight() + 4);
    }

    private void buildCalendarGrid(JPopupMenu popup, JSpinner targetSpinner, Calendar cal) {
        popup.removeAll();

        JPanel root = new JPanel(new BorderLayout(0, 6));
        root.setBackground(CARD_BACKGROUND);
        root.setPreferredSize(new Dimension(240, 220));

        // Month Navigation Header
        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setOpaque(false);

        String[] monthNames = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        JLabel monthLabel = new JLabel(monthNames[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR), SwingConstants.CENTER);
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        monthLabel.setForeground(TEXT_PRIMARY);

        JButton prevBtn = new JButton();
        prevBtn.setIcon(FontIcon.of(Material.CHEVRON_LEFT, 16, TEXT_SLATE));
        prevBtn.setContentAreaFilled(false);
        prevBtn.setBorder(new EmptyBorder(2, 6, 2, 6));
        prevBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevBtn.addActionListener(e -> {
            cal.add(Calendar.MONTH, -1);
            buildCalendarGrid(popup, targetSpinner, cal);
            popup.revalidate();
            popup.repaint();
        });

        JButton nextBtn = new JButton();
        nextBtn.setIcon(FontIcon.of(Material.CHEVRON_RIGHT, 16, TEXT_SLATE));
        nextBtn.setContentAreaFilled(false);
        nextBtn.setBorder(new EmptyBorder(2, 6, 2, 6));
        nextBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextBtn.addActionListener(e -> {
            cal.add(Calendar.MONTH, 1);
            buildCalendarGrid(popup, targetSpinner, cal);
            popup.revalidate();
            popup.repaint();
        });

        navHeader.add(prevBtn, BorderLayout.WEST);
        navHeader.add(monthLabel, BorderLayout.CENTER);
        navHeader.add(nextBtn, BorderLayout.EAST);

        // Day of week labels
        JPanel daysGrid = new JPanel(new GridLayout(7, 7, 2, 2));
        daysGrid.setOpaque(false);

        String[] dayNames = {"S", "M", "T", "W", "T", "F", "S"};
        for (String dayName : dayNames) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
            lbl.setForeground(TEXT_MUTED);
            daysGrid.add(lbl);
        }

        // Days calculation
        Calendar temp = (Calendar) cal.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = temp.get(Calendar.DAY_OF_WEEK) - 1; // 0 for Sunday
        int maxDaysInMonth = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar selectedCal = Calendar.getInstance();
        if (targetSpinner.getValue() instanceof Date d) {
            selectedCal.setTime(d);
        }
        boolean sameMonthYear = (cal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR))
                && (cal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH));

        // Empty slots before 1st day
        for (int i = 0; i < firstDayOfWeek; i++) {
            daysGrid.add(new JLabel(""));
        }

        // Day buttons
        for (int day = 1; day <= maxDaysInMonth; day++) {
            final int chosenDay = day;
            JButton dayBtn = new JButton(String.valueOf(day));
            dayBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
            dayBtn.setMargin(new Insets(1, 1, 1, 1));
            dayBtn.setFocusPainted(false);
            dayBtn.setContentAreaFilled(false);
            dayBtn.setOpaque(true);
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isCurrentSelected = sameMonthYear && (day == selectedCal.get(Calendar.DAY_OF_MONTH));
            if (isCurrentSelected) {
                dayBtn.setBackground(BRAND_CRIMSON);
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setBorder(new LineBorder(BRAND_CRIMSON, 1, true));
            } else {
                dayBtn.setBackground(CARD_BACKGROUND);
                dayBtn.setForeground(TEXT_DARK);
                dayBtn.setBorder(BorderFactory.createEmptyBorder());
                dayBtn.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        dayBtn.setBackground(BRAND_CRIMSON_TINT);
                        dayBtn.setForeground(BRAND_CRIMSON);
                    }
                    @Override
                    public void mouseExited(MouseEvent e) {
                        dayBtn.setBackground(CARD_BACKGROUND);
                        dayBtn.setForeground(TEXT_DARK);
                    }
                });
            }

            dayBtn.addActionListener(e -> {
                Calendar result = (Calendar) cal.clone();
                result.set(Calendar.DAY_OF_MONTH, chosenDay);
                targetSpinner.setValue(result.getTime());
                popup.setVisible(false);
            });

            daysGrid.add(dayBtn);
        }

        // Fill remaining slots
        int totalCells = firstDayOfWeek + maxDaysInMonth;
        for (int i = totalCells; i < 42; i++) {
            daysGrid.add(new JLabel(""));
        }

        // Bottom Today shortcut
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        bottomBar.setOpaque(false);
        JButton todayBtn = new JButton("Today");
        todayBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        todayBtn.setForeground(BLUE_ACCENT);
        todayBtn.setContentAreaFilled(false);
        todayBtn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        todayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        todayBtn.addActionListener(e -> {
            targetSpinner.setValue(new Date());
            popup.setVisible(false);
        });
        bottomBar.add(todayBtn);

        root.add(navHeader, BorderLayout.NORTH);
        root.add(daysGrid, BorderLayout.CENTER);
        root.add(bottomBar, BorderLayout.SOUTH);

        popup.add(root);
    }

    private JComboBox<Resource> createResourceComboBox() {
        JComboBox<Resource> combo = new JComboBox<>();
        styleComboBox(combo);

        try {
            List<Resource> resources = ResourceService.getAllResources();
            for (Resource r : resources) {
                combo.addItem(r);
            }
        } catch (Exception e) {
            showError("Failed to load campus resources: " + e.getMessage());
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Resource res) {
                    setText(res.getResourceName() + "  —  [" + res.getAvailableQuantity() + " available]");
                } else if (value == null) {
                    setText("-- Select Campus Resource --");
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });

        return combo;
    }

    private JComboBox<User> createUserComboBox() {
        JComboBox<User> combo = new JComboBox<>();
        styleComboBox(combo);

        try {
            List<User> users = UserService.getAllUsers();
            for (User u : users) {
                combo.addItem(u);
            }
        } catch (Exception e) {
            showError("Failed to load campus users: " + e.getMessage());
        }

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User u) {
                    setText(u.getFullName() + "  (" + u.getEnrollmentNo() + " - " + u.getRole() + ")");
                } else if (value == null) {
                    setText("-- Select Borrower / Member --");
                }
                setBorder(new EmptyBorder(6, 8, 6, 8));
                return this;
            }
        });

        return combo;
    }

    private JButton createPresetButton(String text, int daysToAdd) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setForeground(TEXT_SLATE);
        btn.setBackground(CARD_BACKGROUND);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER_CARD, 1, true), new EmptyBorder(2, 6, 2, 6)));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(PAGE_BACKGROUND);
                btn.setForeground(TEXT_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(CARD_BACKGROUND);
                btn.setForeground(TEXT_SLATE);
            }
        });

        btn.addActionListener(e -> {
            Date base = (startDateSpinner.getValue() instanceof Date d) ? d : new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(base);
            cal.add(Calendar.DAY_OF_MONTH, daysToAdd);
            dueDateSpinner.setValue(cal.getTime());
        });

        return btn;
    }

    private void updateStockHint() {
        Resource selected = (Resource) resourceComboBox.getSelectedItem();
        if (selected != null) {
            stockHintLabel.setText("Available: " + selected.getAvailableQuantity() + " / " + selected.getTotalQuantity()
                    + " unit(s)  •  Location: " + (selected.getLocation() != null ? selected.getLocation() : "Campus"));
            int avail = selected.getAvailableQuantity();
            if (avail > 0) {
                quantitySpinner.setModel(new SpinnerNumberModel(1, 1, avail, 1));
            }
        } else {
            stockHintLabel.setText(" ");
        }
    }

    private void populateFieldsForView() {
        if (allocationToView == null) {
            return;
        }

        // Lock form components for inspection
        userComboBox.setEnabled(false);
        resourceComboBox.setEnabled(false);
        quantitySpinner.setEnabled(false);
        startDateSpinner.setEnabled(false);
        dueDateSpinner.setEnabled(false);
        startCalendarBtn.setEnabled(false);
        dueCalendarBtn.setEnabled(false);
        remarksField.setEditable(false);

        remarksField.setBackground(new Color(248, 250, 252));

        quantitySpinner.setValue(allocationToView.getQuantity());

        try {
            if (allocationToView.getIssueDate() != null) {
                String clean = allocationToView.getIssueDate().substring(0, Math.min(10, allocationToView.getIssueDate().length()));
                startDateSpinner.setValue(DATE_FORMAT.parse(clean));
            }
            if (allocationToView.getDueDate() != null) {
                String clean = allocationToView.getDueDate().substring(0, Math.min(10, allocationToView.getDueDate().length()));
                dueDateSpinner.setValue(DATE_FORMAT.parse(clean));
            }
        } catch (Exception ignored) {}

        remarksField.setText(allocationToView.getRemarks() != null ? allocationToView.getRemarks() : "");

        // Set matching user in combo
        for (int i = 0; i < userComboBox.getItemCount(); i++) {
            User u = userComboBox.getItemAt(i);
            if (u.getUserId() != null && u.getUserId().equals(allocationToView.getUserId())) {
                userComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Set matching resource in combo
        for (int i = 0; i < resourceComboBox.getItemCount(); i++) {
            Resource r = resourceComboBox.getItemAt(i);
            if (r.getResourceId() != null && r.getResourceId().equals(allocationToView.getResourceId())) {
                resourceComboBox.setSelectedIndex(i);
                break;
            }
        }

        stockHintLabel.setText("Status: " + allocationToView.getStatus()
                + (allocationToView.getReturnDate() != null ? "  •  Returned: " + allocationToView.getReturnDate() : ""));
    }

    private void onSave() {
        hideError();

        if (allocationToView != null) {
            // View mode -> close dialog
            dispose();
            return;
        }

        Resource selectedResource = (Resource) resourceComboBox.getSelectedItem();
        User selectedUser = (User) userComboBox.getSelectedItem();
        int quantity = (Integer) quantitySpinner.getValue();

        Date startDateObj = (startDateSpinner.getValue() instanceof Date d) ? d : new Date();
        Date dueDateObj = (dueDateSpinner.getValue() instanceof Date d) ? d : new Date();

        String startDate = DATE_FORMAT.format(startDateObj);
        String dueDate = DATE_FORMAT.format(dueDateObj);
        String remarks = remarksField.getText().trim();

        if (selectedResource == null) {
            showError("Please select a resource to allocate.");
            resourceComboBox.requestFocusInWindow();
            return;
        }

        if (selectedResource.getAvailableQuantity() <= 0) {
            showError("The selected resource '" + selectedResource.getResourceName() + "' is out of stock.");
            resourceComboBox.requestFocusInWindow();
            return;
        }

        if (selectedUser == null) {
            showError("Please select a registered campus borrower.");
            userComboBox.requestFocusInWindow();
            return;
        }

        if (quantity <= 0) {
            showError("Quantity must be at least 1.");
            quantitySpinner.requestFocusInWindow();
            return;
        }

        if (quantity > selectedResource.getAvailableQuantity()) {
            showError("Quantity requested (" + quantity + ") exceeds available stock (" + selectedResource.getAvailableQuantity() + ").");
            quantitySpinner.requestFocusInWindow();
            return;
        }

        if (dueDateObj.before(startDateObj)) {
            showError("Due date cannot be earlier than start date (" + startDate + ").");
            dueDateSpinner.requestFocusInWindow();
            return;
        }

        saveButton.setEnabled(false);

        try {
            Allocation newAlloc = new Allocation();
            newAlloc.setResourceId(selectedResource.getResourceId());
            newAlloc.setUserId(selectedUser.getUserId());
            newAlloc.setQuantity(quantity);
            newAlloc.setIssueDate(startDate);
            newAlloc.setDueDate(dueDate);
            newAlloc.setRemarks(remarks);
            newAlloc.setIssuedBy("admin");

            AllocationService.issueResource(newAlloc);

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }
            dispose();

        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
            saveButton.setEnabled(true);
        } catch (Exception ex) {
            showError("Failed to issue allocation: " + ex.getMessage());
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
