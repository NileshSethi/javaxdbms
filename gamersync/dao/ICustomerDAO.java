package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.SQLException;
import java.util.List;

// Interface defining the contract for all Customer DB operations
public interface ICustomerDAO extends CRUDOperations<Customer, Integer> {
    void addCustomer(Customer c) throws SQLException, InvalidDataException;
    List<Customer> getAllCustomers() throws SQLException;
    Customer getCustomerById(int id) throws SQLException;          // Overloaded method 1
    List<Customer> getCustomerByName(String name) throws SQLException; // Overloaded method 2
    void updateCustomer(Customer c) throws SQLException, InvalidDataException;
    void deleteCustomer(int custId) throws SQLException;
}
