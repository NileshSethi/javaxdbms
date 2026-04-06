package gamersync;

import gamersync.db.DBConnection;
import gamersync.service.CustomerService;
import gamersync.service.QueryService;
import gamersync.service.SessionService;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println();
        System.out.println("  ╔════════════════════════════════════════╗");
        System.out.println("  ║       GAMERSYNC — CAFÉ MANAGEMENT      ║");
        System.out.println("  ║          Console Application           ║");
        System.out.println("  ╚════════════════════════════════════════╝");

        try {
            DBConnection.getConnection();
            System.out.println("  [✓] Connected to GamerSync database.\n");
        } catch (Exception e) {
            System.out.println("  [✗] DB Connection FAILED: " + e.getMessage());
            System.out.println("  Check DBConnection.java and ensure MySQL is running.");
            return;
        }

        CustomerService customerService = new CustomerService(sc);
        SessionService  sessionService  = new SessionService(sc);
        QueryService    queryService    = new QueryService();

        boolean running = true;
        while (running) {
            System.out.println("  ╔════════════════════════════════════════╗");
            System.out.println("  ║              MAIN MENU                 ║");
            System.out.println("  ╠════════════════════════════════════════╣");
            System.out.println("  ║  1. Customer Module                    ║");
            System.out.println("  ║  2. Gaming Session Module              ║");
            System.out.println("  ║  3. Analytical Queries                 ║");
            System.out.println("  ║  0. Exit                               ║");
            System.out.println("  ╚════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": customerService.menu(); break;
                case "2": sessionService.menu();  break;
                case "3": queryMenu(sc, queryService); break;
                case "0":
                    running = false;
                    DBConnection.close();
                    System.out.println("\n  Goodbye! GamerSync session ended.");
                    break;
                default:
                    System.out.println("  [!] Invalid choice. Please enter 0-3.");
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
                case "0": back = true;                  break;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }
}
