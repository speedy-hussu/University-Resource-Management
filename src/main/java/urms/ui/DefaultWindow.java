package urms.ui;

import javax.swing.*;
import java.awt.*;

public class DefaultWindow {

    public static void showWindow(String fullName) {
        JFrame frame = new JFrame("University Resource Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 700);
        frame.setMinimumSize(new Dimension(780, 520));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(SidebarPanel.createSidebar(), BorderLayout.WEST);
        frame.add(createContent(), BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static JComponent createContent() {
        JPanel content = new JPanel();
        content.setBackground(new Color(228, 243, 255));
        return content;
    }
}
