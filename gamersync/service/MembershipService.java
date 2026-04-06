package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import java.sql.*;
import java.util.Scanner;

public class MembershipService {
    private final Scanner sc;
    private Connection con;

    public MembershipService(Scanner sc) {
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
            System.out.println("  ║          MEMBERSHIP MODULE               ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Add Membership (Hourly/Weekly/Month) ║");
            System.out.println("  ║  2. View All Memberships                 ║");
            System.out.println("  ║  3. View Memberships by Customer ID      ║");
            System.out.println("  ║  4. View Hourly Memberships              ║");
            System.out.println("  ║  5. View Weekly Memberships              ║");
            System.out.println("  ║  6. View Monthly Memberships             ║");
            System.out.println("  ║  7. Delete Membership                    ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addMembership(); break;
                case "2": viewAllMemberships(); break;
                case "3": viewMembershipsByCustId(); break;
                case "4": viewHourly(); break;
                case "5": viewWeekly(); break;
                case "6": viewMonthly(); break;
                case "7": deleteMembership(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void addMembership() {
        try {
            System.out.println("  1. Hourly");
            System.out.println("  2. Weekly");
            System.out.println("  3. Monthly");
            System.out.print("  Select Membership Type: ");
            String mChoice = sc.nextLine().trim();
            if(!mChoice.equals("1") && !mChoice.equals("2") && !mChoice.equals("3")) {
                throw new InvalidDataException("Invalid membership type choice.");
            }
            
            System.out.print("  Enter MEMBERSHIP_ID: ");
            int memId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter START_DATE (YYYY-MM-DD): ");
            String startDate = sc.nextLine().trim();
            System.out.print("  Enter END_DATE (YYYY-MM-DD): ");
            String endDate = sc.nextLine().trim();
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());

            String typeStr = "Hourly";
            if (mChoice.equals("2")) typeStr = "Weekly";
            if (mChoice.equals("3")) typeStr = "Monthly";

            PreparedStatement psMem = con.prepareStatement(
                "INSERT INTO MEMBERSHIP (MEMBERSHIP_ID, MEM_TYPE, START_DATE, END_DATE, CUST_ID) VALUES (?, ?, ?, ?, ?)");
            psMem.setInt(1, memId);
            psMem.setString(2, typeStr);
            psMem.setString(3, startDate);
            psMem.setString(4, endDate);
            psMem.setInt(5, custId);
            psMem.executeUpdate();
            psMem.close();

            if (mChoice.equals("1")) {
                System.out.print("  Enter HRS_PURCHASED: ");
                int hrs = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter COST_HR: ");
                double cost = Double.parseDouble(sc.nextLine().trim());
                if (hrs <= 0 || cost <= 0) throw new InvalidDataException("Hours and cost must be > 0");
                PreparedStatement psType = con.prepareStatement("INSERT INTO HOURLY (MEMBERSHIP_ID, HRS_PURCHASED, COST_HR) VALUES (?, ?, ?)");
                psType.setInt(1, memId);
                psType.setInt(2, hrs);
                psType.setDouble(3, cost);
                psType.executeUpdate();
                psType.close();
            } else if (mChoice.equals("2")) {
                System.out.print("  Enter VALID_DAYS: ");
                int days = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter WEEKLY_FEE: ");
                double fee = Double.parseDouble(sc.nextLine().trim());
                if (days <= 0 || fee <= 0) throw new InvalidDataException("Days and fee must be > 0");
                PreparedStatement psType = con.prepareStatement("INSERT INTO WEEKLY (MEMBERSHIP_ID, VALID_DAYS, WEEKLY_FEE) VALUES (?, ?, ?)");
                psType.setInt(1, memId);
                psType.setInt(2, days);
                psType.setDouble(3, fee);
                psType.executeUpdate();
                psType.close();
            } else if (mChoice.equals("3")) {
                System.out.print("  Enter VALID_WEEKS: ");
                int wks = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter MONTHLY_FEE: ");
                double fee = Double.parseDouble(sc.nextLine().trim());
                if (wks <= 0 || fee <= 0) throw new InvalidDataException("Weeks and fee must be > 0");
                PreparedStatement psType = con.prepareStatement("INSERT INTO MONTHLY (MEMBERSHIP_ID, VALID_WEEKS, MONTHLY_FEE) VALUES (?, ?, ?)");
                psType.setInt(1, memId);
                psType.setInt(2, wks);
                psType.setDouble(3, fee);
                psType.executeUpdate();
                psType.close();
            }
            System.out.println("  [✓] Membership added successful.");

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

    private void viewAllMemberships() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM MEMBERSHIP");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-12s | %-12s | %-12s | %-8s%n", "MEM_ID", "TYPE", "START", "END", "CUST_ID");
            System.out.println("  " + "-".repeat(60));
            while (rs.next()) {
                System.out.printf("  %-6d | %-12s | %-12s | %-12s | %-8d%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("MEM_TYPE"), rs.getString("START_DATE"), rs.getString("END_DATE"), rs.getInt("CUST_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewMembershipsByCustId() {
        try {
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM MEMBERSHIP WHERE CUST_ID = ?");
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-12s | %-12s | %-12s%n", "MEM_ID", "TYPE", "START", "END");
            System.out.println("  " + "-".repeat(50));
            while (rs.next()) {
                System.out.printf("  %-6d | %-12s | %-12s | %-12s%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("MEM_TYPE"), rs.getString("START_DATE"), rs.getString("END_DATE"));
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

    private void viewHourly() {
        try {
            String sql = "SELECT M.MEMBERSHIP_ID, M.START_DATE, M.END_DATE, H.HRS_PURCHASED, H.COST_HR, M.CUST_ID " + 
                         "FROM MEMBERSHIP M JOIN HOURLY H ON M.MEMBERSHIP_ID = H.MEMBERSHIP_ID";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-12s | %-12s | %-5s | %-8s | %-8s%n", "MEM_ID", "START", "END", "HRS", "COST_HR", "CUST_ID");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-6d | %-12s | %-12s | %-5d | Rs.%-5.2f | %-8d%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("START_DATE"), rs.getString("END_DATE"), 
                    rs.getInt("HRS_PURCHASED"), rs.getDouble("COST_HR"), rs.getInt("CUST_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewWeekly() {
        try {
            String sql = "SELECT M.MEMBERSHIP_ID, M.START_DATE, M.END_DATE, W.VALID_DAYS, W.WEEKLY_FEE, M.CUST_ID " + 
                         "FROM MEMBERSHIP M JOIN WEEKLY W ON M.MEMBERSHIP_ID = W.MEMBERSHIP_ID";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-12s | %-12s | %-5s | %-8s | %-8s%n", "MEM_ID", "START", "END", "DAYS", "WK_FEE", "CUST_ID");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-6d | %-12s | %-12s | %-5d | Rs.%-5.2f | %-8d%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("START_DATE"), rs.getString("END_DATE"), 
                    rs.getInt("VALID_DAYS"), rs.getDouble("WEEKLY_FEE"), rs.getInt("CUST_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewMonthly() {
        try {
            String sql = "SELECT M.MEMBERSHIP_ID, M.START_DATE, M.END_DATE, Y.VALID_WEEKS, Y.MONTHLY_FEE, M.CUST_ID " + 
                         "FROM MEMBERSHIP M JOIN MONTHLY Y ON M.MEMBERSHIP_ID = Y.MEMBERSHIP_ID";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-12s | %-12s | %-5s | %-8s | %-8s%n", "MEM_ID", "START", "END", "WKS", "MO_FEE", "CUST_ID");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-6d | %-12s | %-12s | %-5d | Rs.%-5.2f | %-8d%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("START_DATE"), rs.getString("END_DATE"), 
                    rs.getInt("VALID_WEEKS"), rs.getDouble("MONTHLY_FEE"), rs.getInt("CUST_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
    
    private void deleteMembership() {
        try {
            System.out.print("  Enter MEMBERSHIP_ID: ");
            int memId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Confirm delete? (yes/no): ");
            if (!sc.nextLine().trim().toLowerCase().equals("yes")) return;

            // Delete from all sub-tables first, then main table (Cascade is preferred but we'll do it manually)
            PreparedStatement ps1 = con.prepareStatement("DELETE FROM HOURLY WHERE MEMBERSHIP_ID = ?");
            ps1.setInt(1, memId); ps1.executeUpdate(); ps1.close();
            
            PreparedStatement ps2 = con.prepareStatement("DELETE FROM WEEKLY WHERE MEMBERSHIP_ID = ?");
            ps2.setInt(1, memId); ps2.executeUpdate(); ps2.close();
            
            PreparedStatement ps3 = con.prepareStatement("DELETE FROM MONTHLY WHERE MEMBERSHIP_ID = ?");
            ps3.setInt(1, memId); ps3.executeUpdate(); ps3.close();

            PreparedStatement ps4 = con.prepareStatement("DELETE FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            ps4.setInt(1, memId); ps4.executeUpdate(); ps4.close();
            System.out.println("  [✓] Membership deleted successful.");
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}