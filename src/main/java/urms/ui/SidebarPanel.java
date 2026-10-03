package urms.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.kordamp.ikonli.material.Material;
import org.kordamp.ikonli.swing.FontIcon;
import urms.util.ColorConstants;

public final class SidebarPanel {

    private static final int SIDEBAR_WIDTH = 220;

    private static final Color SIDEBAR_BG = ColorConstants.SIDEBAR_BACKGROUND;
    private static final Color BORDER_COLOR = ColorConstants.BORDER_DIVIDER;
    private static final Color BRAND_CRIMSON = ColorConstants.BRAND_CRIMSON;
    private static final Color ACTIVE_ITEM_BG = ColorConstants.BRAND_CRIMSON_TINT;
    private static final Color TEXT_ACTIVE = ColorConstants.TEXT_NAV_ACTIVE;
    private static final Color TEXT_INACTIVE = ColorConstants.TEXT_NAV_INACTIVE;

    private static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);

    private SidebarPanel() {
    }

    public static JComponent createSidebar() {
        return createSidebar(null);
    }

    public static JComponent createSidebar(Consumer<String> onNavigate) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 0));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(SIDEBAR_BG);

        // Brand Header (Full-width Logo)
        JPanel brand = new JPanel(new BorderLayout());
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        brand.setBackground(SIDEBAR_BG);
        brand.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 10, 14, 10)
        ));

        java.net.URL logoUrl = SidebarPanel.class.getResource("/images/navrachana_logo.png");
        if (logoUrl != null) {
            ImageIcon sourceLogo = new ImageIcon(logoUrl);
            int origWidth = sourceLogo.getIconWidth();
            int origHeight = sourceLogo.getIconHeight();
            int targetWidth = 180;
            int targetHeight = (origWidth > 0) ? Math.max(1, (origHeight * targetWidth / origWidth)) : 42;
            Image scaledLogo = sourceLogo.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_DEFAULT);
            JLabel mark = new JLabel(new ImageIcon(scaledLogo), SwingConstants.CENTER);
            brand.add(mark, BorderLayout.CENTER);
        }

        top.add(brand);
        top.add(Box.createVerticalStrut(12));

        // Navigation Items
        ButtonGroup navigation = new ButtonGroup();
        String[] menuItems = {"Dashboard", "Resources", "Allocations", "Categories", "Reports"};
        Material[] menuIcons = {
                Material.DASHBOARD,     // Modern 4-quadrant layout
                Material.ALL_INBOX,      // Stacked storage/inventory boxes
                Material.SWAP_HORIZ,     // Resource allocations & loan exchanges (left/right arrows)
                Material.TOC,            // Categories
                Material.INSERT_CHART    // Modern analytics and reports chart
        };
        for (int i = 0; i < menuItems.length; i++) {
            top.add(createNavigationItem(menuItems[i], menuIcons[i], i == 0, navigation, onNavigate));
        }

        sidebar.add(top, BorderLayout.NORTH);
        return sidebar;
    }

    private static JToggleButton createNavigationItem(String text, Material icon, boolean selected, ButtonGroup navigation, Consumer<String> onNavigate) {
        JToggleButton item = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setColor(isSelected() ? ACTIVE_ITEM_BG : SIDEBAR_BG);
                canvas.fillRect(0, 0, getWidth(), getHeight());
                if (isSelected()) {
                    canvas.setColor(BRAND_CRIMSON);
                    canvas.fillRect(0, 0, 3, getHeight());
                }
                canvas.dispose();
                super.paintComponent(graphics);
            }
        };

        item.setAlignmentX(Component.LEFT_ALIGNMENT);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        item.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 38));
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setMargin(new Insets(0, 16, 0, 16));
        item.setFocusPainted(false);
        item.setContentAreaFilled(false);
        item.setBorderPainted(false);
        item.setOpaque(false);
        item.setSelected(selected);
        item.setActionCommand(text);
        item.setIcon(FontIcon.of(icon, 18, selected ? TEXT_ACTIVE : TEXT_INACTIVE));
        item.setIconTextGap(12);

        updateStyle(item, selected);

        item.addItemListener(event -> {
            boolean isSelected = (event.getStateChange() == ItemEvent.SELECTED);
            updateStyle(item, isSelected);
            if (isSelected && onNavigate != null) {
                onNavigate.accept(text);
            }
        });

        navigation.add(item);
        return item;
    }

    private static void updateStyle(JToggleButton button, boolean selected) {
        Color color = selected ? TEXT_ACTIVE : TEXT_INACTIVE;
        button.setForeground(color);
        button.setFont(selected ? FONT_BOLD : FONT_REGULAR);
        if (button.getIcon() instanceof FontIcon icon) {
            icon.setIconColor(color);
        }
        button.repaint();
    }
}
