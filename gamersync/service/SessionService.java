package gamersync.service;

import gamersync.dao.SessionDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

// Service layer — handles CLI input/output for Session module
public class SessionService {

    private final SessionDAO dao = new SessionDAO();
    private final Scanner sc;

    public SessionService(Scanner sc) { this.sc = sc; }

    // ── Main menu loop ────────────────────────────────────────────────────────
    public void menu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n  ╔══════════════════════════╗");
            System.out.println("  ║   GAMING SESSION MODULE  ║");
            System.out.println("  ╠══════════════════════════╣");
            System.out.println("  ║  1. Add Session          ║");
            System.out.println("  ║  2. View All Sessions    ║");
            System.out.println("  ║  3. Delete Session       ║");
            System.out.println("  ║  0. Back                 ║");
            System.out.println("  ╚══════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addSession();    break;
                case "2": viewAll();       break;
                case "3": deleteSession(); break;
                case "0": back = true;     break;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── ADD ───────────────────────────────────────────────────────────────────
    private void addSession() {
        try {
            System.out.print("  Session ID          : "); int sid  = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Start (YYYY-MM-DD HH:MM): "); String start = sc.nextLine().trim();
            System.out.print("  End   (YYYY-MM-DD HH:MM): "); String end   = sc.nextLine().trim();
            System.out.print("  Duration (minutes)  : "); int dur  = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Game Name           : "); String game  = sc.nextLine().trim();
            System.out.print("  Customer ID         : "); int cid  = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  PC ID               : "); int pcid = Integer.parseInt(sc.nextLine().trim());

            dao.addSession(new GamingSession(sid, start, end, dur, game, cid, pcid));
            System.out.println("  [✓] Session added successfully.");

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

    // ── VIEW ALL ──────────────────────────────────────────────────────────────
    private void viewAll() {
        try {
            List<GamingSession> list = dao.getAllSessions();
            if (list.isEmpty()) { System.out.println("  No sessions found."); return; }
            System.out.println("\n  " + "-".repeat(90));
            System.out.printf("  | %-5s | %-18s | %-18s | %-5s | %-12s | %-6s | %-5s |%n",
                "SID","START","END","MINS","GAME","CUSTID","PCID");
            System.out.println("  " + "-".repeat(90));
            for (GamingSession s : list) System.out.println("  " + s);
            System.out.println("  " + "-".repeat(90));
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    private void deleteSession() {
        try {
            System.out.print("  Session ID to delete: "); int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  This will also remove linked payments & food orders. Confirm? (yes/no): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("  Cancelled."); return; }
            dao.deleteSession(id);
            System.out.println("  [✓] Session and linked records deleted.");
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] ID must be a number.");
        }
    }
}
