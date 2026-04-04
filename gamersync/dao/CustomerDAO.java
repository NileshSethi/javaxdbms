package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// CustomerDAO extends BaseDAO (Inheritance) and implements ICustomerDAO (Interface)
public class CustomerDAO extends BaseDAO implements ICustomerDAO {

    // INSERT customer
    @Override
    public void addCustomer(Customer c) throws SQLException, InvalidDataException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new InvalidDataException("Customer name cannot be empty.");
        }
        String sql = "INSERT INTO CUSTOMER (CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, c.getCustId());
        ps.setString(2, c.getName());
        ps.setString(3, c.getPhone());
        ps.setString(4, c.getEmail());
        ps.setString(5, c.getRegisteredDate());
        ps.executeUpdate();
        ps.close();
    }

    // SELECT all customers
    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Customer c = new Customer(
                rs.getInt("CUST_ID"),
                rs.getString("NAME"),
                rs.getString("PHONE"),
                rs.getString("EMAIL"),
                rs.getString("REGISTERED_DATE")
            );
            list.add(c);
        }
        rs.close();
        ps.close();
        return list;
    }

    // Overloaded method 1 - get by ID (Polymorphism via Method Overloading)
    @Override
    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT * FROM CUSTOMER WHERE CUST_ID = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Customer(
                rs.getInt("CUST_ID"),
                rs.getString("NAME"),
                rs.getString("PHONE"),
                rs.getString("EMAIL"),
                rs.getString("REGISTERED_DATE")
            );
        }
        return null;
    }

    // Overloaded method 2 - get by Name (Polymorphism via Method Overloading)
    @Override
    public List<Customer> getCustomerByName(String name) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER WHERE NAME LIKE ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, "%" + name + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new Customer(
                rs.getInt("CUST_ID"),
                rs.getString("NAME"),
                rs.getString("PHONE"),
                rs.getString("EMAIL"),
                rs.getString("REGISTERED_DATE")
            ));
        }
        rs.close();
        ps.close();
        return list;
    }

    // UPDATE customer
    @Override
    public void updateCustomer(Customer c) throws SQLException, InvalidDataException {
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new InvalidDataException("Customer name cannot be empty.");
        }
        String sql = "UPDATE CUSTOMER SET NAME=?, PHONE=?, EMAIL=?, REGISTERED_DATE=? WHERE CUST_ID=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, c.getName());
        ps.setString(2, c.getPhone());
        ps.setString(3, c.getEmail());
        ps.setString(4, c.getRegisteredDate());
        ps.setInt(5, c.getCustId());
        ps.executeUpdate();
        ps.close();
    }

    // DELETE customer
    @Override
    public void deleteCustomer(int custId) throws SQLException {
        String sql = "DELETE FROM CUSTOMER WHERE CUST_ID = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, custId);
        ps.executeUpdate();
        ps.close();
    }
}
