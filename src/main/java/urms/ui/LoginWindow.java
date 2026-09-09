package urms.ui;

import urms.dao.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/** Login screen for active URMS operators. */
public final class LoginWindow {

    private LoginWindow() {
    }

    public static void showWindow() {
        JFrame frame = new JFrame("URMS | Sign in");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(460, 390);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBorder(new EmptyBorder(32, 42, 30, 42));
        root.setBackground(new Color(246, 248, 251));

        JLabel heading = new JLabel("University Resource Register");
        heading.setFont(new Font("SansSerif", Font.BOLD, 23));
        heading.setForeground(new Color(27, 48, 76));

        JLabel subtitle = new JLabel("Sign in to manage campus resources");
        subtitle.setForeground(new Color(92, 105, 122));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(heading);
        header.add(Box.createVerticalStrut(7));
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        styleField(usernameField);
        styleField(passwordField);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setOpaque(false);
        form.add(labelFor("Username"));
        form.add(usernameField);
        form.add(labelFor("Password"));
        form.add(passwordField);

        JButton signInButton = new JButton("Sign in");
        signInButton.setForeground(Color.WHITE);
        signInButton.setBackground(new Color(35, 93, 153));
        signInButton.setFocusPainted(false);
        signInButton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));

        JLabel status = new JLabel(" ");
        status.setForeground(new Color(180, 55, 55));

        JPanel actions = new JPanel(new BorderLayout(0, 8));
        actions.setOpaque(false);
        actions.add(signInButton, BorderLayout.NORTH);
        actions.add(status, BorderLayout.SOUTH);

        JPanel center = new JPanel(new BorderLayout(0, 18));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(actions, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        Runnable authenticate = () -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            if (username.isEmpty() || password.isEmpty()) {
                status.setText("Enter your username and password.");
                return;
            }

            signInButton.setEnabled(false);
            try {
                String fullName = DatabaseConnection.authenticate(username, password);
                if (fullName == null) {
                    status.setText("Invalid username or password.");
                    passwordField.setText("");
                    passwordField.requestFocusInWindow();
                } else {
                    frame.dispose();
                    DefaultWindow.showWindow(fullName);
                }
            } catch (SQLException | IllegalArgumentException exception) {
                status.setText("Unable to sign in. Check the database connection.");
            } finally {
                signInButton.setEnabled(true);
            }
        };

        signInButton.addActionListener(event -> authenticate.run());
        passwordField.addActionListener(event -> authenticate.run());
        frame.setContentPane(root);
        frame.getRootPane().setDefaultButton(signInButton);
        frame.setVisible(true);
        usernameField.requestFocusInWindow();
    }

    private static JLabel labelFor(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 12f));
        label.setForeground(new Color(57, 69, 84));
        return label;
    }

    private static void styleField(JTextField field) {
        field.setFont(field.getFont().deriveFont(14f));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(196, 205, 216)),
                BorderFactory.createEmptyBorder(8, 9, 8, 9)));
    }
}