package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import java.sql.*;
import java.util.Scanner;

public class FoodPaymentService {
    private final Scanner sc;
    private Connection con;

    public FoodPaymentService(Scanner sc) {
        this.sc = sc;
        try {
            this.con = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("  [ERROR] DB connection failed: " + e.getMessage());
        }
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║         FOOD & PAYMENT MODULE            ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Add Food Order to Session            ║");
            System.out.println("  ║  2. View All Food Orders                 ║");
            System.out.println("  ║  3. View Food Orders by Session ID       ║");
            System.out.println("  ║  4. Add Payment                          ║");
            System.out.println("  ║  5. View All Payments                    ║");
            System.out.println("  ║  6. View Payments by Customer ID         ║");
            System.out.println("  ║  7. View Full Session Bill               ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addFoodOrder(); break;
                case "2": viewAllFoodOrders(); break;
                case "3": viewFoodOrdersBySessionId(); break;
                case "4": addPayment(); break;
                case "5": viewAllPayments(); break;
                case "6": viewPaymentsByCustId(); break;
                case "7": viewFullSessionBill(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void addFoodOrder() {
        try {
            System.out.print("  Enter ORDER_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter ORDER_ITEM: ");
            String item = sc.nextLine().trim();
            System.out.print("  Enter TOTAL_AMOUNT: ");
            double amount = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Enter SESSION_ID: ");
            int sessionId = Integer.parseInt(sc.nextLine().trim());

            if (item.isEmpty()) throw new InvalidDataException("ORDER_ITEM cannot be empty");
            if (amount <= 0) throw new InvalidDataException("TOTAL_AMOUNT must be greater than 0");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO FOOD_ORDER (ORDER_ID, ORDER_ITEM, TOTAL_AMOUNT, SESSION_ID) VALUES (?,?,?,?)");
            ps.setInt(1, id);
            ps.setString(2, item);
            ps.setDouble(3, amount);
            ps.setInt(4, sessionId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Food order added successful.");
        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewAllFoodOrders() {
        try {
            String sql = "SELECT F.ORDER_ID, F.ORDER_ITEM, F.TOTAL_AMOUNT, F.SESSION_ID, G.GAME_NAME " +
                         "FROM FOOD_ORDER F JOIN GAMING_SESSION G ON F.SESSION_ID = G.SESSION_ID";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-8s | %-20s | %-12s | %-10s | %-15s%n", "ORDER_ID", "ORDER_ITEM", "AMOUNT", "SESSION_ID", "GAME_NAME");
            System.out.println("  " + "-".repeat(75));
            while (rs.next()) {
                System.out.printf("  %-8d | %-20s | Rs.%-9.2f | %-10d | %-15s%n",
                    rs.getInt("ORDER_ID"), rs.getString("ORDER_ITEM"), rs.getDouble("TOTAL_AMOUNT"),
                    rs.getInt("SESSION_ID"), rs.getString("GAME_NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewFoodOrdersBySessionId() {
        try {
            System.out.print("  Enter SESSION_ID: ");
            int sessionId = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM FOOD_ORDER WHERE SESSION_ID = ?");
            ps.setInt(1, sessionId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-8s | %-20s | %-12s | %-10s%n", "ORDER_ID", "ORDER_ITEM", "AMOUNT", "SESSION_ID");
            System.out.println("  " + "-".repeat(58));
            while (rs.next()) {
                System.out.printf("  %-8d | %-20s | Rs.%-9.2f | %-10d%n",
                    rs.getInt("ORDER_ID"), rs.getString("ORDER_ITEM"), rs.getDouble("TOTAL_AMOUNT"),
                    rs.getInt("SESSION_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void addPayment() {
        try {
            System.out.print("  Enter PAYMENT_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter PAYMENT_MODE: ");
            String mode = sc.nextLine().trim();
            System.out.print("  Enter AMOUNT: ");
            double amount = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter SESSION_ID: ");
            int sessionId = Integer.parseInt(sc.nextLine().trim());

            if (mode.isEmpty()) throw new InvalidDataException("PAYMENT_MODE cannot be empty");
            if (amount <= 0) throw new InvalidDataException("Amount must be greater than 0");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO PAYMENT (PAYMENT_ID, PAYMENT_MODE, AMOUNT, CUST_ID, SESSION_ID) VALUES (?,?,?,?,?)");
            ps.setInt(1, id);
            ps.setString(2, mode);
            ps.setDouble(3, amount);
            ps.setInt(4, custId);
            ps.setInt(5, sessionId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Payment added successful.");
        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage().contains("Invalid Amount")) {
                System.out.println("  [DB TRIGGER] Payment rejected by database: amount must be > 0");
            } else {
                System.out.println("  [SQL ERROR] " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewAllPayments() {
        try {
            String sql = "SELECT P.PAYMENT_ID, P.PAYMENT_MODE, P.AMOUNT, P.CUST_ID, P.SESSION_ID, C.NAME " +
                         "FROM PAYMENT P JOIN CUSTOMER C ON P.CUST_ID = C.CUST_ID";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-10s | %-12s | %-10s | %-8s | %-10s | %-15s%n", "PAYMENT_ID", "MODE", "AMOUNT", "CUST_ID", "SESSION_ID", "CUST_NAME");
            System.out.println("  " + "-".repeat(82));
            while (rs.next()) {
                System.out.printf("  %-10d | %-12s | Rs.%-7.2f | %-8d | %-10d | %-15s%n",
                    rs.getInt("PAYMENT_ID"), rs.getString("PAYMENT_MODE"), rs.getDouble("AMOUNT"),
                    rs.getInt("CUST_ID"), rs.getInt("SESSION_ID"), rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewPaymentsByCustId() {
        try {
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT PAYMENT_ID, PAYMENT_MODE, AMOUNT, SESSION_ID FROM PAYMENT WHERE CUST_ID = ?");
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-10s | %-12s | %-10s | %-10s%n", "PAYMENT_ID", "MODE", "AMOUNT", "SESSION_ID");
            System.out.println("  " + "-".repeat(50));
            while (rs.next()) {
                System.out.printf("  %-10d | %-12s | Rs.%-7.2f | %-10d%n",
                    rs.getInt("PAYMENT_ID"), rs.getString("PAYMENT_MODE"), rs.getDouble("AMOUNT"), rs.getInt("SESSION_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewFullSessionBill() {
        try {
            System.out.print("  Enter SESSION_ID: ");
            int sessionId = Integer.parseInt(sc.nextLine().trim());

            String sqlInfo = "SELECT G.SESSION_ID, C.NAME, G.GAME_NAME, G.START_TIME, G.END_TIME, " +
                             "G.DURATION, G.PC_ID, PC.HOURLY_RATE " +
                             "FROM GAMING_SESSION G " +
                             "JOIN CUSTOMER C ON G.CUST_ID = C.CUST_ID " +
                             "JOIN PC ON G.PC_ID = PC.PC_ID " +
                             "WHERE G.SESSION_ID = ?";
            PreparedStatement ps1 = con.prepareStatement(sqlInfo);
            ps1.setInt(1, sessionId);
            ResultSet rs1 = ps1.executeQuery();

            if (!rs1.next()) {
                System.out.println("  [!] Session not found.");
                rs1.close(); ps1.close();
                return;
            }

            String cName = rs1.getString("NAME");
            String gName = rs1.getString("GAME_NAME");
            int pcId = rs1.getInt("PC_ID");
            int duration = rs1.getInt("DURATION");
            rs1.close(); ps1.close();

            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║        GAMERSYNC — SESSION BILL          ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.printf("  ║  Customer   : %-26s ║%n", cName);
            System.out.printf("  ║  Game       : %-26s ║%n", gName);
            System.out.printf("  ║  PC ID      : %-26d ║%n", pcId);
            System.out.printf("  ║  Duration   : %-21s mins ║%n", duration);
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  FOOD ORDERS:                            ║");

            PreparedStatement ps2 = con.prepareStatement("SELECT ORDER_ITEM, TOTAL_AMOUNT FROM FOOD_ORDER WHERE SESSION_ID = ?");
            ps2.setInt(1, sessionId);
            ResultSet rs2 = ps2.executeQuery();
            double totalFood = 0;
            boolean hasFood = false;
            while (rs2.next()) {
                hasFood = true;
                String iName = rs2.getString("ORDER_ITEM");
                double iAmt = rs2.getDouble("TOTAL_AMOUNT");
                totalFood += iAmt;
                System.out.printf("  ║  %-18s  Rs. %-13.2f ║%n", iName, iAmt);
            }
            rs2.close(); ps2.close();

            if (!hasFood) {
                System.out.println("  ║  No food orders for this session.        ║");
            }
            System.out.printf("  ║  Food Total        : Rs. %-15.2f ║%n", totalFood);
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  PAYMENT:                                ║");

            PreparedStatement ps3 = con.prepareStatement("SELECT PAYMENT_MODE, AMOUNT FROM PAYMENT WHERE SESSION_ID = ?");
            ps3.setInt(1, sessionId);
            ResultSet rs3 = ps3.executeQuery();
            boolean hasPayment = false;
            while (rs3.next()) {
                hasPayment = true;
                System.out.printf("  ║  Mode              : %-19s ║%n", rs3.getString("PAYMENT_MODE"));
                System.out.printf("  ║  Amount Paid       : Rs. %-15.2f ║%n", rs3.getDouble("AMOUNT"));
            }
            rs3.close(); ps3.close();

            if (!hasPayment) {
                System.out.println("  ║  No payment recorded for this session.   ║");
            }
            System.out.println("  ╚══════════════════════════════════════════╝");
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}