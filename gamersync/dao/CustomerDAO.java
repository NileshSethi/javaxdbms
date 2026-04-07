package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.*;
import java.util.*;

// CO2: extends BaseDAO (Inheritance) + implements ICustomerDAO (Interface)
// CO4: DML (insert, update, delete) + DRL (select)
public class CustomerDAO extends BaseDAO implements ICustomerDAO {

    // ── INSERT (DML) ─────────────────────────────────────────────────────────
    @Override
    public void addCustomer(Customer c) throws SQLException, InvalidDataException {
        if (c.getName() == null || c.getName().trim().isEmpty())
            throw new InvalidDataException("Customer name cannot be empty.");
        if (c.getPhone().length() > 15)
            throw new InvalidDataException("Phone number too long (max 15 digits).");

        String sql = "INSERT INTO CUSTOMER (CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getCustId());
        ps.setString(2, c.getName());
        ps.setString(3, c.getPhone());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getRegisteredDate());
        ps.executeUpdate();
        ps.close();
    }

    // ── SELECT ALL (DRL) ─────────────────────────────────────────────────────
    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM CUSTOMER");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Customer(
                rs.getInt("CUST_ID"), rs.getString("NAME"),
                rs.getString("PHONE"), rs.getString("EMAIL"),
                rs.getString("REGISTERED_DATE")
            ));
        }
        rs.close(); ps.close();
        return list;
    }

    // ── SELECT BY ID — overloaded method 1 (CO2: Polymorphism) ──────────────
    @Override
    public Customer getCustomerById(int id) throws SQLException {
        PreparedStatement ps = con.prepareStatement("SELECT * FROM CUSTOMER WHERE CUST_ID=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Customer(rs.getInt("CUST_ID"), rs.getString("NAME"),
                rs.getString("PHONE"), rs.getString("EMAIL"), rs.getString("REGISTERED_DATE"));
        }
        return null;
    }

    // ── SELECT BY NAME — overloaded method 2 (CO2: Polymorphism) ────────────
    @Override
    public List<Customer> getCustomerByName(String name) throws SQLException {
        List<Customer> list = new ArrayList<>();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM CUSTOMER WHERE NAME LIKE ?");
        ps.setString(1, "%" + name + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Customer(rs.getInt("CUST_ID"), rs.getString("NAME"),
                rs.getString("PHONE"), rs.getString("EMAIL"), rs.getString("REGISTERED_DATE")));
        }
        rs.close(); ps.close();
        return list;
    }

    // ── UPDATE (DML) ─────────────────────────────────────────────────────────
    @Override
    public void updateCustomer(Customer c) throws SQLException, InvalidDataException {
        if (c.getName() == null || c.getName().trim().isEmpty())
            throw new InvalidDataException("Customer name cannot be empty.");

        String sql = "UPDATE CUSTOMER SET NAME=?, PHONE=?, EMAIL=?, REGISTERED_DATE=? WHERE CUST_ID=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getName());
        ps.setString(2, c.getPhone());
        ps.setString(3, c.getEmail());
        ps.setString(4, c.getRegisteredDate());
        ps.setInt(5, c.getCustId());
        int rows = ps.executeUpdate();
        ps.close();
        if (rows == 0) throw new InvalidDataException("No customer found with ID: " + c.getCustId());
    }

    // ── DELETE (DML) ─────────────────────────────────────────────────────────
    @Override
    public void deleteCustomer(int custId) throws SQLException {
        // Delete child rows first to avoid foreign key constraint errors
        con.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE GAMING_ACC_ID IN (SELECT GAMING_ACC_ID FROM GAMING_ACC WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM GAMING_ACC WHERE CUST_ID=" + custId).executeUpdate();
        
        con.prepareStatement("DELETE FROM FOOD_ORDER WHERE SESSION_ID IN (SELECT SESSION_ID FROM GAMING_SESSION WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM PAYMENT WHERE SESSION_ID IN (SELECT SESSION_ID FROM GAMING_SESSION WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM PAYMENT WHERE CUST_ID=" + custId).executeUpdate(); // Catch any payments just linked to customer
        con.prepareStatement("DELETE FROM GAMING_SESSION WHERE CUST_ID=" + custId).executeUpdate();

        con.prepareStatement("DELETE FROM HOURLY WHERE MEMBERSHIP_ID IN (SELECT MEMBERSHIP_ID FROM MEMBERSHIP WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM WEEKLY WHERE MEMBERSHIP_ID IN (SELECT MEMBERSHIP_ID FROM MEMBERSHIP WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM MONTHLY WHERE MEMBERSHIP_ID IN (SELECT MEMBERSHIP_ID FROM MEMBERSHIP WHERE CUST_ID=" + custId + ")").executeUpdate();
        con.prepareStatement("DELETE FROM MEMBERSHIP WHERE CUST_ID=" + custId).executeUpdate();

        con.prepareStatement("DELETE FROM TOURNAMENTS WHERE CUST_ID=" + custId).executeUpdate();

        // Finally delete the customer
        PreparedStatement ps = con.prepareStatement("DELETE FROM CUSTOMER WHERE CUST_ID=?");
        ps.setInt(1, custId);
        ps.executeUpdate();
        ps.close();
    }
}
