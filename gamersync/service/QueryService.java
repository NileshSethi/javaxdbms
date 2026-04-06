package gamersync.service;

import gamersync.db.DBConnection;
import java.sql.*;
import java.util.Scanner;

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

    public void runCustomSelect(Scanner sc) {
        System.out.print("  Enter any SELECT query:\n  > ");
        String query = sc.nextLine().trim();

        if (!query.toLowerCase().startsWith("select")) {
            System.out.println("  [BLOCKED] Only SELECT queries are allowed.");
            return;
        }

        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            System.out.println();
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("  %-18s", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("  " + "-".repeat(colCount * 19));

            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", rs.getString(i));
                }
                System.out.println();
                rowCount++;
            }
            if (rowCount == 0) System.out.println("  (No results found)");
            else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    public void runSelectWithFilter(Scanner sc) {
        System.out.println("  Available tables: CUSTOMER, PC, GAMING_SESSION, GAMING_ACC,");
        System.out.println("  ACHIEVEMENTS, MEMBERSHIP, TOURNAMENTS, PAYMENT, FOOD_ORDER");
        System.out.print("  Enter table name: ");
        String table = sc.nextLine().trim();
        
        System.out.print("  Enter column to filter by (or press Enter to skip): ");
        String column = sc.nextLine().trim();

        String sql;
        String filterValue = null;

        if (column.isEmpty()) {
            sql = "SELECT * FROM " + table;
        } else {
            System.out.print("  Enter value to filter: ");
            filterValue = sc.nextLine().trim();
            sql = "SELECT * FROM " + table + " WHERE " + column + " = ?";
        }

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            if (filterValue != null) {
                ps.setString(1, filterValue);
            }

            ResultSet rs = ps.executeQuery();
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            System.out.println();
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("  %-18s", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("  " + "-".repeat(colCount * 19));

            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", rs.getString(i));
                }
                System.out.println();
                rowCount++;
            }
            if (rowCount == 0) System.out.println("  (No results found)");
            else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    public void runGuidedJoin(Scanner sc) {
        System.out.println("  Pick a JOIN query to run:");
        System.out.println("  1.  CUSTOMER + GAMING_SESSION (sessions per customer)");
        System.out.println("  2.  CUSTOMER + PAYMENT (payments per customer)");
        System.out.println("  3.  GAMING_SESSION + FOOD_ORDER (food ordered per session)");
        System.out.println("  4.  CUSTOMER + GAMING_ACC + ACHIEVEMENTS (achievements per player)");
        System.out.println("  5.  CUSTOMER + TOURNAMENTS (tournament registrations)");
        System.out.println("  6.  FOOD_ORDER total amount per session (GROUP BY)");
        System.out.println("  7.  Most expensive food orders (ORDER BY DESC)");
        System.out.println("  8.  Sessions that have food orders (INNER JOIN filter)");
        System.out.println("  9.  Food order summary per customer (3-table JOIN)");
        System.out.println("  10. All food orders with customer name and game played");
        System.out.print("  Choice: ");
        
        String choice = sc.nextLine().trim();
        String sql = null;

        switch (choice) {
            case "1":
                sql = "SELECT C.NAME, G.SESSION_ID, G.GAME_NAME, G.DURATION " +
                      "FROM CUSTOMER C JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID";
                break;
            case "2":
                sql = "SELECT C.NAME, P.PAYMENT_ID, P.PAYMENT_MODE, P.AMOUNT " +
                      "FROM CUSTOMER C JOIN PAYMENT P ON C.CUST_ID = P.CUST_ID";
                break;
            case "3":
                sql = "SELECT G.SESSION_ID, G.GAME_NAME, F.ORDER_ITEM, F.TOTAL_AMOUNT " +
                      "FROM GAMING_SESSION G JOIN FOOD_ORDER F ON G.SESSION_ID = F.SESSION_ID";
                break;
            case "4":
                sql = "SELECT C.NAME, GA.GAME_NAME, GA.RANKK, A.ACHIEVE_NAME, A.DATE_UNLOCKED " +
                      "FROM CUSTOMER C " +
                      "JOIN GAMING_ACC GA ON C.CUST_ID = GA.CUST_ID " +
                      "JOIN ACHIEVEMENTS A ON GA.GAMING_ACC_ID = A.GAMING_ACC_ID";
                break;
            case "5":
                sql = "SELECT C.NAME, T.GAME_NAME, T.TOURNAMENT_DATE, T.ENTRY_FEE " +
                      "FROM CUSTOMER C JOIN TOURNAMENTS T ON C.CUST_ID = T.CUST_ID";
                break;
            case "6":
                sql = "SELECT F.SESSION_ID, SUM(F.TOTAL_AMOUNT) AS TOTAL_FOOD_BILL " +
                      "FROM FOOD_ORDER F GROUP BY F.SESSION_ID ORDER BY TOTAL_FOOD_BILL DESC";
                break;
            case "7":
                sql = "SELECT F.ORDER_ID, F.ORDER_ITEM, F.TOTAL_AMOUNT, G.GAME_NAME " +
                      "FROM FOOD_ORDER F JOIN GAMING_SESSION G ON F.SESSION_ID = G.SESSION_ID " +
                      "ORDER BY F.TOTAL_AMOUNT DESC";
                break;
            case "8":
                sql = "SELECT G.SESSION_ID, G.GAME_NAME, G.DURATION, F.ORDER_ITEM " +
                      "FROM GAMING_SESSION G INNER JOIN FOOD_ORDER F ON G.SESSION_ID = F.SESSION_ID";
                break;
            case "9":
                sql = "SELECT C.NAME, G.GAME_NAME, F.ORDER_ITEM, F.TOTAL_AMOUNT " +
                      "FROM CUSTOMER C " +
                      "JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID " +
                      "JOIN FOOD_ORDER F ON G.SESSION_ID = F.SESSION_ID";
                break;
            case "10":
                sql = "SELECT C.NAME, G.GAME_NAME, G.START_TIME, F.ORDER_ITEM, F.TOTAL_AMOUNT " +
                      "FROM CUSTOMER C " +
                      "JOIN GAMING_SESSION G ON C.CUST_ID = G.CUST_ID " +
                      "JOIN FOOD_ORDER F ON G.SESSION_ID = F.SESSION_ID " +
                      "ORDER BY C.NAME";
                break;
            default:
                System.out.println("  [!] Invalid option.");
                return;
        }

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            System.out.println();
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("  %-18s", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("  " + "-".repeat(colCount * 19));

            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", rs.getString(i));
                }
                System.out.println();
                rowCount++;
            }
            if (rowCount == 0) System.out.println("  (No results found)");
            else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");

            rs.close();
            ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    public void callStoredProcedures(Scanner sc) {
        System.out.println("  1. get_customer_data (by Customer ID)");
        System.out.println("  2. get_sessions (all sessions)");
        System.out.print("  Choice: ");
        String choice = sc.nextLine().trim();

        try {
            if (choice.equals("1")) {
                System.out.print("  Enter Customer ID: ");
                int cid = Integer.parseInt(sc.nextLine().trim());
                CallableStatement cs = con.prepareCall("{CALL get_customer_data(?)}");
                cs.setInt(1, cid);
                ResultSet rs = cs.executeQuery();
                
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                System.out.println();
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", meta.getColumnName(i));
                }
                System.out.println();
                System.out.println("  " + "-".repeat(colCount * 19));
                
                int rowCount = 0;
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        System.out.printf("  %-18s", rs.getString(i));
                    }
                    System.out.println();
                    rowCount++;
                }
                if (rowCount == 0) System.out.println("  (No results found)");
                else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");
                
                cs.close();
            } else if (choice.equals("2")) {
                CallableStatement cs = con.prepareCall("{CALL get_sessions()}");
                ResultSet rs = cs.executeQuery();
                
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();
                System.out.println();
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", meta.getColumnName(i));
                }
                System.out.println();
                System.out.println("  " + "-".repeat(colCount * 19));
                
                int rowCount = 0;
                while (rs.next()) {
                    for (int i = 1; i <= colCount; i++) {
                        System.out.printf("  %-18s", rs.getString(i));
                    }
                    System.out.println();
                    rowCount++;
                }
                if (rowCount == 0) System.out.println("  (No results found)");
                else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");
                
                cs.close();
            } else {
                System.out.println("  [!] Invalid choice.");
            }
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    public void callDBFunctions(Scanner sc) {
        System.out.println("  1. get_total_payment(cust_id)  — total payments by a customer");
        System.out.println("  2. session_duration(session_id) — duration of a session");
        System.out.print("  Choice: ");
        String choice = sc.nextLine().trim();

        try {
            if (choice.equals("1")) {
                System.out.print("  Enter Customer ID: ");
                int cid = Integer.parseInt(sc.nextLine().trim());
                PreparedStatement ps = con.prepareStatement("SELECT get_total_payment(?) AS TOTAL_PAYMENT");
                ps.setInt(1, cid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("  Total Payment for Customer " + cid + ": Rs. " + rs.getInt("TOTAL_PAYMENT"));
                }
                rs.close(); ps.close();
            } else if (choice.equals("2")) {
                System.out.print("  Enter Session ID: ");
                int sid = Integer.parseInt(sc.nextLine().trim());
                PreparedStatement ps = con.prepareStatement("SELECT session_duration(?) AS DURATION");
                ps.setInt(1, sid);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("  Duration of Session " + sid + ": " + rs.getInt("DURATION") + " minutes");
                }
                rs.close(); ps.close();
            } else {
                System.out.println("  [!] Invalid choice.");
            }
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Numeric fields must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    public void queryViews() {
        Scanner sc = new Scanner(System.in);
        System.out.println("  1. customer_sessions view");
        System.out.println("  2. total_spending view");
        System.out.print("  Choice: ");
        String choice = sc.nextLine().trim();

        try {
            String sql = "";
            if (choice.equals("1")) {
                sql = "SELECT * FROM customer_sessions";
            } else if (choice.equals("2")) {
                sql = "SELECT * FROM total_spending";
            } else {
                System.out.println("  [!] Invalid choice.");
                return;
            }

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();
            System.out.println();
            for (int i = 1; i <= colCount; i++) {
                System.out.printf("  %-18s", meta.getColumnName(i));
            }
            System.out.println();
            System.out.println("  " + "-".repeat(colCount * 19));
            
            int rowCount = 0;
            while (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("  %-18s", rs.getString(i));
                }
                System.out.println();
                rowCount++;
            }
            if (rowCount == 0) System.out.println("  (No results found)");
            else System.out.println("\n  [✓] " + rowCount + " row(s) returned.");
            
            rs.close(); ps.close();
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}
