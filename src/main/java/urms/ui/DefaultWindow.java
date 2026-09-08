package urms.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Default Main Application Window Frame.
 */
public class DefaultWindow {

    public static void showWindow() {
        JFrame frame = new JFrame("University Resource Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);

        // Header Panel
        JPanel header = new JPanel();
        header.setBackground(new Color(45, 45, 45));
        JLabel title = new JLabel("University Resource Register (URMS)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        header.add(title);

        // Center Content
        JLabel label = new JLabel("System & Database Ready!", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));

        frame.setLayout(new BorderLayout());
        frame.add(header, BorderLayout.NORTH);
        frame.add(label, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
