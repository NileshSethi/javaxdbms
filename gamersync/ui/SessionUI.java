package gamersync.ui;

import gamersync.dao.SessionDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Gaming Session UI - Insert, View, Delete
public class SessionUI extends JFrame {

    private SessionDAO dao = new SessionDAO();

    // Input fields
    private JTextField tfSessionId  = new JTextField(6);
    private JTextField tfStartTime  = new JTextField(16); // Format: YYYY-MM-DD HH:MM:SS
    private JTextField tfEndTime    = new JTextField(16);
    private JTextField tfDuration   = new JTextField(5);
    private JTextField tfGameName   = new JTextField(12);
    private JTextField tfCustId     = new JTextField(5);
    private JTextField tfPcId       = new JTextField(5);

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public SessionUI() {
        setTitle("Gaming Session Module – GamerSync");
        setSize(820, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ── Input Panel ──
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Session Details"));
        inputPanel.add(new JLabel("Session ID:"));    inputPanel.add(tfSessionId);
        inputPanel.add(new JLabel("Start (YYYY-MM-DD HH:MM):")); inputPanel.add(tfStartTime);
        inputPanel.add(new JLabel("End (YYYY-MM-DD HH:MM):"));   inputPanel.add(tfEndTime);
        inputPanel.add(new JLabel("Duration (mins):")); inputPanel.add(tfDuration);
        inputPanel.add(new JLabel("Game Name:"));     inputPanel.add(tfGameName);
        inputPanel.add(new JLabel("Cust ID:"));       inputPanel.add(tfCustId);
        inputPanel.add(new JLabel("PC ID:"));         inputPanel.add(tfPcId);

        // ── Button Panel ──
        JButton btnInsert = new JButton("INSERT");
        JButton btnView   = new JButton("VIEW");
        JButton btnDelete = new JButton("DELETE");

        styleButton(btnInsert, new Color(0, 120, 215));
        styleButton(btnView,   new Color(100, 100, 100));
        styleButton(btnDelete, new Color(200, 0, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.add(btnInsert);
        btnPanel.add(btnView);
        btnPanel.add(btnDelete);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        // ── Table ──
        String[] cols = {"SESSION_ID", "START_TIME", "END_TIME", "DURATION", "GAME_NAME", "CUST_ID", "PC_ID"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFieldsFromSelection());

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Button Actions ──
        btnInsert.addActionListener(e -> insertSession());
        btnView.addActionListener(e -> viewSessions());
        btnDelete.addActionListener(e -> deleteSession());

        // Load data on open
        viewSessions();
    }

    private void insertSession() {
        try {
            GamingSession s = buildSessionFromFields();
            dao.addSession(s);
            JOptionPane.showMessageDialog(this, "Session added successfully!");
            clearFields();
            viewSessions();
        } catch (InvalidDataException e) {
            JOptionPane.showMessageDialog(this, "Validation Error: " + e.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSessions() {
        try {
            List<GamingSession> list = dao.getAllSessions();
            tableModel.setRowCount(0);
            for (GamingSession s : list) {
                tableModel.addRow(new Object[]{
                    s.getSessionId(), s.getStartTime(), s.getEndTime(),
                    s.getDuration(), s.getGameName(), s.getCustId(), s.getPcId()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSession() {
        try {
            int id = Integer.parseInt(tfSessionId.getText().trim());
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete Session ID " + id + "? This also removes linked payments and food orders.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dao.deleteSession(id);
                JOptionPane.showMessageDialog(this, "Session deleted successfully!");
                clearFields();
                viewSessions();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Session ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fillFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tfSessionId.setText(tableModel.getValueAt(row, 0).toString());
            tfStartTime.setText(tableModel.getValueAt(row, 1).toString());
            tfEndTime.setText(tableModel.getValueAt(row, 2).toString());
            tfDuration.setText(tableModel.getValueAt(row, 3).toString());
            tfGameName.setText(tableModel.getValueAt(row, 4).toString());
            tfCustId.setText(tableModel.getValueAt(row, 5).toString());
            tfPcId.setText(tableModel.getValueAt(row, 6).toString());
        }
    }

    private GamingSession buildSessionFromFields() throws InvalidDataException {
        String idStr  = tfSessionId.getText().trim();
        String durStr = tfDuration.getText().trim();
        String cIdStr = tfCustId.getText().trim();
        String pIdStr = tfPcId.getText().trim();

        if (idStr.isEmpty()) throw new InvalidDataException("Session ID cannot be empty.");
        int sessionId, duration, custId, pcId;
        try { sessionId = Integer.parseInt(idStr); } catch (NumberFormatException e) { throw new InvalidDataException("Session ID must be a number."); }
        try { duration  = Integer.parseInt(durStr); } catch (NumberFormatException e) { throw new InvalidDataException("Duration must be a number."); }
        try { custId    = Integer.parseInt(cIdStr); } catch (NumberFormatException e) { throw new InvalidDataException("Customer ID must be a number."); }
        try { pcId      = Integer.parseInt(pIdStr); } catch (NumberFormatException e) { throw new InvalidDataException("PC ID must be a number."); }

        return new GamingSession(sessionId, tfStartTime.getText().trim(), tfEndTime.getText().trim(),
                                 duration, tfGameName.getText().trim(), custId, pcId);
    }

    private void clearFields() {
        tfSessionId.setText(""); tfStartTime.setText(""); tfEndTime.setText("");
        tfDuration.setText(""); tfGameName.setText(""); tfCustId.setText(""); tfPcId.setText("");
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
