package gamersync.service;

import gamersync.db.DBConnection;
import java.sql.*;

// CO4: DRL queries (SELECT with JOIN, GROUP BY, WINDOW FUNCTIONS)
// These are the specific queries from the GamerSync SQL file
public class QueryService {

    private Connection con;

    public QueryService() {
        try { this.con = DBConnection.getConnection(); }
        catch (SQLException e) { System.out.println("[ERROR] " + e.getMessage()); }
    }

    // ── Q1: Customer with Game and PC Used (JOIN) ────────────────────────────
    public void customerGamePC() {
        String sql = "SELECT C.NAME, G.GAME_NAME, P.PC_ID " +
                     "FROM CUSTOMER C " +
                     "JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID " +
                     "JOIN PC P ON G.PC_ID = P.PC_ID";
        System.out.println("\n  Customer | Game Played | PC Used");
        System.out.println("  " + "-".repeat(45));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-15s | %-15s | PC-%d%n",
                    rs.getString("NAME"), rs.getString("GAME_NAME"), rs.getInt("PC_ID"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q2: Total Spending per Customer (GROUP BY + SUM) ─────────────────────
    public void totalSpendingPerCustomer() {
        String sql = "SELECT C.NAME, SUM(P.AMOUNT) AS TOTAL_SPENDING " +
                     "FROM CUSTOMER C JOIN PAYMENT P ON C.CUST_ID = P.CUST_ID " +
                     "GROUP BY C.NAME";
        System.out.println("\n  Customer         | Total Spending");
        System.out.println("  " + "-".repeat(35));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-17s| Rs. %.2f%n",
                    rs.getString("NAME"), rs.getDouble("TOTAL_SPENDING"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q3: Customers Playing More Than 100 Minutes ──────────────────────────
    public void longSessionCustomers() {
        String sql = "SELECT C.NAME, G.DURATION " +
                     "FROM CUSTOMER C JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID " +
                     "WHERE G.DURATION > 100";
        System.out.println("\n  Customer         | Duration (mins)");
        System.out.println("  " + "-".repeat(35));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            while (rs.next()) {
                System.out.printf("  %-17s| %d mins%n",
                    rs.getString("NAME"), rs.getInt("DURATION"));
                found = true;
            }
            if (!found) System.out.println("  No customers with sessions > 100 minutes.");
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q4: Membership with Games Played ─────────────────────────────────────
    public void membershipWithGames() {
        String sql = "SELECT C.NAME, M.MEM_TYPE, G.GAME_NAME " +
                     "FROM CUSTOMER C " +
                     "JOIN MEMBERSHIP M ON C.CUST_ID = M.CUST_ID " +
                     "JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID";
        System.out.println("\n  Customer         | Membership | Game");
        System.out.println("  " + "-".repeat(50));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-17s| %-10s | %s%n",
                    rs.getString("NAME"), rs.getString("MEM_TYPE"), rs.getString("GAME_NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q5: Ranking Customers by Total Spending (WINDOW FUNCTION) ────────────
    public void rankCustomersBySpending() {
        String sql = "SELECT C.NAME, SUM(P.AMOUNT) AS TOTAL, " +
                     "RANK() OVER (ORDER BY SUM(P.AMOUNT) DESC) AS RANKING " +
                     "FROM CUSTOMER C JOIN PAYMENT P ON C.CUST_ID = P.CUST_ID " +
                     "GROUP BY C.NAME";
        System.out.println("\n  Rank | Customer         | Total Spent");
        System.out.println("  " + "-".repeat(40));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  #%-4d| %-17s| Rs. %.2f%n",
                    rs.getInt("RANKING"), rs.getString("NAME"), rs.getDouble("TOTAL"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q6: All Tournaments with Entry Fees ──────────────────────────────────
    public void viewTournaments() {
        String sql = "SELECT T.TOURNAMENT_ID, T.GAME_NAME, T.TOURNAMENT_DATE, T.ENTRY_FEE, C.NAME " +
                     "FROM TOURNAMENTS T JOIN CUSTOMER C ON T.CUST_ID = C.CUST_ID";
        System.out.println("\n  TID | Game         | Date       | Fee    | Registered By");
        System.out.println("  " + "-".repeat(60));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-4d| %-13s| %-10s | Rs.%-5.0f| %s%n",
                    rs.getInt("TOURNAMENT_ID"), rs.getString("GAME_NAME"),
                    rs.getString("TOURNAMENT_DATE"), rs.getDouble("ENTRY_FEE"),
                    rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q7: Payment Summary per Mode ─────────────────────────────────────────
    public void paymentSummaryByMode() {
        String sql = "SELECT PAYMENT_MODE, COUNT(*) AS COUNT, SUM(AMOUNT) AS TOTAL " +
                     "FROM PAYMENT GROUP BY PAYMENT_MODE";
        System.out.println("\n  Mode   | Count | Total Collected");
        System.out.println("  " + "-".repeat(35));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-7s| %-6d| Rs. %.2f%n",
                    rs.getString("PAYMENT_MODE"), rs.getInt("COUNT"), rs.getDouble("TOTAL"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── Q8: Top Gaming Accounts by Level ─────────────────────────────────────
    public void topGamingAccounts() {
        String sql = "SELECT GA.GAME_USER_ID, GA.GAME_NAME, GA.RANKK, GA.LEVEL, C.NAME " +
                     "FROM GAMING_ACC GA JOIN CUSTOMER C ON GA.CUST_ID = C.CUST_ID " +
                     "ORDER BY GA.LEVEL DESC";
        System.out.println("\n  Username    | Game       | Rank     | Lvl | Player");
        System.out.println("  " + "-".repeat(58));
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                System.out.printf("  %-12s| %-11s| %-9s| %-4d| %s%n",
                    rs.getString("GAME_USER_ID"), rs.getString("GAME_NAME"),
                    rs.getString("RANKK"), rs.getInt("LEVEL"), rs.getString("NAME"));
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }
}
