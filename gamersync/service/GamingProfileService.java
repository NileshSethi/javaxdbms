package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import gamersync.db.ValidationHelper;
import java.sql.*;
import java.util.Scanner;

public class GamingProfileService {
    private final Scanner sc;
    private Connection con;

    public GamingProfileService(Scanner sc) {
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
            System.out.println("\n  ╔══════════════════════════════════════╗");
            System.out.println("  ║        GAMING PROFILE MODULE         ║");
            System.out.println("  ╠══════════════════════════════════════╣");
            System.out.println("  ║  1. Add Gaming Account               ║");
            System.out.println("  ║  2. View All Gaming Accounts         ║");
            System.out.println("  ║  3. View Accounts by Customer ID     ║");
            System.out.println("  ║  4. Add Achievement                  ║");
            System.out.println("  ║  5. View Achievements by Account ID  ║");
            System.out.println("  ║  6. Delete Gaming Account            ║");
            System.out.println("  ║  0. Back                             ║");
            System.out.println("  ╚══════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addGamingAccount(); break;
                case "2": viewAllGamingAccounts(); break;
                case "3": viewAccountsByCustId(); break;
                case "4": addAchievement(); break;
                case "5": viewAchievementsByAccId(); break;
                case "6": deleteGamingAccount(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void addGamingAccount() {
        try {
            System.out.print("  Enter GAMING_ACC_ID: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter GAME_NAME: ");
            String game = sc.nextLine().trim();
            System.out.print("  Enter GAME_USER_ID: ");
            String user = sc.nextLine().trim();
            System.out.print("  Enter RANKK: ");
            String rankk = sc.nextLine().trim();
            System.out.print("  Enter LEVEL: ");
            int level = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());

            if (level <= 0) throw new InvalidDataException("Level must be greater than 0");
            if (user.isEmpty()) throw new InvalidDataException("Username cannot be empty");

            ValidationHelper.validatePositiveInt(id, "Account ID");
            ValidationHelper.validateNotEmpty(game, "Game Name");
            ValidationHelper.validateNotEmpty(user, "Game Username");
            ValidationHelper.validateRank(rankk);
            ValidationHelper.validatePositiveInt(level, "Level");
            ValidationHelper.validatePositiveInt(custId, "Customer ID");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO GAMING_ACC (GAMING_ACC_ID, GAME_NAME, GAME_USER_ID, RANKK, LEVEL, CUST_ID) VALUES (?,?,?,?,?,?)");
            ps.setInt(1, id);
            ps.setString(2, game);
            ps.setString(3, user);
            ps.setString(4, rankk);
            ps.setInt(5, level);
            ps.setInt(6, custId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Gaming account registered successful.");
        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("gaming_acc")) {
                    System.out.println("  [SQL ERROR] The Gaming Account ID you entered does not exist!");
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

    private void viewAllGamingAccounts() {
        try {
            String sql = "SELECT GA.GAMING_ACC_ID, GA.GAME_NAME, GA.GAME_USER_ID, GA.RANKK, GA.LEVEL, C.NAME " +
                         "FROM GAMING_ACC GA JOIN CUSTOMER C ON GA.CUST_ID = C.CUST_ID ORDER BY GA.LEVEL DESC";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-15s | %-15s | %-10s | %-5s | %-15s%n", "ACC_ID", "GAME", "USER_ID", "RANK", "LEVEL", "CUST_NAME");
            System.out.println("  " + "-".repeat(80));
            while (rs.next()) {
                System.out.printf("  %-6d | %-15s | %-15s | %-10s | %-5d | %-15s%n",
                    rs.getInt("GAMING_ACC_ID"), rs.getString("GAME_NAME"), rs.getString("GAME_USER_ID"),
                    rs.getString("RANKK"), rs.getInt("LEVEL"), rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void viewAccountsByCustId() {
        try {
            System.out.print("  Enter CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());
            PreparedStatement ps = con.prepareStatement("SELECT * FROM GAMING_ACC WHERE CUST_ID = ?");
            ps.setInt(1, custId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-15s | %-15s | %-10s | %-5s%n", "ACC_ID", "GAME", "USER_ID", "RANK", "LEVEL");
            System.out.println("  " + "-".repeat(65));
            while (rs.next()) {
                System.out.printf("  %-6d | %-15s | %-15s | %-10s | %-5d%n",
                    rs.getInt("GAMING_ACC_ID"), rs.getString("GAME_NAME"), rs.getString("GAME_USER_ID"),
                    rs.getString("RANKK"), rs.getInt("LEVEL"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("gaming_acc")) {
                    System.out.println("  [SQL ERROR] The Gaming Account ID you entered does not exist!");
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

    private void addAchievement() {
        try {
            System.out.print("  Enter ACHIEVEMENT_ID: ");
            int achId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter ACHIEVE_NAME: ");
            String achName = sc.nextLine().trim();
            System.out.print("  Enter DATE_UNLOCKED (YYYY-MM-DD): ");
            String dateUnlocked = sc.nextLine().trim();
            System.out.print("  Enter GAMING_ACC_ID: ");
            int accId = Integer.parseInt(sc.nextLine().trim());

            if (achName.isEmpty()) throw new InvalidDataException("Achievement name cannot be empty.");

            ValidationHelper.validatePositiveInt(achId, "Achievement ID");
            ValidationHelper.validateNotEmpty(achName, "Achievement Name");
            ValidationHelper.validateDate(dateUnlocked, "Date Unlocked");
            ValidationHelper.validatePositiveInt(accId, "Gaming Account ID");

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ACHIEVEMENTS (ACHIEVEMENT_ID, ACHIEVE_NAME, DATE_UNLOCKED, GAMING_ACC_ID) VALUES (?,?,?,?)");
            ps.setInt(1, achId);
            ps.setString(2, achName);
            ps.setString(3, dateUnlocked);
            ps.setInt(4, accId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Achievement added successful.");
        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("gaming_acc")) {
                    System.out.println("  [SQL ERROR] The Gaming Account ID you entered does not exist!");
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

    private void viewAchievementsByAccId() {
        try {
            System.out.print("  Enter GAMING_ACC_ID: ");
            int accId = Integer.parseInt(sc.nextLine().trim());
            String sql = "SELECT A.ACHIEVEMENT_ID, A.ACHIEVE_NAME, A.DATE_UNLOCKED, GA.GAME_USER_ID " +
                         "FROM ACHIEVEMENTS A JOIN GAMING_ACC GA ON A.GAMING_ACC_ID = GA.GAMING_ACC_ID " +
                         "WHERE A.GAMING_ACC_ID = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, accId);
            ResultSet rs = ps.executeQuery();
            System.out.println();
            System.out.printf("  %-6s | %-25s | %-12s | %-15s%n", "ACH_ID", "ACHIEVEMENT", "DATE", "USER_ID");
            System.out.println("  " + "-".repeat(68));
            while (rs.next()) {
                System.out.printf("  %-6d | %-25s | %-12s | %-15s%n",
                    rs.getInt("ACHIEVEMENT_ID"), rs.getString("ACHIEVE_NAME"), rs.getString("DATE_UNLOCKED"),
                    rs.getString("GAME_USER_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("gaming_acc")) {
                    System.out.println("  [SQL ERROR] The Gaming Account ID you entered does not exist!");
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

    private void deleteGamingAccount() {
        try {
            System.out.print("  Enter GAMING_ACC_ID: ");
            int accId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Confirm delete? (yes/no): ");
            if (!sc.nextLine().trim().toLowerCase().equals("yes")) {
                return;
            }

            PreparedStatement ps1 = con.prepareStatement("DELETE FROM ACHIEVEMENTS WHERE GAMING_ACC_ID = ?");
            ps1.setInt(1, accId);
            ps1.executeUpdate();
            ps1.close();

            PreparedStatement ps2 = con.prepareStatement("DELETE FROM GAMING_ACC WHERE GAMING_ACC_ID = ?");
            ps2.setInt(1, accId);
            ps2.executeUpdate();
            ps2.close();
            
            System.out.println("  [✓] Gaming account and achievements deleted successful.");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("gaming_acc")) {
                    System.out.println("  [SQL ERROR] The Gaming Account ID you entered does not exist!");
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

