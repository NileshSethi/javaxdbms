package gamersync.ui;

import gamersync.dao.CustomerDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

// Customer CRUD UI - Insert, View, Update, Delete
public class CustomerUI extends JFrame {

    private CustomerDAO dao = new CustomerDAO();

    // Input fields
    private JTextField tfId    = new JTextField(8);
    private JTextField tfName  = new JTextField(15);
    private JTextField tfPhone = new JTextField(12);
    private JTextField tfEmail = new JTextField(18);
    private JTextField tfDate  = new JTextField(12); // Format: YYYY-MM-DD

    // Table
    private JTable table;
    private DefaultTableModel tableModel;

    public CustomerUI() {
        setTitle("Customer Module – GamerSync");
        setSize(750, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ── Input Panel ──
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Customer Details"));
        inputPanel.add(new JLabel("Cust ID:")); inputPanel.add(tfId);
        inputPanel.add(new JLabel("Name:"));    inputPanel.add(tfName);
        inputPanel.add(new JLabel("Phone:"));   inputPanel.add(tfPhone);
        inputPanel.add(new JLabel("Email:"));   inputPanel.add(tfEmail);
        inputPanel.add(new JLabel("Reg Date (YYYY-MM-DD):")); inputPanel.add(tfDate);

        // ── Button Panel ──
        JButton btnInsert = new JButton("INSERT");
        JButton btnView   = new JButton("VIEW");
        JButton btnUpdate = new JButton("UPDATE");
        JButton btnDelete = new JButton("DELETE");

        styleButton(btnInsert, new Color(0, 120, 215));
        styleButton(btnView,   new Color(100, 100, 100));
        styleButton(btnUpdate, new Color(200, 130, 0));
        styleButton(btnDelete, new Color(200, 0, 0));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.add(btnInsert);
        btnPanel.add(btnView);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(btnPanel, BorderLayout.SOUTH);

        // ── Table ──
        String[] cols = {"CUST_ID", "NAME", "PHONE", "EMAIL", "REGISTERED_DATE"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFieldsFromSelection());

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Button Actions ──
        btnInsert.addActionListener(e -> insertCustomer());
        btnView.addActionListener(e -> viewCustomers());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());

        // Load data on open
        viewCustomers();
    }

    private void insertCustomer() {
        try {
            Customer c = buildCustomerFromFields();
            dao.addCustomer(c);
            JOptionPane.showMessageDialog(this, "Customer added successfully!");
            clearFields();
            viewCustomers();
        } catch (InvalidDataException e) {
            JOptionPane.showMessageDialog(this, "Validation Error: " + e.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewCustomers() {
        try {
            List<Customer> list = dao.getAllCustomers();
            tableModel.setRowCount(0);
            for (Customer c : list) {
                tableModel.addRow(new Object[]{
                    c.getCustId(), c.getName(), c.getPhone(), c.getEmail(), c.getRegisteredDate()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        try {
            Customer c = buildCustomerFromFields();
            dao.updateCustomer(c);
            JOptionPane.showMessageDialog(this, "Customer updated successfully!");
            clearFields();
            viewCustomers();
        } catch (InvalidDataException e) {
            JOptionPane.showMessageDialog(this, "Validation Error: " + e.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Unexpected Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCustomer() {
        try {
            int id = Integer.parseInt(tfId.getText().trim());
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Customer ID: " + id + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dao.deleteCustomer(id);
                JOptionPane.showMessageDialog(this, "Customer deleted successfully!");
                clearFields();
                viewCustomers();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid Customer ID.", "Input Error", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB Error: " + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Fill input fields when user clicks a table row
    private void fillFieldsFromSelection() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            tfId.setText(tableModel.getValueAt(row, 0).toString());
            tfName.setText(tableModel.getValueAt(row, 1).toString());
            tfPhone.setText(tableModel.getValueAt(row, 2).toString());
            tfEmail.setText(tableModel.getValueAt(row, 3).toString());
            tfDate.setText(tableModel.getValueAt(row, 4).toString());
        }
    }

    private Customer buildCustomerFromFields() throws InvalidDataException {
        String idStr = tfId.getText().trim();
        if (idStr.isEmpty()) throw new InvalidDataException("Customer ID cannot be empty.");
        int id;
        try { id = Integer.parseInt(idStr); }
        catch (NumberFormatException e) { throw new InvalidDataException("Customer ID must be a number."); }
        return new Customer(id, tfName.getText().trim(), tfPhone.getText().trim(),
                            tfEmail.getText().trim(), tfDate.getText().trim());
    }

    private void clearFields() {
        tfId.setText(""); tfName.setText(""); tfPhone.setText("");
        tfEmail.setText(""); tfDate.setText("");
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
