package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import gamersync.db.ValidationHelper;
import java.sql.*;
import java.util.Scanner;

public class TournamentService {
    private final Scanner sc;
    private Connection con;

    public TournamentService(Scanner sc) {
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
            System.out.println("  ║          TOURNAMENT MODULE               ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Register Customer for Tournament     ║");
            System.out.println("  ║  2. View All Tournaments                 ║");
            System.out.println("  ║  3. View Tournaments by Game Name        ║");
            System.out.println("  ║  4. View Tournaments by Customer ID      ║");
            System.out.println("  ║  5. Delete Tournament Entry              ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": registerCustomer(); break;
                case "2": viewAllTournaments(); break;
                case "3": viewTournamentsByGame(); break;
                case "4": viewTournamentsByCustId(); break;
                case "5": deleteTournamentEntry(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void registerCustomer() {
        try {
            System.out.print("  Enter TOURNAMENT_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter GAME_NAME: ");
            String game = sc.nextLine().trim();
            System.out.print("  Enter TOURNAMENT_DATE (YYYY-MM-DD): ");
            String date = sc.nextLine().trim();
            System.out.print("  Enter ENTRY_FEE: ");
            double fee = Double.parseDouble(sc.nextLine().trim());
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());

            if (game.isEmpty()) throw new InvalidDataException("Game name cannot be empty");
            if (fee <= 0) throw new InvalidDataException("Entry fee must be greater than 0");

            ValidationHelper.validatePositiveInt(id, "Tournament ID");
            ValidationHelper.validateNotEmpty(game, "Game Name");
            ValidationHelper.validateDate(date, "Tournament Date");
            ValidationHelper.validatePositiveAmount(fee, "Entry Fee");
            ValidationHelper.validatePositiveInt(custId, "Customer ID");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO TOURNAMENTS (TOURNAMENT_ID, GAME_NAME, TOURNAMENT_DATE, ENTRY_FEE, CUST_ID) VALUES (?,?,?,?,?)");
            ps.setInt(1, id);
            ps.setString(2, game);
            ps.setString(3, date);
            ps.setDouble(4, fee);
            ps.setInt(5, custId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Registration successful.");
        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else {
                    System.out.println("  [SQL ERROR] A parent ID does not exist.");
                }
            } else {
                System.out.println("  [SQL ERROR] " + msg);
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewAllTournaments() {
        try {
            String sql = "SELECT T.TOURNAMENT_ID, T.GAME_NAME, T.TOURNAMENT_DATE, T.ENTRY_FEE, C.NAME " +
                         "FROM TOURNAMENTS T JOIN CUSTOMER C ON T.CUST_ID = C.CUST_ID ORDER BY T.TOURNAMENT_DATE ASC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-15s | %-12s | %-10s | %-15s%n", "T_ID", "GAME", "DATE", "FEE", "CUST_NAME");
            System.out.println("  " + "-".repeat(68));
            while (rs.next()) {
                System.out.printf("  %-6d | %-15s | %-12s | Rs.%-7.2f | %-15s%n",
                    rs.getInt("TOURNAMENT_ID"), rs.getString("GAME_NAME"), rs.getString("TOURNAMENT_DATE"),
                    rs.getDouble("ENTRY_FEE"), rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewTournamentsByGame() {
        try {
            System.out.print("  Enter game name (partial ok): ");
            String game = sc.nextLine().trim();
            String sql = "SELECT T.TOURNAMENT_ID, T.GAME_NAME, T.TOURNAMENT_DATE, T.ENTRY_FEE, C.NAME " +
                         "FROM TOURNAMENTS T JOIN CUSTOMER C ON T.CUST_ID = C.CUST_ID WHERE T.GAME_NAME LIKE ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, "%" + game + "%");
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-15s | %-12s | %-10s | %-15s%n", "T_ID", "GAME", "DATE", "FEE", "CUST_NAME");
            System.out.println("  " + "-".repeat(68));
            while (rs.next()) {
                System.out.printf("  %-6d | %-15s | %-12s | Rs.%-7.2f | %-15s%n",
                    rs.getInt("TOURNAMENT_ID"), rs.getString("GAME_NAME"), rs.getString("TOURNAMENT_DATE"),
                    rs.getDouble("ENTRY_FEE"), rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewTournamentsByCustId() {
        try {
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT T.TOURNAMENT_ID, T.GAME_NAME, T.TOURNAMENT_DATE, T.ENTRY_FEE FROM TOURNAMENTS T WHERE CUST_ID = ?");
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-15s | %-12s | %-10s%n", "T_ID", "GAME", "DATE", "FEE");
            System.out.println("  " + "-".repeat(50));
            while (rs.next()) {
                System.out.printf("  %-6d | %-15s | %-12s | Rs.%-7.2f%n",
                    rs.getInt("TOURNAMENT_ID"), rs.getString("GAME_NAME"), rs.getString("TOURNAMENT_DATE"), rs.getDouble("ENTRY_FEE"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else {
                    System.out.println("  [SQL ERROR] A parent ID does not exist.");
                }
            } else {
                System.out.println("  [SQL ERROR] " + msg);
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void deleteTournamentEntry() {
        try {
            System.out.print("  Enter TOURNAMENT_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Confirm delete? (yes/no): ");
            if (!sc.nextLine().trim().toLowerCase().equals("yes")) return;

            PreparedStatement ps = con.prepareStatement("DELETE FROM TOURNAMENTS WHERE TOURNAMENT_ID = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Delete successful.");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else {
                    System.out.println("  [SQL ERROR] A parent ID does not exist.");
                }
            } else {
                System.out.println("  [SQL ERROR] " + msg);
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}
