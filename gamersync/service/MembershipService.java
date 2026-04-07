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
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ╔══════════════════════════════════════════╗");
            System.out.println("  ║         MEMBERSHIP MODULE                ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. View All Memberships                 ║");
            System.out.println("  ║  2. View Membership by Customer ID       ║");
            System.out.println("  ║  3. View Membership Details (with type)  ║");
            System.out.println("  ║  4. Update Membership Type               ║");
            System.out.println("  ║  5. Update Membership Dates              ║");
            System.out.println("  ║  6. Delete Membership                    ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": viewAllMemberships(); break;
                case "2": viewMembershipByCustId(); break;
                case "3": viewMembershipDetails(); break;
                case "4": updateMembershipType(); break;
                case "5": updateMembershipDates(); break;
                case "6": deleteMembership(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void viewAllMemberships() {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT M.MEMBERSHIP_ID, M.MEM_TYPE, M.START_DATE, M.END_DATE, C.NAME " +
                "FROM MEMBERSHIP M JOIN CUSTOMER C ON M.CUST_ID = C.CUST_ID ORDER BY M.MEMBERSHIP_ID"
            );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n  " + "-".repeat(80));
            System.out.printf("  | %-5s | %-10s | %-12s | %-12s | %-25s |%n", "MEMID", "TYPE", "START", "END", "NAME");
            System.out.println("  " + "-".repeat(80));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  | %-5d | %-10s | %-12s | %-12s | %-25s |%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("MEM_TYPE"),
                    rs.getString("START_DATE"), rs.getString("END_DATE"), rs.getString("NAME")
                );
            }
            if (!found) System.out.println("  No memberships found.");
            System.out.println("  " + "-".repeat(80));
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewMembershipByCustId() {
        try {
            System.out.print("  Enter Customer ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = con.prepareStatement(
                "SELECT M.MEMBERSHIP_ID, M.MEM_TYPE, M.START_DATE, M.END_DATE FROM MEMBERSHIP M WHERE M.CUST_ID = ?"
            );
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n  " + "-".repeat(55));
            System.out.printf("  | %-5s | %-10s | %-12s | %-12s |%n", "MEMID", "TYPE", "START", "END");
            System.out.println("  " + "-".repeat(55));
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("  | %-5d | %-10s | %-12s | %-12s |%n",
                    rs.getInt("MEMBERSHIP_ID"), rs.getString("MEM_TYPE"),
                    rs.getString("START_DATE"), rs.getString("END_DATE")
                );
            }
            if (!found) System.out.println("  No memberships found.");
            System.out.println("  " + "-".repeat(55));
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewMembershipDetails() {
        try {
            System.out.print("  Enter Membership ID: ");
            int memId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = con.prepareStatement("SELECT MEM_TYPE FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            ps.setInt(1, memId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("  [!] Membership not found.");
                rs.close(); ps.close();
                return;
            }
            String memType = rs.getString("MEM_TYPE");
            rs.close(); ps.close();

            if (memType.equals("Hourly")) {
                PreparedStatement psh = con.prepareStatement(
                    "SELECT H.HRS_PURCHASED, H.COST_HR, M.START_DATE, M.END_DATE " +
                    "FROM HOURLY H JOIN MEMBERSHIP M ON H.MEMBERSHIP_ID = M.MEMBERSHIP_ID WHERE H.MEMBERSHIP_ID = ?"
                );
                psh.setInt(1, memId);
                ResultSet rsh = psh.executeQuery();
                if (rsh.next()) {
                    System.out.println("  [Hourly Membership Details]");
                    System.out.println("  Hours Purchased: " + rsh.getInt("HRS_PURCHASED"));
                    System.out.println("  Cost per Hour  : Rs. " + rsh.getDouble("COST_HR"));
                    System.out.println("  Start Date     : " + rsh.getString("START_DATE"));
                    System.out.println("  End Date       : " + rsh.getString("END_DATE"));
                }
                rsh.close(); psh.close();
            } else if (memType.equals("Weekly")) {
                PreparedStatement psw = con.prepareStatement(
                    "SELECT W.VALID_DAYS, W.WEEKLY_FEE, M.START_DATE, M.END_DATE " +
                    "FROM WEEKLY W JOIN MEMBERSHIP M ON W.MEMBERSHIP_ID = M.MEMBERSHIP_ID WHERE W.MEMBERSHIP_ID = ?"
                );
                psw.setInt(1, memId);
                ResultSet rsw = psw.executeQuery();
                if (rsw.next()) {
                    System.out.println("  [Weekly Membership Details]");
                    System.out.println("  Valid Days : " + rsw.getInt("VALID_DAYS"));
                    System.out.println("  Weekly Fee : Rs. " + rsw.getDouble("WEEKLY_FEE"));
                    System.out.println("  Start Date : " + rsw.getString("START_DATE"));
                    System.out.println("  End Date   : " + rsw.getString("END_DATE"));
                }
                rsw.close(); psw.close();
            } else if (memType.equals("Monthly")) {
                PreparedStatement psm = con.prepareStatement(
                    "SELECT MO.VALID_WEEKS, MO.MONTHLY_FEE, M.START_DATE, M.END_DATE " +
                    "FROM MONTHLY MO JOIN MEMBERSHIP M ON MO.MEMBERSHIP_ID = M.MEMBERSHIP_ID WHERE MO.MEMBERSHIP_ID = ?"
                );
                psm.setInt(1, memId);
                ResultSet rsm = psm.executeQuery();
                if (rsm.next()) {
                    System.out.println("  [Monthly Membership Details]");
                    System.out.println("  Valid Weeks : " + rsm.getInt("VALID_WEEKS"));
                    System.out.println("  Monthly Fee : Rs. " + rsm.getDouble("MONTHLY_FEE"));
                    System.out.println("  Start Date  : " + rsm.getString("START_DATE"));
                    System.out.println("  End Date    : " + rsm.getString("END_DATE"));
                }
                rsm.close(); psm.close();
            }

        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void updateMembershipType() {
        try {
            System.out.print("  Enter Membership ID to update: ");
            int memId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = con.prepareStatement("SELECT MEM_TYPE, START_DATE, END_DATE, CUST_ID FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            ps.setInt(1, memId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("  [!] Membership not found.");
                rs.close(); ps.close();
                return;
            }
            String oldType = rs.getString("MEM_TYPE");
            rs.close(); ps.close();

            System.out.println("  Current type: " + oldType);
            System.out.print("  New type (Hourly / Weekly / Monthly): ");
            String newType = sc.nextLine().trim();

            if (!newType.equals("Hourly") && !newType.equals("Weekly") && !newType.equals("Monthly")) {
                throw new InvalidDataException("Type must be Hourly, Weekly, or Monthly");
            }
            if (oldType.equals(newType)) {
                System.out.println("  [!] Already on this membership type.");
                return;
            }

            // Step A
            if (oldType.equals("Hourly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM HOURLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            } else if (oldType.equals("Weekly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM WEEKLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            } else if (oldType.equals("Monthly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM MONTHLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            }

            // Step B
            System.out.print("  New Start Date (YYYY-MM-DD): ");
            String newStart = sc.nextLine().trim();
            System.out.print("  New End Date   (YYYY-MM-DD): ");
            String newEnd = sc.nextLine().trim();

            PreparedStatement ups = con.prepareStatement("UPDATE MEMBERSHIP SET MEM_TYPE=?, START_DATE=?, END_DATE=? WHERE MEMBERSHIP_ID=?");
            ups.setString(1, newType);
            ups.setString(2, newStart);
            ups.setString(3, newEnd);
            ups.setInt(4, memId);
            ups.executeUpdate();
            ups.close();

            // Step C
            if (newType.equals("Hourly")) {
                System.out.print("  Enter HRS_PURCHASED: ");
                int hrs = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter COST_HR: ");
                double costHr = Double.parseDouble(sc.nextLine().trim());
                PreparedStatement ips = con.prepareStatement("INSERT INTO HOURLY (MEMBERSHIP_ID, HRS_PURCHASED, COST_HR) VALUES (?,?,?)");
                ips.setInt(1, memId); ips.setInt(2, hrs); ips.setDouble(3, costHr);
                ips.executeUpdate(); ips.close();
            } else if (newType.equals("Weekly")) {
                System.out.print("  Enter VALID_DAYS: ");
                int vDays = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter WEEKLY_FEE: ");
                double wFee = Double.parseDouble(sc.nextLine().trim());
                PreparedStatement ips = con.prepareStatement("INSERT INTO WEEKLY (MEMBERSHIP_ID, VALID_DAYS, WEEKLY_FEE) VALUES (?,?,?)");
                ips.setInt(1, memId); ips.setInt(2, vDays); ips.setDouble(3, wFee);
                ips.executeUpdate(); ips.close();
            } else if (newType.equals("Monthly")) {
                System.out.print("  Enter VALID_WEEKS: ");
                int vWks = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter MONTHLY_FEE: ");
                double mFee = Double.parseDouble(sc.nextLine().trim());
                PreparedStatement ips = con.prepareStatement("INSERT INTO MONTHLY (MEMBERSHIP_ID, VALID_WEEKS, MONTHLY_FEE) VALUES (?,?,?)");
                ips.setInt(1, memId); ips.setInt(2, vWks); ips.setDouble(3, mFee);
                ips.executeUpdate(); ips.close();
            }

            System.out.println("  [✓] Membership updated from " + oldType + " to " + newType + " successfully.");

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

    private void updateMembershipDates() {
        try {
            System.out.print("  Enter Membership ID to update: ");
            int memId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = con.prepareStatement("SELECT START_DATE, END_DATE FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            ps.setInt(1, memId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("  [!] Membership not found.");
                rs.close(); ps.close();
                return;
            }
            System.out.println("  Current Start Date: " + rs.getString("START_DATE"));
            System.out.println("  Current End Date  : " + rs.getString("END_DATE"));
            rs.close(); ps.close();

            System.out.print("  New Start Date (YYYY-MM-DD): ");
            String newStart = sc.nextLine().trim();
            System.out.print("  New End Date   (YYYY-MM-DD): ");
            String newEnd = sc.nextLine().trim();

            if (newStart.isEmpty() || newEnd.isEmpty()) {
                throw new InvalidDataException("Start date and End date cannot be empty.");
            }

            PreparedStatement ups = con.prepareStatement("UPDATE MEMBERSHIP SET START_DATE=?, END_DATE=? WHERE MEMBERSHIP_ID=?");
            ups.setString(1, newStart);
            ups.setString(2, newEnd);
            ups.setInt(3, memId);
            ups.executeUpdate();
            ups.close();

            System.out.println("  [✓] Membership dates updated.");

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

    private void deleteMembership() {
        try {
            System.out.print("  Enter Membership ID: ");
            int memId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement ps = con.prepareStatement("SELECT MEM_TYPE FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            ps.setInt(1, memId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println("  [!] Membership not found.");
                rs.close(); ps.close();
                return;
            }
            String memType = rs.getString("MEM_TYPE");
            rs.close(); ps.close();

            System.out.print("  Confirm delete membership ID " + memId + "? (yes/no): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("yes")) {
                System.out.println("  Cancelled.");
                return;
            }

            if (memType.equals("Hourly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM HOURLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            } else if (memType.equals("Weekly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM WEEKLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            } else if (memType.equals("Monthly")) {
                PreparedStatement dps = con.prepareStatement("DELETE FROM MONTHLY WHERE MEMBERSHIP_ID = ?");
                dps.setInt(1, memId); dps.executeUpdate(); dps.close();
            }

            PreparedStatement dps = con.prepareStatement("DELETE FROM MEMBERSHIP WHERE MEMBERSHIP_ID = ?");
            dps.setInt(1, memId);
            dps.executeUpdate();
            dps.close();

            System.out.println("  [✓] Membership deleted.");

        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}