package urms.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import urms.ui.pages.CategoryPanel;
import urms.ui.pages.ResourcePanel;
import urms.util.ColorConstants;

import static urms.util.ColorConstants.*;

public class DefaultWindow {

    public static void showWindow(String fullName) {
        JFrame frame = new JFrame("University Resource Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(780, 520));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // CardLayout container for swapping center content
        CardLayout cardLayout = new CardLayout();
        JPanel contentCards = new JPanel(cardLayout);

        // Register cards corresponding to sidebar items
        contentCards.add(createSamplePage("Dashboard", "Overview of campus assets, occupancy, and pending requests"), "Dashboard");
        ResourcePanel resourcePanel = new ResourcePanel();
        contentCards.add(resourcePanel, "Resources");
        contentCards.add(createSamplePage("Allocations", "Track resource assignments, bookings, and schedules"), "Allocations");
        contentCards.add(new CategoryPanel(), "Categories");
        contentCards.add(createSamplePage("Users", "Manage operator accounts, faculty, and role permissions"), "Users");
        contentCards.add(createSamplePage("Reports", "Generate usage summaries, audit logs, and analytics"), "Reports");

        // Hook up sidebar selection callback to show the matching card
        JComponent sidebar = SidebarPanel.createSidebar(cardName -> {
            cardLayout.show(contentCards, cardName);
            if ("Resources".equals(cardName)) {
                resourcePanel.loadData();
            }
        });

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentCards, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JPanel createSamplePage(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE_BACKGROUND);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);

        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        JLabel placeholder = new JLabel("Content for " + title + " view will be rendered here.");
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 14));
        placeholder.setForeground(TEXT_MUTED);
        body.add(placeholder);

        page.add(body, BorderLayout.CENTER);
        return page;
    }
}
