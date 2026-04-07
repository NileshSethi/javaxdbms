package gamersync;

import gamersync.db.DBConnection;
import gamersync.service.CustomerService;
import gamersync.service.FoodPaymentService;
import gamersync.service.GamingProfileService;
import gamersync.service.MembershipService;
import gamersync.service.PCService;
import gamersync.service.QueryService;
import gamersync.service.SessionService;
import gamersync.service.TournamentService;
import gamersync.service.WalkInService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║       GAMERSYNC  CAFÉ MANAGEMENT        ║");
        System.out.println("  ║          Console Application             ║");
        System.out.println("  ╚══════════════════════════════════════════╝");
        
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            System.out.println("  [✓] Connected to GamerSync database.\n");
        } catch (Exception e) {
            System.out.println("  [✗] DB Connection FAILED: " + e.getMessage());
            System.out.println("  Check DBConnection.java and ensure MySQL is running.");
            return;
        }

        WalkInService        walkInService      = new WalkInService(sc);
        CustomerService      customerService    = new CustomerService(sc);
        SessionService       sessionService     = new SessionService(sc);
        GamingProfileService profileService     = new GamingProfileService(sc);
        FoodPaymentService   foodPaymentService = new FoodPaymentService(sc);
        TournamentService    tournamentService  = new TournamentService(sc);
        PCService            pcService          = new PCService(sc);
        MembershipService    membershipService  = new MembershipService(sc);
        QueryService         queryService       = new QueryService();

        boolean running = true;
        while (running) {
            System.out.println("  ╔══════════════════════════════════════════╗");
            System.out.println("  ║                MAIN MENU                 ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Customer Module                      ║");
            System.out.println("  ║  2. PC Management                        ║");
            System.out.println("  ║  3. Session Module                       ║");
            System.out.println("  ║  4. Gaming Profile (GAMING_ACC & ACHIEV) ║");
            System.out.println("  ║  5. Food & Payment                       ║");
            System.out.println("  ║  6. Tournaments                          ║");
            System.out.println("  ║  7. Walk-In Flow (Combinations)          ║");
            System.out.println("  ║  8. Analytical Queries                   ║");
            System.out.println("  ║  9. Membership Module                    ║");
            System.out.println("  ║  10. View All Tables                     ║");
            System.out.println("  ║  0. Exit                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": customerService.menu();      break;
                case "2": pcService.menu();            break;
                case "3": sessionService.menu();       break;
                case "4": profileService.menu();       break;
                case "5": foodPaymentService.menu();   break;
                case "6": tournamentService.menu();    break;
                case "7": walkInService.menu();        break;
                case "8": queryMenu(sc, queryService); break;
                case "9": membershipService.menu();    break;
                case "10": viewAllTables(con);         break;
                case "0":
                    running = false;
                    DBConnection.close();
                    System.out.println("\n  Goodbye! GamerSync session ended.");
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please enter 0-9.");
            }
        }
        sc.close();
    }

    private static void queryMenu(Scanner sc, QueryService qs) {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ╔═════════════════════════════════════════╗");
            System.out.println("  ║         ANALYTICAL QUERIES              ║");
            System.out.println("  ╠═════════════════════════════════════════╣");
            System.out.println("  ║  1. Customer + Game + PC Used           ║");
            System.out.println("  ║  2. Total Spending per Customer         ║");
            System.out.println("  ║  3. Customers with Sessions > 100 min   ║");
            System.out.println("  ║  4. Membership + Games Played           ║");
            System.out.println("  ║  5. Rank Customers by Spending          ║");
            System.out.println("  ║  6. Tournament Overview                 ║");
            System.out.println("  ║  7. Payment Summary by Mode             ║");
            System.out.println("  ║  8. Top Gaming Accounts by Level        ║");
            System.out.println("  ║  9.  Custom SELECT Query                ║");
            System.out.println("  ║  10. Custom SELECT with Filter          ║");
            System.out.println("  ║  11. Guided JOIN Query                  ║");
            System.out.println("  ║  12. Call Stored Procedures             ║");
            System.out.println("  ║  13. Call DB Functions                  ║");
            System.out.println("  ║  14. Query Views                        ║");
            System.out.println("  ║  0. Back                                ║");
            System.out.println("  ╚═════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": qs.customerGamePC();          break;
                case "2": qs.totalSpendingPerCustomer(); break;
                case "3": qs.longSessionCustomers();    break;
                case "4": qs.membershipWithGames();     break;
                case "5": qs.rankCustomersBySpending(); break;
                case "6": qs.viewTournaments();         break;
                case "7": qs.paymentSummaryByMode();    break;
                case "8": qs.topGamingAccounts();       break;
                case "9": qs.runCustomSelect(sc);       break;
                case "10": qs.runSelectWithFilter(sc);  break;
                case "11": qs.runGuidedJoin(sc);        break;
                case "12": qs.callStoredProcedures(sc); break;
                case "13": qs.callDBFunctions(sc);      break;
                case "14": qs.queryViews();             break;
                case "0": back = true;                  break;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private static void viewAllTables(Connection con) {

        // All 12 table names in logical order
        String[][] tables = {
            {"CUSTOMER",       "CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE"},
            {"PC",             "PC_ID, CONFIGURATION, STATUS, HOURLY_RATE"},
            {"GAMING_SESSION", "SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID"},
            {"GAMING_ACC",     "GAMING_ACC_ID, GAME_NAME, GAME_USER_ID, RANKK, LEVEL, CUST_ID"},
            {"ACHIEVEMENTS",   "ACHIEVEMENT_ID, ACHIEVE_NAME, DATE_UNLOCKED, GAMING_ACC_ID"},
            {"MEMBERSHIP",     "MEMBERSHIP_ID, MEM_TYPE, START_DATE, END_DATE, CUST_ID"},
            {"HOURLY",         "MEMBERSHIP_ID, HRS_PURCHASED, COST_HR"},
            {"WEEKLY",         "MEMBERSHIP_ID, VALID_DAYS, WEEKLY_FEE"},
            {"MONTHLY",        "MEMBERSHIP_ID, VALID_WEEKS, MONTHLY_FEE"},
            {"FOOD_ORDER",     "ORDER_ID, ORDER_ITEM, TOTAL_AMOUNT, SESSION_ID"},
            {"TOURNAMENTS",    "TOURNAMENT_ID, GAME_NAME, TOURNAMENT_DATE, ENTRY_FEE, CUST_ID"},
            {"PAYMENT",        "PAYMENT_ID, PAYMENT_MODE, AMOUNT, CUST_ID, SESSION_ID"}
        };

        System.out.println("\n  ╔══════════════════════════════════════════╗");
        System.out.println("  ║         VIEW ALL TABLES — GAMERSYNC     ║");
        System.out.println("  ║         Showing all 12 DB tables        ║");
        System.out.println("  ╚══════════════════════════════════════════╝\n");

        for (String[] tableInfo : tables) {
            String tableName = tableInfo[0];

            System.out.println("  ┌─────────────────────────────────────────");
            System.out.println("  │ TABLE: " + tableName);
            System.out.println("  └─────────────────────────────────────────");

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM " + tableName);
                ResultSet rs = ps.executeQuery();
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                // Print column headers
                System.out.print("  ");
                for (int i = 1; i <= colCount; i++) {
                    System.out.printf("%-20s", meta.getColumnName(i));
                }
                System.out.println();
                System.out.println("  " + "-".repeat(colCount * 20));

                // Print rows
                int rowCount = 0;
                while (rs.next()) {
                    System.out.print("  ");
                    for (int i = 1; i <= colCount; i++) {
                        String val = rs.getString(i);
                        System.out.printf("%-20s", val != null ? val : "NULL");
                    }
                    System.out.println();
                    rowCount++;
                }

                if (rowCount == 0)
                    System.out.println("  (No records in this table)");
                else
                    System.out.println("  → " + rowCount + " record(s)");

                rs.close();
                ps.close();

            } catch (SQLException e) {
                System.out.println("  [SQL ERROR reading " + tableName + "] " + e.getMessage());
            }

            System.out.println();
        }

        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║   All 12 tables displayed successfully  ║");
        System.out.println("  ╚══════════════════════════════════════════╝\n");
    }
}
