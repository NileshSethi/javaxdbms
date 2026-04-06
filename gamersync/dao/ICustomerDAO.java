package gamersync.dao;

import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.SQLException;
import java.util.List;

// CO2: Interface — defines contract for Customer operations
public interface ICustomerDAO {
    void         addCustomer(Customer c)          throws SQLException, InvalidDataException;
    List<Customer> getAllCustomers()              throws SQLException;
    Customer     getCustomerById(int id)          throws SQLException;        // overloaded 1
    List<Customer> getCustomerByName(String name) throws SQLException;        // overloaded 2
    void         updateCustomer(Customer c)       throws SQLException, InvalidDataException;
    void         deleteCustomer(int custId)       throws SQLException;
}
