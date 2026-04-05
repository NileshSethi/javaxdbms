package gamersync;

import gamersync.dao.CRUDOperations;
import gamersync.dao.CustomerDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.Customer;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class Phase2DAOCheckpoint {
    public static void main(String[] args) {
        CRUDOperations<Customer, Integer> dao = new CustomerDAO(); // Polymorphism
        CustomerDAO customerDAO = (CustomerDAO) dao;

        int testId = 900000 + (int) (System.currentTimeMillis() % 100000);
        String suffix = String.valueOf(testId);
        String phone = "9" + suffix.substring(Math.max(0, suffix.length() - 9));

        Customer testCustomer = new Customer(
            testId,
            "Phase2_Test_User",
            phone,
            "phase2_" + testId + "@gamersync.local",
            LocalDate.now().toString()
        );

        boolean inserted = false;

        try {
            dao.insert(testCustomer);
            inserted = true;
            System.out.println("Insert checkpoint passed for CUST_ID = " + testId);

            Customer insertedCustomer = customerDAO.getCustomerById(testId);
            if (insertedCustomer == null) {
                throw new SQLException("Inserted customer was not found during verification.");
            }

            List<Customer> allCustomers = dao.getAll();
            System.out.println("Select checkpoint passed. Rows fetched = " + allCustomers.size());

            insertedCustomer.setName("Phase2_Updated_User");
            insertedCustomer.setPhone("8" + suffix.substring(Math.max(0, suffix.length() - 9)));
            insertedCustomer.setEmail("phase2_updated_" + testId + "@gamersync.local");
            dao.update(insertedCustomer);

            Customer updatedCustomer = customerDAO.getCustomerById(testId);
            if (updatedCustomer == null || !"Phase2_Updated_User".equals(updatedCustomer.getName())) {
                throw new SQLException("Updated customer verification failed.");
            }
            System.out.println("Update checkpoint passed for CUST_ID = " + testId);

            dao.delete(testId);
            inserted = false;

            Customer deletedCustomer = customerDAO.getCustomerById(testId);
            if (deletedCustomer != null) {
                throw new SQLException("Delete checkpoint failed. Customer still exists.");
            }
            System.out.println("Delete checkpoint passed for CUST_ID = " + testId);

            System.out.println("PHASE 2 CHECKPOINT PASSED");
        } catch (InvalidDataException e) {
            System.out.println("Validation failure during Phase 2: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("SQLException during Phase 2: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Root cause: " + e.getCause().getMessage());
            }
        } finally {
            if (inserted) {
                try {
                    customerDAO.deleteCustomer(testId);
                    System.out.println("Cleanup done for CUST_ID = " + testId);
                } catch (SQLException cleanupError) {
                    System.out.println("Cleanup warning: " + cleanupError.getMessage());
                }
            }
        }
    }
}
