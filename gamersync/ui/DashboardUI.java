package gamersync.ui;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;

// Main dashboard - entry screen shown on launch
public class DashboardUI extends JFrame {

    public DashboardUI() {
        setTitle("GamerSync - Gaming Cafe Management System");
        setSize(520, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("GamerSync", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 8, 0));

        JLabel subtitle = new JLabel("Gaming Cafe Management System", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel headerPanel = new JPanel(new GridLayout(2, 1));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        headerPanel.add(title);
        headerPanel.add(subtitle);

        // Buttons panel
        JButton btnCustomer = new JButton("Customer Module");
        JButton btnSession = new JButton("Gaming Session Module");

        styleButton(btnCustomer);
        styleButton(btnSession);

        btnCustomer.addActionListener(e -> {
            openModule(CustomerUI::new);
        });

        btnSession.addActionListener(e -> {
            openModule(SessionUI::new);
        });

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));
        btnPanel.add(btnCustomer);
        btnPanel.add(btnSession);

        // Footer
        JLabel footer = new JLabel("GamerSync DB  |  GamerSync v1.0", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        JPanel footerPanel = new JPanel();
        footerPanel.add(footer);

        add(headerPanel, BorderLayout.NORTH);
        add(btnPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(true);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void openModule(Supplier<JFrame> moduleFactory) {
        try {
            JFrame module = moduleFactory.get();
            module.setVisible(true);
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Module could not be opened.\n" + ex.getMessage(),
                "Startup Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
