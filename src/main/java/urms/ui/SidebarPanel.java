package urms.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public final class SidebarPanel {

    private static final Color SIDEBAR = new Color(248, 250, 251);
    private static final Color BLUE = new Color(28, 78, 111);

    private SidebarPanel() {
    }

    public static JComponent createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(221, 228, 232)));

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(SIDEBAR);

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 19, 18));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        brand.setBackground(SIDEBAR);

        JLabel mark = new JLabel("UR", SwingConstants.CENTER);
        mark.setPreferredSize(new Dimension(35, 35));
        mark.setOpaque(true);
        mark.setBackground(BLUE);
        mark.setForeground(Color.WHITE);
        mark.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel identity = new JLabel("<html><b>URMS</b><br><span style='font-size:9px; letter-spacing:1px;'>REGISTRAR DESK</span></html>");
        identity.setForeground(new Color(40, 53, 62));
        identity.setFont(new Font("SansSerif", Font.PLAIN, 12));

        brand.add(mark);
        brand.add(identity);
        top.add(brand);
        top.add(separator());

        JLabel section = new JLabel();
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setBorder(BorderFactory.createEmptyBorder(14, 20, 9, 0));
        section.setForeground(new Color(143, 155, 163));
        section.setFont(new Font("SansSerif", Font.BOLD, 10));
        top.add(section);

        ButtonGroup navigation = new ButtonGroup();
        top.add(navigationItem("Dashboard", true, navigation));
        top.add(navigationItem("Resources", false, navigation));
        top.add(navigationItem("Allocations", false, navigation));
        top.add(navigationItem("Categories", false, navigation));
        top.add(navigationItem("Users", false, navigation));
        top.add(navigationItem("Reports", false, navigation));

        sidebar.add(top, BorderLayout.NORTH);
        return sidebar;
    }

    private static JComponent navigationItem(String text, boolean selected, ButtonGroup navigation) {
        JPanel holder = new JPanel(new BorderLayout());
        holder.setAlignmentX(Component.LEFT_ALIGNMENT);
        holder.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        holder.setPreferredSize(new Dimension(250, 38));
        holder.setBackground(SIDEBAR);
        holder.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        JToggleButton item = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D canvas = (Graphics2D) graphics.create();
                canvas.setColor(isSelected() ? new Color(224, 234, 239) : SIDEBAR);
                canvas.fillRect(0, 0, getWidth(), getHeight());
                if (isSelected()) {
                    canvas.setColor(BLUE);
                    canvas.fillRect(0, 0, 3, getHeight());
                }
                canvas.dispose();
                super.paintComponent(graphics);
            }
        };

        item.setActionCommand(text);
        item.setHorizontalAlignment(SwingConstants.LEFT);
        item.setMargin(new Insets(0, 10, 0, 10));
        item.setFocusPainted(false);
        item.setContentAreaFilled(false);
        item.setBorderPainted(false);
        item.setOpaque(false);
        item.setSelected(selected);
        item.addItemListener(event -> styleNavigationButton(item, event.getStateChange() == ItemEvent.SELECTED));
        styleNavigationButton(item, selected);
        navigation.add(item);
        holder.add(item, BorderLayout.CENTER);
        return holder;
    }

    private static void styleNavigationButton(JToggleButton button, boolean selected) {
        button.setForeground(selected ? new Color(37, 71, 91) : new Color(84, 98, 108));
        button.setFont(new Font("SansSerif", selected ? Font.BOLD : Font.PLAIN, 14));
        button.repaint();
    }

    private static JComponent separator() {
        JPanel line = new JPanel();
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(250, 1));
        line.setBackground(new Color(221, 228, 232));
        return line;
    }
}
