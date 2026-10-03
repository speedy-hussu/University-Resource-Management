package urms.ui.pages;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import urms.util.ColorConstants;

import static urms.util.ColorConstants.*;

/**
 * Panel for managing resource categories.
 * Teammates can implement category tables, forms, and actions here.
 */
public class CategoryPanel extends JPanel {

    public CategoryPanel() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BACKGROUND);

        add(createHeader(), BorderLayout.NORTH);
        add(createContentBody(), BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_SUBTLE),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)
        ));

        JLabel titleLabel = new JLabel("Categories");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Organize and manage campus resource categories");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);

        return header;
    }

    private JPanel createContentBody() {
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        // Placeholder note for teammate
        JLabel placeholder = new JLabel("<html><center><b>Category Management Page</b><br>"
                + "<span style='color:#8C99A3; font-size:12px;'>UI components (table, add/edit forms) will be placed here.</span></center></html>");
        placeholder.setFont(new Font("SansSerif", Font.PLAIN, 15));
        placeholder.setForeground(TEXT_SLATE);

        body.add(placeholder);
        return body;
    }

    /**
     * Called when this tab becomes active, or when data needs refreshing.
     */
    public void refreshData() {
        // TODO for teammate: Reload categories using CategoryDAO.selectAllActive() and calculate counts via loop
    }
}
