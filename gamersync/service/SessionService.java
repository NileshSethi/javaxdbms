package gamersync.service;

import gamersync.dao.SessionDAO;
import gamersync.db.InvalidDataException;
import gamersync.model.GamingSession;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            System.out.println("  ║  3. Update Session       ║");
            System.out.println("  ║  4. Delete Session       ║");
            System.out.println("  ║  0. Back                 ║");
            System.out.println("  ╚══════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": addSession();    break;
                case "2": viewAll();       break;
                case "3": updateSession(); break;
                case "4": deleteSession(); break;
                case "0": back = true;     break;
                default:  System.out.println("  [!] Invalid choice.");
            }
        }
    }

    // ── ADD ───────────────────────────────────────────────────────────────────
    private void addSession() {
        try {
            System.out.print("  Session ID          : "); int sid  = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Start (YYYY-MM-DD HH:MM:SS): "); String start = sc.nextLine().trim();
            System.out.print("  Duration (minutes)  : "); int dur  = Integer.parseInt(sc.nextLine().trim());
            
            LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTimeObj = startTime.plusMinutes(dur);
            String end = endTimeObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println("  Auto-calculated End Time: " + end);

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

    // ── UPDATE ────────────────────────────────────────────────────────────────
    private void updateSession() {
        try {
            System.out.print("  Enter Session ID to update: ");
            int sid = Integer.parseInt(sc.nextLine().trim());

            GamingSession current = dao.getSessionById(sid);
            if (current == null) {
                System.out.println("  [!] Session not found.");
                return;
            }

            System.out.println("  Current values:");
            System.out.println("  Game Name  : " + current.getGameName());
            System.out.println("  Start Time : " + current.getStartTime());
            System.out.println("  Duration   : " + current.getDuration() + " mins");
            System.out.println("  End Time   : " + current.getEndTime() + " (auto-calculated)");
            System.out.println("  Cust ID    : " + current.getCustId());
            System.out.println("  PC ID      : " + current.getPcId());

            System.out.print("  New Game Name (or press Enter to keep current): ");
            String gameInput = sc.nextLine().trim();
            System.out.print("  New Start Time YYYY-MM-DD HH:MM:SS (or Enter to keep): ");
            String startInput = sc.nextLine().trim();
            System.out.print("  New Duration in minutes (or Enter to keep): ");
            String durInput = sc.nextLine().trim();
            System.out.print("  New Cust ID (or Enter to keep): ");
            String custInput = sc.nextLine().trim();
            System.out.print("  New PC ID (or Enter to keep): ");
            String pcInput = sc.nextLine().trim();

            String newGame = gameInput.isEmpty() ? current.getGameName() : gameInput;
            if (newGame.trim().isEmpty()) throw new InvalidDataException("Game name cannot be set to empty");

            String newStart = startInput.isEmpty() ? current.getStartTime() : startInput;
            int newDur = durInput.isEmpty() ? current.getDuration() : Integer.parseInt(durInput);
            if (newDur <= 0) throw new InvalidDataException("Duration must be > 0");

            int newCust = custInput.isEmpty() ? current.getCustId() : Integer.parseInt(custInput);
            int newPc = pcInput.isEmpty() ? current.getPcId() : Integer.parseInt(pcInput);

            LocalDateTime startTime = LocalDateTime.parse(newStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String newEnd = startTime.plusMinutes(newDur).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            if (!startInput.isEmpty() || !durInput.isEmpty()) {
                System.out.println("  Recalculated End Time: " + newEnd);
            }

            GamingSession updateData = new GamingSession(sid, newStart, newEnd, newDur, newGame, newCust, newPc);
            dao.updateSession(updateData);
            System.out.println("  [✓] Session updated successfully.");

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
