package urms.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

public final class SidebarPanel {

    private static final int SIDEBAR_WIDTH = 220;

    private static final Color SIDEBAR_BG = new Color(250, 250, 251); // Clean warm sidebar bg
    private static final Color BORDER_COLOR = new Color(232, 227, 227); // Subtle divider matching warm tone  
    private static final Color BRAND_CRIMSON = new Color(179, 32, 37); // Navrachana Logo Crimson (#B32025)
    private static final Color ACTIVE_ITEM_BG = new Color(254, 241, 242); // Soft crimson tint highlight
    private static final Color TEXT_ACTIVE = new Color(168, 28, 33); // Active text and icon
    private static final Color TEXT_INACTIVE = new Color(95, 105, 115); // Unselected slate text and icon

    private static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_BRAND_TEXT = new Font("SansSerif", Font.PLAIN, 12);

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

        // Brand Header
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 16));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));
        brand.setBackground(SIDEBAR_BG);
        brand.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        ImageIcon sourceLogo = new ImageIcon(SidebarPanel.class.getResource("/images/navrachana_logo.png"));
        Image scaledLogo = sourceLogo.getImage().getScaledInstance(-1, 52, Image.SCALE_SMOOTH);
        JLabel mark = new JLabel(new ImageIcon(scaledLogo));
        mark.setPreferredSize(new Dimension(45, 52));

        JLabel identity = new JLabel("<html><b style='color:#B32025;'>NUV</b><br><span style='font-size:9px; letter-spacing:1px; color:#5D6772;'>REGISTRAR DESK</span></html>");
        identity.setFont(FONT_BRAND_TEXT);

        brand.add(mark);
        brand.add(identity);
        top.add(brand);
        top.add(Box.createVerticalStrut(12));

        // Navigation Items
        ButtonGroup navigation = new ButtonGroup();
        String[] menuItems = {"Dashboard", "Resources", "Allocations", "Categories", "Users", "Reports"};
        Material[] menuIcons = {
                Material.DASHBOARD,      // Modern 4-quadrant layout
                Material.ALL_INBOX,      // Stacked storage/inventory boxes
                Material.SWAP_HORIZ,     // Resource allocations & loan exchanges (left/right arrows)
                Material.TOC,            // Categories
                Material.PEOPLE,         // User accounts and roles
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
