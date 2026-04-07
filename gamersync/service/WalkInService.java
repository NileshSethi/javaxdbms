package gamersync.service;

import gamersync.db.DBConnection;
import gamersync.db.InvalidDataException;
import gamersync.db.ValidationHelper;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WalkInService {
    private final Scanner sc;
    private Connection con;

    public WalkInService(Scanner sc) {
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
            System.out.println("  ║            WALK-IN FLOW                  ║");
            System.out.println("  ║   New customer or returning customer?    ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.println("  ║  1. Start Full Walk-In (step by step)    ║");
            System.out.println("  ║  2. Quick Session Start (existing cust)  ║");
            System.out.println("  ║  0. Back                                 ║");
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.print("  Choice: ");

            switch (sc.nextLine().trim()) {
                case "1": startFullWalkIn(); break;
                case "2": quickSessionStart(); break;
                case "0": back = true; break;
                default: System.out.println("  [!] Invalid choice.");
            }
        }
    }

    private void startFullWalkIn() {
        int custId = -1;
        int sessionId = -1;
        String gameName = "N/A";
        String memType = "None";
        String gamingUsername = "None";

        try {
            // STEP 1
            System.out.println("\n  STEP 1/5 — CUSTOMER REGISTRATION");
            System.out.print("  Is this a new customer? (yes/no): ");
            String isNew = sc.nextLine().trim().toLowerCase();

            if (isNew.equals("yes")) {
                System.out.print("  Enter CUST_ID: ");
                custId = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter NAME: ");
                String name = sc.nextLine().trim();
                System.out.print("  Enter PHONE: ");
                String phone = sc.nextLine().trim();
                System.out.print("  Enter EMAIL: ");
                String email = sc.nextLine().trim();

                if (name.isEmpty() || phone.isEmpty()) {
                    throw new InvalidDataException("NAME and PHONE cannot be empty");
                }

                String regDate = LocalDate.now().toString();

                ValidationHelper.validatePositiveInt(custId, "Customer ID");
                ValidationHelper.validateNotEmpty(name, "Name");
                ValidationHelper.validatePhone(phone);
                ValidationHelper.validateEmail(email);

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO CUSTOMER (CUST_ID, NAME, PHONE, EMAIL, REGISTERED_DATE) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, custId);
                ps.setString(2, name);
                ps.setString(3, phone);
                ps.setString(4, email);
                ps.setString(5, regDate);
                ps.executeUpdate();
                ps.close();
                System.out.println("  [✓] Customer registered.");
            } else {
                System.out.print("  Enter existing CUST_ID: ");
                custId = Integer.parseInt(sc.nextLine().trim());
                
                PreparedStatement ps = con.prepareStatement("SELECT NAME FROM CUSTOMER WHERE CUST_ID = ?");
                ps.setInt(1, custId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    System.out.println("  [✓] Welcome back, " + rs.getString("NAME") + "!");
                } else {
                    System.out.println("  [!] Customer not found.");
                    rs.close(); ps.close();
                    return;
                }
                rs.close(); ps.close();
            }

            // STEP 2
            System.out.println("\n  STEP 2/5 — START GAMING SESSION");
            PreparedStatement psAvail = con.prepareStatement("SELECT PC_ID, CONFIGURATION, HOURLY_RATE FROM PC WHERE STATUS = 'Available'");
            ResultSet rsAvail = psAvail.executeQuery();
            boolean hasPc = false;
            System.out.println("  Available PCs:");
            while (rsAvail.next()) {
                hasPc = true;
                System.out.printf("  PC_ID: %d | %s | Rs.%.2f/hr%n", rsAvail.getInt("PC_ID"), rsAvail.getString("CONFIGURATION"), rsAvail.getDouble("HOURLY_RATE"));
            }
            rsAvail.close(); psAvail.close();

            if (!hasPc) {
                System.out.println("  [!] No PCs available.");
                System.out.println("  Skipping to Step 3...");
            } else {
                System.out.print("  Enter SESSION_ID: ");
                sessionId = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter PC_ID: ");
                int pcId = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter START_TIME (YYYY-MM-DD HH:MM:SS): ");
                String startTime = sc.nextLine().trim();
                System.out.print("  Enter DURATION (minutes): ");
                int duration = Integer.parseInt(sc.nextLine().trim());
                
                LocalDateTime startObj = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime endObj = startObj.plusMinutes(duration);
                String endTime = endObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                ValidationHelper.validatePositiveInt(sessionId, "Session ID");
                ValidationHelper.validateDateTime(startTime, "Start Time");
                ValidationHelper.validateDuration(duration);
                ValidationHelper.validateNotEmpty(gameName, "Game Name");

                System.out.println("  Auto-calculated End Time: " + endTime);

                System.out.print("  Enter GAME_NAME: ");
                gameName = sc.nextLine().trim();

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO GAMING_SESSION (SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID) VALUES (?, ?, ?, ?, ?, ?, ?)");
                ps.setInt(1, sessionId);
                ps.setString(2, startTime);
                ps.setString(3, endTime);
                ps.setInt(4, duration);
                ps.setString(5, gameName);
                ps.setInt(6, custId);
                ps.setInt(7, pcId);
                ps.executeUpdate();
                ps.close();
                System.out.println("  [✓] Session started. PC status auto-updated by DB trigger.");

                System.out.print("\n  Add food order for this session? (yes/no): ");
                if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectFoodOrder(sessionId);
                System.out.print("  Collect payment now? (yes/no): ");
                if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectPayment(custId, sessionId);
            }

            // STEP 3
            System.out.println("\n  STEP 3/5 — MEMBERSHIP");
            System.out.print("  Add a membership? (yes/no): ");
            if (sc.nextLine().trim().toLowerCase().equals("yes")) {
                System.out.println("  1. Hourly");
                System.out.println("  2. Weekly");
                System.out.println("  3. Monthly");
                System.out.print("  Choice: ");
                String mChoice = sc.nextLine().trim();
                
                System.out.print("  Enter MEMBERSHIP_ID: ");
                int memId = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter START_DATE (YYYY-MM-DD): ");
                String startDate = sc.nextLine().trim();
                System.out.print("  Enter END_DATE (YYYY-MM-DD): ");
                String endDate = sc.nextLine().trim();
                
                String typeStr = "Hourly";
                if (mChoice.equals("2")) typeStr = "Weekly";
                if (mChoice.equals("3")) typeStr = "Monthly";
                memType = typeStr;

                PreparedStatement psMem = con.prepareStatement(
                    "INSERT INTO MEMBERSHIP (MEMBERSHIP_ID, MEM_TYPE, START_DATE, END_DATE, CUST_ID) VALUES (?, ?, ?, ?, ?)");
                psMem.setInt(1, memId);
                psMem.setString(2, typeStr);
                psMem.setString(3, startDate);
                psMem.setString(4, endDate);
                psMem.setInt(5, custId);
                psMem.executeUpdate();
                psMem.close();

                if (mChoice.equals("1")) {
                    System.out.print("  Enter HRS_PURCHASED: ");
                    int hrs = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("  Enter COST_HR: ");
                    double cost = Double.parseDouble(sc.nextLine().trim());
                    PreparedStatement psType = con.prepareStatement("INSERT INTO HOURLY (MEMBERSHIP_ID, HRS_PURCHASED, COST_HR) VALUES (?, ?, ?)");
                    psType.setInt(1, memId);
                    psType.setInt(2, hrs);
                    psType.setDouble(3, cost);
                    psType.executeUpdate();
                    psType.close();
                } else if (mChoice.equals("2")) {
                    System.out.print("  Enter VALID_DAYS: ");
                    int days = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("  Enter WEEKLY_FEE: ");
                    double fee = Double.parseDouble(sc.nextLine().trim());
                    PreparedStatement psType = con.prepareStatement("INSERT INTO WEEKLY (MEMBERSHIP_ID, VALID_DAYS, WEEKLY_FEE) VALUES (?, ?, ?)");
                    psType.setInt(1, memId);
                    psType.setInt(2, days);
                    psType.setDouble(3, fee);
                    psType.executeUpdate();
                    psType.close();
                } else if (mChoice.equals("3")) {
                    System.out.print("  Enter VALID_WEEKS: ");
                    int wks = Integer.parseInt(sc.nextLine().trim());
                    System.out.print("  Enter MONTHLY_FEE: ");
                    double fee = Double.parseDouble(sc.nextLine().trim());
                    PreparedStatement psType = con.prepareStatement("INSERT INTO MONTHLY (MEMBERSHIP_ID, VALID_WEEKS, MONTHLY_FEE) VALUES (?, ?, ?)");
                    psType.setInt(1, memId);
                    psType.setInt(2, wks);
                    psType.setDouble(3, fee);
                    psType.executeUpdate();
                    psType.close();
                }
                System.out.println("  [✓] Membership added.");
            } else {
                System.out.println("  Skipped.");
            }

            // STEP 4
            System.out.println("\n  STEP 4/5 — GAMING ACCOUNT");
            System.out.print("  Register a gaming account? (yes/no): ");
            if (sc.nextLine().trim().toLowerCase().equals("yes")) {
                System.out.print("  Enter GAMING_ACC_ID: ");
                int accId = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Enter GAME_NAME: ");
                String accGame = sc.nextLine().trim();
                System.out.print("  Enter GAME_USER_ID: ");
                String accUser = sc.nextLine().trim();
                System.out.print("  Enter RANKK: ");
                String rankk = sc.nextLine().trim();
                System.out.print("  Enter LEVEL: ");
                int level = Integer.parseInt(sc.nextLine().trim());

                if (level <= 0) throw new InvalidDataException("Level must be > 0");
                if (accUser.isEmpty()) throw new InvalidDataException("Username cannot be empty");

                PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO GAMING_ACC (GAMING_ACC_ID, GAME_NAME, GAME_USER_ID, RANKK, LEVEL, CUST_ID) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setInt(1, accId);
                ps.setString(2, accGame);
                ps.setString(3, accUser);
                ps.setString(4, rankk);
                ps.setInt(5, level);
                ps.setInt(6, custId);
                ps.executeUpdate();
                ps.close();
                gamingUsername = accUser;
                System.out.println("  [✓] Gaming account registered.");
            } else {
                System.out.println("  Skipped.");
            }

            // STEP 5
            System.out.println("\n  STEP 5/5 — WALK-IN SUMMARY");
            System.out.println("  ╔══════════════════════════════════════════╗");
            System.out.println("  ║           WALK-IN SUMMARY                ║");
            System.out.println("  ╠══════════════════════════════════════════╣");
            System.out.printf("  ║  Customer ID   : %-23s ║%n", custId);
            System.out.printf("  ║  Session ID    : %-23s ║%n", (sessionId == -1 ? "N/A" : sessionId));
            System.out.printf("  ║  Game          : %-23s ║%n", gameName);
            System.out.printf("  ║  Membership    : %-23s ║%n", memType);
            System.out.printf("  ║  Gaming Acc    : %-23s ║%n", gamingUsername);
            System.out.println("  ╚══════════════════════════════════════════╝");
            System.out.println("  [✓] Walk-in complete.");

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

    private void quickSessionStart() {
        try {
            System.out.print("  Enter existing CUST_ID: ");
            int custId = Integer.parseInt(sc.nextLine().trim());

            PreparedStatement psCheck = con.prepareStatement("SELECT NAME FROM CUSTOMER WHERE CUST_ID = ?");
            psCheck.setInt(1, custId);
            ResultSet rsCheck = psCheck.executeQuery();
            if(!rsCheck.next()) {
                System.out.println("  [!] Customer not found.");
                rsCheck.close(); psCheck.close();
                return;
            }
            rsCheck.close(); psCheck.close();

            PreparedStatement psAvail = con.prepareStatement("SELECT PC_ID, CONFIGURATION, HOURLY_RATE FROM PC WHERE STATUS = 'Available'");
            ResultSet rsAvail = psAvail.executeQuery();
            boolean hasPc = false;
            System.out.println("  Available PCs:");
            while (rsAvail.next()) {
                hasPc = true;
                System.out.printf("  PC_ID: %d | %s | Rs.%.2f/hr%n", rsAvail.getInt("PC_ID"), rsAvail.getString("CONFIGURATION"), rsAvail.getDouble("HOURLY_RATE"));
            }
            rsAvail.close(); psAvail.close();

            if (!hasPc) {
                System.out.println("  [!] No PCs available.");
                return;
            }

            System.out.print("  Enter SESSION_ID: ");
            int sessionId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter PC_ID: ");
            int pcId = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Enter START_TIME (YYYY-MM-DD HH:MM:SS): ");
            String startTime = sc.nextLine().trim();
            System.out.print("  Enter DURATION (minutes): ");
            int duration = Integer.parseInt(sc.nextLine().trim());

            LocalDateTime startObj = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endObj = startObj.plusMinutes(duration);
            String endTime = endObj.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            System.out.println("  Auto-calculated End Time: " + endTime);

            System.out.print("  Enter GAME_NAME: ");
            String gameName = sc.nextLine().trim();

            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO GAMING_SESSION (SESSION_ID, START_TIME, END_TIME, DURATION, GAME_NAME, CUST_ID, PC_ID) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setInt(1, sessionId);
            ps.setString(2, startTime);
            ps.setString(3, endTime);
            ps.setInt(4, duration);
            ps.setString(5, gameName);
            ps.setInt(6, custId);
            ps.setInt(7, pcId);
            ps.executeUpdate();
            ps.close();
            System.out.println("  [✓] Session started for existing customer.");

            System.out.print("\n  Add food order for this session? (yes/no): ");
            if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectFoodOrder(sessionId);
            System.out.print("  Collect payment now? (yes/no): ");
            if (sc.nextLine().trim().equalsIgnoreCase("yes")) collectPayment(custId, sessionId);

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
