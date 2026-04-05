package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// CustomerDAO extends BaseDAO (Inheritance) and implements ICustomerDAO (Interface)
public class CustomerDAO extends BaseDAO implements ICustomerDAO {

    // INSERT customer via generic CRUD interface
    @Override
    public void insert(Customer c) throws SQLException, InvalidDataException {
        validateCustomer(c);
        String sql = "INSERT INTO CUSTOMER (CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getCustId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getRegisteredDate());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No customer row inserted for ID: " + c.getCustId());
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to insert customer: " + e.getMessage(), e);
        }
    }

    // SELECT all customers via generic CRUD interface
    @Override
    public List<Customer> getAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE FROM CUSTOMER";

        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to fetch customers: " + e.getMessage(), e);
        }

        return list;
    }

    // Overloaded method 1 - get by ID (Polymorphism via Method Overloading)
    @Override
    public Customer getCustomerById(int id) throws SQLException {
        String sql = "SELECT * FROM CUSTOMER WHERE CUST_ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapCustomer(rs);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to fetch customer by ID " + id + ": " + e.getMessage(), e);
        }

        return null;
    }

    // Overloaded method 2 - get by Name (Polymorphism via Method Overloading)
    @Override
    public List<Customer> getCustomerByName(String name) throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER WHERE NAME LIKE ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to fetch customers by name: " + e.getMessage(), e);
        }

        return list;
    }

    // UPDATE customer via generic CRUD interface
    @Override
    public void update(Customer c) throws SQLException, InvalidDataException {
        validateCustomer(c);
        String sql = "UPDATE CUSTOMER SET NAME=?, PHONE=?, EMAIL=?, REGISTERED_DATE=? WHERE CUST_ID=?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getRegisteredDate());
            ps.setInt(5, c.getCustId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No customer row updated for ID: " + c.getCustId());
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to update customer: " + e.getMessage(), e);
        }
    }

    // DELETE customer via generic CRUD interface
    @Override
    public void delete(Integer custId) throws SQLException {
        if (custId == null) {
            throw new SQLException("Customer ID cannot be null for delete.");
        }

        String sql = "DELETE FROM CUSTOMER WHERE CUST_ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, custId);

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No customer row deleted for ID: " + custId);
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to delete customer: " + e.getMessage(), e);
        }
    }

    // Alias methods retained for existing UI/service calls
    @Override
    public void addCustomer(Customer c) throws SQLException, InvalidDataException {
        insert(c);
    }

    @Override
    public List<Customer> getAllCustomers() throws SQLException {
        return getAll();
    }

    @Override
    public void updateCustomer(Customer c) throws SQLException, InvalidDataException {
        update(c);
    }

    @Override
    public void deleteCustomer(int custId) throws SQLException {
        delete(custId);
    }

    private void validateCustomer(Customer c) throws InvalidDataException {
        if (c == null) {
            throw new InvalidDataException("Customer object cannot be null.");
        }
        if (c.getCustId() <= 0) {
            throw new InvalidDataException("Customer ID must be greater than 0.");
        }
        if (c.getName() == null || c.getName().trim().isEmpty()) {
            throw new InvalidDataException("Customer name cannot be empty.");
        }
        if (c.getRegisteredDate() == null || c.getRegisteredDate().trim().isEmpty()) {
            throw new InvalidDataException("Registered date cannot be empty.");
        }
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("CUST_ID"),
            rs.getString("NAME"),
            rs.getString("PHONE"),
            rs.getString("EMAIL"),
            rs.getString("REGISTERED_DATE")
        );
    }
}
