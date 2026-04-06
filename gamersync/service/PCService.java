package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import java.sql.*;
import java.util.Scanner;

public class PCService {
    private final Scanner sc;
    private Connection con;

    public PCService(Scanner sc) {
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
            System.out.println("  ║           PC MANAGEMENT MODULE           ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. View All PCs                         ║");
            System.out.println("  ║  2. View Available PCs                   ║");
            System.out.println("  ║  3. View PCs In Use                      ║");
            System.out.println("  ║  4. Update PC Status                     ║");
            System.out.println("  ║  5. View PC by ID                        ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": viewAllPcs(); break;
                case "2": viewAvailablePcs(); break;
                case "3": viewPcsInUse(); break;
                case "4": updatePcStatus(); break;
                case "5": viewPcById(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void viewAllPcs() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM PC");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-18s | %-12s | %-12s%n", "PC_ID", "CONFIGURATION", "STATUS", "HOURLY_RATE");
            System.out.println("  " + "-".repeat(55));
            while (rs.next()) {
                System.out.printf("  %-6d | %-18s | %-12s | Rs.%-9.2f%n",
                    rs.getInt("PC_ID"), rs.getString("CONFIGURATION"), rs.getString("STATUS"), rs.getDouble("HOURLY_RATE"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewAvailablePcs() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM PC WHERE STATUS = 'Available'");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-18s | %-12s | %-12s%n", "PC_ID", "CONFIGURATION", "STATUS", "HOURLY_RATE");
            System.out.println("  " + "-".repeat(55));
            while (rs.next()) {
                System.out.printf("  %-6d | %-18s | %-12s | Rs.%-9.2f%n",
                    rs.getInt("PC_ID"), rs.getString("CONFIGURATION"), rs.getString("STATUS"), rs.getDouble("HOURLY_RATE"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewPcsInUse() {
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM PC WHERE STATUS = 'In Use'");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-18s | %-12s | %-12s%n", "PC_ID", "CONFIGURATION", "STATUS", "HOURLY_RATE");
            System.out.println("  " + "-".repeat(55));
            while (rs.next()) {
                System.out.printf("  %-6d | %-18s | %-12s | Rs.%-9.2f%n",
                    rs.getInt("PC_ID"), rs.getString("CONFIGURATION"), rs.getString("STATUS"), rs.getDouble("HOURLY_RATE"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void updatePcStatus() {
        try {
            System.out.print("  Enter PC_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement check = con.prepareStatement("SELECT STATUS FROM PC WHERE PC_ID = ?");
            check.setInt(1, id);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("  Current status: " + rs.getString("STATUS"));
            } else {
                System.out.println("  [!] PC not found.");
                rs.close(); check.close();
                return;
            }
            rs.close(); check.close();

            System.out.print("  New status (Available / In Use / Maintenance): ");
            String status = sc.nextLine().trim();

            if (!status.equals("Available") && !status.equals("In Use") && !status.equals("Maintenance")) {
                throw new InvalidDataException("Invalid status. Use: Available, In Use, or Maintenance");
            }

            PreparedStatement ps = con.prepareStatement("UPDATE PC SET STATUS = ? WHERE PC_ID = ?");
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] PC status updated.");
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

    private void viewPcById() {
        try {
            System.out.print("  Enter PC_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM PC WHERE PC_ID = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println();
                System.out.printf("  %-6s | %-18s | %-12s | %-12s%n", "PC_ID", "CONFIGURATION", "STATUS", "HOURLY_RATE");
                System.out.println("  " + "-".repeat(55));
                System.out.printf("  %-6d | %-18s | %-12s | Rs.%-9.2f%n",
                    rs.getInt("PC_ID"), rs.getString("CONFIGURATION"), rs.getString("STATUS"), rs.getDouble("HOURLY_RATE"));
            } else {
                System.out.println("  [!] PC not found.");
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
}