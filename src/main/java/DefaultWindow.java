import javax.swing.*;
import java.awt.*;

public class DefaultWindow {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Default Window");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 200);
            frame.setLocationRelativeTo(null);

            JPanel header = new JPanel();
            header.setBackground(new Color(45, 45, 45));
            JLabel title = new JLabel("Default Window");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            title.setForeground(Color.WHITE);
            title.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            header.add(title);

            JLabel label = new JLabel("Java Swing is running!", SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 20));

            frame.setLayout(new BorderLayout());
            frame.add(header, BorderLayout.NORTH);
            frame.add(label, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }
}