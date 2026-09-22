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
        contentCards.add(createSamplePage("Resources", "Manage university rooms, equipment, labs, and assets"), "Resources");
        contentCards.add(createSamplePage("Allocations", "Track resource assignments, bookings, and schedules"), "Allocations");
        contentCards.add(new CategoryPanel(), "Categories");
        contentCards.add(createSamplePage("Users", "Manage operator accounts, faculty, and role permissions"), "Users");
        contentCards.add(createSamplePage("Reports", "Generate usage summaries, audit logs, and analytics"), "Reports");

        // Hook up sidebar selection callback to show the matching card
        JComponent sidebar = SidebarPanel.createSidebar(cardName -> cardLayout.show(contentCards, cardName));

        frame.add(sidebar, BorderLayout.WEST);
        frame.add(contentCards, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JPanel createSamplePage(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(242, 246, 249));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(221, 228, 232)),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(new Color(28, 78, 111));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(110, 125, 136));

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);

        page.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        JLabel placeholder = new JLabel("Content for " + title + " view will be rendered here.");
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 14));
        placeholder.setForeground(new Color(140, 153, 163));
        body.add(placeholder);

        page.add(body, BorderLayout.CENTER);
        return page;
    }
}
