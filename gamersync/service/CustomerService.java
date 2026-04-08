package gamersync.service;

import gamersync.dao.CustomerDAO;
import gamersync.db.InvalidDataException;
import gamersync.db.ValidationHelper;
import gamersync.model.Customer;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CustomerService {

    private final CustomerDAO dao = new CustomerDAO();
    private final Scanner sc;

    public CustomerService(Scanner sc) { this.sc = sc; }

    // ── Main menu loop ────────────────────────────────────────────────────────
    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ╔══════════════════════════╗");
            System.out.println("  ║    CUSTOMER MODULE       ║");
            System.out.println("  ╠══════════════════════════╣");
            System.out.println("  ║  1. Add Customer         ║");
            System.out.println("  ║  2. View All Customers   ║");
            System.out.println("  ║  3. Search by ID         ║");
            System.out.println("  ║  4. Search by Name       ║");
            System.out.println("  ║  5. Update Customer      ║");
            System.out.println("  ║  6. Delete Customer      ║");
            System.out.println("  ║  0. Back                 ║");
            System.out.println("  ╚══════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addCustomer();    break;
                case "2": viewAll();        break;
                case "3": searchById();     break;
                case "4": searchByName();   break;
                case "5": updateCustomer(); break;
                case "6": deleteCustomer(); break;
                case "0": back = true;      break;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── ADD ───────────────────────────────────────────────────────────────────
    private void addCustomer() {
        try {
            System.out.print("  Customer ID   : "); int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Name          : "); String name = sc.nextLine().trim();
            System.out.print("  Phone         : "); String phone = sc.nextLine().trim();
            System.out.print("  Email         : "); String email = sc.nextLine().trim();
            System.out.print("  Reg Date (YYYY-MM-DD): "); String date = sc.nextLine().trim();

            ValidationHelper.validateNotEmpty(name, "Name");
            ValidationHelper.validatePhone(phone);
            ValidationHelper.validateEmail(email);
            ValidationHelper.validateDate(date, "Registered Date");
            ValidationHelper.validatePositiveInt(id, "Customer ID");

            dao.addCustomer(new Customer(id, name, phone, email, date));
            System.out.println("  [✓] Customer added successfully.");

        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Customer ID must be a number.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    // ── VIEW ALL ──────────────────────────────────────────────────────────────
    private void viewAll() {
        try {
            List<Customer> list = dao.getAllCustomers();
            if (list.isEmpty()) { System.out.println("  No customers found."); return; }
            System.out.println("\n  " + "-".repeat(80));
            System.out.printf("  | %-4s | %-15s | %-12s | %-25s | %-12s |%n",
                "ID","NAME","PHONE","EMAIL","REG DATE");
            System.out.println("  " + "-".repeat(80));
            for (Customer c : list) System.out.println("  " + c);
            System.out.println("  " + "-".repeat(80));
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── SEARCH BY ID ──────────────────────────────────────────────────────────
    private void searchById() {
        try {
            System.out.print("  Enter Customer ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            Customer c = dao.getCustomerById(id);
            if (c == null) System.out.println("  No customer found with ID: " + id);
            else { System.out.println("\n  " + "-".repeat(80)); System.out.println("  " + c); System.out.println("  " + "-".repeat(80)); }
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] ID must be a number.");
        }
    }

    // ── SEARCH BY NAME ────────────────────────────────────────────────────────
    private void searchByName() {
        try {
            System.out.print("  Enter Name (partial ok): ");
            String name = sc.nextLine().trim();
            List<Customer> list = dao.getCustomerByName(name);
            if (list.isEmpty()) System.out.println("  No customers found matching: " + name);
            else { for (Customer c : list) System.out.println("  " + c); }
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────
    private void updateCustomer() {
        try {
            System.out.print("  Customer ID to update: "); 
            int id = Integer.parseInt(sc.nextLine().trim());
            
            Customer current = dao.getCustomerById(id);
            if (current == null) {
                System.out.println("  [!] Customer not found.");
                return;
            }
            
            System.out.println("  Current Name      : " + current.getName());
            System.out.println("  Current Phone     : " + current.getPhone());
            System.out.println("  Current Email     : " + current.getEmail());
            System.out.println("  Current Reg Date  : " + current.getRegisteredDate());

            System.out.print("  New Name (or press Enter to keep current)  : "); 
            String nameInput  = sc.nextLine().trim();
            System.out.print("  New Phone (or press Enter to keep current) : "); 
            String phoneInput = sc.nextLine().trim();
            System.out.print("  New Email (or press Enter to keep current) : "); 
            String emailInput = sc.nextLine().trim();
            System.out.print("  New Date (YYYY-MM-DD) (or Enter to keep)   : "); 
            String dateInput = sc.nextLine().trim();
String name = nameInput.isEmpty() ? current.getName() : nameInput;
            String phone = phoneInput.isEmpty() ? current.getPhone() : phoneInput;
            String email = emailInput.isEmpty() ? current.getEmail() : emailInput;
            String date = dateInput.isEmpty() ? current.getRegisteredDate() : dateInput;

            ValidationHelper.validateNotEmpty(name, "Name");
            ValidationHelper.validatePhone(phone);
            ValidationHelper.validateEmail(email);
            ValidationHelper.validateDate(date, "Registered Date");
            ValidationHelper.validatePositiveInt(id, "Customer ID");

            dao.updateCustomer(new Customer(id, name, phone, email, date));
            System.out.println("  [✓] Customer updated successfully.");

        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Customer ID must be a number.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    private void deleteCustomer() {
        try {
            System.out.print("  Customer ID to delete: "); int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Confirm delete ID " + id + "? (yes/no): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("  Cancelled."); return; }
            dao.deleteCustomer(id);
            System.out.println("  [✓] Customer deleted.");
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] ID must be a number.");
        }
    }
}

