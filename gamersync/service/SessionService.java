package gamersync.service;

import gamersync.dao.SessionDAO;
import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import gamersync.db.ValidationHelper;
import gamersync.model.GamingSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class SessionService {

    private final SessionDAO dao = new SessionDAO();
    private final Scanner sc;
    private Connection con;

    public SessionService(Scanner sc) {
        this.sc = sc;
        try {
            this.con = DBConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("  [ERROR] DB connection failed: " + e.getMessage());
        }
    }

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

            ValidationHelper.validatePositiveInt(sid, "Session ID");
            ValidationHelper.validateDateTime(start, "Start Time");
            ValidationHelper.validateDuration(dur);
            ValidationHelper.validateNotEmpty(game, "Game Name");
            ValidationHelper.validatePositiveInt(cid, "Customer ID");
            ValidationHelper.validatePositiveInt(pcid, "PC ID");

            dao.addSession(new GamingSession(sid, start, end, dur, game, cid, pcid));
            System.out.println("  [✓] Session added successfully.");

            System.out.print("\n  Add food order for this session? (yes/no): ");
            if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectFoodOrder(sid);
            System.out.print("  Collect payment now? (yes/no): ");
            if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectPayment(cid, sid);

        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("pc")) {
                    System.out.println("  [SQL ERROR] The PC ID you entered does not exist!");
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

            ValidationHelper.validatePositiveInt(sid, "Session ID");
            ValidationHelper.validateDateTime(newStart, "Start Time");
            ValidationHelper.validateDuration(newDur);
            ValidationHelper.validateNotEmpty(newGame, "Game Name");
            ValidationHelper.validatePositiveInt(newCust, "Customer ID");
            ValidationHelper.validatePositiveInt(newPc, "PC ID");

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
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("pc")) {
                    System.out.println("  [SQL ERROR] The PC ID you entered does not exist!");
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

    // ── DELETE ────────────────────────────────────────────────────────────────
    private void deleteSession() {
        try {
            System.out.print("  Session ID to delete: "); int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  This will also remove linked payments & food orders. Confirm? (yes/no): ");
            if (!sc.nextLine().trim().equalsIgnoreCase("yes")) { System.out.println("  Cancelled."); return; }
            dao.deleteSession(id);
            System.out.println("  [✓] Session and linked records deleted.");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("a foreign key constraint fails")) {
                if (msg.toLowerCase().contains("customer")) {
                    System.out.println("  [SQL ERROR] The Customer ID you entered does not exist!");
                } else if (msg.toLowerCase().contains("pc")) {
                    System.out.println("  [SQL ERROR] The PC ID you entered does not exist!");
                } else {
                    System.out.println("  [SQL ERROR] A parent ID does not exist.");
                }
            } else {
                System.out.println("  [SQL ERROR] " + msg);
            }
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] ID must be a number.");
        }
    }


    private void collectFoodOrder(int sessionId) {
        try {
            System.out.print("  Food Order ID : "); 
            int orderId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Order Item    : "); 
            String item = sc.nextLine().trim();
            System.out.print("  Total Amount  : "); 
            double amount = Double.parseDouble(sc.nextLine().trim());

            ValidationHelper.validatePositiveInt(orderId, "Order ID");
            ValidationHelper.validateNotEmpty(item, "Order Item");
            ValidationHelper.validatePositiveAmount(amount, "Total Amount");

            String sql = "INSERT INTO FOOD_ORDER (ORDER_ID, ORDER_ITEM, TOTAL_AMOUNT, SESSION_ID) VALUES (?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, orderId);
            ps.setString(2, item);
            ps.setDouble(3, amount);
            ps.setInt(4, sessionId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [\u2713] Food order added: " + item + " \u2014 Rs. " + amount);

        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Amount and ID must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }

    private void collectPayment(int custId, int sessionId) {
        try {
            System.out.print("  Payment ID    : "); 
            int payId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Payment Mode (UPI/Cash/Card): "); 
            String mode = sc.nextLine().trim();
            System.out.print("  Amount        : "); 
            double amount = Double.parseDouble(sc.nextLine().trim());

            ValidationHelper.validatePositiveInt(payId, "Payment ID");
            ValidationHelper.validatePaymentMode(mode);
            ValidationHelper.validatePositiveAmount(amount, "Amount");

            String sql = "INSERT INTO PAYMENT (PAYMENT_ID, PAYMENT_MODE, AMOUNT, CUST_ID, SESSION_ID) VALUES (?,?,?,?,?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, payId);
            ps.setString(2, mode);
            ps.setDouble(3, amount);
            ps.setInt(4, custId);
            ps.setInt(5, sessionId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [\u2713] Payment recorded: " + mode + " \u2014 Rs. " + amount);

            System.out.println("\n  \u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557");
            System.out.println("  \u2551         QUICK RECEIPT                \u2551");
            System.out.println("  \u2560\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2563");
            System.out.printf ("  \u2551  Session ID  : %-21d\u2551%n", sessionId);
            System.out.printf ("  \u2551  Customer ID : %-21d\u2551%n", custId);
            System.out.printf ("  \u2551  Mode        : %-21s\u2551%n", mode);
            System.out.printf ("  \u2551  Amount Paid : Rs. %-18.2f\u2551%n", amount);
            System.out.println("  \u255a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255d");

        } catch (InvalidDataException e) {
            System.out.println("  [VALIDATION ERROR] " + e.getMessage());
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("Invalid Amount"))
                System.out.println("  [DB TRIGGER] Payment rejected \u2014 amount must be > 0");
            else
                System.out.println("  [SQL ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("  [INPUT ERROR] Amount and ID must be numbers.");
        } catch (Exception e) {
            System.out.println("  [UNEXPECTED ERROR] " + e.getMessage());
        }
    }
}
